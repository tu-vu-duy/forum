window.eXo.ks = window.eXo.ks || {};

eXo.ks.UIContextMenu = {
	menus : [],
	setup : function() {
		if (!this.container)
			return;
		var i = this.container.length;
		while (i--) {
			eXo.ks.EventManager.addEvent(this.container[i], "contextmenu", this.show);
		}
	},
	setContainer : function(obj) {
		if (!this.container)
			this.container = [];
		this.container.push(obj);
	},
	getMenu : function(evt) {
		var element = this.getMenuElement(evt);
		if (!element)
			return;
		var menuId = String(element.id).replace("Context", "");
		var cont = eXo.core.DOMUtil.findAncestorByClass(element, "PORTLET-FRAGMENT");
		var menu = eXo.core.DOMUtil.findDescendantById(cont, menuId);
		if (!menu)
			return;
		if (element.tagName != "TR")
			element.parentNode.appendChild(menu);
		return menu;
	},
	getMenuElement : function(evt) {
		var target = eXo.ks.EventManager.getEventTarget(evt);
		while (target) {
			var className = target.className;
			if (!className) {
				target = target.parentNode;
				continue;
			}
			className = className.replace(/^\s+/g, "").replace(/\s+$/g, "");
			var classArray = className.split(/[ ]+/g);
			for (i = 0; i < classArray.length; i++) {
				if (this.classNames.contains(classArray[i])) {
					return target;
				}
			}
			target = target.parentNode;
		}
		return null;
	},
	hideElement : function() {
		var ln = eXo.core.DOMUtil.hideElementList.length;
		if (ln > 0) {
			for ( var i = 0; i < ln; i++) {
				eXo.core.DOMUtil.hideElementList[i].style.display = "none";
			}
			eXo.core.DOMUtil.hideElementList.clear();
		}
	},
	setPosition : function(obj, evt) {
		var Browser = eXo.core.Browser;
		var x = Browser.findMouseXInPage(evt) - 2;
		var y = Browser.findMouseYInPage(evt) - 2;
		obj.style.position = "absolute";
		obj.style.display = "block";
		if (obj.offsetParent)
			x -= Browser.findPosX(obj.offsetParent);
		if (Browser.isDesktop()) {
			x = Browser.findMouseXInPage(evt) - Browser.findPosX(obj.offsetParent);
			y -= Browser.findPosY(obj.offsetParent);
			obj.style.left = x + "px";
		} else {
			obj.style.left = x + "px";
		}
		obj.style.top = y + "px";
	},
	show : function(evt) {
		eXo.forum.ForumUtils.cancelEvent(evt);
		var ctx = eXo.ks.UIContextMenu;
		var menu = ctx.getMenu(evt);
		ctx.hideElement();
		if (!menu)
			return;
		ctx.setPosition(menu, evt);
		eXo.core.DOMUtil.listHideElements(menu);
		return false;
	}
};