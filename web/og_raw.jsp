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

Copyright 2008 Sun Microsystems, Inc.  All rights reserved.
Use is subject to license terms.

Portions Copyright 2011 Jens Elkner.

--%>
<%@page import="
java.io.File,
java.io.FileInputStream,
java.io.InputStream,
java.io.OutputStream,
org.watermint.sourcecolon.org.opensolaris.opengrok.web.PageConfig"
    %>
<%
  {
    PageConfig cfg = PageConfig.get(request);

    File f = cfg.getResourceFile();
    InputStream in = null;
    try {
        in = new FileInputStream(f);
        response.setContentLength((int) f.length());
        response.setDateHeader("Last-Modified", f.lastModified());
    } catch (Exception e) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    String mimeType = getServletContext().getMimeType(f.getAbsolutePath());
    response.setContentType(mimeType);

    try {
      response.setHeader("content-disposition", "attachment; filename="
          + f.getName());
      OutputStream o = response.getOutputStream();
      byte[] buffer = new byte[8192];
      int nr;
      while ((nr = in.read(buffer)) > 0) {
        o.write(buffer, 0, nr);
      }
      o.flush();
      o.close();
    } finally {
      in.close();
    }
  }
%>