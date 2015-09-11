/**
 * Some simple jquery plugin apply on Forum
 */
(function($) {

  /**
   * Check has attribute
   * Ex: $('body').hasAttr('class')
   */
  $.fn.hasAttr = function(name) {
    var attr = $(this).attr(name);
    return (typeof attr !== 'undefined' && attr !== false) ? true : false;
  };
 
  /**
   * Check existing element.
   * Ex:
   *  if($('.ABC').exists()) {
   *    $('.ABC').remove();
   *  }
   */
  $.fn.exists = function() {
    return ($(this).length > 0);
  }

  /**
   * find element by id.
   * Ex:
   *  var portlet = $.fn.findId('UIForumPortlet');
   *  var cat = porltet.findId('UICategory');
   * or
   *  var cat = porltet.findId('#UICategory');
   */
  $.fn.findId = function(elm) {

    if (!$(elm).exists() && String(elm).indexOf('#') != 0
        && $('#' + elm).exists()) {
      elm = '#' + elm;
    }

    if ($(this).exists() && !$.isWindow(this)) {
      return $(this).find(elm);
    } else {
      return $(elm);
    }
  }

})(gj);
