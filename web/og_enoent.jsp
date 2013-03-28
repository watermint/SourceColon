<%--
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

--%>
<%@page session="false" isErrorPage="true" import="org.watermint.sourcecolon.org.opensolaris.opengrok.web.Prefix" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
  /* ---------------------- enoent.jsp start --------------------- */
  {
    cfg = PageConfig.get(request);
    cfg.setTitle("File not found");

    String context = request.getContextPath();
    cfg.getEnv().setUrlPrefix(context + Prefix.SEARCH_R + "?");
    String configError = "";
    if (cfg.getSourceRootPath().isEmpty()) {
      configError = "CONFIGURATION parameter has not been configured in " + "web.xml! Please configure your webapp.";
    } else if (!cfg.getEnv().getSourceRootFile().isDirectory()) {
      configError = "The source root specified in your configuration does " + "not point to a valid directory! Please configure your webapp.";
    }
%>
<%@ include file="og_header.jspf" %>
<div class="container-fluid">
  <div class="row-fluid">
    <div class="span3">
      <%@ include file="og_menu.jspf" %>
    </div>
    <div class="span9">
      <h1 class="error">Error: File not found!</h1>

      <p>The requested resource is not available. <%= configError %>
      </p>
    </div>
  </div>
</div>
<% } %>
<%@ include file="og_foot.jspf" %>
