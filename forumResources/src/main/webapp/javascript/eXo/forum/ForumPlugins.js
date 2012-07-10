// this plugin to check existing element.
;(function($, window, document, undefined) {
	// preventing against multiple instantiations
	$.fn.exists = function() {
		return ($(this).length > 0);
	}
})(gj, window, document);

// this plugin to find element by id.
;(function($, window, document, undefined) {
  // preventing against multiple instantiations
  $.fn.findId = function(elm) {
    var jelm = $(elm);
    if (!jelm.exists() && String(elm).indexOf('#') != 0 && $('#' + elm).exists()) {
      jelm = $('#' + elm);
    }
    if($(this).exists()) {
      return $(this).find(jelm);
    } else {
      return jelm;
    }
  }

  window.findId = window.findId || $.fn.findId;
  $.findId = window.findId;
})(gj, window, document);
