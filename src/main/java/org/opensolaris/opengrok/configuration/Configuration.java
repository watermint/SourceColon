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
 * Copyright (c) 2007, 2012, Oracle and/or its affiliates. All rights reserved.
 */
package org.opensolaris.opengrok.configuration;

import org.opensolaris.opengrok.index.Filter;
import org.opensolaris.opengrok.index.IgnoredNames;
import org.opensolaris.opengrok.util.IOUtils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Placeholder class for all configuration variables. Due to the multithreaded
 * nature of the web application, each thread will use the same instance of the
 * configuration object for each page request. Class and methods should have
 * package scope, but that didn't work with the XMLDecoder/XMLEncoder.
 */
public final class Configuration {
    private String ctags;

    private List<Project> projects;
    private String sourceRoot;
    private String dataRoot;
    private String urlPrefix;
    private boolean generateHtml;
    /**
     * Default project will be used, when no project is selected and no project is in cookie, so basically only the first time you open the first page, or when you clear your web cookies
     */
    private Project defaultProject;
    private int indexWordLimit;
    private boolean verbose;
    //if below is set, then we count how many files per project we need to process and print percentage of completion per project
    private boolean printProgress;
    private boolean allowLeadingWildcard;
    private IgnoredNames ignoredNames;
    private Filter includedNames;
    private String userPage;
    private String userPageSuffix;
    private String bugPage;
    private String bugPattern;
    private String reviewPage;
    private String reviewPattern;
    private boolean remoteScmSupported;
    private boolean optimizeDatabase;
    private boolean useLuceneLocking;
    private boolean compressXref;
    private boolean indexVersionedFilesOnly;
    private int hitsPerPage;
    private int cachePages;
    private String databaseDriver;
    private String databaseUrl;
    private int scanningDepth;
    private Set<String> allowedSymlinks;
    private boolean obfuscatingEMailAddresses;
    private boolean chattyStatusPage;
    private final Map<String, String> cmds;
    private int tabSize;

    /**
     * Get the default tab size (number of space characters per tab character)
     * to use for each project. If {@code <= 0} tabs are read/write as is.
     *
     * @return current tab size set.
     * @see Project#getTabSize()
     * @see org.opensolaris.opengrok.analysis.ExpandTabsReader
     */
    public int getTabSize() {
        return tabSize;
    }

    /**
     * Set the default tab size (number of space characters per tab character)
     * to use for each project. If {@code <= 0} tabs are read/write as is.
     *
     * @param tabSize tabsize to set.
     * @see Project#setTabSize(int)
     * @see org.opensolaris.opengrok.analysis.ExpandTabsReader
     */
    public void setTabSize(int tabSize) {
        this.tabSize = tabSize;
    }

    public int getScanningDepth() {
        return scanningDepth;
    }

    public void setScanningDepth(int scanningDepth) {
        this.scanningDepth = scanningDepth;
    }

    /**
     * Creates a new instance of Configuration
     */
    public Configuration() {
        //defaults for an opengrok instance configuration
        setProjects(new ArrayList<Project>());
        setUrlPrefix("/SourceColon/s?");
        //setUrlPrefix("../s?"); // TODO generate relative search paths, get rid of -w <webapp> option to indexer !
        setCtags(System.getProperty("org.opensolaris.opengrok.analysis.Ctags", "ctags"));
        //below can cause an out of memory error, since it is defaulting to NO LIMIT
        setIndexWordLimit(Integer.MAX_VALUE);
        setVerbose(false);
        setPrintProgress(false);
        setGenerateHtml(true);
        setQuickContextScan(true);
        setIgnoredNames(new IgnoredNames());
        setIncludedNames(new Filter());
        setBugPage("http://bugs.opensolaris.org/bugdatabase/view_bug.do?bug_id=");
        setBugPattern("\\b([12456789][0-9]{6})\\b");
        setReviewPage("http://arc.opensolaris.org/caselog/PSARC/");
        setReviewPattern("\\b(\\d{4}/\\d{3})\\b"); // in form e.g. PSARC 2008/305
        setRemoteScmSupported(false);
        setOptimizeDatabase(true);
        setUsingLuceneLocking(false);
        setCompressXref(true);
        setIndexVersionedFilesOnly(false);
        setHitsPerPage(25);
        setCachePages(5);
        setScanningDepth(3); // default depth of scanning for repositories
        setDataRoot(RuntimeEnvironment.DEFAULT_SOURCECOLON_DATA);
        setAllowedSymlinks(new HashSet<String>());
        //setTabSize(4);
        cmds = new HashMap<>();
    }

