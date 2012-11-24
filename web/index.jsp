<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" errorPage="error.jsp" %>
<%@ include file="projects.jspf" %>
<%
  {
    cfg = PageConfig.get(request);
    cfg.setTitle("Search");
%>
<%@ include file="header.jspf" %>
<div class="container">
  <%@ include file="menu.jspf" %>
  <%@ include file="index_body.html" %>
</div>
<% } %>
<%@ include file="foot.jspf" %>
