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

--%>
<%@page session="false" errorPage="og_error.jsp" import="
org.watermint.sourcecolon.org.opensolaris.opengrok.search.Results,
org.watermint.sourcecolon.org.opensolaris.opengrok.web.SearchHelper,
org.watermint.sourcecolon.org.opensolaris.opengrok.web.SortOrder,
org.watermint.sourcecolon.org.opensolaris.opengrok.web.Suggestion"
    %>
<%@ include file="og_projects.jspf" %>
<%!
  private StringBuilder createUrl(SearchHelper sh, boolean menu) {
    StringBuilder url = new StringBuilder(64);
    QueryBuilder qb = sh.builder;
    if (menu) {
      url.append("search?");
    } else {
      Util.appendQuery(url, "sort", sh.order.toString());
    }
    if (qb != null) {
      Util.appendQuery(url, "q", qb.getFreetext());
      Util.appendQuery(url, "defs", qb.getDefs());
      Util.appendQuery(url, "refs", qb.getRefs());
      Util.appendQuery(url, "path", qb.getPath());
    }
    if (sh.projects != null && sh.projects.size() != 0) {
      Util.appendQuery(url, "project", cfg.getRequestedProjectsAsString());
    }
    return url;
  }
%><%
  /* ---------------------- search.jsp start --------------------- */
  {
    cfg = PageConfig.get(request);

    long starttime = System.currentTimeMillis();

    SearchHelper searchHelper = cfg.prepareSearch()
        .prepareExec(cfg.getRequestedProjects()).executeQuery().prepareSummary();
    if (searchHelper.redirect != null) {
      response.sendRedirect(searchHelper.redirect);
    }
    if (searchHelper.errorMsg != null) {
      cfg.setTitle("Search Error");
    } else {
      cfg.setTitle("Search");
    }
    response.addCookie(new Cookie("sourccolon_sort", searchHelper.order.toString()));
%>
<%@ include file="og_header.jspf" %>
<div class="container">
  <div id="menu">
    <%@ include file="og_menu.jspf" %>
  </div>
  <ul class="nav nav-tabs"><%
    StringBuilder url = createUrl(searchHelper, true).append("&amp;sort=");
    for (SortOrder o : SortOrder.values()) {
        if (searchHelper.order == o) {
  %>
    <li class="active"><a href="#"><%= o.getDesc() %></a></li>
    <%
        } else {
    %>
    <li><a href="<%= url %><%= o %>"><%= o.getDesc() %></a></li>
    <%
        }
      }
    %>
  </ul>
    <%
    // TODO spellchecking cycle below is not that great and we only create
    // suggest links for every token in query, not for a query as whole
    if (searchHelper.errorMsg != null) {
        %><h3>Error</h3>

  <p><%
    if (searchHelper.errorMsg.startsWith((SearchHelper.PARSE_ERROR_MSG))) {
  %><%= Util.htmlize(SearchHelper.PARSE_ERROR_MSG) %>
    <br/>You might try to enclose your search term in quotes,
    <a href="og_help.jsp#escaping">escape special characters</a>
    with <b>\</b>, or read the <a href="og_help.jsp">Help</a>
    on the query language. Error message from parser:<br/>
    <%= Util.htmlize(searchHelper.errorMsg.substring(
        SearchHelper.PARSE_ERROR_MSG.length())) %><%
    } else {
    %><%= Util.htmlize(searchHelper.errorMsg) %><%
      }%></p><%
    } else if (searchHelper.hits == null) {
        %><p>No hits</p><%
    } else if (searchHelper.hits.length == 0) {
        List<Suggestion> hints = searchHelper.getSuggestions();
        for (Suggestion hint : hints) {
        %><p><span class="text-error">Did you mean (for <%= hint.name %>)</span>:<%
  for (String word : hint.freetext) {
%> <a href=search?q=<%= word %>><%= word %>
</a> &nbsp;  <%
  }
  for (String word : hint.refs) {
%> <a href=search?refs=<%= word %>><%= word %>
</a> &nbsp;  <%
  }
  for (String word : hint.defs) {
%> <a href=search?defs=<%= word %>><%= word %>
</a> &nbsp;  <%
  }
%></p><%
        }
        %>
  <p> Your search <b><%= searchHelper.query %>
  </b> did not match any files.
    <br/> Suggestions:<br/>
  </p>
  <ul>
    <li>Make sure all terms are spelled correctly.</li>
    <li>Try different keywords.</li>
    <li>Try more general keywords.</li>
    <li>Use 'wil*' cards if you are looking for partial match.</li>
  </ul>
    <%
    } else {
        // We have a lots of results to show: create a slider for
        String slider = "";
        int thispage;  // number of items to display on the current page
        int start = searchHelper.start;
        int max = searchHelper.maxItems;
        int totalHits = searchHelper.totalHits;
        if (searchHelper.maxItems < searchHelper.totalHits) {
            StringBuilder buf = new StringBuilder(4096);
            thispage = (start + max) < totalHits ? max : totalHits - start;
            StringBuilder urlp = createUrl(searchHelper, false);
            int labelStart = 1;
            int sstart = start - max * (start / max % 10 + 1) ;
            if (sstart < 0) {
                sstart = 0;
                labelStart = 1;
            } else {
                labelStart = sstart / max + 1;
            }
            int label = labelStart;
            int labelEnd = label + 11;
            for (int i = sstart; i < totalHits && label <= labelEnd; i+= max) {
                if (i <= start && start < i + max) {
                    buf.append("<li class=\"active\"><a href=\"#\">").append(label).append("</a></li>");
                } else {
                    buf.append("<li><a href=\"s?n=").append(max)
                        .append("&amp;start=").append(i).append(urlp).append("\">");
                    if (label == labelStart && label != 1) {
                        buf.append("&lt;&lt");
                    } else if (label == labelEnd && i < totalHits) {
                        buf.append("&gt;&gt;");
                    } else {
                        buf.append(label);
                    }
                    buf.append("</a></li>");
                }
                label++;
            }
            slider = buf.toString();
        } else {
            // set the max index to max or last
            thispage = totalHits - start;
        }
        %>
  <div class="container">
    <p class="pagetitle">Searched <b><%= searchHelper.query
    %>
    </b> (Results <b><%= start + 1 %> - <%= thispage + start
    %>
    </b> of <b><%= totalHits %>
    </b>) sorted by <span class="label label-info"><%=
    searchHelper.order.getDesc() %></span></p>
    <% if (slider.length() > 0) { %>
    <div class="pagination">
      <ul><%= slider %>
      </ul>
    </div>
    <% } %>
    <table class="table table-striped"><%
        try {
            Results.prettyPrint(out, searchHelper, start, start + thispage);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    %>
    </table>
    <% if (slider.length() > 0) { %>
    <div class="pagination">
      <ul><%= slider %>
      </ul>
    </div>
    <% } %>
  </div>
  <div class="container">
    <p class="muted pull-right">
      Completed in <span class="label label-info"><%= System.currentTimeMillis() - starttime %></span> milliseconds
    </p>
  </div>
<%
    }
    searchHelper.destroy();
  }
/* ---------------------- search.jsp end --------------------- */
%>
<%@ include file="og_foot.jspf" %>