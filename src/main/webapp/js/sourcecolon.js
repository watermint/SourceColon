
var SourceColon = {
  chunkSize: 100,
  chunkProfile: function (page, index, chunks) {
    var chunkIndex = Math.floor(index / SourceColon.chunkSize);
    var chunkBegin = Math.max(chunkIndex * SourceColon.chunkSize + 1, page.lwm);
    var chunkEnd = Math.min((chunkIndex + 1) * SourceColon.chunkSize, page.hwm);
    var nextChunkBegin = (chunkIndex + 1) * SourceColon.chunkSize + 1;
    var prevChunkEnd = chunkIndex * SourceColon.chunkSize - 1;
    var nextChunk = null;
    var prevChunk = null;

    if (chunks.next && nextChunkBegin <= page.hwm) {
      nextChunk = SourceColon.chunkProfile(page, nextChunkBegin, {next: true});
    }
    if (chunks.prev && page.lwm < prevChunkEnd) {
      prevChunk = SourceColon.chunkProfile(page, prevChunkEnd, {prev: true});
    }

    return {
      page: page,
      offset: chunks.offset,
      target: null,
      chunk: {
        begin: chunkBegin,
        end: chunkEnd
      },
      next: nextChunk,
      prev: prevChunk
    }
  },
  newChunk: function (chunk) {
    var c = $('<div>Loading.. ' + chunk.chunk.begin + ' - ' + chunk.chunk.end + '</div>');
    c.attr('id', chunk.fileHash + '-' + chunk.chunk.begin);
    c.appear(function () {
      setTimeout(SourceColon.loadChunk(jQuery.extend({container: c}, chunk)), 10);
    });

    return c;
  },
  loadChunk: function (chunk) {
    jQuery.ajax('chunk.json', {
      data: {
        fileHash: chunk.page.fileHash,
        begin: chunk.chunk.begin,
        end: chunk.chunk.end
      },
      dataType: 'json',
      success: function (rows) {
        chunk.container.empty();
        jQuery.each(rows.rows, function (i, d) {
          var n = chunk.chunk.begin + i;
          var t = "Text line number #" + n;
          var row = $("<div id='" + chunk.page.fileHash + "-" + n + "'><span class='label label-info'>" + n + "</span> " + t + "</div>");

          chunk.container.append(row);
        });

        if (chunk.prev || chunk.next) {
          setTimeout(function () {
            if (chunk.prev) {
              var prev = SourceColon.newChunk(chunk.prev);
              chunk.container.before(prev);
              window.scrollTo(window.scrollX, window.scrollY + chunk.container.height());
            }
            if (chunk.next) {
              chunk.container.after(SourceColon.newChunk(chunk.next));
            }
          }, 100);
        }
      }
    })
  },
  loadCode: function (container, fileHash, index) {
    jQuery.ajax('page.json', {
      dataType: 'json',
      success: function (page) {
        var p = SourceColon.chunkProfile(page, index, {next: true, prev: true, offset: index});
        var c = SourceColon.newChunk(p)
        $(container).append(c);
      }
    })
  }
}

$(document).ready(function () {
  SourceColon.loadCode($('#source1234'), '1234', 501);
})

