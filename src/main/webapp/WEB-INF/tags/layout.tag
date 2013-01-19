<%@ tag pageEncoding="utf-8" %>
<%@ attribute name="pageTitle" required="true" type="java.lang.String" %>
<%@ attribute name="pageScript" required="false" type="java.lang.String" %>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <title>${pageTitle}</title>
  <meta name="robots" content="noindex,nofollow"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <link rel="icon" href="${pageContext.request.contextPath}/img/icon-16.png" type="image/png"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.min.css" media="screen"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/jquery-ui-1.9.2.custom.min.css" media="screen"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/sourcecolon.css" media="screen"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/prettify.min.css" media="screen"/>
</head>
<body>
<div class="navbar navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      <a href="${pageContext.request.contextPath}" class="brand">Source:</a>
    </div>
  </div>
</div>
<jsp:doBody />
<div class="container">
  <footer>
    <div class="muted">
      Powered by <a href="https://github.com/watermint/SourceColon">Source:</a>
    </div>
  </footer>
</div>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-ui-1.9.2.custom.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-appear.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/sourcecolon.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/prettify.js"></script>
<script type="text/javascript">${pageScript}</script>
</body>
</html>