    public String getRepoCmd(String clazzName) {
        return cmds.get(clazzName);
    }

    public String setRepoCmd(String clazzName, String cmd) {
        if (clazzName == null) {
            return null;
        }
        if (cmd == null || cmd.length() == 0) {
            return cmds.remove(clazzName);
        }
        return cmds.put(clazzName, cmd);
    }

    public String getCtags() {
        return ctags;
    }

    public void setCtags(String ctags) {
        this.ctags = ctags;
    }

    public int getCachePages() {
        return cachePages;
    }

    public void setCachePages(int cachePages) {
        this.cachePages = cachePages;
    }

    public int getHitsPerPage() {
        return hitsPerPage;
    }

    public void setHitsPerPage(int hitsPerPage) {
        this.hitsPerPage = hitsPerPage;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public String getSourceRoot() {
        return sourceRoot;
    }

    public void setSourceRoot(String sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    public String getDataRoot() {
        return dataRoot;
    }

    public void setDataRoot(String dataRoot) {
        File dr = new File(dataRoot);
        if (!dr.isDirectory()) {
            dr.mkdirs();
        }
        if (!dr.isDirectory()) {
            // TODO: replace to appropriate exception
            throw new RuntimeException(dataRoot + " is not a directory. or failed to create directory");
        }
        this.dataRoot = dataRoot;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    /**
     * Set the URL prefix to be used by the {@link
     * org.opensolaris.opengrok.analysis.executables.JavaClassAnalyzer} as well
     * as lexers (see {@link org.opensolaris.opengrok.analysis.JFlexXref})
     * when they create output with html links.
     *
     * @param urlPrefix prefix to use.
     */
    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public void setGenerateHtml(boolean generateHtml) {
        this.generateHtml = generateHtml;
    }

    public boolean isGenerateHtml() {
        return generateHtml;
    }

    public void setDefaultProject(Project defaultProject) {
        this.defaultProject = defaultProject;
    }

    public Project getDefaultProject() {
        return defaultProject;
    }

    public int getIndexWordLimit() {
        return indexWordLimit;
    }

    public void setIndexWordLimit(int indexWordLimit) {
        this.indexWordLimit = indexWordLimit;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isPrintProgress() {
        return printProgress;
    }

    public void setPrintProgress(boolean printProgress) {
        this.printProgress = printProgress;
    }

    public void setAllowLeadingWildcard(boolean allowLeadingWildcard) {
        this.allowLeadingWildcard = allowLeadingWildcard;
    }

    public boolean isAllowLeadingWildcard() {
        return allowLeadingWildcard;
    }

    private boolean quickContextScan;

    public boolean isQuickContextScan() {
        return quickContextScan;
    }

    public void setQuickContextScan(boolean quickContextScan) {
        this.quickContextScan = quickContextScan;
    }

    public void setIgnoredNames(IgnoredNames ignoredNames) {
        this.ignoredNames = ignoredNames;
    }

    public IgnoredNames getIgnoredNames() {
        return ignoredNames;
    }

    public void setIncludedNames(Filter includedNames) {
        this.includedNames = includedNames;
    }

    public Filter getIncludedNames() {
        return includedNames;
    }

    public void setUserPage(String userPage) {
        this.userPage = userPage;
    }

    public String getUserPage() {
        return userPage;
    }

    public void setUserPageSuffix(String userPageSuffix) {
        this.userPageSuffix = userPageSuffix;
    }

    public String getUserPageSuffix() {
        return userPageSuffix;
    }

    public void setBugPage(String bugPage) {
        this.bugPage = bugPage;
    }

    public String getBugPage() {
        return bugPage;
    }

    public void setBugPattern(String bugPattern) {
        this.bugPattern = bugPattern;
    }

    public String getBugPattern() {
        return bugPattern;
    }

    public String getReviewPage() {
        return reviewPage;
    }

    public void setReviewPage(String reviewPage) {
        this.reviewPage = reviewPage;
    }

    public String getReviewPattern() {
        return reviewPattern;
    }

    public void setReviewPattern(String reviewPattern) {
        this.reviewPattern = reviewPattern;
    }

    public boolean isRemoteScmSupported() {
        return remoteScmSupported;
    }

    public void setRemoteScmSupported(boolean remoteScmSupported) {
        this.remoteScmSupported = remoteScmSupported;
    }

    public boolean isOptimizeDatabase() {
        return optimizeDatabase;
    }

    public void setOptimizeDatabase(boolean optimizeDatabase) {
        this.optimizeDatabase = optimizeDatabase;
    }

    public boolean isUsingLuceneLocking() {
        return useLuceneLocking;
    }

    public void setUsingLuceneLocking(boolean useLuceneLocking) {
        this.useLuceneLocking = useLuceneLocking;
    }

    public void setCompressXref(boolean compressXref) {
        this.compressXref = compressXref;
    }

    public boolean isCompressXref() {
        return compressXref;
    }

    public boolean isIndexVersionedFilesOnly() {
        return indexVersionedFilesOnly;
    }

    public void setIndexVersionedFilesOnly(boolean indexVersionedFilesOnly) {
        this.indexVersionedFilesOnly = indexVersionedFilesOnly;
    }

    private transient Date lastModified;

    /**
     * Get the date of the last index update.
     *
     * @return the time of the last index update.
     */
    public Date getDateForLastIndexRun() {
        if (lastModified == null) {
            File timestamp = new File(getDataRoot(), "timestamp");
            lastModified = new Date(timestamp.lastModified());
        }
        return lastModified;
    }

    /**
     * The name of the eftar file relative to the <var>DATA_ROOT</var>, which
     * contains definition tags.
     */
    public static final String EFTAR_DTAGS_FILE = "index/dtags.eftar";

    private transient String dtagsEftar = null;

    /**
     * Get the eftar file, which contains definition tags.
     *
     * @return {@code null} if there is no such file, the file otherwise.
     */
    public File getDtagsEftar() {
        if (dtagsEftar == null) {
            File tmp = new File(getDataRoot() + "/" + EFTAR_DTAGS_FILE);
            if (tmp.canRead()) {
                dtagsEftar = tmp.getName();
            } else {
                dtagsEftar = "";
            }
        }
        return dtagsEftar.isEmpty() ? null : new File(dtagsEftar);
    }

    public String getDatabaseDriver() {
        return databaseDriver;
    }

    public void setDatabaseDriver(String databaseDriver) {
        this.databaseDriver = databaseDriver;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public Set<String> getAllowedSymlinks() {
        return allowedSymlinks;
    }

    public void setAllowedSymlinks(Set<String> allowedSymlinks) {
        this.allowedSymlinks = allowedSymlinks;
    }

    public boolean isObfuscatingEMailAddresses() {
        return obfuscatingEMailAddresses;
    }

    public void setObfuscatingEMailAddresses(boolean obfuscate) {
        this.obfuscatingEMailAddresses = obfuscate;
    }

    public boolean isChattyStatusPage() {
        return chattyStatusPage;
    }

    public void setChattyStatusPage(boolean chattyStatusPage) {
        this.chattyStatusPage = chattyStatusPage;
    }

    /**
     * Write the current configuration to a file
     *
     * @param file the file to write the configuration into
     * @throws IOException if an error occurs
     */
    public void write(File file) throws IOException {
        if (!file.getParentFile().isDirectory()) {
            file.getParentFile().mkdirs();
        }
        if (!file.getParentFile().isDirectory()) {
            throw new RuntimeException("Cannot write configuration file: " + file.getAbsolutePath());
        }
        final FileOutputStream out = new FileOutputStream(file);
        try {
            this.encodeObject(out);
        } finally {
            IOUtils.close(out);
        }
    }

    public String getXMLRepresentationAsString() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        this.encodeObject(bos);
        return bos.toString();
    }

    private void encodeObject(OutputStream out) {
        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(out));
        e.writeObject(this);
        e.close();
    }

    public static Configuration read(File file) throws IOException {
        final FileInputStream in = new FileInputStream(file);
        try {
            return decodeObject(in);
        } finally {
            IOUtils.close(in);
        }
    }


    public static Configuration makeXMLStringAsConfiguration(String xmlconfig) throws IOException {
        final Configuration ret;
        final ByteArrayInputStream in = new ByteArrayInputStream(xmlconfig.getBytes());
        ret = decodeObject(in);
        return ret;
    }

    private static Configuration decodeObject(InputStream in) throws IOException {
        XMLDecoder d = new XMLDecoder(new BufferedInputStream(in));
        final Object ret = d.readObject();
        d.close();

        if (!(ret instanceof Configuration)) {
            throw new IOException("Not a valid config file");
        }
        return (Configuration) ret;
    }

}
