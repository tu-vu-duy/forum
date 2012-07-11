;(function($, window, document) {
  var UIContextMenu = {
    container = [],
    menus : [],
    setup : function() {
      var i = this.container.length;
      while (i--) {
        $(this.container[i]).on('contextmenu', this.show);
      }
    },
    setContainer : function(obj) {
      this.container.push(obj);
    },
    getMenu : function(evt) {
      var element = this.getMenuElement(evt);
      if (!element && !element.exists())
        return;
      var menuId = String(element.attr('id')).replace("Context", "");
      var jcont = element.parents('.PORTLET-FRAGMENT');
      var jmenu = jcont.findId(menuId);
      if (!jmenu.exists())
        return;
      if (element[0].tagName != "TR")
        $(element[0].parentNode).append(jmenu);
      return jmenu;
    },
    getMenuElement : function(evt) {
      var target = eXo.ks.EventManager.getEventTarget(evt);
      for (i = 0; i < this.classNames.length; i++) {
        var parent = $(target).parents('.'+this.classNames[i]);
        if (parent.exists()) {
          return parent;
        }
      }
      return null;
    },
    setPosition : function(jobj, evt) {
      var Browser = eXo.core.Browser;
      var x = Browser.findMouseXInPage(evt) - 2;
      var y = Browser.findMouseYInPage(evt) - 2;
      jobj.css('position', 'absolute').show();
      var obj = jobj[0];
      if (obj.offsetParent)
        x -= Browser.findPosX(obj.offsetParent);
      if (Browser.isDesktop()) {
        x = Browser.findMouseXInPage(evt) - Browser.findPosX(obj.offsetParent);
        y -= Browser.findPosY(obj.offsetParent);
        jobj.css('left', x + 'px');
      } else {
        jobj.css('left', x + 'px');
      }
      jobj.css('top', y + 'px');
    },
    show : function(evt) {
      eXo.forum.ForumUtils.cancelEvent(evt);
      eXo.forum.ForumUtils.hideElements();
      var jmenu = this.getMenu(evt);
      if (!jmenu){
        return;
      }
      this.setPosition(jmenu, evt);
      eXo.forum.ForumUtils.addhideElement(jmenu);
      return false;
    }
  };

  window.eXo = window.eXo || {};
  window.eXo.forum = window.eXo.forum || {};
  window.eXo.forum.UIContextMenu = UIContextMenu;
})(gj, window, document);