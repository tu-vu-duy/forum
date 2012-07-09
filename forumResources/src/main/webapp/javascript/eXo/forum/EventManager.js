function EventManager() {
}

EventManager.prototype.addEvent = function(obj, type, fn) {
	if (obj.attachEvent) {
		obj['e' + type + fn] = fn;
		obj[type + fn] = function() {
			obj['e' + type + fn](window.event);
		}
		obj.attachEvent('on' + type, obj[type + fn]);
	} else
		obj.addEventListener(type, fn, false);
};

EventManager.prototype.removeEvent = function(obj, type, fn) {
	if (obj.detachEvent) {
		obj.detachEvent('on' + type, obj[type + fn]);
		obj[type + fn] = null;
	} else
		obj.removeEventListener(type, fn, false);
};

EventManager.prototype.getMouseButton = function(evt) {
	var evt = evt || window.event;
	return evt.button;
};

EventManager.prototype.getEventTarget = function(evt) {
	var evt = evt || window.event;
	var target = evt.target || evt.srcElement;
	if (target.nodeType == 3) { // check textNode
		target = target.parentNode;
	}
	return target;
};

EventManager.prototype.getEventTargetByClass = function(evt, className) {
	var target = this.getEventTarget(evt);
	if (eXo.core.DOMUtil.hasClass(target, className))
		return target;
	else
		return eXo.core.DOMUtil.findAncestorByClass(target, className);
};

EventManager.prototype.getEventTargetByTagName = function(evt, tagName) {
	var target = this.getEventTarget(evt);
	if (target.tagName.toLowerCase() == tagName.trim())
		return target;
	else
		return eXo.core.DOMUtil.findAncestorByTagName(target, tagName);
};

EventManager.prototype.cancelBubble = function(evt) {
	if (eXo.ks.Browser.browserType == 'ie')
		window.event.cancelBubble = true;
	else
		evt.stopPropagation();
};

EventManager.prototype.cancelEvent = function(evt) {
	eXo.ks.EventManager.cancelBubble(evt);
	if (eXo.ks.Browser.browserType == 'ie')
		window.event.returnValue = true;
	else
		evt.preventDefault();
};

window.eXo.ks = window.eXo.ks || {};
window.eXo.ks.EventManager = new EventManager();