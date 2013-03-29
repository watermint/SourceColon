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
Portions Copyright (c) 2013 Takayuki Okazaki.

--%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<t:layout pageTitle="Search">
  <%--@elvariable id="pageConfig" type="org.watermint.sourcecolon.org.opensolaris.opengrok.web.PageConfig"--%>

  <c:choose>
    <c:when test="${pageConfig.currentSearchHelper.errorMsg != null}">
      <h2>Error</h2>

      <div class="alert alert-error">${pageConfig.currentSearchQueryErrorMessage}</div>
    </c:when>

    <c:when test="${empty(pageConfig.currentSearchHelper.hits)}">
      <div class="alert">No hits for <code>${pageConfig.currentQueryString}</code></div>
      <c:forEach var="suggestion" items="${pageConfig.currentSearchHelper.suggestions}">
        Do you mean (for ${suggestion.name}):
        <ul class="inline">
          <c:forEach var="text" items="${suggestion.freetext}">
            <li><a href="${pageContext.request.contextPath}/search?q=${text}">${text}</a></li>
          </c:forEach>
          <c:forEach var="text" items="${suggestion.defs}">
            <li>Defs: <a href="${pageContext.request.contextPath}/search?defs=${text}">${text}</a></li>
          </c:forEach>
          <c:forEach var="text" items="${suggestion.refs}">
            <li>Symbol: <a href="${pageContext.request.contextPath}/search?refs=${text}">${text}</a></li>
          </c:forEach>
        </ul>
      </c:forEach>
    </c:when>

    <c:otherwise>
      <ul class="nav nav-tabs">
        <c:forEach var="order" items="${pageConfig.sortOrderList}">
          <c:choose>
            <c:when test="${order.active}">
              <li class="active"><a href="#">${order.name}</a></li>
            </c:when>
            <c:otherwise>
              <li><a href="${order.link}">${order.name}</a></li>
            </c:otherwise>
          </c:choose>
        </c:forEach>
      </ul>

      <p>
        Searched <code>${pageConfig.currentQueryString}</code>
        (Results <strong>${pageConfig.currentSearchHelper.start + 1}</strong>
        - <strong>${pageConfig.currentSearchHelper.thisPageEndIndex}</strong>
        of <strong>${pageConfig.currentSearchHelper.totalHits}</strong>)
        sorted by <span class="label label-info">${pageConfig.currentSearchHelper.order.desc}</span>
      </p>

      <table class="table table-striped">${pageConfig.currentSearchHelper.searchResultTable}</table>

      <c:if test="${pageConfig.currentSearchHelper.pagingEnabled}">
        <div class="pagination">
          <ul>
            <c:forEach var="page" items="${pageConfig.currentSearchHelper.paging}">
              <c:choose>
                <c:when test="${page.active}">
                  <li class="active"><a href="#">${page.label}</a></li>
                </c:when>
                <c:otherwise>
                  <li><a href="${page.link}">${page.label}</a></li>
                </c:otherwise>
              </c:choose>
            </c:forEach>
          </ul>
        </div>
      </c:if>
    </c:otherwise>
  </c:choose>
</t:layout>
