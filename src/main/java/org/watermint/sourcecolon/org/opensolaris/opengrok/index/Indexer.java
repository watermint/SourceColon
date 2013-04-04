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
 * Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
 *
 * Portions Copyright 2011 Jens Elkner.
 * Portions Copyright (c) 2013 Takayuki Okazaki.
 */
package org.watermint.sourcecolon.org.opensolaris.opengrok.index;

import org.watermint.sourcecolon.org.opensolaris.opengrok.Info;
import org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.AnalyzerGuru;
import org.watermint.sourcecolon.org.opensolaris.opengrok.configuration.Configuration;
import org.watermint.sourcecolon.org.opensolaris.opengrok.configuration.Project;
import org.watermint.sourcecolon.org.opensolaris.opengrok.configuration.RuntimeEnvironment;
import org.watermint.sourcecolon.org.opensolaris.opengrok.util.Executor;
import org.watermint.sourcecolon.org.opensolaris.opengrok.util.GetOpts;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Creates and updates an inverted source index
 * as well as generates Xref, file stats etc., if specified
 * in the options
 */
@SuppressWarnings({"PMD.AvoidPrintStackTrace", "PMD.SystemPrintln"})
public final class Indexer {

    private static final String ON = "on";
    private static final String OFF = "off";
    private static Indexer index = new Indexer();
    private static final Logger log = Logger.getLogger(Indexer.class.getName());

    public static Indexer getInstance() {
        return index;
    }

