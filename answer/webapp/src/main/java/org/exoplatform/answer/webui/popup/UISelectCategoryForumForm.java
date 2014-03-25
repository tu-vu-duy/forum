/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.answer.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.answer.webui.FAQUtils;
import org.exoplatform.answer.webui.UIAnswersPortlet;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.forum.common.webui.UIPopupAction;
import org.exoplatform.forum.common.webui.UIPopupContainer;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.filter.model.ForumFilter;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/answer/webui/popup/UISelectCategoryForumForm.gtmpl", 
    events = {
        @EventConfig(listeners = UISelectCategoryForumForm.CloseActionListener.class, phase = Phase.DECODE), 
        @EventConfig(listeners = UISelectCategoryForumForm.AddCategoryActionListener.class, phase = Phase.DECODE) 
    }
)
public class UISelectCategoryForumForm extends UIForm implements UIPopupComponent {
  private ForumService   forumService;

  public UISelectCategoryForumForm() {
    forumService = CommonsUtils.getService(ForumService.class);
  }

  protected List<Forum> getForums(String categoryId) {
    if (FAQUtils.isFieldEmpty(categoryId) == false) {
      return forumService.getForums(new ForumFilter(categoryId, true).isPublic(true));
    }
    return new ArrayList<Forum>();
  }

  protected List<Category> getCategories() throws Exception {
    return forumService.getCategories();
  }

  public void activate() {
  }

  public void deActivate() {
  }

  private List<String> getPathName(String allPath) throws Exception {
    int t = allPath.indexOf(";");
    List<String> list = new ArrayList<String>();
    if (t > 0) {
      list.add(allPath.substring(0, t));
      list.add(allPath.substring(t + 1));
    }
    return list;
  }

  static public class CloseActionListener extends EventListener<UISelectCategoryForumForm> {
    public void execute(Event<UISelectCategoryForumForm> event) throws Exception {
      UISelectCategoryForumForm uiForm = event.getSource();
      try {
        UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
        UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      } catch (Exception e) {
        UIAnswersPortlet portlet = uiForm.getAncestorOfType(UIAnswersPortlet.class);
        portlet.cancelAction();
      }
    }
  }

  static public class AddCategoryActionListener extends EventListener<UISelectCategoryForumForm> {
    public void execute(Event<UISelectCategoryForumForm> event) throws Exception {
      UISelectCategoryForumForm uiForm = event.getSource();
      String allPath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAnswersPortlet portlet = uiForm.getAncestorOfType(UIAnswersPortlet.class);
      UIAnswerEditModeForm settingForm = portlet.findFirstComponentOfType(UIAnswerEditModeForm.class);
      settingForm.setPathCatygory(uiForm.getPathName(allPath));
      event.getRequestContext().addUIComponentToUpdateByAjax(settingForm.getChildById(UIAnswerEditModeForm.DISCUSSION_TAB));
      try {
        UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
        UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      } catch (Exception e) {
        portlet.cancelAction();
      }
    }
  }
}
