;(function($, window, document) {
  
var DragDrop = {
    DOMUtil : eXo.core.DOMUtil,
    dragObject : null,
    targetClass : [],
    init : function(compid) {
      var comp = findId(compid);
      //comp.find('div.FAQCategory').on('mousedown', this.attach);
      comp.on('selectstart', eXo.forum.ForumUtils.returnFalse);
      comp.on('dragstart', eXo.forum.ForumUtils.returnFalse);
    },
    attach : function(evt) {
      evt = evt || window.event;
      if (eXo.forum.EventManager.getMouseButton(evt) == 2)
        return;
      var dnd = eXo.answer.DragDrop;
      var dragObject = this.cloneNode(true);
      dragObject.className = "FAQDnDCategory";
      dragObject.style.border = "solid 1px #333333";
      document.body.appendChild(dragObject);
      dragObject.style.width = this.offsetWidth + "px";
      dnd.rootNode = this;
      dnd.mousePos = {
        x : evt.clientX,
        y : evt.clientY
      };
      dnd.setup(dragObject, [ "FAQCategory", "FAQBack", "FAQTmpCategory" ]);
      dnd.dropCallback = function(dragObj, target) {
        this.DOMUtil.removeElement(dragObj);
        if (this.lastTarget)
          this.lastTarget.style.border = "";
        if (target && dnd.isMoved) {
          var action = this.getAction(this.dragObject, target);
          if (!action) {
            this.showElement();
            return;
          }
          eval(action);
          
        } else
          this.showElement();
      }
      dnd.dragCallback = function(dragObj, target) {
        if (dnd.lastTarget) {
          dnd.lastTarget.style.border = "";
          if (this.DOMUtil.hasClass(dnd.lastTarget, "FAQHighlightCategory"))
            this.DOMUtil.replaceClass(dnd.lastTarget, "FAQHighlightCategory", "");
        }
        if (!target)
          return;
        dnd.lastTarget = target;
        if (this.DOMUtil.hasClass(target, "FAQBack"))
          target.onclick();
        if (this.DOMUtil.hasClass(target, "FAQTmpCategory"))
          this.DOMUtil.addClass(dnd.lastTarget, "FAQHighlightCategory");
        target.style.border = "dotted 1px #cccccc";
        if (!dnd.hided)
          dnd.hideElement(dnd.rootNode);
        
      }
    },
    
    setup : function(dragObject, targetClass) {
      this.dragObject = dragObject;
      this.targetClass = targetClass;
      document.onmousemove = eXo.answer.DragDrop.onDrag;
      document.onmouseup = eXo.answer.DragDrop.onDrop;
    },
    
    onDrag : function(evt) {
      var dnd = eXo.answer.DragDrop;
      var dragObject = dnd.dragObject;
      dragObject.style.left = eXo.ks.Browser.findMouseXInPage(evt) + 2 + "px";
      dragObject.style.top = eXo.ks.Browser.findMouseYInPage(evt) + 2 + "px";
      if (dnd.dragCallback) {
        var target = dnd.findTarget(evt);
        dnd.dragCallback(dragObject, target);
      }
    },
    
    onDrop : function(evt) {
      evt = evt || window.event;
      var dnd = eXo.answer.DragDrop;
      dnd.isMoved = true;
      if (dnd.mousePos.x == evt.clientX && dnd.mousePos.y == evt.clientY)
        dnd.isMoved = false;
      if (dnd.dropCallback) {
        var target = dnd.findTarget(evt);
        dnd.dropCallback(dnd.dragObject, target);
      }
      delete dnd.dragObject;
      delete dnd.targetClass;
      delete dnd.dragCallback;
      delete dnd.hided;
      delete dnd.rootNode;
      document.onmousemove = null;
      document.onmouseup = null;
    },
    
    findTarget : function(evt) {
      var targetClass = eXo.answer.DragDrop.targetClass;
      var i = targetClass.length;
      while (i--) {
        var target = eXo.forum.EventManager.getEventTargetByClass(evt, targetClass[i]);
        if (target)
          return target;
      }
    },
    hideElement : function(obj) {
      // var preElement = this.DOMUtil.findPreviousElementByTagName(obj, "div");
      // preElement.style.display = "none";
      obj.style.display = "none";
      this.hided = true;
    },
    showElement : function() {
      var dnd = eXo.answer.DragDrop;
      if (!dnd.rootNode)
        return;
      var preElement = this.DOMUtil.findPreviousElementByTagName(dnd.rootNode, "div");
      if (preElement)
        preElement.style.display = "";
      dnd.rootNode.style.display = "";
      if (dnd.lastTarget) {
        dnd.lastTarget.style.border = "";
        if (this.DOMUtil.hasClass(dnd.lastTarget, "FAQHighlightCategory"))
          this.DOMUtil.replaceClass(dnd.lastTarget, "FAQHighlightCategory", "");
      }
    },
    getAction : function(obj, target) {
      var info = this.DOMUtil.findFirstDescendantByClass(obj, "input", "InfoCategory");
      if (this.DOMUtil.hasClass(target, "FAQTmpCategory")) {
        var preElement = this.DOMUtil.findPreviousElementByTagName(target, "div");
        var top = " ";
        if (!preElement) {
          preElement = this.DOMUtil.findNextElementByTagName(target, "div");
          top = "top";
        }
        var preElementInfo = this.DOMUtil.findFirstDescendantByClass(preElement, "input", "InfoCategory");
        if (info.id == preElementInfo.id)
          return false;
        var actionLink = info.value;
        actionLink = actionLink.replace("=objectId", ("=" + info.id + "," + preElementInfo.id + "," + top));
      } else if (this.DOMUtil.hasClass(target, "FAQCategory")) {
        var actionLink = info.value;
        var targetInfo = this.DOMUtil.findFirstDescendantByClass(target, "input", "InfoCategory");
        actionLink = actionLink.replace("=objectId", "=" + info.id + "," + targetInfo.id);
        actionLink = actionLink.replace("ChangeIndex", "MoveCategoryInto");
      }
      return actionLink;
    }
  };


  // Expose
  window.eXo = eXo || {};
  window.eXo.answer = eXo.answer || {} ;
  window.eXo.answer.DragDrop = DragDrop;
})(gj, window, document);