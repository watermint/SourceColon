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
 */
package org.opensolaris.opengrok.web;

import org.opensolaris.opengrok.configuration.Project;
import org.opensolaris.opengrok.configuration.RuntimeEnvironment;
import org.opensolaris.opengrok.index.IgnoredNames;
import org.opensolaris.opengrok.search.QueryBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A simple container to lazy initialize common vars wrt. a single request.
 * It MUST NOT be shared between several requests and {@link #cleanup()} should
 * be called before the page context gets destroyed (e.g. by overwriting
 * {@code jspDestroy()} or when leaving the {@code service} method.
 * <p/>
 * Purpose is to decouple implementation details from web design, so that the
 * JSP developer does not need to know every implementation detail and normally
 * has to deal with this class/wrapper, only (so some people may like to call
 * this class a bean with request scope ;-)). Furthermore it helps to keep the
 * pages (how content gets generated) consistent and to document the request
 * parameters used.
 * <p/>
 * General contract for this class (i.e. if not explicitly documented):
 * no method of this class changes neither the request nor the response.
 *
 * @author Jens Elkner
 * @version $Revision$
 */
public final class PageConfig {
    // TODO if still used, get it from the app context

    private RuntimeEnvironment env;
    private IgnoredNames ignoredNames;
    private String path;
    private File resourceFile;
    private String resourcePath;
    private String sourceRootPath;
    private Boolean isDir;
    private String uriEncodedPath;
    private Prefix prefix;
    private String pageTitle;
    private String rev;
    private SortedSet<String> requestedProjects;
    private String requestedProjectsString;
    private List<String> dirFileList;
    private QueryBuilder queryBuilder;
    private File dataRoot;
    private static final Logger log = Logger.getLogger(PageConfig.class.getName());

    /**
     * Get a list of filenames in the requested path.
     *
     * @return an empty list, if the resource does not exist, is not a
     *         directory or an error occurred when reading it, otherwise a list of
     *         filenames in that directory, sorted alphabetically
     * @see #getResourceFile()
     * @see #isDir()
     */
    public List<String> getResourceFileList() {
        if (dirFileList == null) {
            String[] files = null;
            if (isDir() && getResourcePath().length() > 1) {
                files = getResourceFile().list();
            }
            if (files == null) {
                dirFileList = Collections.emptyList();
            } else {
                Arrays.sort(files, String.CASE_INSENSITIVE_ORDER);
                dirFileList =
                        Collections.unmodifiableList(Arrays.asList(files));
            }
        }
        return dirFileList;
    }

    /**
     * Get the time of last modification of the related file or directory.
     *
     * @return the last modification time of the related file or directory.
     * @see File#lastModified()
     */
    public long getLastModified() {
        return getResourceFile().lastModified();
    }

    /**
     * Get the int value of the given request parameter.
     *
     * @param name         name of the parameter to lookup.
     * @param defaultValue value to return, if the parameter is not set, is not
     *                     a number, or is &lt; 0.
     * @return the parsed int value on success, the given default value otherwise.
     */
    public int getIntParam(String name, int defaultValue) {
        int ret = defaultValue;
        String s = req.getParameter(name);
        if (s != null && s.length() != 0) {
            try {
                int x = Integer.parseInt(s, 10);
                if (x >= 0) {
                    ret = x;
                }
            } catch (Exception e) {
                log.log(Level.INFO, "Failed to parse integer " + s, e);
            }
        }
        return ret;
    }

    /**
     * Get the <b>start</b> index for a search result to return by looking up
     * the {@code start} request parameter.
     *
     * @return 0 if the corresponding start parameter is not set or not a number,
     *         the number found otherwise.
     */
    public int getSearchStart() {
        return getIntParam("start", 0);
    }

    /**
     * Get the number of search results to max. return by looking up the
     * {@code n} request parameter.
     *
     * @return the default number of hits if the corresponding start parameter
     *         is not set or not a number, the number found otherwise.
     */
    public int getSearchMaxItems() {
        return getIntParam("n", getEnv().getHitsPerPage());
    }

    /**
     * Get sort orders from the request parameter {@code sort} and if this list
     * would be empty from the cookie {@code OpenGrokorting}.
     *
     * @return a possible empty list which contains the sort order values in
     *         the same order supplied by the request parameter or cookie(s).
     */
    public List<SortOrder> getSortOrder() {
        List<SortOrder> sort = new ArrayList<>();
        List<String> vals = getParamVals("sort");
        for (String s : vals) {
            SortOrder so = SortOrder.get(s);
            if (so != null) {
                sort.add(so);
            }
        }
        if (sort.isEmpty()) {
            vals = getCookieVals("OpenGrokSorting");
            for (String s : vals) {
                SortOrder so = SortOrder.get(s);
                if (so != null) {
                    sort.add(so);
                }
            }
        }
        return sort;
    }

    /**
     * Get a reference to the {@code QueryBuilder} wrt. to the current request
     * parameters:
     * <dl>
     * <dt>q</dt>
     * <dd>freetext lookup rules</dd>
     * <dt>defs</dt>
     * <dd>definitions lookup rules</dd>
     * <dt>path</dt>
     * <dd>path related rules</dd>
     * </dl>
     *
     * @return a query builder with all relevant fields populated.
     */
    public QueryBuilder getQueryBuilder() {
        if (queryBuilder == null) {
            queryBuilder = new QueryBuilder().setFreetext(req.getParameter("q")).setDefs(req.getParameter("defs")).setRefs(req.getParameter("refs")).setPath(req.getParameter("path"));

            // This is for backward compatibility with links created by OpenGrok
            // 0.8.x and earlier. We used to concatenate the entire query into a
            // single string and send it in the t parameter. If we get such a
            // link, just add it to the freetext field, and we'll get the old
            // behaviour. We can probably remove this code in the first feature
            // release after 0.9.
            String t = req.getParameter("t");
            if (t != null) {
                queryBuilder.setFreetext(t);
            }
        }
        return queryBuilder;
    }

    /**
     * Get the revision parameter {@code r} from the request.
     *
     * @return {@code "r=<i>revision</i>"} if found, an empty string otherwise.
     */
    public String getRequestedRevision() {
        if (rev == null) {
            String tmp = req.getParameter("r");
            rev = (tmp != null && tmp.length() > 0) ? "r=" + tmp : "";
        }
        return rev;
    }

    /**
     * Get the name which should be show as "Crossfile"
     *
     * @return the name of the related file or directory.
     */
    public String getCrossFilename() {
        return getResourceFile().getName();
    }

    /**
     * Get the {@code path} parameter and display value for "Search only in"
     * option.
     *
     * @return always an array of 3 fields, whereby field[0] contains the
     *         path value to use (starts and ends always with a '/'). Field[1] the
     *         contains string to show in the UI. field[2] is set to
     *         {@code disabled=""} if the current path is the "/" directory,
     *         otherwise set to an empty string.
     */
    public String[] getSearchOnlyIn() {
        if (isDir()) {
            return path.length() == 0
                    ? new String[]{"/", "/", "disabled=\"\""}
                    : new String[]{path, path, ""};
        }
        String[] res = new String[3];
        res[0] = path.substring(0, path.lastIndexOf('/') + 1);
        res[1] = path.substring(res[0].length());
        res[2] = "";
        return res;
    }

    /**
     * Same as {@link #getRequestedProjects()} but returns the project names as
     * a coma separated String.
     *
     * @return a possible empty String but never {@code null}.
     */
    public String getRequestedProjectsAsString() {
        if (requestedProjectsString == null) {
            Set<String> projects = getRequestedProjects();
            if (projects.isEmpty()) {
                requestedProjectsString = "";
            } else {
                StringBuilder buf = new StringBuilder();
                for (String name : projects) {
                    buf.append(name).append(',');
                }
                buf.setLength(buf.length() - 1);
                requestedProjectsString = buf.toString();
            }
        }
        return requestedProjectsString;
    }

    /**
     * Get the document hash provided by the request parameter {@code h}.
     *
     * @return {@code null} if the request does not contain such a parameter,
     *         its value otherwise.
     */
    public String getDocumentHash() {
        return req.getParameter("h");
    }

    /**
     * Get a reference to a set of requested projects via request parameter
     * {@code project} or cookies or defaults.
     * <p/>
     * NOTE: This method assumes, that project names do <b>not</b> contain
     * a comma (','), since this character is used as name separator!
     *
     * @return a possible empty set of project names aka descriptions but never
     *         {@code null}. It is determined as
     *         follows:
     *         <ol>
     *         <li>If there is no project in the runtime environment (RTE) an empty
     *         set is returned. Otherwise:</li>
     *         <li>If there is only one project in the RTE, this one gets returned (no
     *         matter, what the request actually says). Otherwise</li>
     *         <li>If the request parameter {@code project} contains any available
     *         project, the set with invalid projects removed gets returned.
     *         Otherwise:</li>
     *         <li>If the request has a cookie with the name {@code OpenGrokProject}
     *         and it contains any available project, the set with invalid
     *         projects removed gets returned. Otherwise:</li>
     *         <li>If a default project is set in the RTE, this project gets returned.
     *         Otherwise:</li>
     *         <li>an empty set</li>
     *         </ol>
     */
    public SortedSet<String> getRequestedProjects() {
        if (requestedProjects == null) {
            requestedProjects =
                    getRequestedProjects("project", "OpenGrokProject");
        }
        return requestedProjects;
    }

    private static Pattern COMMA_PATTERN = Pattern.compile(",");

    private static void splitByComma(String value, List<String> result) {
        if (value == null || value.length() == 0) {
            return;
        }
        String p[] = COMMA_PATTERN.split(value);
        for (String aP : p) {
            if (aP.length() != 0) {
                result.add(aP);
            }
        }
    }

    /**
     * Get the cookie values for the given name. Splits comma separated values
     * automatically into a list of Strings.
     *
     * @param cookieName name of the cookie.
     * @return a possible empty list.
     */
    public List<String> getCookieVals(String cookieName) {
        Cookie[] cookies = req.getCookies();
        ArrayList<String> res = new ArrayList<>();
        if (cookies != null) {
            for (int i = cookies.length - 1; i >= 0; i--) {
                if (cookies[i].getName().equals(cookieName)) {
                    splitByComma(cookies[i].getValue(), res);
                }
            }
        }
        return res;
    }

    /**
     * Get the parameter values for the given name. Splits comma separated
     * values automatically into a list of Strings.
     *
     * @param paramName name of the parameter.
     * @return a possible empty list.
     */
    private List<String> getParamVals(String paramName) {
        String vals[] = req.getParameterValues(paramName);
        List<String> res = new ArrayList<>();
        if (vals != null) {
            for (int i = vals.length - 1; i >= 0; i--) {
                splitByComma(vals[i], res);
            }
        }
        return res;
    }

    /**
     * Same as {@link #getRequestedProjects()}, but with a variable cookieName
     * and parameter name. This way it is trivial to implement a project filter
     * ...
     *
     * @param paramName  the name of the request parameter, which possibly
     *                   contains the project list in question.
     * @param cookieName name of the cookie which possible contains project
     *                   lists used as fallback
     * @return a possible empty set but never {@code null}.
     */
    protected SortedSet<String> getRequestedProjects(String paramName,
                                                     String cookieName) {
        TreeSet<String> set = new TreeSet<>();
        List<Project> projects = getEnv().getProjects();
        if (projects == null) {
            return set;
        }
        if (projects.size() == 1) {
            set.add(projects.get(0).getProjectId());
            return set;
        }
        List<String> vals = getParamVals(paramName);
        for (String s : vals) {
            if (Project.getByProjectId(s) != null) {
                set.add(s);
            }
        }
        if (set.isEmpty()) {
            List<String> cookies = getCookieVals(cookieName);
            for (String s : cookies) {
                if (Project.getByProjectId(s) != null) {
                    set.add(s);
                }
            }
        }
        if (set.isEmpty()) {
            Project defaultProject = env.getDefaultProject();
            if (defaultProject != null) {
                set.add(defaultProject.getProjectId());
            }
        }
        return set;
    }

    /**
     * Set the page title to use.
     *
     * @param title title to set (might be {@code null}).
     */
    public void setTitle(String title) {
        pageTitle = title;
    }

    /**
     * Get the page title to use.
     *
     * @return {@code null} if not set, the page title otherwise.
     */
    public String getTitle() {
        return pageTitle;
    }

    /**
     * Get the current runtime environment.
     *
     * @return the runtime env.
     * @see RuntimeEnvironment#getInstance()
     * @see RuntimeEnvironment#register()
     */
    public RuntimeEnvironment getEnv() {
        if (env == null) {
            env = RuntimeEnvironment.getInstance().register();
        }
        return env;
    }

    /**
     * Get the name patterns used to determine, whether a file should be
     * ignored.
     *
     * @return the corresponding value from the current runtime config..
     */
    public IgnoredNames getIgnoredNames() {
        if (ignoredNames == null) {
            ignoredNames = getEnv().getIgnoredNames();
        }
        return ignoredNames;
    }

    /**
     * Get the canonical path to root of the source tree. File separators are
     * replaced with a '/'.
     *
     * @return The on disk source root directory.
     * @see RuntimeEnvironment#getSourceRootPath()
     */
    public String getSourceRootPath() {
        if (sourceRootPath == null) {
            sourceRootPath = getEnv().getSourceRootPath().replace(File.separatorChar, '/');
        }
        return sourceRootPath;
    }

    /**
     * Get the prefix for the related request.
     *
     * @return {@link Prefix#UNKNOWN} if the servlet path matches any known
     *         prefix, the prefix otherwise.
     */
    public Prefix getPrefix() {
        if (prefix == null) {
            prefix = Prefix.get(req.getServletPath());
        }
        return prefix;
    }

    /**
     * Get the canonical path of the related resource relative to the
     * source root directory (used file separators are all '/'). No check is
     * made, whether the obtained path is really an accessible resource on disk.
     *
     * @return a possible empty String (denotes the source root directory) but
     *         not {@code null}.
     * @see HttpServletRequest#getPathInfo()
     */
    public String getPath() {
        if (path == null) {
            path = Util.getCanonicalPath(req.getPathInfo(), '/');
            if ("/".equals(path)) {
                path = "";
            }
        }
        return path;
    }

    /**
     * If a requested resource is not available, append "/on/" to
     * the source root directory and try again to resolve it.
     *
     * @return on success a none-{@code null} gets returned, which should be
     *         used to redirect the client to the propper path.
     */
    public String getOnRedirect() {
        boolean check4on = true;
        if (check4on) {
            File newFile = new File(getSourceRootPath() + "/on/" + getPath());
            if (newFile.canRead()) {
                return req.getContextPath() + req.getServletPath() + "/on"
                        + getUriEncodedPath()
                        + (newFile.isDirectory() ? trailingSlash(path) : "");
            }
        }
        return null;
    }

    /**
     * Get the on disk file to the request related file or directory.
     * <p/>
     * NOTE: If a repository contains hard or symbolic links, the returned
     * file may finally point to a file outside of the source root directory.
     *
     * @return {@code new File("/")} if the related file or directory is not
     *         available (can not be find below the source root directory),
     *         the readable file or directory otherwise.
     * @see #getSourceRootPath()
     * @see #getPath()
     */
    public File getResourceFile() {
        if (resourceFile == null) {
            resourceFile = new File(getSourceRootPath(), getPath());
            if (!resourceFile.canRead()) {
                resourceFile = new File("/");
            }
        }
        return resourceFile;
    }

    /**
     * Get the canonical on disk path to the request related file or directory
     * with all file separators replaced by a '/'.
     *
     * @return "/" if the evaluated path is invalid or outside the source root
     *         directory), otherwise the path to the readable file or directory.
     * @see #getResourceFile()
     */
    public String getResourcePath() {
        if (resourcePath == null) {
            resourcePath = getResourceFile().getPath().replace(File.separatorChar, '/');
        }
        return resourcePath;
    }

    /**
     * Check, whether the related request resource matches a valid file or
     * directory below the source root directory and wether it matches an
     * ignored pattern.
     *
     * @return {@code true} if the related resource does not exists or should be
     *         ignored.
     * @see #getIgnoredNames()
     * @see #getResourcePath()
     */
    public boolean resourceNotAvailable() {
        getIgnoredNames();
        return getResourcePath().equals("/") || ignoredNames.ignore(getPath())
                || ignoredNames.ignore(resourceFile.getParentFile().getName());
    }

    /**
     * Check, whether the request related path represents a directory.
     *
     * @return {@code true} if directory related request
     */
    public boolean isDir() {
        if (isDir == null) {
            isDir = getResourceFile().isDirectory();
        }
        return isDir;
    }

    private static String trailingSlash(String path) {
        return path.length() == 0 || path.charAt(path.length() - 1) != '/'
                ? "/"
                : "";
    }

    private File checkFile(File dir, String name, boolean compressed) {
        File f = null;
        if (compressed) {
            f = new File(dir, name + ".gz");
            if (f.exists() && f.isFile()
                    && f.lastModified() >= resourceFile.lastModified()) {
                return f;
            }
        }
        f = new File(dir, name);
        if (f.exists() && f.isFile()
                && f.lastModified() >= resourceFile.lastModified()) {
            return f;
        }
        return null;
    }

    private File checkFileResolve(File dir, String name, boolean compressed) {
        File lresourceFile = new File(getSourceRootPath() + getPath(), name);
        if (!lresourceFile.canRead()) {
            lresourceFile = new File("/");
        }
        File f = null;
        if (compressed) {
            f = new File(dir, name + ".gz");
            if (f.exists() && f.isFile()
                    && f.lastModified() >= lresourceFile.lastModified()) {
                return f;
            }
        }
        f = new File(dir, name);
        if (f.exists() && f.isFile()
                && f.lastModified() >= lresourceFile.lastModified()) {
            return f;
        }
        return null;
    }

    /**
     * Find the files with the given names in the {@link #getPath()} directory
     * relative to the crossfile directory of the opengrok data directory. It
     * is tried to find the compressed file first by appending the file extension
     * ".gz" to the filename. If that fails or an uncompressed version of the
     * file is younger than its compressed version, the uncompressed file gets
     * used.
     *
     * @param filenames filenames to lookup.
     * @return an empty array if the related directory does not exist or the
     *         given list is {@code null} or empty, otherwise an array, which may
     *         contain {@code null} entries (when the related file could not be found)
     *         having the same order as the given list.
     */
    public File[] findDataFiles(List<String> filenames) {
        if (filenames == null || filenames.isEmpty()) {
            return new File[0];
        }
        File[] res = new File[filenames.size()];
        File dir = new File(getEnv().getDataRootPath() + Prefix.XREF_P + path);
        if (dir.exists() && dir.isDirectory()) {
            getResourceFile();
            boolean compressed = getEnv().isCompressXref();
            for (int i = res.length - 1; i >= 0; i--) {
                res[i] = checkFileResolve(dir, filenames.get(i), compressed);
            }
        }
        return res;
    }

    /**
     * Lookup the file {@link #getPath()} relative to the crossfile directory
     * of the opengrok data directory. It is tried to find the compressed file
     * first by appending the file extension ".gz" to the filename. If that
     * fails or an uncompressed version of the file is younger than its
     * compressed version, the uncompressed file gets used.
     *
     * @return {@code null} if not found, the file otherwise.
     */
    public File findDataFile() {
        return checkFile(new File(getEnv().getDataRootPath() + Prefix.XREF_P),
                path, env.isCompressXref());
    }

    /**
     * Get the path the request should be redirected (if any).
     *
     * @return {@code null} if there is no reason to redirect, the URI encoded
     *         redirect path to use otherwise.
     */
    public String getDirectoryRedirect() {
        if (isDir()) {
            if (path.length() == 0) {
                // => /
                return null;
            }
            getPrefix();
            if (prefix != Prefix.XREF_P) {
                //if it is an existing dir perhaps people wanted dir xref
                return req.getContextPath() + Prefix.XREF_P
                        + getUriEncodedPath() + trailingSlash(path);
            }
            String ts = trailingSlash(path);
            if (ts.length() != 0) {
                return req.getContextPath() + prefix + getUriEncodedPath() + ts;
            }
        }
        return null;
    }

    /**
     * Get the URI encoded canonical path to the related file or directory
     * (the URI part between the servlet path and the start of the query string).
     *
     * @return an URI encoded path which might be an empty string but not
     *         {@code null}.
     * @see #getPath()
     */
    public String getUriEncodedPath() {
        if (uriEncodedPath == null) {
            uriEncodedPath = Util.URIEncodePath(getPath());
        }
        return uriEncodedPath;
    }

    /**
     * Get opengrok's configured dataroot directory.
     * It is veriefied, that the used environment has a valid opengrok data root
     * set and that it is an accessable directory.
     *
     * @return the opengrok data directory.
     * @throws InvalidParameterException if inaccessable or not set.
     */
    public File getDataRoot() {
        if (dataRoot == null) {
            String tmp = getEnv().getDataRootPath();
            if (tmp == null || tmp.length() == 0) {
                throw new InvalidParameterException("dataRoot parameter is not "
                        + "set in configuration.xml!");
            }
            dataRoot = new File(tmp);
            if (!(dataRoot.isDirectory() && dataRoot.canRead())) {
                throw new InvalidParameterException("The configured dataRoot '"
                        + tmp
                        + "' refers to a none-exsting or unreadable directory!");
            }
        }
        return dataRoot;
    }

    /**
     * Prepare a search helper with all required information, ready to execute
     * the query implied by the related request parameters and cookies.
     * <p/>
     * NOTE: One should check the {@link SearchHelper#errorMsg} as well as
     * {@link SearchHelper#redirect} and take the appropriate action before
     * executing the prepared query or continue processing.
     * <p/>
     * This method stops populating fields as soon as an error occurs.
     *
     * @return a search helper.
     */
    public SearchHelper prepareSearch() {
        SearchHelper sh = new SearchHelper();
        sh.dataRoot = getDataRoot(); // throws Exception if none-existent
        List<SortOrder> sortOrders = getSortOrder();
        sh.order = sortOrders.isEmpty() ? SortOrder.RELEVANCY : sortOrders.get(0);
        if (getRequestedProjects().isEmpty() && getEnv().hasProjects()) {
            sh.errorMsg = "You must select a project!";
            return sh;
        }
        sh.builder = getQueryBuilder();
        if (sh.builder.getSize() == 0) {
            // Entry page show the map
            sh.redirect = req.getContextPath() + '/';
            return sh;
        }
        sh.start = getSearchStart();
        sh.maxItems = getSearchMaxItems();
        sh.contextPath = req.getContextPath();
        sh.isCrossRefSearch = getPrefix() == Prefix.SEARCH_R;
        sh.compressed = env.isCompressXref();
        sh.sourceRoot = new File(getSourceRootPath());
        return sh;
    }

    /**
     * Get the config wrt. the given request. If there is none yet, a new config
     * gets created, attached to the request and returned.
     * <p/>
     *
     * @param request the request to use to initialize the config parameters.
     * @return always the same none-{@code null} config for a given request.
     * @throws NullPointerException if the given parameter is {@code null}.
     */
    public static PageConfig get(HttpServletRequest request) {
        Object cfg = request.getAttribute(ATTR_NAME);
        if (cfg != null) {
            return (PageConfig) cfg;
        }
        PageConfig pcfg = new PageConfig(request);
        request.setAttribute(ATTR_NAME, pcfg);
        return pcfg;
    }

    private static final String ATTR_NAME = PageConfig.class.getCanonicalName();
    private HttpServletRequest req;

    private PageConfig(HttpServletRequest req) {
        this.req = req;
    }

    /**
     * Cleanup all allocated resources. Should always be called right before
     * leaving the _jspService / service.
     */
    public void cleanup() {
        if (req != null) {
            req.removeAttribute(ATTR_NAME);
            req = null;
        }
        env = null;
    }
}
