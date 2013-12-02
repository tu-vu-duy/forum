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
import java.util.List;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.common.webui.BaseEventListener;
import org.exoplatform.forum.common.webui.UIPopupAction;
import org.exoplatform.forum.common.webui.UIPopupContainer;
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.ForumSearchResult;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIModerationForum.gtmpl",
    events = {
      @EventConfig(listeners = UIModerationForum.OpenActionListener.class),
      @EventConfig(listeners = UIModerationForum.CloseActionListener.class, phase=Phase.DECODE)
    }
)
public class UIModerationForum extends BaseForumForm implements UIPopupComponent {
  private String[]            path            = new String[] {};

  List<ForumSearchResult>           list_;

  private boolean             isShowIter      = true;

  private boolean             isReloadPortlet = false;

  public final String         SEARCH_ITERATOR = "moderationIterator";

  private JCRPageList         pageList;

  private UIForumPageIterator pageIterator;

  public UIModerationForum() throws Exception {
    pageIterator = addChild(UIForumPageIterator.class, null, SEARCH_ITERATOR);
    setActions(new String[] { "Close" });
  }

  public void activate() {
  }

  public void deActivate() {
  }

  public void setReloadPortlet(boolean isReloadPortlet) {
    this.isReloadPortlet = isReloadPortlet;
  }

  public void setUserProfile(UserProfile userProfile) throws Exception {
    this.userProfile = userProfile;
    if (this.userProfile == null) {
      this.userProfile = getAncestorOfType(UIForumPortlet.class).getUserProfile();
    }
  }

  public String[] getPath() {
    if (userProfile.getUserRole() <= 1) {
      if (userProfile.getUserRole() == 1) {
        path = this.userProfile.getModerateForums();
      } else
        path = new String[] {};
    }
    return path;
  }

  public void setPath(String[] path) {
    this.path = path;
  }

  public boolean getIsShowIter() {
    return isShowIter;
  }

  @SuppressWarnings("unchecked")
  protected List<ForumSearchResult> getListObject() throws Exception {
    try {
      list_ = getForumService().getJobWattingForModerator(getPath());
    } catch (Exception e) {
      list_ = new ArrayList<ForumSearchResult>();
      log.error("list of forum search must not null: ", e);
    }
    pageList = new ForumPageList(10, list_.size());
    pageList.setPageSize(10);
    pageIterator.initPage(10, pageList.getCurrentPage(),
                          pageList.getAvailable(), pageList.getAvailablePage());
    isShowIter = true;
    if (pageList.getAvailablePage() <= 1)
      isShowIter = false;
    int pageSelect = pageIterator.getPageSelected();
    List<ForumSearchResult> list = new ArrayList<ForumSearchResult>();
    list.addAll(pageList.getPageSearch(pageSelect, list_));
    return list;
  }

  private ForumSearchResult getObject(String id) throws Exception {
    for (ForumSearchResult obj : list_) {
      if (obj.getId().equals(id))
        return obj;
    }
    return null;
  }

  static public class OpenActionListener extends BaseEventListener<UIModerationForum> {
    public void onEvent(Event<UIModerationForum> event, UIModerationForum moderationForum, final String objectId) throws Exception {
      ForumSearchResult forumSearch = moderationForum.getObject(objectId);
      UIPopupContainer popupContainer = moderationForum.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
      if (forumSearch.getType().equals(Utils.TOPIC)) {
        try {
          Topic topic = moderationForum.getForumService().getTopicByPath(forumSearch.getPath(), false);
          UIViewTopic viewTopic = popupAction.activate(UIViewTopic.class, 700);
          viewTopic.setTopic(topic);
          viewTopic.setActionForm(new String[] { "Approve", "DeleteTopic", "Close" });
          event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
        } catch (Exception e) {
          moderationForum.log.warn("Failed to view topic: ", e);
        }
      } else {
        try {
          Post post = moderationForum.getForumService().getPost(ForumUtils.EMPTY_STR, ForumUtils.EMPTY_STR, ForumUtils.EMPTY_STR, forumSearch.getPath());
          UIViewPost viewPost = popupAction.activate(UIViewPost.class, 700);
          viewPost.setPostView(post);
          viewPost.setViewUserInfo(false);
          viewPost.setActionForm(new String[] { "Approve", "DeletePost", "Close", "OpenTopicLink" });
          event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
        } catch (Exception e) {
          moderationForum.log.warn("Failed to view post: ", e);
        }
      }
    }
  }

  static public class CloseActionListener extends EventListener<UIModerationForum> {
    public void execute(Event<UIModerationForum> event) throws Exception {
      UIModerationForum uiform = event.getSource();
      UIForumPortlet forumPortlet = uiform.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
      if (uiform.isReloadPortlet) {
        uiform.isReloadPortlet = false;
        event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
      }
    }
  }
}