    /**
     * Program entry point
     *
     * @param argv argument vector
     */
    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    public static void main(String argv[]) {
        boolean runIndex = true;
        boolean update = true;
        ArrayList<String> zapCache = new ArrayList<>();
        CommandLineOptions cmdOptions = new CommandLineOptions();

        if (argv.length == 0) {
            System.err.println(cmdOptions.getUsage());
            System.exit(1);
        }

        Executor.registerErrorHandler();
        ArrayList<String> subFiles = new ArrayList<>();
        HashSet<String> allowedSymlinks = new HashSet<>();
        String configFilename = RuntimeEnvironment.DEFAULT_SOURCECOLON_CONFIG;
        String configHost = null;
        boolean addProjects = false;
        String defaultProject = null;
        boolean listFiles = false;
        boolean createDict = false;
        int noThreads = 2 + (2 * Runtime.getRuntime().availableProcessors());

        // Parse command line options:
        GetOpts getOpts = new GetOpts(argv, cmdOptions.getCommandString());

        try {
            getOpts.parse();
        } catch (ParseException ex) {
            System.err.println("OpenGrok: " + ex.getMessage());
            System.err.println(cmdOptions.getUsage());
            System.exit(1);
        }

        try {
            Configuration cfg = null;
            int cmd;

            // We need to read the configuration file first, since we
            // will try to overwrite options..
            while ((cmd = getOpts.getOpt()) != -1) {
                if (cmd == 'R') {
                    cfg = Configuration.read(new File(getOpts.getOptionArgument()));
                    break;
                }
            }

            if (cfg == null) {
                cfg = new Configuration();
            }

            // Now we can handle all the other options..
            getOpts.reset();
            while ((cmd = getOpts.getOpt()) != -1) {
                switch (cmd) {
                    case 'x':
                        createDict = true;
                        runIndex = false;
                        break;

                    case 'e':
                        cfg.setGenerateHtml(false);
                        break;
                    case 'P':
                        addProjects = true;
                        break;
                    case 'p':
                        defaultProject = getOpts.getOptionArgument();
                        break;
                    case 'c':
                        cfg.setCtags(getOpts.getOptionArgument());
                        break;
                    case 'w': {
                        String webapp = getOpts.getOptionArgument();
                        if (webapp.charAt(0) != '/' && !webapp.startsWith("http")) {
                            webapp = "/" + webapp;
                        }
                        if (webapp.endsWith("/")) {
                            cfg.setUrlPrefix(webapp + "s?");
                        } else {
                            cfg.setUrlPrefix(webapp + "/s?");
                        }
                    }
                    break;
                    case 'W':
                        configFilename = getOpts.getOptionArgument();
                        break;
                    case 'N':
                        allowedSymlinks.add(getOpts.getOptionArgument());
                        break;
                    case 'n':
                        runIndex = false;
                        break;
                    case 'v':
                        cfg.setVerbose(true);
                        break;
                    case 'C':
                        cfg.setPrintProgress(true);
                        break;

                    case 's': {
                        File sourceRoot = new File(getOpts.getOptionArgument());
                        if (!sourceRoot.isDirectory()) {
                            System.err.println("ERROR: Source root must be a directory");
                            System.exit(1);
                        }
                        cfg.setSourceRoot(sourceRoot.getCanonicalPath());
                        break;
                    }
                    case 'd': {
                        File dataRoot = new File(getOpts.getOptionArgument());
                        if (!dataRoot.exists() && !dataRoot.mkdirs()) {
                            System.err.println("ERROR: Cannot create data root");
                            System.exit(1);
                        }
                        if (!dataRoot.isDirectory()) {
                            System.err.println("ERROR: Data root must be a directory");
                            System.exit(1);
                        }
                        cfg.setDataRoot(dataRoot.getCanonicalPath());
                        break;
                    }
                    case 'i':
                        cfg.getIgnoredNames().add(getOpts.getOptionArgument());
                        break;
                    case 'I':
                        cfg.getIncludedNames().add(getOpts.getOptionArgument());
                        break;
                    case 'Q':
                        if (getOpts.getOptionArgument().equalsIgnoreCase(ON)) {
                            cfg.setQuickContextScan(true);
                        } else if (getOpts.getOptionArgument().equalsIgnoreCase(OFF)) {
                            cfg.setQuickContextScan(false);
                        } else {
                            System.err.println("ERROR: You should pass either \"on\" or \"off\" as argument to -Q");
                            System.err.println("       Ex: \"-Q on\" will just scan a \"chunk\" of the file and insert \"[..all..]\"");
                            System.err.println("           \"-Q off\" will try to build a more accurate list by reading the complete file.");
                        }

                        break;
                    case 'm': {
                        try {
                            cfg.setIndexWordLimit(Integer.parseInt(getOpts.getOptionArgument()));
                        } catch (NumberFormatException exp) {
                            System.err.println("ERROR: Failed to parse argument to \"-m\": " + exp.getMessage());
                            System.exit(1);
                        }
                        break;
                    }
                    case 'a':
                        if (getOpts.getOptionArgument().equalsIgnoreCase(ON)) {
                            cfg.setAllowLeadingWildcard(true);
                        } else if (getOpts.getOptionArgument().equalsIgnoreCase(OFF)) {
                            cfg.setAllowLeadingWildcard(false);
                        } else {
                            System.err.println("ERROR: You should pass either \"on\" or \"off\" as argument to -a");
                            System.err.println("       Ex: \"-a on\" will allow a search to start with a wildcard");
                            System.err.println("           \"-a off\" will disallow a search to start with a wildcard");
                            System.exit(1);
                        }

                        break;

                    case 'A': {
                        String[] arg = getOpts.getOptionArgument().split(":");
                        if (arg.length != 2) {
                            System.err.println("ERROR: You must specify: -A extension:class");
                            System.err.println("       Ex: -A foo:org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.c.CAnalyzer");
                            System.err.println("           will use the C analyzer for all files ending with .foo");
                            System.err.println("       Ex: -A c:-");
                            System.err.println("           will disable the c-analyzer for for all files ending with .c");
                            System.exit(1);
                        }

                        arg[0] = arg[0].substring(arg[0].lastIndexOf('.') + 1).toUpperCase();
                        if (arg[1].equals("-")) {
                            AnalyzerGuru.addExtension(arg[0], null);
                            break;
                        }

                        try {
                            AnalyzerGuru.addExtension(
                                    arg[0],
                                    AnalyzerGuru.findFactory(arg[1]));
                        } catch (Exception e) {
                            log.log(Level.SEVERE, "Unable to use {0} as a FileAnalyzerFactory", arg[1]);
                            log.log(Level.SEVERE, "Stack: ", e.fillInStackTrace());
                            System.exit(1);
                        }
                    }
                    break;
                    case 'T':
                        try {
                            noThreads = Integer.parseInt(getOpts.getOptionArgument());
                        } catch (NumberFormatException exp) {
                            System.err.println("ERROR: Failed to parse argument to \"-T\": " + exp.getMessage());
                            System.exit(1);
                        }
                        break;
                    case 'l':
                        if (getOpts.getOptionArgument().equalsIgnoreCase(ON)) {
                            cfg.setUsingLuceneLocking(true);
                        } else if (getOpts.getOptionArgument().equalsIgnoreCase(OFF)) {
                            cfg.setUsingLuceneLocking(false);
                        } else {
                            System.err.println("ERROR: You should pass either \"on\" or \"off\" as argument to -l");
                            System.err.println("       Ex: \"-l on\" will enable locks in Lucene");
                            System.err.println("           \"-l off\" will disable locks in Lucene");
                        }
                        break;
                    case 'V':
                        System.out.println(Info.getFullVersion());
                        System.exit(0);
                        break;
                    case '?':
                        System.err.println(cmdOptions.getUsage());
                        System.exit(0);
                        break;
                    case 't':
                        try {
                            int tmp = Integer.parseInt(getOpts.getOptionArgument());
                            cfg.setTabSize(tmp);
                        } catch (NumberFormatException exp) {
                            System.err.println("ERROR: Failed to parse argument to \"-t\": " + exp.getMessage());
                            System.exit(1);
                        }
                        break;
                    default:
                        System.err.println("Internal Error - Unimplemented cmdline option: " + (char) cmd);
                        System.exit(1);
                }
            }

            int optind = getOpts.getOptionIndex();
            if (optind != -1) {
                while (optind < argv.length) {
                    subFiles.add(argv[optind]);
                    ++optind;
                }
            }

            //logging starts here
            if (cfg.isVerbose()) {
                String fn = LogManager.getLogManager().getProperty("java.util.logging.FileHandler.pattern");
                if (fn != null) {
                    System.out.println("Logging filehandler pattern: " + fn);
                }
            }

            // automatically allow symlinks that are directly in source root
            String file = cfg.getSourceRoot();
            if (file != null) {
                File sourceRootFile = new File(file);
                File[] projectDirs = sourceRootFile.listFiles();
                if (projectDirs != null) {
                    for (File projectDir : projectDirs) {
                        if (!projectDir.getCanonicalPath().equals(projectDir.getAbsolutePath())) {
                            allowedSymlinks.add(projectDir.getAbsolutePath());
                        }
                    }
                }
            }

            allowedSymlinks.addAll(cfg.getAllowedSymlinks());
            cfg.setAllowedSymlinks(allowedSymlinks);

            //Set updated configuration in RuntimeEnvironment
            RuntimeEnvironment env = RuntimeEnvironment.getInstance();
            env.setConfiguration(cfg);

            getInstance().prepareIndexer(env, addProjects,
                    defaultProject, configFilename, listFiles, createDict, subFiles, zapCache);
            if (!zapCache.isEmpty()) {
                return;
            }
            if (runIndex) {
                IndexChangedListener progress = new DefaultIndexChangedListener();
                getInstance().doIndexerExecution(update, noThreads, subFiles,
                        progress);
            }
            getInstance().sendToConfigHost(configHost);
        } catch (IndexerException ex) {
            log.log(Level.SEVERE, "Exception running indexer", ex);
            System.err.println(cmdOptions.getUsage());
            System.exit(1);
        } catch (Throwable e) {
            System.err.println("Exception: " + e.getLocalizedMessage());
            log.log(Level.SEVERE, "Unexpected Exception", e);
            System.exit(1);
        }
    }

