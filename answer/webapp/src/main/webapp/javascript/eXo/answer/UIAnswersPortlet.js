;(function($, window, document) {

  function UIAnswersPortlet() {
    this.viewImage = true;
    this.scrollManagerLoaded = false;
    this.hiddenMenu = true;
    this.scrollMgr = [];
    this.portletId = 'UIAnswersPortlet';
  };
  
  UIAnswersPortlet.prototype.init = function (portletId) {
    this.portletId = new String(portletId);
    this.updateContainersHeight();
    this.controlWorkSpace();
    this.disableContextMenu();
    eXo.core.Browser.addOnResizeCallback(portletId, this.resizeCallback)
    eXo.core.Browser.init();
  };

  UIAnswersPortlet.prototype.resizeCallback = function() {
    eXo.forum.ForumUtils.setMaskLayer(this.portletId);
  };
  
  UIAnswersPortlet.prototype.updateContainersHeight = function () {
    var viewQuestionContentEl = findId(this.portletId + ' div.CategoriesContainer div.ViewQuestionContent');
    viewQuestionContentEl.css('height', viewQuestionContentEl.height() - 67);
  };
  
  UIAnswersPortlet.prototype.controlWorkSpace = function () {
    $('#ControlWorkspaceSlidebar div.SlidebarButton').on('click', this.onClickSlidebarButton);
    setTimeout(this.reSizeImages, 1500);
  };
  
  UIAnswersPortlet.prototype.disableContextMenu = function () {
    var oncontextmenus = findId(this.portletId + ' .oncontextmenu');
    for (var i = 0; i < oncontextmenus.length; i++) {
      oncontextmenus.eq(i).on('contextmenu', function() {
        return false;
      });
    }
  };
  
  UIAnswersPortlet.prototype.selectCateInfor = function (number) {
    var obj = null;
    for (var i = 0; i < 3; i++) {
      obj = $('#uicategoriesCateInfors' + i);
      if (i == number) obj.css('fontWeight', 'bold');
      else obj.css('fontWeight', 'normal');
    }
  };
  
  UIAnswersPortlet.prototype.setCheckEvent = function (isCheck) {
    this.hiddenMenu = isCheck;
  };
  
  UIAnswersPortlet.prototype.viewTitle = function (id) {
    findId(id).css('display', 'block');
    this.hiddenMenu = false;
  };
  
  UIAnswersPortlet.prototype.hiddenTitle = function (id) {
    findId(id).css('display', 'none');
  };
  
  UIAnswersPortlet.prototype.hiddenMenu = function () {
    if (this.hiddenMenu) {
      this.hiddenTitle('FAQCategroManager');
      this.hiddenMenu = false;
    }
    setTimeout('eXo.answer.UIAnswersPortlet.checkAction()', 1000);
  };
  
  UIAnswersPortlet.prototype.checkAction = function () {
    if (this.hiddenMenu) {
      setTimeout('eXo.answer.UIAnswersPortlet.hiddenMenu()', 1500);
    }
  };
  
  UIAnswersPortlet.prototype.checkCustomView = function (isNotSpace, hideTitle, showTitle) {
    var cookie = eXo.core.Browser.getCookie('FAQCustomView');
    cookie = (cookie == 'none' || cookie == '' && isNotSpace == 'false') ? 'none' : '';
    $('#FAQViewCategoriesColumn').css('display', cookie);
    
    var title = $('#FAQTitlePanels');
    if (cookie == 'none') {
      $('#FAQCustomView').addClass('FAQCustomViewRight');
      title.attr('title', showTitle);
    } else {
      title.attr('title', hideTitle);
      cookie = 'block';
    }
    eXo.core.Browser.setCookie("FAQCustomView", cookie, 1);
  };
  
  UIAnswersPortlet.prototype.changeCustomView = function (change, hideTitle, showTitle) {
    var columnCategories = $('#FAQViewCategoriesColumn');
    var buttomView = $('#FAQCustomView');
    var title = $('#FAQTitlePanels');
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
    
    eXo.core.Browser.setCookie("FAQCustomView", cookie, 1);
    if ($.isFunction(this.initActionScroll)) this.initActionScroll();
    if ($.isFunction(this.initBreadCumbScroll)) this.initBreadCumbScroll();
  };
  
  UIAnswersPortlet.prototype.changeStarForVoteQuestion = function (i, id) {
    findId(id + i).attr('class', 'OverVote');
    
    for (var j = 0; j <= i; j++) {
      findId(id + j).attr('class', 'OverVote');
    }
    
    for (var j = i + 1; j < 5; j++) {
      obj = findId(id + j).attr('class', 'RatedVote');
    }
  };
  
  UIAnswersPortlet.prototype.jumToQuestion = function (id) {
    var viewContent = findId(id).parent('.ViewQuestionContent');
    if (viewContent.exists()) viewContent.scrollTop(viewContent.position().top);
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
    //this.elements.pushAll(eXo.ks.KSUtils.findDescendantsByClass(this.mainContainer, elementClass).reverse());
  };
  
  // Expose
  window.eXo = eXo || {};
  window.eXo.answer = eXo.answer || {} ;
  window.eXo.answer.UIAnswersPortlet = new UIAnswersPortlet();

})(gj, window, document);
