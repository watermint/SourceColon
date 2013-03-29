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
Portions Copyright (c) 2013 Takayuki Okazaki.

--%>
<%@ page session="false" isErrorPage="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<t:layout pageTitle="File not found">
  <%--@elvariable id="exception" type="java.lang.Exception"--%>
  <h1>Error</h1>

  <c:choose>
    <c:when test="${exception}">
      ${exception}
    </c:when>
    <c:otherwise>
      Unknown error.
    </c:otherwise>
  </c:choose>
</t:layout>