    // PMD wants us to use length() > 0 && charAt(0) instead of startsWith()
    // for performance. We prefer clarity over performance here, so silence it.
    @SuppressWarnings("PMD.SimplifyStartsWith")
    public void prepareIndexer(RuntimeEnvironment env, boolean addProjects, String defaultProject, String configFilename, boolean listFiles, boolean createDict, List<String> subFiles, List<String> zapCache) throws IndexerException, IOException {

        if (env.getDataRootPath() == null) {
            throw new IndexerException("ERROR: Please specify a DATA ROOT path");
        }
        if (env.getSourceRootFile() == null) {
            throw new IndexerException("ERROR: please specify a SRC_ROOT with option -s !");
        }
        if (zapCache == null) {
            throw new IndexerException("Internal error, zapCache shouldn't be null");
        }

        if (addProjects) {
            File files[] = env.getSourceRootFile().listFiles();
            List<Project> projects = env.getProjects();

            // Keep a copy of the old project list so that we can preserve
            // the customization of existing projects.
            Map<String, Project> oldProjects = new HashMap<>();
            for (Project p : projects) {
                oldProjects.put(p.getPath(), p);
            }

            projects.clear();

            // Add a project for each top-level directory in source root.
            for (File file : files) {
                String name = file.getName();
                String path = "/" + name;
                if (oldProjects.containsKey(path)) {
                    // This is an existing object. Reuse the old project,
                    // possibly with customizations, instead of creating a
                    // new with default values.
                    projects.add(oldProjects.get(path));
                } else if (!name.startsWith(".") && file.isDirectory()) {
                    // Found a new directory with no matching project, so
                    // create a new project with default properties.
                    Project p = new Project();
                    p.setProjectId(name);
                    p.setPath(path);
                    p.setTabSize(env.getConfiguration().getTabSize());
                    projects.add(p);
                }
            }

            // The projects should be sorted...
            Collections.sort(projects, new Comparator<Project>() {

                @Override
                public int compare(Project p1, Project p2) {
                    String s1 = p1.getProjectId();
                    String s2 = p2.getProjectId();

                    int ret;
                    if (s1 == null) {
                        ret = (s2 == null) ? 0 : 1;
                    } else {
                        ret = s1.compareTo(s2);
                    }
                    return ret;
                }
            });
        }

        if (defaultProject != null) {
            for (Project p : env.getProjects()) {
                if (p.getPath().equals(defaultProject)) {
                    env.setDefaultProject(p);
                    break;
                }
            }
        }

        if (configFilename != null) {
            log.log(Level.INFO, "Writing configuration to {0}", configFilename);
            env.writeConfiguration(new File(configFilename));
            log.info("Done...");
        }

        if (listFiles) {
            IndexDatabase.listAllFiles(subFiles);
        }

        if (createDict) {
            IndexDatabase.listFrequentTokens(subFiles);
        }
    }

