/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIWatchToolsForm.gtmpl",
    events = {
      @EventConfig(listeners = UIWatchToolsForm.DeleteEmailActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIWatchToolsForm.EditEmailActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIWatchToolsForm.CloseActionListener.class, phase=Phase.DECODE)
    }
)
public class UIWatchToolsForm extends BaseForumForm implements UIPopupComponent {
  public final String  WATCHTOOLS_ITERATOR  = "WatchToolsPageIterator";
  
  private final int PAGE_SIZE = 6;

  private String       path                = ForumUtils.EMPTY_STR;

  private String[]     emails              = new String[] {};

  private boolean      isTopic             = false;

  UIForumPageIterator  pageIterator;

  private List<String> listEmail           = new ArrayList<String>();

  private Log          log                 = ExoLogger.getLogger(UIWatchToolsForm.class);

  public UIWatchToolsForm() throws Exception {
    pageIterator = addChild(UIForumPageIterator.class, null, WATCHTOOLS_ITERATOR);
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public boolean getIsTopic() {
    return isTopic;
  }

  public void setIsTopic(boolean isTopic) {
    this.isTopic = isTopic;
  }

  public String[] getEmails() throws Exception {
    emails = getListEmail().toArray(new String[] {});
    return emails;
  }
  
  private int getMaxPage(int size){
    int p = size / PAGE_SIZE;
    if(size % PAGE_SIZE > 0 || p == 0) {
      p += 1;
    }
    return p;
  }

  public void setEmails(String[] emails) {
    setEmails(Arrays.asList(emails));
  }

  public void setEmails(List<String> emails) {
    listEmail.clear();
    listEmail.addAll(emails);
    initPageIterator();
  }

  private void initPageIterator() {
    int maxPage = getMaxPage(listEmail.size());
    pageIterator = this.getChild(UIForumPageIterator.class);
    pageIterator.initPage(PAGE_SIZE, 1, maxPage, getMaxPage(maxPage));
    if (maxPage <= 1) {
      pageIterator.setRendered(false);
    }
  }

  @SuppressWarnings("unchecked")
  public List<String> getListEmail() {
    return (List<String>) pageIterator.load(listEmail);
  }

  public void setUnWatchEmail(String[] emails, String unwatchEmail) {
    if (emails.length == 1) {
      setEmails(emails);
    } else if (emails.length > 1) {
      List<String> emails_ = new ArrayList<String>(Arrays.asList(emails));
      if (emails_.contains(unwatchEmail)) {
        emails_.remove(unwatchEmail);
      }
      setEmails(emails_);
    }
  }

  public void activate() {
  }

  public void deActivate() {
  }

  static public class DeleteEmailActionListener extends EventListener<UIWatchToolsForm> {
    public void execute(Event<UIWatchToolsForm> event) throws Exception {
      String email = event.getRequestContext().getRequestParameter(OBJECTID);
      UIWatchToolsForm uiForm = event.getSource();
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
      try {
        String path = uiForm.path;
        forumService.removeWatch(1, path, ForumUtils.SLASH + email);
        uiForm.listEmail.remove(email);
        uiForm.initPageIterator();
        if (uiForm.getIsTopic()) {
          UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class);
          topicDetail.setIsEditTopic(true);
          uiForm.isTopic = false;
          event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail);
        } else if (path.indexOf(Utils.CATEGORY) < path.lastIndexOf(Utils.FORUM)) {
          UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
          event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer);
        } else {
          event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet.findFirstComponentOfType(UICategory.class));
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
      } catch (Exception e) {
        forumPortlet.addMessage(new ApplicationMessage("UIWatchToolsForm.msg.fail-delete-email", null, ApplicationMessage.WARNING));
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);        
      }
    }
  }

  static public class EditEmailActionListener extends EventListener<UIWatchToolsForm> {
    public void execute(Event<UIWatchToolsForm> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }

  static public class CloseActionListener extends EventListener<UIWatchToolsForm> {
    public void execute(Event<UIWatchToolsForm> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }
}
