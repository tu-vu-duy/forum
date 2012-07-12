
;(function($, window, document) {

  function SearchTagName() {
    this.searchTagNameNode = null;
    this.uiGridNode = null;
    this.SEARCH_IP_BAN = 'Search tag name ajax action';
    this.data = null;
  }

  SearchTagName.prototype.init = function(userName) {
    var DOMUtil = eXo.core.DOMUtil;
    this.parentNode = document.getElementById('searchTagName');
    if(!this.parentNode) return;
    $(this.parentNode).hide();
    this.parentNode.style.visibility = "hidden";
    var searchInputId =  this.parentNode.getAttribute("inputId");
    this.searchTagNameNode = document.getElementById(searchInputId);
    if (!this.searchTagNameNode) {
      return;
    }
    this.searchTagNameNode.value = "";
    this.searchTagNameNode.onkeydown = this.searchIpBanWrapper;
    this.searchTagNameNode.onclick = this.submitInput;
    var buttonSearch = document.getElementById('ButtonSearch');
    if(buttonSearch){buttonSearch.onclick = this.submitInput;}
  };

  SearchTagName.prototype.submitInput = function(event) {
    var str = String(eXo.forum.webservice.SearchTagName.searchTagNameNode.value)
    if(eXo.forum.webservice.SearchTagName.parentNode.style.visibility === "hidden" && str.trim().length === 0) {
      eXo.forum.webservice.SearchTagName.searchTagName('onclickForm');
    }
  };

  SearchTagName.prototype.searchIpBanWrapper = function(event) {
    var key = eXo.forum.ForumUtils.getKeynum(event);
    if(key == 13){
      var object = eXo.forum.webservice.SearchTagName;
      var str = String(object.searchTagNameNode.value);
      if(object.parentNode.style.visibility === "visible"){
        object.searchTagNameNode.focus();
        object.parentNode.style.visibility = "hidden";
        object.searchTagName(' ');
      } else if(str.trim().length > 0){
        var linkSubmit = String(object.parentNode.getAttribute('linkSubmit'));
        linkSubmit = linkSubmit.replace("javascript:", "");
        eval(linkSubmit);
      }
      return;
    }
    if(key == 38 || key == 40){
      var DOMUtil = eXo.core.DOMUtil;
      var items = DOMUtil.findDescendantsByClass(this.parentNode, "div", "TagNameItem");
      if(items && items.length > 0) {
        var itemSl =  DOMUtil.findFirstDescendantByClass(this.parentNode, "div", "Selected");
        if(itemSl) {
          var t = items.length;
          for (var i = 0; i < t; i++) {
            if(items[i] === itemSl){
              items[i].className = "TagNameItem";
              if(i == 0 && key == 38) {
                eXo.forum.webservice.SearchTagName.setValueInput(items[t-1]);
              }else if(i == (t-1) && key == 40){
                eXo.forum.webservice.SearchTagName.setValueInput(items[0]);
              } else if(key == 38){
                eXo.forum.webservice.SearchTagName.setValueInput(items[i-1]);
              } else if(key == 40) {
                eXo.forum.webservice.SearchTagName.setValueInput(items[i+1]);
              }
            }
          }
        } else {
          eXo.forum.webservice.SearchTagName.setValueInput(items[0]);
        }
      }
    }else if(key > 40 || key == 8) {
      var str = String(eXo.forum.webservice.SearchTagName.searchTagNameNode.value)
      if(key == 8 && (str.trim().length === 0 ||str.trim().length === 1)){
        eXo.forum.webservice.SearchTagName.searchTagName('onclickForm');
      } else {
        window.setTimeout(eXo.forum.webservice.SearchTagName.searchIpBanTimeout, 50);
      }
    }
  };

  SearchTagName.prototype.setValueInput = function(elm) {
    elm.className = "TagNameItem Selected";
    var str = String(this.searchTagNameNode.value);
    str = str.substring(0, str.lastIndexOf(" "));
    var value = String(elm.innerHTML);
    value = value.substring(0, value.indexOf(" "));
    if(str.length == 0) str = value ;
    else str = str + " " + value;
    this.searchTagNameNode.value = str;
  };

  SearchTagName.prototype.searchIpBanTimeout = function() {
    eXo.forum.webservice.SearchTagName.searchTagName(eXo.forum.webservice.SearchTagName.searchTagNameNode.value);
  };

  SearchTagName.prototype.searchTagName = function(keyword) {
    // Get data from service, url: /ks/forum/filterTagNameForum/{strTagName}/
    keyword = String(keyword);
    var strs = keyword.split(" ");
    if(strs.length >= 1)keyword = strs[strs.length-1];
    keyword = keyword || 'onclickForm';
    var userAndTopicId = this.parentNode.getAttribute("userAndTopicId");
    var restPath = this.parentNode.getAttribute("restPath");
    if(userAndTopicId){
      var url = restPath + '/ks/forum/filterTagNameForum/' + userAndTopicId + '/' + keyword + '/';
      this.request = $.getJSON(url);
      setTimeout(eXo.forum.webservice.SearchTagName.processing, 200);
    }
  };

  SearchTagName.prototype.processing = function() {
   var SearchTagName = eXo.forum.webservice.SearchTagName;
   if(SearchTagName.request.isResolved()) {
      SearchTagName.data = eXo.core.JSON.parse(SearchTagName.request.responseText);
      if(SearchTagName.data) {
        SearchTagName.updateIpBanList();
        $(SearchTagName.parentNode).show(300);
      }
    }
  };

  SearchTagName.prototype.updateIpBanList = function() {
    var DOMUtil = eXo.core.DOMUtil;
    // Remove all old items
    var oldTagNameList = DOMUtil.findDescendantsByClass(this.parentNode, 'div', 'TagNameItem');
    for(var i=0; i < oldTagNameList.length; i++) {
      DOMUtil.removeElement(oldTagNameList[i]);		
    }
    // Fill up with new list
    var t = 0;
    var length_ = this.data.jsonList.length ;
    for(var i=0; i < length_; i++) {
      this.parentNode.appendChild(this.buildItemNode(this.data.jsonList[i].ip));
      t = 1;
    }
    if(t==1) this.parentNode.style.visibility = "visible";
    else this.parentNode.style.visibility = "hidden";
  };

  SearchTagName.prototype.buildItemNode = function(ip) {
    var SearchTagName = eXo.forum.webservice.SearchTagName;
    var itemNode = $('<div></div>').addClass('TagNameItem').html(ip);
    this.searchTagNameNode;
    itemNode.onclick = function() {
      var str = String(eXo.forum.webservice.SearchTagName.searchTagNameNode.value);
      str = str.substring(0, str.lastIndexOf(' '))
      if(str.length == 0) str = ip ;
      else str = str + " " + ip;
      SearchTagName.searchTagNameNode.value = str;
      SearchTagName.searchTagNameNode.focus();
      SearchTagName.parentNode.style.visibility = "hidden";
      SearchTagName.searchTagName(' ');
    }
    itemNode.onmouseover = SearchTagName.mouseEvent(this, true);
    itemNode.onfocus = SearchTagName.mouseEvent(this, true);
    itemNode.onmouseout = SearchTagName.mouseEvent(this, false);
    itemNode.onblur = SearchTagName.mouseEvent(this, false);
    return itemNode;
  };


  SearchTagName.prototype.mouseEvent = function(elm, isOv) {
    if (isOv) {
      if (elm.className === 'TagNameItem') {
        elm.className = 'TagNameItem OverItem';
      } else {
        elm.className = 'TagNameItem OverItem Slect';
      }
    } else {
      if (elm.className === 'TagNameItem OverItem') {
        elm.className = 'TagNameItem';
      } else {
        elm.className = 'TagNameItem Selected';
      }
    }
  };
  
  window.eXo.forum = window.eXo.forum || {};
  window.eXo.forum.webservice = window.eXo.forum.webservice || {};
  window.eXo.forum.webservice.SearchTagName = new SearchTagName();

})(gj, window, document);
