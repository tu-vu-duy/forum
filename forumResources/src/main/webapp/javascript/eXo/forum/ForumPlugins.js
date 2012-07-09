// this plugin to check existing element.
;(function($, window, document, undefined) {
	// preventing against multiple instantiations
	$.fn.exists = function() {
		return ($(this).length > 0);
	}
})(gj, window, document);

