var SourceColon = {
  chunkSize: 1000,
  lineHeight: 20,
  chunkProfiles: function (page) {
    var list = [];
    var pageNum = Math.ceil((page.hwm - page.lwm + 1.0) / SourceColon.chunkSize);
    for (var i = 0; i < pageNum; i++) {
      var begin = i * SourceColon.chunkSize + page.lwm;
      list.push({
        page: page,
        chunkBegin: begin,
        chunkEnd: Math.min(begin + SourceColon.chunkSize - 1, page.hwm)
      })
    }
    return list;
  },
  appendChunks: function (container, page, focusIndex) {
    var chunks = [];
    jQuery.each(SourceColon.chunkProfiles(page), function () {
      var c = SourceColon.createChunk(this);
      container.append(c);
      chunks.push(jQuery.extend({
        container: c}, this));
    })
    window.scrollTo(window.scrollX, focusIndex * SourceColon.lineHeight + container.position().top);
    jQuery.each(chunks, function (i, d) {
      $(this.container).appear(function () {
        setTimeout(function() {
          SourceColon.loadChunk(d);
        }, 10);
      })
    })
  },
  createChunk: function (chunkProfile) {
    var chunk = $('<ol class="prettyprint linenums" start="' + chunkProfile.chunkBegin + '"></ol>');
    var placeHolder = $('<div><h1>Place Holder for ' + chunkProfile.chunkBegin + '</h1></div>');
    var placeHolderHeight = SourceColon.lineHeight * (chunkProfile.chunkEnd - chunkProfile.chunkBegin);
    placeHolder.css('height', placeHolderHeight + 'px');

    chunk.attr('id', chunkProfile.page.fileHash + "-" + chunk.chunkBegin);
    chunk.append(placeHolder);

    return chunk;
  },
  loadChunk: function (chunk) {
    jQuery.ajax('chunk.json', {
      data: {
        begin: chunk.chunkBegin,
        end: chunk.chunkEnd,
        fileHash: chunk.page.fileHash
      },
      dataType: 'json',
      success: function (rows) {
        chunk.container.empty();

        // dummy data
        rows.rows = []
        for (var i = 0; i < SourceColon.chunkSize; i++) {
          rows.rows.push({
            n: chunk.chunkBegin + i, t: 'Text line number #' + (chunk.chunkBegin + i)
          })
        }

        jQuery.each(rows.rows, function (i, d) {
          var n = d.n;
          var t = d.t;
          var chunkId = chunk.page.fileHash + "-" + n;
          var row = $('<li></li>');
          var a = $('<a></a>');

          a.attr('name', n);
          row.append(a);
          row.append(t);

          chunk.container.append(row);
        });
      }
    })
  },
  loadPage: function(container, fileHash, focusIndex) {
    jQuery.ajax('page.json', {
      data: {
        fileHash: fileHash
      },
      dataType: 'json',
      success: function(page) {
        SourceColon.appendChunks(container, page, focusIndex);
      }
    })
  }
}
