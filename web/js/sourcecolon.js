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
 */

function pageReadyList() {
  document.sym_div_width = 240;
  document.sym_div_height_max = 480;
  document.sym_div_top = 100;
  document.sym_div_left_margin = 40;
  document.sym_div_height_margin = 40;
  document.highlight_count = 0;
  $(window).resize(function() {
    if (document.sym_div_shown == 1) {
      document.sym_div.style.left = get_sym_div_left() + "px";
      document.sym_div.style.height = get_sym_div_height() + "px";
    }
  });
}

/* ------ Navigation window for definitions ------ */
/**
 * Create the Navigation toggle link as well as its contents.
 */
function getNavigationSymbolContents() {
  var contents = "";
  if (typeof getNavigationSymbols != 'function') {
    return contents;
  }

  var symbol_classes = getNavigationSymbols();
  for ( var i = 0; i < symbol_classes.length; i++) {
    if (i > 0) {
      contents += "<br/>";
    }
    var symbol_class = symbol_classes[i];
    var class_name = symbol_class[1];
    var symbols = symbol_class[2];
    contents += "<b>" + symbol_class[0] + "</b><br/>";

    for (var j = 0; j < symbols.length; j++) {
      var symbol = symbols[j][0];
      var line = symbols[j][1];
      contents += "<a href=\"#" + line + "\" class=\"" + class_name + "\">"
          + escape_html(symbol) + "</a><br/>";
    }
  }

  return contents;
}

function escape_html(string) {
  return string.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
}

// Toggle the display of the 'Navigation' window used to highlight definitions.
function toggleSourceNavigation() {
  if (document.sym_div == null) {
    document.sym_div = document.createElement("div");
    document.sym_div.id = "sym_div";
    document.sym_div_container = document.createElement("div");
    document.sym_div_container.className = "modal-body";
    document.sym_div_container.innerHTML = getNavigationSymbolContents();
    document.sym_div.appendChild(document.sym_div_container);
    document.body.appendChild(document.sym_div);

    $('#sym_div').dialog({
      title: "Navigation",
      closeOnEscape: false,
      resizable: false,
      closeText: 'Close',
      show: "fade",
      hide: "fade",
      dialogClass: "modal",
      position: { my: "right", at: "right-60", of: window },
      open: function(event, ui) {
        $(this).parent().find('div.ui-dialog-titlebar').addClass('modal-header');
      }
    })
  } else {
    $('#sym_div').dialog("open");
  }
}

// Toggle the display of line numbers.
function toggleSourceLineNumber() {
  $("a.line-number").toggle();
}

