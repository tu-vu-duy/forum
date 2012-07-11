;(function($, window, document) {
  
  function gj(elm) {
    if(String(elm).indexOf('#') != 0 && !$(elm).exists() && $('#'+elm).exists()) {
      return $('#'+elm);
    }
    return $(elm);
  }

  function UIAnswersPortlet() {
    this.viewImage = true;
    this.scrollManagerLoaded = false;
    this.hiddentMenu = true;
    this.scrollMgr = [];
    this.portletId = 'UIAnswersPortlet';
  };
  
  UIAnswersPortlet.prototype.init = function (portletId) {
    this.portletId = new String(portletId);
    this.updateContainersHeight();
    this.controlWorkSpace();
    this.disableContextMenu();
  };
  
  UIAnswersPortlet.prototype.updateContainersHeight = function () {
    var viewQuestionContentEl = gj('#' + this.portletId + ' div.CategoriesContainer div.ViewQuestionContent');
    if (viewQuestionContentEl.length) viewQuestionContentEl.css('height', viewQuestionContentEl.height() - 67);
  };
  
  UIAnswersPortlet.prototype.controlWorkSpace = function () {
    var slidebarButton = gj('#ControlWorkspaceSlidebar div.SlidebarButton');
    if (slidebarButton.length) slidebarButton.on('click', this.onClickSlidebarButton);
    setTimeout(this.reSizeImages, 1500);
  };
  
  UIAnswersPortlet.prototype.disableContextMenu = function () {
    var oncontextmenus = gj('#' + this.portletId + ' .oncontextmenu');
    for (var i = 0; i < oncontextmenus.length; i++) {
      oncontextmenus.eq(i).bind('contextmenu', function() {
        return false;
    });
    }
  };
  
  UIAnswersPortlet.prototype.selectCateInfor = function (number) {
    var obj = null;
    for (var i = 0; i < 3; i++) {
      obj = gj('#uicategoriesCateInfors' + i);
      if (obj.length) {
        if (i == number) obj.css('fontWeight', 'bold');
        else obj.css('fontWeight', 'normal');
      }
    }
  };
  
  UIAnswersPortlet.prototype.setCheckEvent = function (isCheck) {
    this.hiddentMenu = isCheck;
  };
  
  UIAnswersPortlet.prototype.viewTitle = function (id) {
    gj('#' + id).css('display', 'block');
    this.hiddentMenu = false;
  };
  
  UIAnswersPortlet.prototype.hiddenTitle = function (id) {
    gj('#' + id).css('display', 'none');
  };
  
  UIAnswersPortlet.prototype.hiddenMenu = function () {
    if (this.hiddentMenu) {
      this.hiddenTitle('FAQCategroManager');
      this.hiddentMenu = false;
    }
    setTimeout('eXo.answer.UIAnswersPortlet.checkAction()', 1000);
  };
  
  UIAnswersPortlet.prototype.checkAction = function () {
    if (this.hiddentMenu) {
      setTimeout('eXo.answer.UIAnswersPortlet.hiddenMenu()', 1500);
    }
  };
  
  UIAnswersPortlet.prototype.checkCustomView = function (isNotSpace, hideTitle, showTitle) {
    // TODO: jQuery cookie plugin?
    var cookie = eXo.ks.Browser.getCookie("FAQCustomView");
    cookie = (cookie == "none" || cookie == "" && isNotSpace == "false") ? "none" : "";
    gj('#FAQViewCategoriesColumn').css('display', cookie);
    
    var title = gj('#FAQTitlePanels');
    if (cookie == "none") {
      gj('#FAQCustomView').addClass('FAQCustomViewRight');
      title.attr('title', showTitle);
    } else {
      title.attr('title', hideTitle);
      cookie = "block";
    }
    
    // TODO: jQuery cookie plugin?
    eXo.ks.Browser.setCookie("FAQCustomView", cookie, 1);
  };
  
  UIAnswersPortlet.prototype.changeCustomView = function (change, hideTitle, showTitle) {
    var columnCategories = gj('#FAQViewCategoriesColumn');
    var buttomView = gj('#FAQCustomView');
    var title = gj('#FAQTitlePanels');
    var cookie = '';
    
    if (columnCategories.css('display') != 'none') {
      columnCategories.css('display', 'none');
      buttomView.addClass('FAQCustomViewRight');
      title.attr('title', showTitle);
      cookie = 'none';
    } else {
      columnCategories.css('display', '');
      buttomView.removeClass('FAQCustomViewRight');
      title.attr('title', hideTitle);
      cookie = 'block';
    }
    
    // TODO: jQuery cookie plugin?
    eXo.ks.Browser.setCookie("FAQCustomView", cookie, 1);
    if (gj.isFunction(this.initActionScroll)) this.initActionScroll();
    if (gj.isFunction(this.initBreadCumbScroll)) this.initBreadCumbScroll();
  };
  
  UIAnswersPortlet.prototype.changeStarForVoteQuestion = function (i, id) {
    var objId = id + i;
    var obj = gj('#' + objId);
    if (obj.length) obj.attr('class', 'OverVote');
  
    for (var j = 0; j <= i; j++) {
      objId = id + j;
      obj = gj('#' + objId);
      if (obj.length) obj.attr('class', 'OverVote');
    }
  
    for (var j = i + 1; j < 5; j++) {
      objId = id + j;
      obj = obj = gj('#' + objId);
      if (obj.length) obj.attr('class', 'RatedVote');
    }
  };
  
  UIAnswersPortlet.prototype.jumToQuestion = function (id) {
    var obj = gj('#' + id);
    if (obj.length) {
      var viewContent = obj.parent('.ViewQuestionContent');
      if (viewContent.length) viewContent.scrollTop(viewContent.position().top);
    }
  };
  
  UIAnswersPortlet.prototype.viewDivById = function (id) {
    var obj = gj('#' + id);
    if (obj.css('display') === 'none')
      obj.css('display', 'block');
    else {
      obj.css('display', 'none');
      gj('#' + id.replace("div", "")).val('');
    }
  };
  
  UIAnswersPortlet.prototype.showPicture = function (src) {
    if (this.viewImage) {
      // TODO:
      eXo.ks.MaskLayerControl.showPicture(src);
    }
  };
  
  UIAnswersPortlet.prototype.getImageSize = function (imageNode) {
    var tmp = gj('#' + imageNode.id).clone();
    tmp.css('visibility', 'hidden');
    gj('body').appendChild(tmp);
    var size = {
      width: tmp.offset().left,
      height: tmp.offset().top
    }
    tmp.remove();
    return size;
  };
  
  UIAnswersPortlet.prototype.showFullScreen = function (imageNode, containerNode) {
    var imageSize = this.getImageSize(imageNode);
    var widthMax = gj(window).width();
    if ((imageSize.width + 40) > widthMax) {
      containerNode.css('width', widthMax);
      imageNode.width = widthMax - 40;
      imageNode.css('height', 'auto');
    }
  };
  
  UIAnswersPortlet.prototype.showMenu = function (obj, evt) {
    var menu = gj(obj).find('div.UIRightClickPopupMenu:first');
    eXo.webui.UIPopupSelectCategory.show(obj, evt);
    var top = menu.offset().top;
    menu.css('top', -(top + 20));
  };
  
  UIAnswersPortlet.prototype.printPreview = function (obj) {
    var DOMUtil = eXo.core.DOMUtil;
    var uiPortalApplication = gj("#UIPortalApplication");
    var answerContainer = gj(obj).parents('.AnswersContainer');
    var printArea = answerContainer.find('div.QuestionSelect:first');
    printArea = printArea.clone();
    
    var dummyPortlet = gj(document.createElement('div')).addClass('UIAnswersPortlet UIPrintPreview');
    var FAQContainer = gj(document.createElement('div')).addClass('AnswersContainer');
    var FAQContent = gj(document.createElement('div')).addClass('FAQContent');
    var printActions = gj(document.createElement('div')).addClass('UIAction')
                                                        .css('display', 'block');
  
    var printActionInApp = answerContainer.find('div.PrintAction:first');
    var cancelAction = gj(document.createElement('a')).addClass('ActionButton LightBlueStyle')
                                                      .attr('href', 'javascript:void(0);')
                                                      .html(printActionInApp.attr('title'));
    
    var printAction = gj(document.createElement('a')).addClass('ActionButton LightBlueStyle')
                                                     .html(printActionInApp.html());
  
    printActions.append(printAction);
    printActions.append(cancelAction);
  
    if (!gj.browser.msie) {
      var cssContent = gj(document.createElement('div')).html('<style type="text/css">.DisablePrint{display:none;}</style>').css('display', 'block');
      FAQContent.append(cssContent);
    }
    FAQContent.append(printArea);
    FAQContainer.append(FAQContent);
    FAQContainer.append(printActions);
    dummyPortlet.append(FAQContainer);
    if (gj.browser.msie) {
      var displayElms = dummyPortlet.find('.DisablePrint');
      var i = displayElms.length;
      while (i--) {
        displayElms.eq(i).css('display', 'none');
      }
    }
    dummyPortlet = this.removeLink(dummyPortlet);
    dummyPortlet.css('width', '98.5%');
    this.removeLink(dummyPortlet).insertBefore(uiPortalApplication);
    uiPortalApplication.css('display', 'none');
    gj(window).scrollTop(0).scrollLeft(0);
  
    cancelAction.on('click', function () {
      eXo.answer.UIAnswersPortlet.closePrint();
    });
    printAction.on('click', function () {
      window.print();
    });
  
    this.viewImage = false;
  };
  
  UIAnswersPortlet.prototype.printAll = function (obj) {
    var uiPortalApplication = gj('#UIPortalApplication');
    var container = gj(document.createElement('div')).addClass('UIAnswersPortlet');
    if (typeof (obj) == 'string') obj = gj('#' + obj);
    uiPortalApplication.css('display', 'none');
    container.append(obj.clone());
    gj('body').append(container);
  };
  
  UIAnswersPortlet.prototype.closePrintAll = function () {
    var children = gj('body').find('*');
    var i = children.length;
    while (i--) {
      if (children.eq(i).hasClass('UIAnswersPortlet')) {
        children.eq(i).remove();
        return;
      }
    }
  };
  
  UIAnswersPortlet.prototype.removeLink = function (rootNode) {
    var links = rootNode.find('a');
    var len = links.length;
    for (var i = 0; i < len; i++) {
      links.eq(i).attr('href', 'javascript:void(0);');
      if (links.eq(i).attr('onclick') != undefined) links.eq(i).attr('onclick', 'javascript:void(0);');
    }
    var contextAnchors = rootNode.find('div[onmousedown]');
    i = contextAnchors.length;
    while (i--) {
      contextAnchors.eq(i).attr('onmousedown', null);
      contextAnchors.eq(i).attr('onkeydown', null);
    }
    contextAnchors = rootNode.find('div[onmouseover]');
    i = contextAnchors.length;
    while (i--) {
      contextAnchors.eq(i).attr('onmouseover', null);
      contextAnchors.eq(i).attr('onmouseout', null);
      contextAnchors.eq(i).attr('onfocus', null);
      contextAnchors.eq(i).attr('onblur', null);
    }
  
    contextAnchors = rootNode.find('div[onclick]');
    i = contextAnchors.length;
    while (i--) {
      if (contextAnchors.eq(i).hasClass('ActionButton')) continue;
      if (contextAnchors.eq(i).attr('onclick') != undefined) contextAnchors.eq(i).attr('onclick', 'javascript:void(0);');
    }
    return rootNode;
  };
  
  UIAnswersPortlet.prototype.closePrint = function () {
    gj('#UIPortalApplication').css('display', 'block');
    var children = gj('body').children();
    for (var i = 0; i < children.length; i++) {
      if (children.eq(i).hasClass('UIAnswersPortlet')) children.eq(i).remove();
    }
  
    gj(window).scrollTop(0).scrollLeft(0);
    this.viewImage = true;
  };
  
  UIAnswersPortlet.prototype.loadActionScroll = function () {
    var container = gj('#UIQuestions');
    if (container.length) {
      this.loadScroll('UIQuestions', container, this.initActionScroll);
    }
  };
  
  UIAnswersPortlet.prototype.loadScroll = function (scrollname, container, callback) {
    var controlButtonContainer = gj('td.ControlButtonContainer:first');
    if (container.length && controlButtonContainer.length) {
      this.scrollMgr[scrollname] = new ScrollManager(scrollname);
      this.scrollMgr[scrollname].initFunction = callback;
      this.scrollMgr[scrollname].mainContainer = controlButtonContainer;
      this.scrollMgr[scrollname].answerLoadItems('ControlButton');
      if (this.scrollMgr[scrollname].elements.length <= 0) return;
      this.scrollMgr[scrollname].arrowsContainer = controlButtonContainer.find('div.ScrollButtons:first');
      var button = this.scrollMgr[scrollname].arrowsContainer.find('div');
  
      if (button.length >= 2) {
        this.scrollMgr[scrollname].initArrowButton(button.eq(0), 'left', 'ScrollLeftButton', 'HighlightScrollLeftButton', 'DisableScrollLeftButton');
        this.scrollMgr[scrollname].initArrowButton(button.eq(1), 'right', 'ScrollRightButton', 'HighlightScrollRightButton', 'DisableScrollRightButton');
      }
  
      this.scrollMgr[scrollname].callback = this.scrollCallback;
      this.scrollManagerLoaded = true;
      callback();
    }
  };
  
  UIAnswersPortlet.prototype.initActionScroll = function () {
    if (gj('#UIPortalApplication').css('display') == 'none') return;
    this.scrollMgr['UIQuestions'].init();
    this.scrollMgr['UIQuestions'].checkAvailableSpace();
    this.scrollMgr['UIQuestions'].renderElements();
  };
  
  UIAnswersPortlet.prototype.loadBreadcumbScroll = function () {
    var container = gj('#UIBreadcumbs');
    if (container.length) {
      this.loadScroll('UIBreadcumbs', container, this.initBreadcumbScroll);
    }
  };
  
  UIAnswersPortlet.prototype.initBreadcumbScroll = function () {
    if (gj('#UIPortalApplication').css('display') == 'none') return;
    this.scrollMgr['UIBreadcumbs'].init();
    this.scrollMgr['UIBreadcumbs'].checkAvailableSpace();
    if (this.scrollMgr['UIBreadcumbs'].arrowsContainer) {
      this.scrollMgr['UIBreadcumbs'].renderElements();
    }
  };
  
  UIAnswersPortlet.prototype.scrollCallback = function () {};
  
  UIAnswersPortlet.prototype.onClickSlidebarButton = function () {
    var workspaceContainer = document.getElementById('UIWorkspaceContainer');
    if (workspaceContainer) {
      if (workspaceContainer.style.display === 'none') {
        setTimeout(eXo.answer.UIAnswersPortlet.reSizeImages, 500);
      }
    }
  };
  
  UIAnswersPortlet.prototype.reSizeImagesView = function () {
    setTimeout('eXo.answer.UIAnswersPortlet.setSizeImages(10, "SetWidthImageContent")', 1000);
  };
  
  UIAnswersPortlet.prototype.reSizeImages = function () {
    eXo.answer.UIAnswersPortlet.setSizeImages(10, 'SetWidthContent');
  };
  
  UIAnswersPortlet.prototype.setSizeImages = function (delta, classParant) {
    var widthContent = document.getElementById(classParant);
    if (widthContent) {
      var isDesktop = document.getElementById('UIPageDesktop');
      if (!isDesktop) {
        var max_width = widthContent.offsetWidth - delta;
        var max = max_width;
        if (max_width > 600) max = 600;
        var images_ = widthContent.getElementsByTagName("img");
        for (var i = 0; i < images_.length; i++) {
          var className = String(images_[i].className);
          if (className.indexOf("FAQAvatar") >= 0 || className.indexOf("AttachmentFile") >= 0) {
            continue;
          }
          var img = new Image();
          img.src = images_[i].src;
          if (img.width > max) {
            images_[i].style.width = max + "px";
            images_[i].style.height = "auto";
          } else {
            images_[i].style.width = "auto";
            if (images_[i].width > max) {
              images_[i].style.width = max + "px";
              images_[i].style.height = "auto";
            }
          }
          if (img.width > 600) {
            images_[i].onclick = eXo.answer.UIAnswersPortlet.showImage;
          }
        }
      }
    }
  };
  
  UIAnswersPortlet.prototype.showImage = function () {
    eXo.answer.UIAnswersPortlet.showPicture(this.src);
  };
  
  UIAnswersPortlet.prototype.FAQChangeHeightToAuto = function () {
    var object = document.getElementById("UIFAQPopupWindow");
    if (object) {
      var popupWindow = eXo.core.DOMUtil.findFirstDescendantByClass(object, "div", "PopupContent");
      popupWindow.style.height = "auto";
      popupWindow.style.maxHeight = "500px";
    }
  };
  
  UIAnswersPortlet.prototype.initContextMenu = function (id) {
    var cont = document.getElementById(id);
    if(cont) {
      eXo.answer.UIAnswersPortlet.disableContextMenu(cont);
      var uiContextMenu = eXo.forum.UIContextMenu;
      if (!uiContextMenu.classNames) uiContextMenu.classNames = new Array("FAQCategory", "QuestionContextMenu");
      else {
        uiContextMenu.classNames.push("FAQCategory");
        uiContextMenu.classNames.push("QuestionContextMenu");
      }
      uiContextMenu.setContainer(cont);
      uiContextMenu.setup();
    }
  };
  
  UIAnswersPortlet.prototype.setSelectboxOnchange = function (fid) {
    if (!eXo.ks.Browser.isFF()) return;
    var form = document.getElementById(fid);
    var select = eXo.core.DOMUtil.findFirstDescendantByClass(form, "select", "selectbox");
    if (select) {
      var onchange = select.getAttribute("onchange");
      onchange = onchange.replace("javascript:", "javascript:eXo.answer.UIAnswersPortlet.setDisableSelectbox(this);");
      select.setAttribute("onchange", onchange);
    }
  };
  
  UIAnswersPortlet.prototype.setDisableSelectbox = function (selectbox) {
    selectbox.disabled = true;
  };
  
  UIAnswersPortlet.prototype.voteAnswerUpDown = function (imageId, isVote) {
    var obj = document.getElementById(imageId);
    if (isVote) {
      obj.style.filter = " alpha(opacity: 100)";
      obj.style.MozOpacity = "1";
    } else {
      obj.style.filter = " alpha(opacity: 70)";
      obj.style.MozOpacity = "0.7";
    }
  };
  
  UIAnswersPortlet.prototype.openDiscussLink = function (link) {
    link = link.replace(/&amp;/g, "&");
    window.open(link);
  };
  
  UIAnswersPortlet.prototype.executeLink = function (evt) {
    var onclickAction = String(this.getAttribute("actions"));
    eval(onclickAction);
    eXo.forum.ForumUtils.cancelEvent(evt);
    return false;
  };
  
  
  UIAnswersPortlet.prototype.createLink = function (cpId, isAjax) {
    if (!isAjax || isAjax === 'false') return;
    var comp = document.getElementById(cpId);
    var uiCategoryTitle = eXo.core.DOMUtil.findDescendantsByClass(comp, "a", "ActionLink");
    var i = uiCategoryTitle.length;
    if (!i || (i <= 0)) return;
    while (i--) {
      uiCategoryTitle[i].onclick = this.executeLink;
    }
  };
  
  UIAnswersPortlet.prototype.showTreeNode = function (obj, isShow) {
    if (isShow === "false") return;
    var DOMUtil = eXo.core.DOMUtil;
    var parentNode = DOMUtil.findAncestorByClass(obj, "ParentNode");
    var nodes = DOMUtil.findChildrenByClass(parentNode, "div", "Node");
    var selectedNode = DOMUtil.findAncestorByClass(obj, "Node");
    var nodeSize = nodes.length;
    var childrenContainer = null;
    for (var i = 0; i < nodeSize; i++) {
      childrenContainer = DOMUtil.findFirstDescendantByClass(nodes[i], "div", "ChildNodeContainer");
      if (nodes[i] === selectedNode) {
        childrenContainer.style.display = "block";
        nodes[i].className = "Node SmallGrayPlus";
      } else {
        childrenContainer.style.display = "none";
        if (nodes[i].className === "Node SmallGrayPlus false") continue;
        nodes[i].className = "Node SmallGrayMinus";
      }
    }
  };
  
  UIAnswersPortlet.prototype.submitSearch = function (id) {
    var parentElm = document.getElementById(id);
    if (parentElm) {
      parentElm.onkeydown = eXo.answer.UIAnswersPortlet.submitOnKey;
    }
  };
  
  UIAnswersPortlet.prototype.submitOnKey = function (event) {
    var key = eXo.forum.ForumUtils.getKeynum(event);
    if (key == 13) {
      var searchLinkElm = eXo.core.DOMUtil.findFirstDescendantByClass(this, "a", "ActionSearch");
      if (searchLinkElm) {
        var link = String(searchLinkElm.href);
        link = link.replace("javascript:", "");
        eval(link);
        eXo.forum.ForumUtils.cancelEvent(event);
        return false;
      }
    }
  };
  
  ScrollManager.prototype.answerLoadItems = function (elementClass, clean) {
    if (clean) this.cleanElements();
    this.elements.clear();
    this.elements.pushAll(eXo.ks.KSUtils.findDescendantsByClass(this.mainContainer, elementClass).reverse());
  };
  
  // Expose
  window.eXo = eXo || {};
  window.eXo.answer = eXo.answer || {} ;
  window.eXo.answer.UIAnswersPortlet = new UIAnswersPortlet();

})(gj, window, document);


