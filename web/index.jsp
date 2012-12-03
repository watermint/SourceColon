<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" errorPage="og_error.jsp" %>
<%@ include file="og_projects.jspf" %>
<%
  {
    cfg = PageConfig.get(request);
    cfg.setTitle("Search");
%>
<%@ include file="og_header.jspf" %>
<div class="container">
  <%@ include file="og_menu.jspf" %>
  <%@ include file="index_body.html" %>
</div>
<% } %>
<%@ include file="og_foot.jspf" %>
