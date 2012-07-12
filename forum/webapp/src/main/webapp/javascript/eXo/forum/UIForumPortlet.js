;
(function($, window, document) {
  
  var UIForumPortlet = {
    obj : null,
    event : null,
    wait : false,
    id : 'UIForumPortlet',
    
    init : function(id) {
      UIForumPortlet.id = id;
      var jportlet = findId(id);
      if (jportlet.exists()) {
        jportlet.find('.oncontextmenu').on('contextmenu', eXo.forum.ForumUtils.returnFalse);
      }
      eXo.core.Browser.addOnResizeCallback(id, UIForumPortlet.resizeCallback)
      eXo.core.Browser.init();
    },
    
    resizeCallback : function() {
      eXo.forum.ForumUtils.setMaskLayer(UIForumPortlet.id);
    },
    
    selectItem : function(obj) {
      var DOMUtil = eXo.core.DOMUtil;
      var tr = DOMUtil.findAncestorByTagName(obj, "tr");
      var table = DOMUtil.findAncestorByTagName(obj, "table");
      var tbody = DOMUtil.findAncestorByTagName(obj, "tbody");
      var checkbox = DOMUtil.findFirstDescendantByClass(table, "input", "checkbox");
      var checkboxes = DOMUtil.findDescendantsByClass(tbody, "input", "checkbox");
      var chklen = checkboxes.length;
      var j = 0;
      if (obj.checked) {
        if (!tr.getAttribute("tmpClass")) {
          tr.setAttribute("tmpClass", tr.className);
          tr.className = "SelectedItem";
        }
        for ( var i = 0; i < chklen; i++) {
          if (checkboxes[i].checked)
            j++;
          else
            break;
        }
        if (j == chklen)
          checkbox.checked = true;
      } else {
        if (tr.getAttribute("tmpClass")) {
          tr.className = tr.getAttribute("tmpClass");
          tr.removeAttribute("tmpClass");
        }
        checkbox.checked = false;
      }
      var modMenu = document.getElementById("ModerationMenu");
      if (modMenu) {
        var firstItem = modMenu.getElementsByTagName("a")[0];
        if (j >= 2) {
          if (!firstItem.getAttribute("oldClass")) {
            firstItem.setAttribute("oldHref", firstItem.href);
            firstItem.href = "javascript:void(0);";
            var parentIt = DOMUtil.findAncestorByClass(firstItem, "MenuItem");
            firstItem.setAttribute("oldClass", parentIt.className);
            parentIt.className = "DisableMenuItem";
          }
        } else {
          if (firstItem.getAttribute("oldClass")) {
            firstItem.href = firstItem.getAttribute("oldHref");
            var parentIt = DOMUtil.findAncestorByClass(firstItem, "DisableMenuItem");
            parentIt.className = firstItem.getAttribute("oldClass");
            firstItem.setAttribute("oldClass", "");
          }
        }
      }
    },
    
    numberIsCheckedForum : function(formName, checkAllName, multiAns, onlyAns, notChecked) {
      var total = 0;
      var form = document.forms[formName];
      if (form) {
        var checkboxs = form.elements;
        for ( var i = 0; i < checkboxs.length; i++) {
          if (checkboxs[i].type == "checkbox" && checkboxs[i].checked && checkboxs[i].name != "checkAll") {
            total = total + 1;
          }
        }
      }
      if (total > 1) {
        var text = String(multiAns).replace("?", "").replace('{0}', total) + " ?";
        return confirm(text);
      } else if (total == 1) {
        return confirm(String(onlyAns).replace("?", "") + " ?");
      } else {
        alert(notChecked);
        return false;
      }
    },
    
    checkedPost : function(elm) {
      if (elm) {
        UIForumPortlet.setChecked(elm.checked);
      }
    },
    
    setChecked : function(isChecked) {
      var divChecked = document.getElementById('divChecked');
      var check = 0;
      check = eval(divChecked.getAttribute("checked"));
      if (isChecked)
        divChecked.setAttribute("checked", (check + 1));
      else
        divChecked.setAttribute("checked", (check - 1));
    },
    
    OneChecked : function(formName) {
      var form = document.forms[formName];
      if (form) {
        var checkboxs = form.elements;
        for ( var i = 0; i < checkboxs.length; i++) {
          if (checkboxs[i].checked) {
            return true;
          }
        }
      }
      return false;
    },
    
    numberIsChecked : function(formName, checkAllName, multiAns, onlyAns, notChecked) {
      var divChecked = document.getElementById('divChecked');
      var total = 0;
      total = eval(divChecked.getAttribute("checked"));
      if (total > 1) {
        var text = String(multiAns);
        return confirm(text.replace('{0}', total));
      } else if (total == 1) {
        return confirm(onlyAns);
      } else {
        alert(notChecked);
        return false;
      }
    },
    
    checkAll : function(obj) {
      var DOMUtil = eXo.core.DOMUtil;
      var thead = DOMUtil.findAncestorByTagName(obj, "thead");
      var tbody = DOMUtil.findNextElementByTagName(thead, "tbody");
      var checkboxes = DOMUtil.findDescendantsByClass(tbody, "input", "checkbox");
      var len = checkboxes.length;
      if (obj.checked) {
        for ( var i = 0; i < len; i++) {
          if (!checkboxes[i].checked)
            UIForumPortlet.setChecked(true);
          checkboxes[i].checked = true;
          this.selectItem(checkboxes[i]);
        }
      } else {
        for ( var i = 0; i < len; i++) {
          if (checkboxes[i].checked)
            UIForumPortlet.setChecked(false);
          checkboxes[i].checked = false;
          this.selectItem(checkboxes[i]);
        }
      }
    },
    
    // DungJs
    checkAction : function(obj, evt) {
      UIForumPortlet.showPopup(obj, evt);
      var uiCategory = document.getElementById("UICategory");
      var DOMUtil = eXo.core.DOMUtil;
      var checkboxes = DOMUtil.findDescendantsByClass(uiCategory, "input", "checkbox");
      var uiRightClickPopupMenu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "ul", "UIRightClickPopupMenu");
      if (uiRightClickPopupMenu == null) {
        uiRightClickPopupMenu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIRightClickPopupMenu");
      }
      var clen = checkboxes.length;
      var menuItems = uiRightClickPopupMenu.getElementsByTagName("a");
      var mlen = menuItems.length;
      var checked = false;
      for ( var i = 1; i < clen; i++) {
        if (checkboxes[i].checked && checkboxes[i].name.indexOf("forum") == 0) {
          checked = true;
          break;
        }
      }
      var j = 0;
      for ( var i = 0; i < mlen; i++) {
        if (String(menuItems[i].className).indexOf("AddForumIcon") > 0) {
          j = i + 1;
          break;
        }
      }
      for ( var n = j; n < mlen; n++) {
        if (!checked) {
          if (!menuItems[n].getAttribute("tmpHref")) {
            menuItems[n].setAttribute("tmpHref", menuItems[n].href);
            menuItems[n].href = "javascript:void(0);";
            menuItems[n].setAttribute("tmpClass", menuItems[n].parentNode.className);
            menuItems[n].parentNode.className = "DisableMenuItem";
          }
        } else {
          if (menuItems[n].getAttribute("tmpHref")) {
            menuItems[n].href = menuItems[n].getAttribute("tmpHref");
            menuItems[n].parentNode.className = menuItems[n].getAttribute("tmpClass");
            menuItems[n].removeAttribute("tmpHref");
            menuItems[n].removeAttribute("tmpClass");
          }
        }
      }
    },
    
    visibleAction : function(id) {
      var parent = document.getElementById(id);
      var DOMUtil = eXo.core.DOMUtil;
      var addCategory = DOMUtil.findFirstDescendantByClass(parent, "div", "AddCategory");
      if (!addCategory)
        return;
      var addForum = DOMUtil.findFirstDescendantByClass(parent, "div", "AddForum");
      var isIE = document.all ? true : false;
      if (document.getElementById("UICategories")) {
        addCategory.className = "Icon AddCategory";
        addForum.className = "Icon AddForum";
      } else if (document.getElementById("UICategory")) {
        addCategory.className = "Icon AddCategory DisableAction";
        addForum.className = "Icon AddForum";
        if (isIE)
          addCategory.firstChild.href = "javascript:void(0);";
        else
          addCategory.childNodes[1].href = "javascript:void(0);";
      } else {
        addCategory.className = "Icon AddCategory DisableAction";
        addForum.className = "Icon AddForum DisableAction";
        if (isIE) {
          addCategory.firstChild.href = "javascript:void(0);";
          addForum.firstChild.href = "javascript:void(0);";
        } else {
          addCategory.childNodes[1].href = "javascript:void(0);";
          addForum.childNodes[1].href = "javascript:void(0);";
        }
      }
    },
    
    checkActionTopic : function(obj, evt) {
      UIForumPortlet.showPopup(obj, evt);
      var DOMUtil = eXo.core.DOMUtil;
      var parentMenu = document.getElementById("ModerationMenu");
      var menuItems = parentMenu.getElementsByTagName("a");
      var parentContent = document.getElementById("UITopicContent");
      var checkBoxs = DOMUtil.findDescendantsByClass(parentContent, "input", "checkbox");
      var clen = checkBoxs.length;
      var mlen = menuItems.length;
      var divChecked = document.getElementById('divChecked');
      var j = 0;
      j = eval(divChecked.getAttribute("checked"));
      for ( var i = 1; i < clen; i++) {
        if (checkBoxs[i].checked) {
          j = 1;
          break;
        }
      }
      if (j === 0) {
        for ( var k = 0; k < mlen; k++) {
          if (menuItems[k].className === "ItemIcon SetUnWaiting")
            break;
          if (!menuItems[k].getAttribute("tmpClass")) {
            menuItems[k].setAttribute("tmpHref", menuItems[k].href);
            menuItems[k].href = "javascript:void(0);";
            var parentIt = DOMUtil.findAncestorByClass(menuItems[k], "MenuItem");
            menuItems[k].setAttribute("tmpClass", parentIt.className);
            parentIt.className = "DisableMenuItem";
            parentIt.onclick = eXo.forum.ForumUtils.cancelEvent;
          }
        }
      } else {
        for ( var n = 0; n < mlen; n++) {
          if (menuItems[n].getAttribute("tmpClass")) {
            var parent = DOMUtil.findAncestorByClass(menuItems[n], "DisableMenuItem");
            if (parent)
              parent.className = menuItems[n].getAttribute("tmpClass");
            menuItems[n].href = menuItems[n].getAttribute("tmpHref");
            menuItems[n].removeAttribute("tmpClass");
            menuItems[n].removeAttribute("tmpHref");
          }
        }
      }
    },
    
    expandCollapse : function(obj) {
      var forumToolbar = eXo.core.DOMUtil.findAncestorByClass(obj, "ForumToolbar");
      var contentContainer = eXo.core.DOMUtil.findNextElementByTagName(forumToolbar, "div");
      if (!contentContainer) {
        contentContainer = eXo.core.DOMUtil.findNextElementByTagName(forumToolbar, "ul");
      }
      if (contentContainer.style.display != "none") {
        contentContainer.style.display = "none";
        obj.className = "ExpandButton";
        obj.setAttribute("title", obj.getAttribute("expand"));
        forumToolbar.style.borderBottom = "solid 1px #b7b7b7";
      } else {
        contentContainer.style.display = "block";
        obj.className = "CollapseButton";
        obj.setAttribute("title", obj.getAttribute("collapse"));
        forumToolbar.style.borderBottom = "none";
      }
    },
    
    // Edit by Duy Tu 14-11-07
    showTreeNode : function(obj, isShow) {
      if (isShow === "false")
        return;
      var DOMUtil = eXo.core.DOMUtil;
      var parentNode = DOMUtil.findAncestorByClass(obj, "ParentNode");
      var nodes = DOMUtil.findChildrenByClass(parentNode, "div", "Node");
      var selectedNode = DOMUtil.findAncestorByClass(obj, "Node");
      var nodeSize = nodes.length;
      var childrenContainer = null;
      for ( var i = 0; i < nodeSize; i++) {
        childrenContainer = DOMUtil.findFirstDescendantByClass(nodes[i], "div", "ChildNodeContainer");
        if (nodes[i] === selectedNode) {
          childrenContainer.style.display = "block";
          nodes[i].className = "Node SmallGrayPlus";
        } else {
          childrenContainer.style.display = "none";
          if (nodes[i].className === "Node SmallGrayPlus false")
            continue;
          nodes[i].className = "Node SmallGrayMinus";
        }
      }
    },
    
    checkedNode : function(elm) {
      var input = elm.getElementsByTagName("input")[0];
      var DOMUtil = eXo.core.DOMUtil;
      var parentNode = DOMUtil.findAncestorByClass(input, "Node");
      var containerChild = DOMUtil.findFirstDescendantByClass(parentNode, "div", "ChildNodeContainer");
      if (containerChild) {
        var checkboxes = containerChild.getElementsByTagName("input");
        for ( var i = 0; i < checkboxes.length; ++i) {
          if (input.checked)
            checkboxes[i].checked = true;
          else
            checkboxes[i].checked = false;
        }
      }
    },
    
    checkedChildNode : function(elm) {
      var input = elm.getElementsByTagName("input")[0];
      if (input) {
        if (input.checked) {
          var DOMUtil = eXo.core.DOMUtil;
          var parentCheckBoxNode = elm.parentNode.parentNode.parentNode;
          var parentCheckBox = DOMUtil.findFirstDescendantByClass(parentCheckBoxNode, "div", "ParentCheckBox");
          var parentInput = parentCheckBox.getElementsByTagName("input")[0];
          if (!parentInput.checked)
            parentInput.checked = true;
        }
      }
    },
    
    initVote : function(voteId, rate) {
      var vote = document.getElementById(voteId);
      var DOMUtil = eXo.core.DOMUtil;
      vote.rate = rate = parseInt(rate);
      var optsContainer = DOMUtil.findFirstDescendantByClass(vote, "div", "OptionsContainer");
      var options = DOMUtil.getChildrenByTagName(optsContainer, "div");
      for ( var i = 0; i < options.length - 1; i++) {
        options[i].onmouseover = UIForumPortlet.overVote;
        options[i].onblur = UIForumPortlet.overVote;
        if (i < rate)
          options[i].className = "RatedVote";
      }
      vote.onmouseover = UIForumPortlet.parentOverVote;
      vote.onblur = UIForumPortlet.parentOverVote;
      optsContainer.onmouseover = eXo.forum.ForumUtils.cancelEvent;
      optsContainer.onblur = eXo.forum.ForumUtils.cancelEvent;
    },
    
    parentOverVote : function(event) {
      var optsCon = eXo.core.DOMUtil.findFirstDescendantByClass(this, "div", "OptionsContainer");
      var opts = eXo.core.DOMUtil.getChildrenByTagName(optsCon, "div");
      for ( var j = 0; j < opts.length - 1; j++) {
        if (j < this.rate)
          opts[j].className = "RatedVote";
        else
          opts[j].className = "NormalVote";
      }
    },
    
    overVote : function(event) {
      var optsCont = eXo.core.DOMUtil.findAncestorByClass(this, "OptionsContainer");
      var opts = eXo.core.DOMUtil.getChildrenByTagName(optsCont, "div");
      var i = opts.length - 1;
      for (--i; i >= 0; i--) {
        if (opts[i] == this)
          break;
        opts[i].className = "NormalVote";
      }
      if (opts[i].className == "OverVote")
        return;
      for (; i >= 0; i--) {
        opts[i].className = "OverVote";
      }
    },
    
    showPopup : function(elm, e) {
      var strs = [ '#goPageBottom', '#SearchForm', '.CancelEvent' ];
      for ( var t = 0; t < strs.length; t++) {
        var jelm = $(strs[t]);
        if (jelm.exists()) {
          jelm.on('click', eXo.forum.ForumUtils.cancelEvent);
        }
      }
      eXo.webui.UIPopupSelectCategory.show(elm, e);
      eXo.forum.ForumUtils.cancelEvent(e);
      eXo.forum.ForumUtils.addhideElement($(elm).find('div.UIPopupCategory'));
    },
    
    goLastPost : function(idLastPost) {
      var isDesktop = document.getElementById('UIPageDesktop');
      if (isDesktop === null) {
        if (idLastPost === "top") {
          var body = document.getElementsByTagName('body')[0];
          if (body.scrollTop > 250) {
            script: scroll(0, 0);
            var viewPage = document.getElementById('KSMaskLayer');
            if (viewPage)
              viewPage.scrollIntoView(true);
          }
        } else {
          var obj = document.getElementById(idLastPost);
          if (obj)
            obj.scrollIntoView(true);
        }
      }
    },
    
    setEnableInput : function() {
      var parend = document.getElementById("ForumUserBan");
      var DOMUtil = eXo.core.DOMUtil;
      if (parend) {
        var obj = eXo.core.DOMUtil.findFirstDescendantByClass(parend, "input", "checkbox");
        if (obj) {
          document.getElementById("BanCounter").disabled = "disabled";
          document.getElementById("BanReasonSummary").readonly = "readonly";
          document.getElementById("CreatedDateBan").disabled = "disabled";
          var selectbox = DOMUtil.findFirstDescendantByClass(parend, "select", "selectbox");
          if (!obj.checked) {
            selectbox.disabled = "disabled";
            var banReason = document.getElementById("BanReason");
            banReason.disabled = "disabled";
          }
          $(obj).on('click', function() {
            if (!obj.checked) {
              selectbox.disabled = "disabled";
              banReason.disabled = "disabled";
            } else {
              selectbox.disabled = "";
              banReason.disabled = "";
            }
          });
        }
      }
    },
    
    hidePicture : function() {
      eXo.ks.Browser.onScrollCallback.remove('MaskLayerControl');
      var maskContent = eXo.core.UIMaskLayer.object;
      var maskNode = document.getElementById("MaskLayer") || document.getElementById("subMaskLayer");
      if (maskContent)
        eXo.core.DOMUtil.removeElement(maskContent);
      if (maskNode)
        eXo.core.DOMUtil.removeElement(maskNode);
    },
    
    showPicture : function(src) {
      eXo.forum.MaskLayerControl.showPicture(src);
    },
    
    getImageSize : function(imageNode) {
      var tmp = imageNode.cloneNode(true);
      tmp.style.visibility = "hidden";
      document.body.appendChild(tmp);
      var size = {
        width : tmp.offsetWidth,
        height : tmp.offsetHeight
      }
      eXo.core.DOMUtil.removeElement(tmp);
      return size;
    },
    
    showFullScreen : function(imageNode, containerNode) {
      var imageSize = this.getImageSize(imageNode);
      var widthMax = eXo.ks.Browser.getBrowserWidth();
      if ((imageSize.width + 40) > widthMax) {
        containerNode.style.width = widthMax + "px";
        imageNode.width = (widthMax - 40);
        imageNode.style.height = "auto";
      }
    },
    
    setDisableTexarea : function() {
      var objCmdElm = document.getElementById('moderationOptions');
      var input = eXo.core.DOMUtil.findFirstDescendantByClass(objCmdElm, "input", "checkbox");
      if (input) {
        if (input.name === "AutoAddEmailNotify") {
          UIForumPortlet.onClickDisableTexarea();
          input.onclick = UIForumPortlet.onClickDisableTexarea;
        }
      }
    },
    
    onClickDisableTexarea : function() {
      var objCmdElm = document.getElementById('moderationOptions');
      var input = eXo.core.DOMUtil.findFirstDescendantByClass(objCmdElm, "input", "checkbox");
      if (objCmdElm) {
        var texares = objCmdElm.getElementsByTagName("textarea");
        for ( var i = 0; i < texares.length; ++i) {
          if (texares[i].name === "NotifyWhenAddTopic" || texares[i].name === "NotifyWhenAddPost") {
            if (!input.checked) {
              texares[i].readOnly = false;
            } else {
              texares[i].readOnly = true;
            }
          }
        }
      }
    },
    
    setDisableInfo : function() {
      var strs = new Array("CanPost", "CanView");
      for ( var i = 0; i < strs.length; i++) {
        var elm = document.getElementById(strs[i]);
        if (elm === null)
          return;
        UIForumPortlet.setShowInfo(elm);
        elm.onkeyup = function() {
          UIForumPortlet.setShowInfo(this);
        }
      }
    },
    
    setShowInfo : function(elm) {
      var info = document.getElementById(elm.id + "Info");
      if (elm.value === '') {
        info.style.display = 'block';
      } else {
        info.style.display = 'none';
      }
    },
    
    controlWorkSpace : function() {
      var slidebar = document.getElementById('ControlWorkspaceSlidebar');
      if (slidebar) {
        var slidebarButton = eXo.core.DOMUtil.findFirstDescendantByClass(slidebar, "div", "SlidebarButton");
        if (slidebarButton) {
          slidebarButton.onclick = UIForumPortlet.onClickSlidebarButton;
        }
      }
      setTimeout(UIForumPortlet.reSizeImages, 1500);
    },
    onClickSlidebarButton : function() {
      var workspaceContainer = document.getElementById('UIWorkspaceContainer');
      if (workspaceContainer) {
        if (workspaceContainer.style.display === 'none') {
          setTimeout(eXo.forum.UIForumPortlet.reSizeImages, 500);
        }
      }
    },
    reSizeImgViewPost : function() {
      setTimeout('eXo.forum.UIForumPortlet.setSizeImages(10, "SizeImage")', 1000);
    },
    reSizeImgViewTopic : function() {
      setTimeout('eXo.forum.UIForumPortlet.setSizeImages(225, "SizeImage")', 1000);
    },
    reSizeImages : function() {
      setTimeout('eXo.forum.UIForumPortlet.setSizeImages(225, "UITopicDetail")', 500);
    },
    
    reSizeImagesInMessageForm : function() {
      if (eXo.ks.Browser.isIE6())
        setTimeout('eXo.forum.UIForumPortlet.setSizeImages(130, "UIViewPrivateMessageForm")', 800);
      else
        setTimeout('eXo.forum.UIForumPortlet.setSizeImages(10, "UIViewPrivateMessageForm")', 400);
    },
    
    setSizeImages : function(delta, classParant) {
      var parent_ = document.getElementById(classParant);
      var imageContentContainer = eXo.core.DOMUtil.findFirstDescendantByClass(parent_, "div", "ImageContentContainer");
      if (imageContentContainer) {
        var isDesktop = document.getElementById('UIPageDesktop');
        if (!isDesktop) {
          var max_width = imageContentContainer.offsetWidth - delta;
          var max = max_width;
          if (max_width > 600)
            max = 600;
          var images_ = imageContentContainer.getElementsByTagName("img");
          for ( var i = 0; i < images_.length; i++) {
            var className = String(images_[i].className);
            if (className.indexOf("ImgAvatar") >= 0 || className.indexOf("AttachImage") >= 0) {
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
              images_[i].onclick = UIForumPortlet.showImage;
            }
          }
        }
      }
    },
    
    showImage : function() {
      UIForumPortlet.showPicture(this.src);
    },
    
    resetFielForm : function(idElm) {
      var elm = document.getElementById(idElm);
      var inputs = elm.getElementsByTagName("input");
      if (inputs) {
        for ( var i = 0; i < inputs.length; i++) {
          if (inputs[i].type === "checkbox")
            inputs[i].checked = false;
          else if (eXo.core.DOMUtil.hasClass(inputs[i], "UISliderInput"))
            eXo.webui.UISliderControl.reset(inputs[i]);
          else
            inputs[i].value = "";
        }
      }
      var textAres = elm.getElementsByTagName("textarea");
      if (textAres) {
        for ( var i = 0; i < textAres.length; i++) {
          textAres[i].value = "";
        }
      }
    },
    
    RightClickBookMark : function(elmId) {
      var ancestor = document.getElementById(elmId);
      var DOMUtil = eXo.core.DOMUtil;
      var popupContents = DOMUtil.findDescendantsByClass(ancestor, "ul", "ClickPopupContent");
      if (popupContents == null)
        return;
      var popupContainer = document.getElementById('RightClickContainer');
      if (popupContainer == null)
        return;
      var itemmenuBookMark = DOMUtil.findFirstDescendantByClass(popupContainer, "a", "AddLinkToBookIcon");
      var itemmenuWatching = DOMUtil.findFirstDescendantByClass(popupContainer, "a", "AddWatchingIcon");
      var itemmenuRSS = DOMUtil.findFirstDescendantByClass(popupContainer, "a", "ForumRSSFeed");
      if (itemmenuWatching == null || itemmenuBookMark == null)
        return;
      var labelWatchings = String(itemmenuWatching.innerHTML).split(";");
      for ( var i = 0; i < popupContents.length; i++) {
        var action = popupContents[i].getAttribute('title');
        if (action.indexOf(";") < 0) {
          itemmenuBookMark.href = action;
          itemmenuWatching.parentNode.style.display = "none";
        } else {
          var actions = action.split(";");
          itemmenuBookMark.href = actions[0];
          if (actions[1].toLowerCase().indexOf("unwatch") >= 0) {
            if (actions[1].indexOf("unwatch,") >= 0) {
              actions[1] = actions[1].replace('unwatch,', '');
            }
            itemmenuWatching.innerHTML = labelWatchings[1];
          } else {
            itemmenuWatching.innerHTML = labelWatchings[0];
          }
          itemmenuWatching.href = actions[1];
          if (itemmenuRSS) {
            if (actions.length == 3) {
              var link = actions[2].substring(0, actions[2].indexOf(','));
              var action = actions[2].substring(actions[2].indexOf(',') + 1);
              itemmenuRSS.href = "javascript:window.open('" + link + "'); " + action + ";";// link,action
              itemmenuRSS.parentNode.style.display = "block";
            } else {
              itemmenuRSS.parentNode.style.display = "none";
            }
          }
          itemmenuWatching.parentNode.style.display = "block";
        }
        popupContents[i].removeAttribute('title');
        popupContents[i].innerHTML = popupContainer.innerHTML;
      }
    },
    
    ReloadImage : function() {
      if (eXo.ks.Browser.isIE6()) {
        var aImage = document.getElementsByTagName("img");
        var length = aImage.length;
        for ( var i = 0; i < length; ++i) {
          aImage[i].src = aImage[i].src;
          if (aImage[i].width > 590)
            aImage[i].width = 590 + "px";
        }
      }
    },
    
    shareLink : function(obj) {
      var shareLinkContainer = document.getElementById("popupShareLink");
      if (shareLinkContainer.style.display != "none")
        shareLinkContainer.style.display = "none";
      else
        shareLinkContainer.style.display = "block";
    },
    
    closeShareLink : function(obj) {
      var popup = eXo.core.DOMUtil.findAncestorByClass(obj, "UIPopupWindow");
      popup.style.display = "none";
    },
    
    loadScroll : function(e) {
      var uiNav = eXo.forum.UIForumPortlet;
      var container = document.getElementById("UIForumActionBar");
      if (container) {
        uiNav.scrollMgr = new ScrollManager("UIForumActionBar");
        uiNav.scrollMgr.initFunction = uiNav.initScroll;
        uiNav.scrollMgr.mainContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "td", "ControlButtonContainer");
        uiNav.scrollMgr.arrowsContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "ScrollButtons");
        uiNav.scrollMgr.loadElements("ControlButton", true);
        
        var button = eXo.core.DOMUtil.findDescendantsByTagName(uiNav.scrollMgr.arrowsContainer, "div");
        if (button.length >= 2) {
          uiNav.scrollMgr.initArrowButton(button[0], "left", "ScrollLeftButton", "HighlightScrollLeftButton", "DisableScrollLeftButton");
          uiNav.scrollMgr.initArrowButton(button[1], "right", "ScrollRightButton", "HighlightScrollRightButton", "DisableScrollRightButton");
        }
        
        uiNav.scrollManagerLoaded = true;
        uiNav.initScroll();
        var lastButton = document.getElementById('OpenBookMarkSp');
        if (lastButton && lastButton.style.display == "none") {
          document.getElementById('OpenBookMark').style.display = "none";
        }
      }
    },
    
    initScroll : function() {
      var uiNav = eXo.forum.UIForumPortlet;
      if (!uiNav.scrollManagerLoaded)
        uiNav.loadScroll();
      var elements = uiNav.scrollMgr.elements;
      uiNav.scrollMgr.init();
      if (eXo.ks.Browser.isIE6())
        uiNav.scrollMgr.arrowsContainer.setAttribute("space", 35);
      uiNav.scrollMgr.checkAvailableSpace();
      uiNav.scrollMgr.renderElements();
    },
    
    loadTagScroll : function() {
      var uiNav = eXo.forum.UIForumPortlet;
      var container = document.getElementById("TagContainer");
      if (container) {
        uiNav.tagScrollMgr = new ScrollManager("TagContainer");
        uiNav.tagScrollMgr.initFunction = uiNav.initTagScroll;
        uiNav.tagScrollMgr.mainContainer = container;
        uiNav.tagScrollMgr.arrowsContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "li", "ScrollButtons");
        uiNav.tagScrollMgr.loadItems("MenuItem", true);
        
        var button = eXo.core.DOMUtil.findDescendantsByTagName(uiNav.tagScrollMgr.arrowsContainer, "div");
        if (button.length >= 2) {
          uiNav.tagScrollMgr.initArrowButton(button[0], "left", "ScrollLeftButton", "HighlightScrollLeftButton", "DisableScrollLeftButton");
          uiNav.tagScrollMgr.initArrowButton(button[1], "right", "ScrollRightButton", "HighlightScrollRightButton", "DisableScrollRightButton");
        }
        
        uiNav.scrollManagerLoaded = true;
        uiNav.initTagScroll();
        
      }
    },
    
    initTagScroll : function() {
      var uiNav = eXo.forum.UIForumPortlet;
      var elements = uiNav.tagScrollMgr.elements;
      var menu = eXo.core.DOMUtil.findFirstDescendantByClass(uiNav.tagScrollMgr.arrowsContainer, "ul", "UIRightPopupMenuContainer");
      var tmp = null;
      uiNav.setTagContainerWidth(uiNav.tagScrollMgr.mainContainer);
      uiNav.tagScrollMgr.init();
      uiNav.tagScrollMgr.checkAvailableSpace();
      
      removeChildren(menu);
      uiNav.tagScrollMgr.arrowsContainer.onmouseover = over;
      uiNav.tagScrollMgr.arrowsContainer.onfocus = over;
      uiNav.tagScrollMgr.arrowsContainer.onmouseout = out;
      uiNav.tagScrollMgr.arrowsContainer.onblur = out;
      for ( var i = 0; i < elements.length; i++) {
        if (elements[i].isVisible) {
          elements[i].style.display = "block";
        } else {
          tmp = elements[i].cloneNode(true);
          eXo.core.DOMUtil.replaceClass(tmp, "FloatLeft", "TagItem");
          tmp.style.display = "block";
          menu.appendChild(tmp);
          elements[i].style.display = "none";
          uiNav.tagScrollMgr.arrowsContainer.style.display = "block";
        }
      }
      
      setPosition(menu);
      function removeChildren(cont) {
        var firstChild = eXo.core.DOMUtil.findFirstChildByClass(cont, "div", "MenuTagContainer")
        eXo.core.DOMUtil.removeElement(firstChild);
      }
      
      function setPosition(menu) {
        var uiPopupCategory = eXo.core.DOMUtil.findAncestorByClass(menu, "UIPopupCategory");
        uiPopupCategory.style.display = "block";
        // var posX = 24*2 - uiPopupCategory.offsetWidth ;
        uiPopupCategory.style.top = "24px";
        uiPopupCategory.style.left = "-400px";// posX + "px";
        uiPopupCategory.style.display = "none";
      }
      
      function over() {
        eXo.core.DOMUtil.addClass(this, "ScrollButtonsOver");
      }
      
      function out() {
        eXo.core.DOMUtil.replaceClass(this, "ScrollButtonsOver", "");
      }
    },
    
    setTagContainerWidth : function(container) {
      var nodes = eXo.core.DOMUtil.getChildrenByTagName(container.parentNode, "div");
      var width = 0;
      var i = nodes.length;
      while (i--) {
        if ((nodes[i].className == container.className) || !nodes[i].className)
          continue;
        if (nodes[i].className == "UIForumPageIterator") {
          var right = eXo.core.DOMUtil.findFirstDescendantByClass(nodes[i], "div", "RightPageIteratorBlock");
          var left = eXo.core.DOMUtil.findFirstDescendantByClass(nodes[i], "div", "LeftPageIteratorBlock");
          width += getWidth(left, "div") + getWidth(right, "a");
          continue;
        }
        width += UIForumPortlet.tagScrollMgr.getElementSpace(nodes[i]);
      }
      width = UIForumPortlet.tagScrollMgr.getElementSpace(container.parentNode) - width - 15;
      container.style.width = width + "px";
      // Private method to get real width of the element by html tag name
      function getWidth(obj, tag) {
        if (!obj)
          return 0;
        var children = eXo.core.DOMUtil.findDescendantsByTagName(obj, tag);
        var w = 0;
        var i = children.length;
        while (i--) {
          w += children[i].offsetWidth;
        }
        return w;
      }
    },
    
    executeLink : function(evt) {
      var onclickAction = String(this.getAttribute("rel"));
      eval(onclickAction);
      eXo.forum.ForumUtils.cancelEvent(evt);
      return false;
    },
    
    createLink : function(cpId, isAjax) {
      if (!isAjax || isAjax === 'false') {
        var isM = document.getElementById("SetMode");
        if (isM && isM.innerHTML === 'true') {
          UIForumPortlet.addLink(cpId, "ActionIsMod");
        }
        return;
      }
      UIForumPortlet.addLink(cpId, "ActionLink");
    },
    
    addLink : function(cpId, clazzAction) {
      var comp = document.getElementById(cpId);
      var uiCategoryTitle = eXo.core.DOMUtil.findDescendantsByClass(comp, "a", clazzAction);
      var i = uiCategoryTitle.length;
      if (!i || (i <= 0))
        return;
      while (i--) {
        uiCategoryTitle[i].onclick = this.executeLink;
      }
    },
    
    setAutoScrollTable : function(idroot, idParent, idChild) {
      var rootEl = document.getElementById(idroot);
      var grid = document.getElementById(idChild);
      var tableContent = document.getElementById(idParent);
      var isIE = document.all ? true : false;
      if (isIE) {
        tableContent.style.width = "auto";
        grid.style.width = "auto";
      }
      if ((grid.offsetWidth + 10) >= (tableContent.offsetWidth)) {
        tableContent.style.paddingRight = "16px";
        tableContent.style.width = "auto";
      } else {
        tableContent.style.padding = "1px";
        tableContent.style.width = "100%";
        if (isIE) {
          rootEl.style.width = "96%";
          rootEl.style.margin = "auto";
        }
      }
      if (grid.offsetHeight > 260) {
        tableContent.style.height = "260px";
      } else {
        tableContent.style.height = "auto";
      }
    },
    
    initContextMenu : function(id) {
      var cont = document.getElementById(id);
      var uiContextMenu = eXo.forum.UIContextMenu;
      if (!uiContextMenu.classNames)
        uiContextMenu.classNames = new Array("ActionLink");
      else
        uiContextMenu.classNames.push("ActionLink");
      uiContextMenu.setContainer(cont);
      uiContextMenu.setup();
    },
    
    showBBCodeHelp : function(id, isIn) {
      var parentElm = document.getElementById(id);
      var popupHelp = document.getElementById(id + "ID");
      if (parentElm) {
        if (isIn == "true") {
          popupHelp.style.display = "block";
          var contentHelp = eXo.core.DOMUtil.findFirstDescendantByClass(popupHelp, "div", "ContentHelp");
          contentHelp.style.height = "auto";
          var l = String(contentHelp.innerHTML).length;
          if (l < 100) {
            contentHelp.style.width = (l * 4) + "px"
            contentHelp.style.height = "45px";
          } else {
            contentHelp.style.width = "400px"
            if (l > 150) {
              contentHelp.style.height = "auto";
            } else {
              contentHelp.style.height = "45px";
            }
          }
          var parPopup = document.getElementById("UIForumPopupWindow");
          var parPopup2 = document.getElementById("UIForumChildPopupWindow");
          var left = 0;
          var worksPace = document.getElementById('UIWorkingWorkspace');
          var worksPaceW = 1 * 1;
          if (worksPace) {
            worksPaceW = (worksPace.offsetWidth) * 1;
          } else {
            worksPaceW = (document.getElementById('UIPortalApplication').offsetWidth) * 1;
          }
          left = (parPopup.offsetLeft) * 1 + (parPopup2.offsetLeft) * 1 + parentElm.offsetLeft + parentElm.parentNode.offsetLeft;
          if (left + popupHelp.offsetWidth > worksPaceW) {
            popupHelp.style.left = "-" + (contentHelp.offsetWidth + 18) + "px";
            popupHelp.className = "RightBBCodeHelpPopup";
          } else {
            popupHelp.className = "LeftBBCodeHelpPopup";
            popupHelp.style.left = "-2px";
          }
        } else {
          popupHelp.style.display = "none";
        }
      }
    },
    
    submitSearch : function(id) {
      var parentElm = document.getElementById(id);
      if (parentElm) {
        parentElm.onkeydown = UIForumPortlet.submitOnKey;
      }
    },
    
    submitOnKey : function(event) {
      var key = eXo.forum.ForumUtils.getKeynum(event);
      if (key == 13) {
        var searchLinkElm = eXo.core.DOMUtil.findFirstDescendantByClass(this, "a", "SearchLink");
        if (searchLinkElm) {
          var link = String(searchLinkElm.href);
          link = link.replace("javascript:", "");
          eval(link);
          eXo.forum.ForumUtils.cancelEvent(event);
          return false;
        }
      }
    }
  };
  
  ScrollManager.prototype.loadItems = function(elementClass, clean) {
    if (clean)
      this.cleanElements();
    this.elements.clear();
    var items = eXo.core.DOMUtil.findDescendantsByClass(this.mainContainer, "li", elementClass);
    for ( var i = 0; i < items.length; i++) {
      this.elements.push(items[i]);
    }
  };
  
  window.eXo = window.eXo || {};
  window.eXo.forum = window.eXo.forum || {};
  window.eXo.forum.UIForumPortlet = UIForumPortlet;
})(gj, window, document);
