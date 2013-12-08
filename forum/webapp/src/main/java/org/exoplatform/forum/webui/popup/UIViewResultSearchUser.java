package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.forum.common.webui.UIPopupAction;
import org.exoplatform.forum.common.webui.UIPopupContainer;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.webui.application.WebuiRequestContext;
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
    template = "app:/templates/forum/webui/popup/UIViewResultSearchUser.gtmpl",
    events = {
      @EventConfig(listeners = UIViewResultSearchUser.ViewUserActionListener.class),
      @EventConfig(listeners = UIViewResultSearchUser.CloseActionListener.class,phase = Phase.DECODE)
    }
)

public class UIViewResultSearchUser extends UIForm implements UIPopupComponent {

  private List<UserProfile> userProfiles        = new ArrayList<UserProfile>();

  private JCRPageList       pageList;

  private static String     FORUM_PAGE_ITERATOR = "ForumUserPageIterator";

  private String[]          permissionUser      = null;

  private long              totalPage           = 0;

  public UIViewResultSearchUser() throws Exception {
    addChild(UIForumPageIterator.class, null, FORUM_PAGE_ITERATOR);
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    permissionUser = new String[] { res.getString("UIForumPortlet.label.PermissionAdmin").toLowerCase(), res.getString("UIForumPortlet.label.PermissionModerator").toLowerCase(), res.getString("UIForumPortlet.label.PermissionUser").toLowerCase(), res.getString("UIForumPortlet.label.PermissionGuest").toLowerCase() };
    this.setActions(new String[] { "Close" });
  }

  public void setPageListSearch(JCRPageList pageList) {
    this.pageList = pageList;
    this.pageList.setPageSize(5);
    this.getChild(UIForumPageIterator.class).initPage(5, pageList.getCurrentPage(),
                                                      pageList.getAvailable(), pageList.getAvailablePage());
  }

  @SuppressWarnings("unchecked")
  protected List<UserProfile> getListUserProfile() throws Exception {
    UIForumPageIterator pageIterator = this.getChild(UIForumPageIterator.class);
    int page = pageIterator.getPageSelected();
    List<UserProfile> listUserProfile = pageList.getPage(page);
    pageIterator.setSelectPage(pageList.getCurrentPage());
    if (listUserProfile == null)
      listUserProfile = new ArrayList<UserProfile>();
    this.userProfiles = new ArrayList<UserProfile>();
    for (UserProfile userProfile : listUserProfile) {
      this.userProfiles.add(userProfile);
    }
    return this.userProfiles;
  }

  public void activate() {
  }

  public void deActivate() {
  }

  static public class ViewUserActionListener extends EventListener<UIViewResultSearchUser> {
    public void execute(Event<UIViewResultSearchUser> event) throws Exception {
      UIViewResultSearchUser uiForm = event.getSource();
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      UIModeratorManagementForm moderatorManagementForm = popupContainer.getChild(UIModeratorManagementForm.class);
      moderatorManagementForm.setValueSearch(userId);
      popupContainer.getChild(UIPopupAction.class).deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }

  static public class CloseActionListener extends EventListener<UIViewResultSearchUser> {
    public void execute(Event<UIViewResultSearchUser> event) throws Exception {
      UIViewResultSearchUser uiForm = event.getSource();
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      popupContainer.getChild(UIPopupAction.class).deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }

  public String[] getPermissionUser() {
    return permissionUser;
  }

  public void setPermissionUser(String[] permissionUser) {
    this.permissionUser = permissionUser;
  }

  public long getTotalPage() {
    return totalPage;
  }

  public void setTotalPage(long totalPage) {
    this.totalPage = totalPage;
  }
}
