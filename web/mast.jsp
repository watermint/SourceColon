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

Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
Portions Copyright 2011 Jens Elkner.

--%><%--

After include you are here: /body/div#page/div#content/

--%>
<%@ page session="false" errorPage="error.jsp" import="
java.io.File,
                                                       java.io.IOException,
                                                       org.opensolaris.opengrok.configuration.Project,
                                                       org.opensolaris.opengrok.history.HistoryGuru,
                                                       org.opensolaris.opengrok.web.EftarFileReader,
                                                       org.opensolaris.opengrok.web.PageConfig,
                                                       org.opensolaris.opengrok.web.Prefix,
                                                       org.opensolaris.opengrok.web.Util" %>
<%
  /* ---------------------- mast.jsp start --------------------- */
  {
    cfg = PageConfig.get(request);
    String redir = cfg.canProcess();
    if (redir == null || redir.length() > 0) {
      if (redir == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      } else {
        response.sendRedirect(redir);
      }
      return;
    }
    // jel: hmmm - questionable for dynamic content
    long flast = cfg.getLastModified();
    if (request.getDateHeader("If-Modified-Since") >= flast) {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return;
    }
    response.setDateHeader("Last-Modified", flast);

    // Use UTF-8 if no encoding is specified in the request
    if (request.getCharacterEncoding() == null) {
      request.setCharacterEncoding("UTF-8");
    }

    // set the default page title
    String path = cfg.getPath();
    cfg.setTitle("Cross Reference: " + path);

    String context = request.getContextPath();
    cfg.getEnv().setUrlPrefix(context + Prefix.SEARCH_R + "?");

    String uriEncodedPath = cfg.getUriEncodedPath();
    String rev = cfg.getRequestedRevision();
%>
<%@

    include file="header.jspf"

    %>
<script type="text/javascript">
  document.hash = '<%= cfg.getDocumentHash() %>';
  document.rev = '<%= rev %>';
  document.link = '<%= context + Prefix.XREF_P + uriEncodedPath %>';
  document.annotate = <%= cfg.annotate() %>;
  document.domReady.push(function () {
    domReadyMast();
  });
  document.pageReady.push(function () {
    pageReadyMast();
  });
</script>

<div class="container">
  <div class="navbar">
    <div class="navbar-inner">
      <ul class="nav">
        <% if (!cfg.hasAnnotations()) { %>
        <li class="disabled"><a href="#">Annotate</a></li>
        <% } else if (cfg.annotate()) { %>
        <li>
            <span id="toggle-annotate-by-javascript" style="display: none">
              <a href="#" onclick="javascript:toggle_annotations(); return false;"
                 title="Show or hide line annotation(commit revisions,authors).">Annotate</a>
            </span>
            <span id="toggle-annotate">
              <a href="<%= context + Prefix.XREF_P + uriEncodedPath + (rev.length() == 0 ? "" : "?") + rev %>">Annotate</a>
            </span>
        </li>
        <% } else { %>
        <li><a href="#" onclick="javascript:get_annotations(); return false;">Annotate</a></li>
        <% } %>
        <% if (!cfg.isDir()) { %>
        <% if (cfg.getPrefix() == Prefix.XREF_P) { %>
        <li>
          <a href="#" onclick="javascript:lntoggle();return false;"
             title="<%= "Show or hide line numbers (might be slower if file has more than 10 000 lines)." %>">Line#</a>
        </li>
        <li>
          <a href="#" onclick="javascript:lsttoggle();return false;" title="Show or hide symbol list.">Navigate</a>
        </li>
        <% } %>
        <li><a href="<%= context + Prefix.RAW_P + uriEncodedPath + (rev.length() == 0 ? "" : "?") + rev %>">Download</a>
        </li>
        <% } %>
      </ul>
      <form class="navbar-search pull-right" action="<%= context + Prefix.SEARCH_P %>">
        <input type="hidden" name="path" value="<%= cfg.getSearchOnlyIn()[0] %>"/>
        <input type="text" class="search-query" placeholder="Search under <%= cfg.getCrossFilename() %>" id="search"
               name="q"/>
      </form>
    </div>
  </div>
</div>
<div class="container">
  <ul class="breadcrumb">
    <a href="<%= context + Prefix.XREF_P %>/">xref</a>:
    <%= Util.breadcrumbPath(context + Prefix.XREF_P, path, '/', "", true, cfg.isDir(), true) %>
  </ul>
</div>
<div class="container">
<%
  }
/* ---------------------- mast.jsp end --------------------- */
%>