/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright (c) 2011 Jens Elkner.
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2013 Takayuki Okazaki.
 */
package org.watermint.sourcecolon.org.opensolaris.opengrok.web;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;
import org.watermint.sourcecolon.org.opensolaris.opengrok.OpenGrokLogger;
import org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.CompatibleAnalyser;
import org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.Definitions;
import org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.FileAnalyzer;
import org.watermint.sourcecolon.org.opensolaris.opengrok.search.QueryBuilder;
import org.watermint.sourcecolon.org.opensolaris.opengrok.search.Results;
import org.watermint.sourcecolon.org.opensolaris.opengrok.search.Summarizer;
import org.watermint.sourcecolon.org.opensolaris.opengrok.search.context.Context;
import org.watermint.sourcecolon.org.opensolaris.opengrok.search.context.LineMatcher;
import org.watermint.sourcecolon.org.opensolaris.opengrok.search.context.QueryMatchers;
import org.watermint.sourcecolon.org.opensolaris.opengrok.util.IOUtils;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Working set for a search basically to factor out/separate search related
 * complexity from UI design.
 *
 * @author Jens Elkner
 * @version $Revision$
 */
public class SearchHelper {

    /**
     * opengrok's data root: used to find the search index file
     */
    public File dataRoot;
    /**
     * context path, i.e. the applications context path (usually /source) to
     * use when generating a redirect URL
     */
    public String contextPath;
    /**
     * piggyback: if {@code true}, files in opengrok's data directory are
     * gzipped compressed.
     */
    public boolean compressed;
    /**
     * piggyback: the source root directory.
     */
    public File sourceRoot;
    /**
     * the result cursor start index, i.e. where to start displaying results
     */
    public int start;
    /**
     * max. number of result items to show
     */
    public int maxItems;
    /**
     * the QueryBuilder used to create the query
     */
    public QueryBuilder builder;
    /**
     * the order to use to ordery query results
     */
    public SortOrder order;
    /**
     * Indicate, whether this is search from a cross reference. If {@code true}
     * {@link #executeQuery()} sets {@link #redirect} if certain conditions are
     * met.
     */
    public boolean isCrossRefSearch;
    /**
     * if not {@code null}, the consumer should redirect the client to a
     * separate result page denoted by the value of this field. Automatically
     * set via {@link #prepareExec(SortedSet)} and {@link #executeQuery()}.
     */
    public String redirect;
    /**
     * if not {@code null}, the UI should show this error message and stop
     * processing the search. Automatically set via {@link #prepareExec(SortedSet)}
     * and {@link #executeQuery()}.
     */
    public String errorMsg;
    /**
     * the searcher used to open/search the index. Automatically set via
     * {@link #prepareExec(SortedSet)}.
     */
    public IndexSearcher searcher;
    /**
     * list of docs which result from the executing the query
     */
    public ScoreDoc[] hits;

    public File getDataRoot() {
        return dataRoot;
    }

