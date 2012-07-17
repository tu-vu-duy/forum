;(function($, window, document) {
  
  var DragDrop = {
    dragObject : null,
    targetClass : [],
    
    init : function(compid) {
      var comp = findId(compid);
      comp.find('div.FAQCategory').on('mousedown', this.attach);
      comp.on('selectstart', eXo.forum.ForumUtils.returnFalse);
      comp.on('dragstart', eXo.forum.ForumUtils.returnFalse);
    },
    
    attach : function(evt) {
      evt = evt || window.event;
      if (eXo.forum.EventManager.getMouseButton(evt) == 2) return;
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
        $(dragObj).remove();
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
          if ($(dnd.lastTarget).hasClass('FAQHighlightCategory'))
            $(dnd.lastTarget).removeClass('FAQHighlightCategory');
        }
        if (!target)
          return;
        dnd.lastTarget = target;
        if ($(target).hasClass('FAQBack'))
          target.onclick();
        if ($(target).hasClass('FAQTmpCategory'))
          $(dnd.lastTarget).addClass('FAQHighlightCategory');
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
      var preElement = $(dnd.rootNode).prev('div');
      if (preElement.exists())
        preElement.css('display', '');
      dnd.rootNode.style.display = "";
      if (dnd.lastTarget) {
        dnd.lastTarget.style.border = "";
        if ($(dnd.lastTarget).hasClass('FAQHighlightCategory'))
          $(dnd.lastTarget).removeClass('FAQHighlightCategory');
      }
    },
    
    getAction : function(obj, target) {
      var info = $(obj).find('input.InfoCategory:first');
      if ($(target).hasClass('FAQTmpCategory')) {
        var preElement = $(target).prev('div');
        var top = " ";
        if (!preElement.exists()) {
          preElement = $(target).next('div');
          top = "top";
        }
        var preElementInfo = preElement.find('input.InfoCategory:first');
        if (info.attr('id') == preElementInfo.attr('id')) return false;
        var actionLink = info.attr('value');
        actionLink = actionLink.replace("=objectId", ("=" + info.attr('id') + "," + preElementInfo.attr('id') + "," + top));
      } else if ($(target).hasClass('FAQCategory')) {
        var actionLink = info.attr('value');
        var targetInfo = $(target).find('input.InfoCategory:first');
        actionLink = actionLink.replace("=objectId", "=" + info.attr('id') + "," + targetInfo.attr('id'));
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
