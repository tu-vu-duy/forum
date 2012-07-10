;(function($, window, document) {

  function CheckBoxManager() {
  }
  
  CheckBoxManager.prototype.init = function(cont) {
    if (typeof (cont) == "string"){
      cont = $('#'+cont)
    }
    var checkboxes = cont.find('input.checkbox');
    if (!checkboxes.exists())
      return;
    checkboxes.on('click', this.check);
    checkboxes.eq(0).on('click', this.checkAll);
  };
  
  CheckBoxManager.prototype.checkAll = function() {
    eXo.forum.CheckBox.checkAllItem(this);
  };
  
  CheckBoxManager.prototype.getItems = function(obj) {
    var table = $(obj).parents('table');
    return table.find('input.checkbox');
  };
  
  CheckBoxManager.prototype.check = function() {
    eXo.forum.CheckBox.checkItem(this);
  };
  
  CheckBoxManager.prototype.checkAllItem = function(obj) {
    var checked = obj.checked;
    var items = eXo.forum.CheckBox.getItems(obj);
    for ( var i = 1; i < items.length; i++) {
      items[i].checked = checked;
    }
  };
  
  CheckBoxManager.prototype.checkItem = function(obj) {
    var checkboxes = eXo.forum.CheckBox.getItems(obj);
    var len = checkboxes.length;
    var state = true;
    if (!obj.checked) {
      checkboxes[0].checked = false;
    } else {
      for ( var i = 1; i < len; i++) {
        state = state && checkboxes[i].checked;
      }
      checkboxes[0].checked = state;
    }
  };
  window.eXo.forum = window.eXo.forum || {};
  window.eXo.forum.CheckBoxManager = new CheckBoxManager();
})(gj, window, document);