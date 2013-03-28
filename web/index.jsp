<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" errorPage="og_error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ include file="og_projects.jspf" %>
<% {
    PageConfig cfg = PageConfig.get(request);
    cfg.setTitle("Search");
 } %>
<%@ include file="og_header.jspf" %>
<div class="container-fluid">
  <div class="row-fluid">
    <div class="span3">
      <%@ include file="og_menu.jspf" %>
    </div>
    <div class="span9">
      <%@ include file="index_body.html" %>
    </div>
  </div>
</div>
<%@ include file="og_foot.jspf" %>
