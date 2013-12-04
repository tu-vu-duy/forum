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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.filter.model.ForumFilter;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.input.UICheckBoxInput;

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/forum/webui/popup/UISelectItemForumForm.gtmpl",
  events = {
    @EventConfig(listeners = UISelectItemForum.AddActionListener.class), 
    @EventConfig(listeners = UISelectItemForum.CancelActionListener.class,phase = Phase.DECODE)
  }
)
public class UISelectItemForum extends BaseForumForm implements UIPopupComponent {

  private List<String>                     listIdIsSelected = new ArrayList<String>();

  private List<Category>                   categories       = new ArrayList<Category>();
  private Map<String, List<Forum>>         mapForums        = new HashMap<String, List<Forum>>();

  public UISelectItemForum() {
  }

  public void activate() {
  }

  public void deActivate() {
  }

  private boolean isInspace(String cateId) {
    return Utils.CATEGORY_SPACE_ID_PREFIX.equals(cateId);
  }

  public void initSelectForum(List<String> listIdIsSelected, String userId) throws Exception {
    this.listIdIsSelected = listIdIsSelected;
    categories = getForumService().getCategories();
    for (Category cat : categories) {
      if (isInspace(cat.getId()) == false) {
        List<Forum> forums = getForumService().getForums(new ForumFilter(cat.getId(), true).userId(userId));
        mapForums.put(cat.getId(), forums);
      }
    }
    initCheckboxInput();
  }
  
  private void initCheckboxInput() {
    for (String categoryId : mapForums.keySet()) {
      for (Forum forum : mapForums.get(categoryId)) {
        String inputId = categoryId + "_" + forum.getId();
        UICheckBoxInput checkbox = getUICheckBoxInput(inputId);
        if (checkbox == null) {
          checkbox = new UICheckBoxInput(inputId, inputId, false);
          addUIFormInput(checkbox);
        }
        checkbox.setChecked(getCheckedForum(forum.getId()));
      }
    }
  }
  

  protected List<Category> getCategories() throws Exception {
    return categories;
  }

  protected boolean getCheckedForum(String forumId) {
    return listIdIsSelected.contains(forumId) ? true : false;
  }

  protected List<Forum> getForums(String categoryId) {
    List<Forum> forums = mapForums.get(categoryId);
    return (forums != null) ? forums : new ArrayList<Forum>();
  }

  private Forum getForum(String inputId) throws Exception {
    String cateId = inputId.split("_")[0];
    for (Forum forum : getForums(cateId)) {
      if (inputId.indexOf(forum.getId()) > 0) {
        return forum;
      }
    }
    return null;
  }

  static public class AddActionListener extends EventListener<UISelectItemForum> {
    public void execute(Event<UISelectItemForum> event) throws Exception {
      UISelectItemForum uiForm = event.getSource();
      List<String> listIdSelected = new ArrayList<String>();
      List<UIComponent> children = uiForm.getChildren();
      for (UIComponent child : children) {
        if (child instanceof UICheckBoxInput) {
          if (((UICheckBoxInput) child).isChecked()) {
            Forum forum = uiForm.getForum(child.getId());
            if (forum != null) {
              listIdSelected.add(forum.getForumName() + "(" + Utils.getSubPath(forum.getPath()));
            }
          }
        }
      }
      UIModeratorManagementForm managementForm = uiForm.getAncestorOfType(UIForumPortlet.class).findFirstComponentOfType(UIModeratorManagementForm.class);
      managementForm.setModForunValues(listIdSelected);
      event.getRequestContext().addUIComponentToUpdateByAjax(managementForm);
      uiForm.cancelChildPopupAction();
    }
  }

  static public class CancelActionListener extends EventListener<UISelectItemForum> {
    public void execute(Event<UISelectItemForum> event) throws Exception {
      event.getSource().cancelChildPopupAction();
    }
  }
}
