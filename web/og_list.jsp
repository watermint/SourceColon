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

Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
Portions Copyright 2011 Jens Elkner.
Portions Copyright (c) 2013 Takayuki Okazaki.

--%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<t:layout pageTitle="Search" pageScript="updateNavigationSymbolContents(); prettyPrint();">
  <%--@elvariable id="pageConfig" type="org.watermint.sourcecolon.org.opensolaris.opengrok.web.PageConfig"--%>
  <c:set var="rawPath" value="${pageContext.request.contextPath}/raw/${pageConfig.path}"/>
  <ul class="breadcrumb">
      ${pageConfig.breadcrumbPath}
  </ul>
  <c:choose>
    <c:when test="${pageConfig.dir && !empty(pageConfig.resourceFileList)}">
      <form class="navbar-search pull-right" action="${pageContext.request.contextPath}/search">
        <input type="hidden" name="path" value="${pageConfig.searchOnlyIn[0]}"/>

        <div class="input-prepend input-append">
          <span class="add-on">Search under <strong>${pageConfig.crossFilename}</strong></span>
          <input type="text" class="search-query" placeholder="Search under ${pageConfig.crossFilename}" name="q"/>
          <button class="btn" type="button">Search</button>
        </div>
      </form>
      <h2>Files</h2>

      <table class="table table-striped">
        <tr class="info"><th>Name</th><th>Date</th><th>Size</th></tr>
        <c:forEach var="file" items="${pageConfig.directoryFiles}">
          <tr>
            <td><a href="${file.link}">${file.name}</a></td>
            <td>${file.date}</td>
            <td>${file.size}</td>
          </tr>
        </c:forEach>
      </table>

      <c:forEach var="readme" items="${pageConfig.readmeFiles}">
        <h3>${readme.key}</h3>

        <pre class="prettyprint linenums"><code>${pageConfig.fileContents(readme.value)}</code></pre>
      </c:forEach>
    </c:when>
    <c:when test="${pageConfig.currentDataFile != null}">
      <pre class="prettyprint linenums"><code>${pageConfig.fileContents(pageConfig.currentDataFile)}</code></pre>
    </c:when>
    <c:when test="${pageConfig.image}">
      <img src="${rawPath}"/>
    </c:when>
    <c:when test="${pageConfig.HTML}">
      ${pageConfig.htmlContents}
    </c:when>
    <c:when test="${pageConfig.plain}">
      <pre class="prettyprint linenums"><code>${pageConfig.plainContents}</code></pre>
    </c:when>
    <c:otherwise>
      Download: <a href="${rawPath}">${pageConfig.resourceFile.name}</a>
    </c:otherwise>
  </c:choose>
</t:layout>
