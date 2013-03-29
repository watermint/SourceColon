<%@ tag pageEncoding="utf-8" import="org.watermint.sourcecolon.org.opensolaris.opengrok.web.PageConfig" %>
<%@ attribute name="pageTitle" required="true" type="java.lang.String" %>
<%@ attribute name="pageScript" required="false" type="java.lang.String" %>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% {
  request.setAttribute("pageConfig", PageConfig.get(request));
  request.setAttribute("runtime", PageConfig.get(request).getEnv());
} %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta name="robots" content="noindex,nofollow"/>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta http-equiv="X-UA-Compatible" content="IE=8"/>
  <title>${pageTitle}</title>
  <link rel="icon" href="${pageContext.request.contextPath}/img/icon-16.png" type="image/png"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.min.css" media="screen">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/jquery-ui-1.9.2.custom.min.css" media="screen">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/sourcecolon.css" media="screen">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/prettify.css" type="text/css"/>
  <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.8.3.min.js"></script>
  <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-ui-1.9.2.custom.min.js"></script>
  <script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
  <script type="text/javascript" src="${pageContext.request.contextPath}/js/prettify.js"></script>
  <script type="text/javascript" src="${pageContext.request.contextPath}/js/sourcecolon.js"></script>
</head>
<body>
<h1>Layout finished!</h1>
<div class="container-fluid">
  <div class="row-fluid">
    <div class="span3">
      <div class="well sidebar-nav ">
        <h1><a href="${pageContext.request.contextPath}/">
          <img src="${pageContext.request.contextPath}/img/icon-64.png"
               class="img-rounded"/></a>
        </h1>

        <form action="${pageContext.request.contextPath}/search">
          <ul class="nav nav-list">
            <li><a href="${pageContext.request.contextPath}/xref/"><i class="icon-folder-open"></i> Xref</a></li>
            <li><a href="${pageContext.request.contextPath}/og_help.jsp"><i class="icon-info-sign"></i> Help</a></li>
          </ul>

          <h2>Search</h2>
          <ul class="nav nav-list" style="padding: 0">
            <li>
              <input class="input-medium" type="text" name="q" placeholder="Text"
                     value="${pageConfig.queryBuilder.freeTextQuoted}"/>
            </li>
            <li>
              <input class="input-medium" type="text" name="defs" id="s2" placeholder="Definition"
                     value="${pageConfig.queryBuilder.defsQuoted}"/>
            </li>
            <li>
              <input class="input-medium" type="text" name="refs" id="s3" placeholder="Symbol"
                     value="${pageConfig.queryBuilder.refsQuoted}"/>
            </li>
            <li>
              <input class="input-medium" type="text" name="path" id="s4" placeholder="Path"
                     value="${pageConfig.queryBuilder.pathQuoted}"/>
            </li>
            <li>
              <input class="btn btn-primary" type="submit" value="Search"/>
            </li>
          </ul>
        </form>
        <div id="code-navigation"></div>
        <br/>

        <div class="muted">
          <small>
            Indices created <br/>
            <span class="label label-info">${runtime.dateForLastIndexRun}</span><br/>
            Served by <i class="logo-sourcecolon"></i> <a href="https://github.com/watermint/SourceColon">Source:</a>
            <br/>
            based on <a href="http://www.opensolaris.org/os/project/opengrok/" title="OpenGrok">OpenGrok</a>
          </small>
        </div>
      </div>
    </div>
    <div class="span9">
      <jsp:doBody/>
    </div>
  </div>
</div>
<script type="text/javascript">${pageScript}</script>
</body>
</html>
