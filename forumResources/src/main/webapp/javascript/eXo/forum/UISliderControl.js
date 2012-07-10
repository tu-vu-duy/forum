;(function($, window, document) {
  
  function UISliderControl() {
  }
  
  UISliderControl.prototype.findMouseRelativeX = function(cont, evt) {
    var Browser = eXo.core.Browser;
    var mouseX = Browser.findMouseXInPage(evt);
    var contX = Browser.findPosX(cont);
    var jcontrolWP = $('#UIControlWorkspace');
    if (!Browser.isFF() && jcontrolWP.exists())
      mouseX += jcontrolWP.outerWidth(true);
    return (mouseX - contX);
  };
  
  UISliderControl.prototype.start = function(obj, evt) {
    this.object = $(obj).find('div.SliderPointer').eq(0)[0];
    this.container = obj;
    this.inputField = $(obj.parentNode).find('input').eq(0)[0];
    var mouseX = this.findMouseRelativeX(obj, evt);
    var props = eXo.webui.UISliderControl.getValue(mouseX);
    $(this.object).css('width',props[0] + 'px');
    $(this.inputField).val(props[1] * 5);
    $(this.inputField.previousSibling).html(props[1] * 5);
    $(document).on('mousemove', this.execute);
    $(document).on('mouseup', this.end);
  };
  
  UISliderControl.prototype.execute = function(evt) {
    var obj = eXo.webui.UISliderControl.object;
    var cont = eXo.webui.UISliderControl.container;
    var inputField = eXo.webui.UISliderControl.inputField;
    var mouseX = eXo.webui.UISliderControl.findMouseRelativeX(cont, evt);
    var props = eXo.webui.UISliderControl.getValue(mouseX);
    $(obj).css('width', props[0] + 'px');
    $(inputField).val(props[1] * 5);
    $(inputField.previousSibling).html(props[1] * 5);
  };
  
  UISliderControl.prototype.getValue = function(mouseX) {
    var width = 0;
    var value = 0;
    mouseX = parseInt(mouseX);
    if (mouseX <= 7) {
      width = 14;
      value = 0;
    } else if ((mouseX > 7) && (mouseX <= 200)) {
      width = mouseX + 7;
      value = width - 14;
    } else if ((mouseX > 200) && (mouseX < 221)) {
      width = mouseX + 7;
      value = width - 28;
    } else {
      width = 228;
      value = 200;
    }
    return [ width, value ];
  };
  
  UISliderControl.prototype.end = function() {
    eXo.webui.UISliderControl.object = null;
    eXo.webui.UISliderControl.container = null;
    document.onmousemove = null;
    document.onmouseup = null;
  };
  
  UISliderControl.prototype.reset = function(input) {
    input.value = 0;
    input.previousSibling.innerHTML = 0;
    $(input).parents('.UISliderControl').find('div.SliderPointer').css('width', '14px');
  };
  
  window.eXo.webui = window.eXo.webui || {};
  window.eXo.webui.UISliderControl = new UISliderControl();
})(gj, window, document);