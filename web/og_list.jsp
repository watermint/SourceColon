<%--
$Id$

CDDL HEADER START

The contents of this file are subject to the terms of the
Common Development and Distribution License (the "License").
You may not use this file except in compliance with the License.

See LICENSE.txt included in this distribution for the specific
language governing permissions and limitations under the License.

When distributing Covered Code, include this CDDL HEADER in each
file and include the License file at LICENSE.txt.
If applicable, add the following below this CDDL HEADER, with the
fields enclosed by brackets "[]" replaced with your own identifying
information: Portions Copyright [yyyy] [name of copyright owner]

CDDL HEADER END

Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
Portions Copyright 2011 Jens Elkner.

--%>
<%@page import="
java.io.BufferedInputStream,
                java.io.FileInputStream,
                java.io.InputStream,
                java.io.InputStreamReader,
                java.io.Reader,
                java.util.List,
                java.util.Set,
                org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.AnalyzerGuru,
                org.watermint.sourcecolon.org.opensolaris.opengrok.index.IndexDatabase,
                org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.Definitions,
                org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.FileAnalyzer.Genre,
                org.watermint.sourcecolon.org.opensolaris.opengrok.analysis.FileAnalyzerFactory,
                org.watermint.sourcecolon.org.opensolaris.opengrok.web.DirectoryListing"
    %>
<%@include file="og_mast.jsp" %>
<script type="text/javascript">/* <![CDATA[ */
$(document).ready(function () {
  updateNavigationSymbolContents();
});
/* ]]> */</script>
<%
  /* ---------------------- list.jsp start --------------------- */
  {
    cfg = PageConfig.get(request);
    String rev = cfg.getRequestedRevision();

    File resourceFile = cfg.getResourceFile();
    String path = cfg.getPath();
    String basename = resourceFile.getName();
    String rawPath = request.getContextPath() + Prefix.RAW_P + path;
    Reader r = null;
    if (cfg.isDir()) {
      // valid resource is requested
      // mast.jsp assures, that resourceFile is valid and not /
      // see cfg.resourceNotAvailable()
      Project activeProject = Project.getProject(resourceFile);
      String cookieValue = cfg.getRequestedProjectsAsString();
      if (activeProject != null) {
        Set<String> projects = cfg.getRequestedProjects();
        if (!projects.contains(activeProject.getProjectId())) {
          projects.add(activeProject.getProjectId());
          // update cookie
          cookieValue = cookieValue.length() == 0
              ? activeProject.getProjectId()
              : activeProject.getProjectId() + '/' + cookieValue;
          Cookie cookie = new Cookie("sourcecolon_prj", cookieValue);
          // TODO hmmm, projects.jspf doesn't set a path
          cookie.setPath(request.getContextPath() + '/');
          response.addCookie(cookie);
        }
      }
      // requesting a directory listing
      DirectoryListing dl = new DirectoryListing();
      List<String> files = cfg.getResourceFileList();
      if (!files.isEmpty()) {
        List<String> readMes = dl.listTo(resourceFile, out, path, files);
        File[] catfiles = cfg.findDataFiles(readMes);
        for (int i = 0; i < catfiles.length; i++) {
          if (catfiles[i] == null) {
            continue;
          }
%><h3><%= readMes.get(i) %></h3>
<pre class="prettyprint linenums"><code><% Util.dump(out, catfiles[i], catfiles[i].getName().endsWith(".gz")); %></code></pre>
<%
    }
  }
} else {
  // requesting cross referenced file
  File xrefFile = null;
  xrefFile = cfg.findDataFile();
  if (xrefFile != null) {
%>
    <pre class="prettyprint linenums"><code><%
      Util.dump(out, xrefFile, xrefFile.getName().endsWith(".gz"));
    %></code></pre>
<%
} else {
  // annotate
  BufferedInputStream bin =
      new BufferedInputStream(new FileInputStream(resourceFile));
  try {
    FileAnalyzerFactory a = AnalyzerGuru.find(basename);
    Genre g = AnalyzerGuru.getGenre(a);
    if (g == null) {
      a = AnalyzerGuru.find(bin);
      g = AnalyzerGuru.getGenre(a);
    }
    if (g == Genre.IMAGE) {
%>
  <img src="<%= rawPath %>"/>
<%
} else if (g == Genre.HTML) {
  r = new InputStreamReader(bin);
  Util.dump(out, r);
} else if (g == Genre.PLAIN) {
%>
    <pre class="prettyprint linenums"><code><%
      // We're generating xref for the latest revision, so we can
      // find the definitions in the index.
      Definitions defs = IndexDatabase.getDefinitions(resourceFile);
      r = new InputStreamReader(bin);
      AnalyzerGuru.writeXref(a, r, out, defs,
          Project.getProject(resourceFile));
    %></code></pre>
<%
} else {
%>
Click <a href="<%= rawPath %>">download <%= basename %>
</a><%
          }
        } finally {
          if (r != null) {
            try {
              r.close();
              bin = null;
            } catch (Exception e) { /* ignore */ }
          }
          if (bin != null) {
            try {
              bin.close();
            } catch (Exception e) { /* ignore */ }
          }
        }
      }
    }
  }
/* ---------------------- list.jsp end --------------------- */
%>
<%@ include file="og_foot.jspf" %>