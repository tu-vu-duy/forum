eXo.forum.CheckBox = {
  init : function(cont){
    if(typeof(cont) == "string") cont = document.getElementById(cont) ;
    if(cont){
      var checkboxes = eXo.core.DOMUtil.findDescendantsByClass(cont, "input", "checkbox") ;
      if(checkboxes.length <=0) return ;
      checkboxes[0].onclick = this.checkAll ;
      var len = checkboxes.length ;
      for(var i = 1 ; i < len ; i ++) {
        checkboxes[i].onclick = this.check ;
        eXo.forum.CheckBoxManager.checkItem(checkboxes[i]);
      }
    }
  },
  
  check : function(){
    eXo.forum.CheckBoxManager.checkItem(this);
    var row = eXo.core.DOMUtil.findAncestorByTagName(this,"tr");
    if(this.checked) {
      eXo.core.DOMUtil.addClass(row,"SelectedItem");
      eXo.forum.UIForumPortlet.setChecked(true);
    }else{
      eXo.forum.UIForumPortlet.setChecked(false);
      eXo.core.DOMUtil.replaceClass(row,"SelectedItem","");
    }
  },
  
  checkAll : function(){
    eXo.forum.UIForumPortlet.checkAll(this);
    var table = eXo.core.DOMUtil.findAncestorByTagName(this,"table");
    table = eXo.core.DOMUtil.getChildrenByTagName(table,"tbody")[0];
    var rows = eXo.core.DOMUtil.findDescendantsByTagName(table,"tr");
    var i = rows.length ;
    if(this.checked){
      while(i--) {
        eXo.core.DOMUtil.addClass(rows[i],"SelectedItem");
      }
    } else{
      while(i--){
        eXo.core.DOMUtil.replaceClass(rows[i],"SelectedItem","");
      }
    }
  }
} ;