eXo.answer.DragDrop = {
  DOMUtil : eXo.core.DOMUtil,
  dragObject: null,
  targetClass: [],
  init: function (compid) {
    var comp = document.getElementById(compid);
    var elements = this.DOMUtil.findDescendantsByClass(comp, "div", "FAQCategory");
    var i = elements.length;
    while (i--) {
      elements[i].onmousedown = this.attach;
    }
    eXo.ks.KSUtils.addEv(comp, 'onselectstart', eXo.ks.KSUtils.returnFalse);
    eXo.ks.KSUtils.addEv(comp, 'ondragstart', eXo.ks.KSUtils.returnFalse);
  },
  attach: function (evt) {
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
      x: evt.clientX,
      y: evt.clientY
    };
    dnd.setup(dragObject, ["FAQCategory", "FAQBack", "FAQTmpCategory"]);
    dnd.dropCallback = function (dragObj, target) {
      this.DOMUtil.removeElement(dragObj);
      if (this.lastTarget) this.lastTarget.style.border = "";
      if (target && dnd.isMoved) {
        var action = this.getAction(this.dragObject, target);
        if (!action) {
          this.showElement();
          return;
        }
        eval(action);

      } else this.showElement();
    }
    dnd.dragCallback = function (dragObj, target) {
      if (dnd.lastTarget) {
        dnd.lastTarget.style.border = "";
        if (this.DOMUtil.hasClass(dnd.lastTarget, "FAQHighlightCategory")) this.DOMUtil.replaceClass(dnd.lastTarget, "FAQHighlightCategory", "");
      }
      if (!target) return;
      dnd.lastTarget = target;
      if (this.DOMUtil.hasClass(target, "FAQBack")) target.onclick();
      if (this.DOMUtil.hasClass(target, "FAQTmpCategory")) this.DOMUtil.addClass(dnd.lastTarget, "FAQHighlightCategory");
      target.style.border = "dotted 1px #cccccc";
      if (!dnd.hided) dnd.hideElement(dnd.rootNode);

    }
  },

  setup: function (dragObject, targetClass) {
    this.dragObject = dragObject;
    this.targetClass = targetClass;
    document.onmousemove = eXo.answer.DragDrop.onDrag;
    document.onmouseup = eXo.answer.DragDrop.onDrop;
  },

  onDrag: function (evt) {
    var dnd = eXo.answer.DragDrop;
    var dragObject = dnd.dragObject;
    dragObject.style.left = eXo.ks.Browser.findMouseXInPage(evt) + 2 + "px";
    dragObject.style.top = eXo.ks.Browser.findMouseYInPage(evt) + 2 + "px";
    if (dnd.dragCallback) {
      var target = dnd.findTarget(evt);
      dnd.dragCallback(dragObject, target);
    }
  },

  onDrop: function (evt) {
    evt = evt || window.event;
    var dnd = eXo.answer.DragDrop;
    dnd.isMoved = true;
    if (dnd.mousePos.x == evt.clientX && dnd.mousePos.y == evt.clientY) dnd.isMoved = false;
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

  findTarget: function (evt) {
    var targetClass = eXo.answer.DragDrop.targetClass;
    var i = targetClass.length;
    while (i--) {
      var target = eXo.forum.EventManager.getEventTargetByClass(evt, targetClass[i]);
      if (target) return target;
    }
  },
  hideElement: function (obj) {
    var preElement = this.DOMUtil.findPreviousElementByTagName(obj, "div");
    preElement.style.display = "none";
    obj.style.display = "none";
    this.hided = true;
  },
  showElement: function () {
    var dnd = eXo.answer.DragDrop;
    if (!dnd.rootNode) return;
    var preElement = this.DOMUtil.findPreviousElementByTagName(dnd.rootNode, "div");
    if (preElement) preElement.style.display = "";
    dnd.rootNode.style.display = "";
    if (dnd.lastTarget) {
      dnd.lastTarget.style.border = "";
      if (this.DOMUtil.hasClass(dnd.lastTarget, "FAQHighlightCategory")) this.DOMUtil.replaceClass(dnd.lastTarget, "FAQHighlightCategory", "");
    }
  },
  getAction: function (obj, target) {
    var info = this.DOMUtil.findFirstDescendantByClass(obj, "input", "InfoCategory");
    if (this.DOMUtil.hasClass(target, "FAQTmpCategory")) {
      var preElement = this.DOMUtil.findPreviousElementByTagName(target, "div");
      var top = " ";
      if (!preElement) {
        preElement = this.DOMUtil.findNextElementByTagName(target, "div");
        top = "top";
      }
      var preElementInfo = this.DOMUtil.findFirstDescendantByClass(preElement, "input", "InfoCategory");
      if (info.id == preElementInfo.id) return false;
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
