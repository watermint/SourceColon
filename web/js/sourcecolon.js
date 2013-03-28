/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */
/*
 * Copyright (c) 2009, 2010, Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright 2011 Jens Elkner.
 * Portions Copyright 2013 Takayuki Okazaki.
 */

function escape_html(string) {
  return string.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
}

function updateNavigationSymbolContents() {
  if (typeof getNavigationSymbols != 'function') {
    return;
  }

  var naviContainer = document.getElementById("code-navigation");

  var symbol_classes = getNavigationSymbols();
  var contents = "<h2>Navigation</h2>";
  for (var i = 0; i < symbol_classes.length; i++) {
    var symbol_class = symbol_classes[i];
    var class_name = symbol_class[1];
    var symbols = symbol_class[2];
    contents += "<h3>" + symbol_class[0] + "</h3><ul class='unstyled'>";

    for (var j = 0; j < symbols.length; j++) {
      var symbol = symbols[j][0];
      var line = symbols[j][1];
      contents += "<li><a href=\"#" + line + "\" class=\"" + class_name + "\">"
          + escape_html(symbol) + "</a></li>";
    }
    contents += "</ul>";
  }

  naviContainer.innerHTML = contents;
}