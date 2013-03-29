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
<%@ page session="false" errorPage="og_error.jsp" import="
java.io.File,
                                                          java.io.IOException,
                                                          org.watermint.sourcecolon.org.opensolaris.opengrok.configuration.Project,
                                                          org.watermint.sourcecolon.org.opensolaris.opengrok.web.PageConfig,
                                                          org.watermint.sourcecolon.org.opensolaris.opengrok.web.Prefix,
                                                          org.watermint.sourcecolon.org.opensolaris.opengrok.web.Util" %>
<%
  /* ---------------------- mast.jsp start --------------------- */
  {
    PageConfig cfg = PageConfig.get(request);

    // set the default page title
    String path = cfg.getPath();
    cfg.setTitle("Cross Reference: " + path);

    String context = request.getContextPath();
    cfg.getEnv().setUrlPrefix(context + Prefix.SEARCH_R + "?");

    String uriEncodedPath = cfg.getUriEncodedPath();
%>
<%@ include file="og_header.jspf" %>
<script type="text/javascript">
  document.hash = '${pageconfig.documentHash}';
  document.rev = '${pageconfig.requestedRevision}';
  document.link = '<%= context + Prefix.XREF_P + uriEncodedPath %>';
</script>
<div class="container-fluid">
  <div class="row-fluid">
    <div class="span3">
      <%@ include file="og_menu.jspf" %>
    </div>
    <div class="span9">
      <ul class="breadcrumb">
        <%= Util.breadcrumbPath(context + Prefix.XREF_P, path, '/', "", true, cfg.isDir(), true) %>
      </ul>
        <% } %>
