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

Copyright 2010 Sun Microsystems, Inc.  All rights reserved.
Use is subject to license terms.

Portions Copyright 2011 Jens Elkner.
Portions Copyright (c) 2013 Takayuki Okazaki.

--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" errorPage="og_error.jsp" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<t:layout pageTitle="Search">
  <%--@elvariable id="pageConfig" type="org.watermint.sourcecolon.org.opensolaris.opengrok.web.PageConfig"--%>
  <ul class="breadcrumb">
      ${pageConfig.breadcrumbPath}
  </ul>
  <h1>Lines Matching</h1>

  <div class="alert alert-info">
  ${pageConfig.currentQueryString}
  </div>

  <pre>${pageConfig.currentMatch}</pre>
</t:layout>