    public void doIndexerExecution(final boolean update, int noThreads, List<String> subFiles,
                                   IndexChangedListener progress)
            throws IOException {
        RuntimeEnvironment env = RuntimeEnvironment.getInstance().register();
        log.info("Starting indexing");

        ExecutorService executor = Executors.newFixedThreadPool(noThreads);

        if (subFiles == null || subFiles.isEmpty()) {
            if (update) {
                IndexDatabase.updateAll(executor, progress);
            }
        } else {
            List<IndexDatabase> dbs = new ArrayList<>();

            for (String path : subFiles) {
                Project project = Project.getProject(path);
                if (project == null && env.hasProjects()) {
                    log.log(Level.WARNING, "Could not find a project for \"{0}\"", path);
                } else {
                    IndexDatabase db;
                    if (project == null) {
                        db = new IndexDatabase();
                    } else {
                        db = new IndexDatabase(project);
                    }
                    int idx = dbs.indexOf(db);
                    if (idx != -1) {
                        db = dbs.get(idx);
                    }

                    if (db.addDirectory(path)) {
                        if (idx == -1) {
                            dbs.add(db);
                        }
                    } else {
                        log.log(Level.WARNING, "Directory does not exist \"{0}\"", path);
                    }
                }
            }

            for (final IndexDatabase db : dbs) {
                db.addIndexChangedListener(progress);
                executor.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            if (update) {
                                db.update();
                            }
                        } catch (Throwable e) {
                            log.log(Level.SEVERE, "An error occured while "
                                    + (update ? "updating" : "optimizing")
                                    + " index", e);
                        }
                    }
                });
            }
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                // Wait forever
                executor.awaitTermination(999, TimeUnit.DAYS);
            } catch (InterruptedException exp) {
                log.log(Level.WARNING, "Received interrupt while waiting for executor to finish", exp);
            }
        }
    }

    public void sendToConfigHost(String configHost) {
        if (configHost != null) {
            String[] cfg = configHost.split(":");
            log.log(Level.INFO, "Send configuration to: {0}", configHost);
            if (cfg.length == 2) {
                try {
                    InetAddress host = InetAddress.getByName(cfg[0]);
                    RuntimeEnvironment.getInstance().writeConfiguration(host, Integer.parseInt(cfg[1]));
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Failed to send configuration to " + configHost + " (is web application server running with opengrok deployed?)", ex);
                }
            } else {
                log.severe("Syntax error: ");
                for (String s : cfg) {
                    log.log(Level.SEVERE, "[{0}]", s);
                }
            }
            log.info("Configuration update routine done, check log output for errors.");
        }
    }

    private Indexer() {
    }
}