    public String getContextPath() {
        return contextPath;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public File getSourceRoot() {
        return sourceRoot;
    }

    public int getStart() {
        return start;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public QueryBuilder getBuilder() {
        return builder;
    }

    public SortOrder getOrder() {
        return order;
    }

    public boolean isCrossRefSearch() {
        return isCrossRefSearch;
    }

    public String getRedirect() {
        return redirect;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public IndexSearcher getSearcher() {
        return searcher;
    }

    public ScoreDoc[] getHits() {
        return hits;
    }

    public int getTotalHits() {
        return totalHits;
    }

    public Query getQuery() {
        return query;
    }

    public SortedSet<String> getProjects() {
        return projects;
    }

    public Context getSourceContext() {
        return sourceContext;
    }

    public Summarizer getSummerizer() {
        return summerizer;
    }

    /**
     * total number of hits
     */
    public int totalHits;
    /**
     * the query created by the used {@link QueryBuilder} via
     * {@link #prepareExec(SortedSet)}.
     */
    public Query query;
    /**
     * the lucene sort instruction based on {@link #order} created via
     * {@link #prepareExec(SortedSet)}.
     */
    private Sort sort;
    /**
     * projects to use to setup indexer searchers. Usually setup via
     * {@link #prepareExec(SortedSet)}.
     */
    public SortedSet<String> projects;
    /**
     * opengrok summary context. Usually created via {@link #prepareSummary()}.
     */
    public Context sourceContext = null;
    /**
     * result summarizer usually created via {@link #prepareSummary()}.
     */
    public Summarizer summerizer = null;
    /**
     * Default query parse error message prefix
     */
    public static final String PARSE_ERROR_MSG = "Unable to parse your query: ";

    private static final Logger log = Logger.getLogger(SearchHelper.class.getName());

    /**
     * Create the searcher to use wrt. to currently set parameters and the given
     * projects. Does not produce any {@link #redirect} link. It also does
     * nothing if {@link #redirect} or {@link #errorMsg} have a none-{@code null}
     * value.
     * <p/>
     * Parameters which should be populated/set at this time:
     * <ul>
     * <li>{@link #builder}</li>
     * <li>{@link #dataRoot}</li>
     * <li>{@link #order} (falls back to relevance if unset)</li>
     * </ul>
     * Populates/sets:
     * <ul>
     * <li>{@link #query}</li>
     * <li>{@link #searcher}</li>
     * <li>{@link #sort}</li>
     * <li>{@link #projects}</li>
     * <li>{@link #errorMsg} if an error occurs</li>
     * </ul>
     *
     * @param projects project to use query. If empty, a none-project opengrok
     *                 setup is assumed (i.e. DATA_ROOT/index will be used instead of possible
     *                 multiple DATA_ROOT/$project/index).
     * @return this instance
     */
    public SearchHelper prepareExec(SortedSet<String> projects) {
        if (redirect != null || errorMsg != null) {
            return this;
        }
        // the Query created by the QueryBuilder
        try {
            query = builder.build();
            if (projects == null) {
                errorMsg = "No project selected!";
                return this;
            }
            this.projects = projects;
            File indexDir = new File(dataRoot, "index");
            if (projects.isEmpty()) {
                //no project setup
                FSDirectory dir = FSDirectory.open(indexDir);
                searcher = new IndexSearcher(IndexReader.open(dir));
            } else if (projects.size() == 1) {
                // just 1 project selected
                FSDirectory dir = FSDirectory.open(new File(indexDir, projects.first()));
                searcher = new IndexSearcher(IndexReader.open(dir));
            } else {
                //more projects
                IndexReader[] searchables = new IndexReader[projects.size()];
                int ii = 0;
                //TODO might need to rewrite to Project instead of
                // String , need changes in og_projects.jspf too
                for (String proj : projects) {
                    FSDirectory dir = FSDirectory.open(new File(indexDir, proj));
                    searchables[ii++] = IndexReader.open(dir);
                }
                searcher = new IndexSearcher(new MultiReader(searchables));
            }
            // TODO check if below is somehow reusing sessions so we don't
            // requery again and again, I guess 2min timeout sessions could be
            // usefull, since you click on the next page within 2mins, if not,
            // then wait ;)
            switch (order) {
                case LASTMODIFIED:
                    sort = new Sort(new SortField("date", SortField.STRING, true));
                    break;
                case BY_PATH:
                    sort = new Sort(new SortField("fullpath", SortField.STRING));
                    break;
                default:
                    sort = Sort.RELEVANCE;
                    break;
            }
        } catch (ParseException e) {
            errorMsg = PARSE_ERROR_MSG + e.getMessage();
        } catch (FileNotFoundException e) {
//          errorMsg = "Index database(s) not found: " + e.getMessage();
            errorMsg = "Index database(s) not found.";
        } catch (Exception e) {
            errorMsg = e.getMessage();
        }
        return this;
    }

    /**
     * Start the search prepared by {@link #prepareExec(SortedSet)}.
     * It does nothing if {@link #redirect} or {@link #errorMsg} have a
     * none-{@code null} value.
     * <p/>
     * Parameters which should be populated/set at this time:
     * <ul>
     * <li>all fields required for and populated by {@link #prepareExec(SortedSet)})</li>
     * <li>{@link #start} (default: 0)</li>
     * <li>{@link #maxItems} (default: 0)</li>
     * <li>{@link #isCrossRefSearch} (default: false)</li>
     * </ul>
     * Populates/sets:
     * <ul>
     * <li>{@link #hits} (see {@link TopFieldDocs#scoreDocs})</li>
     * <li>{@link #totalHits} (see {@link TopFieldDocs#totalHits})</li>
     * <li>{@link #contextPath}</li>
     * <li>{@link #errorMsg} if an error occurs</li>
     * <li>{@link #redirect} if certain conditions are met</li>
     * </ul>
     *
     * @return this instance
     */
    public SearchHelper executeQuery() {
        if (redirect != null || errorMsg != null) {
            return this;
        }
        try {
            TopFieldDocs fdocs = searcher.search(query, null, start + maxItems, sort);
            totalHits = fdocs.totalHits;
            hits = fdocs.scoreDocs;
            // Bug #3900: Check if this is a search for a single term, and that
            // term is a definition. If that's the case, and we only have one match,
            // we'll generate a direct link instead of a listing.
            boolean isSingleDefinitionSearch = (query instanceof TermQuery) && (builder.getDefs() != null);

            // Attempt to create a direct link to the definition if we search for
            // one single definition term AND we have exactly one match AND there
            // is only one definition of that symbol in the document that matches.
            boolean uniqueDefinition = false;
            if (isSingleDefinitionSearch && hits != null && hits.length == 1) {
                Document doc = searcher.doc(hits[0].doc);
                if (doc.getFieldable("tags") != null) {
                    byte[] rawTags = doc.getFieldable("tags").getBinaryValue();
                    Definitions tags = Definitions.deserialize(rawTags);
                    String symbol = ((TermQuery) query).getTerm().text();
                    if (tags.occurrences(symbol) == 1) {
                        uniqueDefinition = true;
                    }
                }
            }
            // @TODO fix me. I should try to figure out where the exact hit is
            // instead of returning a page with just _one_ entry in....
            if (uniqueDefinition && hits != null && hits.length > 0 && isCrossRefSearch) {
                redirect = contextPath + Prefix.XREF_P + Util.URIEncodePath(searcher.doc(hits[0].doc).get("path")) + '#' + Util.URIEncode(((TermQuery) query).getTerm().text());
            }
        } catch (BooleanQuery.TooManyClauses e) {
            errorMsg = "Too many results for wildcard!";
        } catch (Exception e) {
            errorMsg = e.getMessage();
        }
        return this;
    }

    private static final Pattern TABSPACE = Pattern.compile("[\t ]+");

    private static void getSuggestion(String term, SpellChecker checker, List<String> result) throws IOException {
        if (term == null) {
            return;
        }
        String[] toks = TABSPACE.split(term, 0);
        for (String tok : toks) {
            if (tok.length() <= 3) {
                continue;
            }
            result.addAll(Arrays.asList(checker.suggestSimilar(tok.toLowerCase(), 5)));
        }
    }

    /**
     * If a search did not return a hit, one may use this method to obtain
     * suggestions for a new search.
     * <p/>
     * <p/>
     * Parameters which should be populated/set at this time:
     * <ul>
     * <li>{@link #projects}</li>
     * <li>{@link #dataRoot}</li>
     * <li>{@link #builder}</li>
     * </ul>
     *
     * @return a possible empty list of sugeestions.
     */
    public List<Suggestion> getSuggestions() {
        if (projects == null) {
            return new ArrayList<>(0);
        }
        File[] spellIndex = null;
        if (projects.isEmpty()) {
            spellIndex = new File[]{new File(dataRoot, "spellIndex")};
        } else if (projects.size() == 1) {
            spellIndex = new File[]{new File(dataRoot, "spellIndex/" + projects.first())};
        } else {
            spellIndex = new File[projects.size()];
            int ii = 0;
            File indexDir = new File(dataRoot, "spellIndex");
            for (String proj : projects) {
                spellIndex[ii++] = new File(indexDir, proj);
            }
        }
        List<Suggestion> res = new ArrayList<>();
        List<String> dummy = new ArrayList<>();
        for (File aSpellIndex : spellIndex) {
            if (!aSpellIndex.exists()) {
                continue;
            }
            SpellChecker checker = null;
            Suggestion s = new Suggestion(aSpellIndex.getName());
            try (FSDirectory spellDirectory = FSDirectory.open(aSpellIndex)) {
                checker = new SpellChecker(spellDirectory);
                getSuggestion(builder.getFreetext(), checker, dummy);
                s.freetext = dummy.toArray(new String[dummy.size()]);
                dummy.clear();
                getSuggestion(builder.getRefs(), checker, dummy);
                s.refs = dummy.toArray(new String[dummy.size()]);
                dummy.clear();
                // TODO it seems the only true spellchecker is for
                // below field, see IndexDatabase
                // createspellingsuggestions ...
                getSuggestion(builder.getDefs(), checker, dummy);
                s.defs = dummy.toArray(new String[dummy.size()]);
                dummy.clear();
                if (s.freetext.length > 0 || s.defs.length > 0 || s.refs.length > 0) {
                    res.add(s);
                }
            } catch (IOException e) {
                log.log(Level.WARNING, "Got excption while getting spelling suggestions: ", e);
            } finally {
                if (checker != null) {
                    try {
                        checker.close();
                    } catch (Exception x) {
                        log.log(Level.WARNING, "Got excption while closing spelling suggestions: ", x);
                    }
                }
            }
        }
        return res;
    }

    public class ResultDirectory {
        private String label;
        private String link;
        private List<ResultFile> files;

        public ResultDirectory() {
            files = new ArrayList<>();
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public void setFiles(List<ResultFile> files) {
            this.files = files;
        }

        public String getLabel() {
            return label;
        }

        public String getLink() {
            return link;
        }

        public List<ResultFile> getFiles() {
            return files;
        }
    }

    public class ResultFile {
        private String label;
        private String link;
        private List<ResultLine> lines;
        private String linkMore;
        private boolean moreResults;

        public ResultFile() {
            lines = new ArrayList<>();
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public void setLines(List<ResultLine> lines) {
            this.lines = lines;
        }

        public void setLinkMore(String linkMore) {
            this.linkMore = linkMore;
        }

        public void setMoreResults(boolean moreResults) {
            this.moreResults = moreResults;
        }

        public boolean isMoreResults() {
            return moreResults;
        }

        public String getLinkMore() {

            return linkMore;
        }

        public String getLabel() {
            return label;
        }

        public String getLink() {
            return link;
        }

        public List<ResultLine> getLines() {
            return lines;
        }
    }

    public class ResultLine {
        private int lineNumber;
        private String line;
        private String link;
        private String matchedSymbol;
        private String type;
        private String summary;

        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public void setLine(String line) {
            this.line = line;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public void setMatchedSymbol(String matchedSymbol) {
            this.matchedSymbol = matchedSymbol;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getSummary() {
            return summary;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getLink() {
            return link;
        }

        public String getLine() {
            return line;
        }

        public String getMatchedSymbol() {
            return matchedSymbol;
        }

        public String getType() {
            return type;
        }
    }

    public List<ResultDirectory> getSearchResults() {
        List<ResultDirectory> result = new ArrayList<>();
        Map<String, ResultDirectory> directories = new HashMap<>();
        Map<String, ResultFile> files = new HashMap<>();
        Map<String, Map<String, ResultFile>> filesOfDirs = new HashMap<>();
        File xrefDataDir = new File(getDataRoot(), Prefix.XREF_P.toString());

        for (int i = getStart(); i < getThisPageEndIndex(); i++) {
            int docId = getHits()[i].doc;
            try {
                Document doc = getSearcher().doc(docId);
                String path = doc.get("path");
                String parent = path.substring(0, path.lastIndexOf('/'));

                ResultDirectory dir;
                if (!directories.containsKey(parent)) {
                    dir = new ResultDirectory();
                    dir.setLabel(parent);
                    dir.setLink(getContextPath() + Prefix.XREF_P + Util.URIEncodePath(parent));

                    directories.put(parent, dir);
                    result.add(dir);
                } else {
                    dir = directories.get(parent);
                }

                ResultFile file;
                if (!files.containsKey(path)) {
                    file = new ResultFile();
                    file.setLabel(path.substring(path.lastIndexOf('/') + 1));
                    file.setLink(getContextPath() + Prefix.XREF_P + Util.URIEncodePath(path));

                    files.put(path, file);
                } else {
                    file = files.get(path);
                }

                if (!filesOfDirs.containsKey(parent)) {
                    HashMap<String, ResultFile> filesOfDir = new HashMap<>();
                    filesOfDir.put(path, file);
                    filesOfDirs.put(parent, filesOfDir);
                    dir.getFiles().add(file);
                } else if (!filesOfDirs.get(parent).containsKey(path)) {
                    filesOfDirs.get(parent).put(path, file);
                    dir.getFiles().add(file);
                }

                FileAnalyzer.Genre genre = FileAnalyzer.Genre.get(doc.get("t"));

                if (genre == FileAnalyzer.Genre.XREFABLE) {
                    ResultLine line = new ResultLine();
                    String tags = Results.getTags(xrefDataDir, path, isCompressed());
                    String summary = getSummerizer().getSummary(tags).toString();
                    line.setSummary(summary);
                    file.getLines().add(line);

                } else if (genre == FileAnalyzer.Genre.HTML) {
                    ResultLine line = new ResultLine();
                    String tags = Results.getTags(getSourceRoot(), path, false);
                    String summary = getSummerizer().getSummary(tags).toString();
                    line.setSummary(summary);
                    file.getLines().add(line);
                } else {
                    Fieldable tagsField = doc.getFieldable("tags");
                    Definitions tags = Definitions.deserialize(tagsField.getBinaryValue());
                    for (Definitions.Tag tag : tags.getTags()) {
                        for (LineMatcher lineMatcher : getSourceContext().getLineMatchers()) {
                            if (lineMatcher.match(tag.symbol) == LineMatcher.MATCHED) {
                                ResultLine line = new ResultLine();
                                line.setSummary(tag.text);
                                line.setLine(tag.text);
                                line.setLineNumber(tag.line);
                                line.setMatchedSymbol(tag.symbol);
                                line.setType(tag.type);

                                file.getLines().add(line);
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    public String getSearchResultTable() {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(content);

        try {
            Results.prettyPrint(writer, this, getStart(), getThisPageEndIndex());
            writer.flush();
            return content.toString();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String createUrl(boolean menu) {
        StringBuilder url = new StringBuilder(64);
        QueryBuilder qb = getBuilder();
        if (menu) {
            url.append("search?");
        } else {
            Util.appendQuery(url, "sort", order.toString());
        }
        if (qb != null) {
            Util.appendQuery(url, "q", qb.getFreetext());
            Util.appendQuery(url, "defs", qb.getDefs());
            Util.appendQuery(url, "refs", qb.getRefs());
            Util.appendQuery(url, "path", qb.getPath());
        }
        return url.toString();
    }

    private List<Map<String,Object>> paging = null;

    public List<Map<String,Object>> getPaging() {
        if (paging == null) {
            paging = createPaging();
        }
        return paging;
    }

    public boolean isPagingEnabled() {
        return getPaging().size() > 1;
    }

    public List<Map<String,Object>> createPaging() {
        List<Map<String,Object>> result = new ArrayList<>();

        int pagesStart = getStart() - getMaxItems() * (getStart() / getMaxItems() % 10 + 1);
        int labelStart = 1;
        if (pagesStart < 0) {
            pagesStart = 0;
        } else {
            labelStart = pagesStart / getMaxItems() + 1;
        }

        int label = labelStart;
        int labelEnd = label + 11;
        String params = createUrl(false);

        for (int i = pagesStart; i < getTotalHits() && labelStart < labelEnd; i += getMaxItems()) {
            Map<String,Object> page = new HashMap<>();

            if (label == labelStart && label != 1) {
                page.put("label", "&lt;&lt;");
            } else if (label == labelEnd && i < getTotalHits()) {
                page.put("label", "&gt;&gt;");
            } else {
                page.put("label", label);
            }

            if (i <= getStart() && getStart() < i + getMaxItems()) {
                page.put("active", true);
                page.put("link", "#");
            } else {
                page.put("active", false);
                page.put("link", getContextPath() + Prefix.SEARCH_R + "?n=" + getMaxItems() + "&amp;start=" + i + params);
            }
            label++;
            result.add(page);
        }

        return result;
    }

    public int getThisPageIndex() {
        if (getMaxItems() < getTotalHits() && (getStart() + getMaxItems()) < getTotalHits()) {
            return getMaxItems();
        }
        return getTotalHits() - getStart();
    }


    public int getThisPageEndIndex() {
        return getThisPageIndex() + getStart();
    }

    /**
     * Prepare the fields to support printing a fullblown summary. Does nothing
     * if {@link #redirect} or {@link #errorMsg} have a none-{@code null} value.
     * <p/>
     * <p/>
     * Parameters which should be populated/set at this time:
     * <ul>
     * <li>{@link #query}</li>
     * <li>{@link #builder}</li>
     * </ul>
     * Populates/sets:
     * Otherwise the following fields are set (includes {@code null}):
     * <ul>
     * <li>{@link #sourceContext}</li>
     * <li>{@link #summerizer}</li>
     * </ul>
     *
     * @return this instance.
     */
    public SearchHelper prepareSummary() {
        if (redirect != null || errorMsg != null) {
            return this;
        }
        try {
            sourceContext = new Context(query, builder.getQueries());
            summerizer = new Summarizer(query, new CompatibleAnalyser());
        } catch (Exception e) {
            OpenGrokLogger.getLogger().log(Level.WARNING, "Summerizer: {0}", e.getMessage());
        }
        return this;
    }

    /**
     * Free any resources associated with this helper (that includes closing
     * the used {@link #searcher}).
     */
    public void destroy() {
        IOUtils.close(searcher);
    }
}
