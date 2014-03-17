/*
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
 */
package org.exoplatform.forum.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.MembershipEntry;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class UserHelper {
  
  public static OrganizationService getOrganizationService() {
    return CommonUtils.getComponent(OrganizationService.class);
  }
  
  public static UserHandler getUserHandler() {
    return getOrganizationService().getUserHandler();
  }
  
  public static GroupHandler getGroupHandler() {
    return getOrganizationService().getGroupHandler();
  }

  public static MembershipHandler getMembershipHandler() {
    return getOrganizationService().getMembershipHandler();
  }

  public static List<Group> getAllGroup() throws Exception {
    Collection<Group> pageList = getGroupHandler().getAllGroups() ;
    List<Group> list = new ArrayList<Group>(pageList) ;
    return list;
  }

  public static String checkValueUser(String values) throws Exception {
    StringBuilder errorUser = new StringBuilder();
    if (values != null && values.trim().length() > 0) {
      String[] userIds = values.split(",");
      for (String str : userIds) {
        str = str.trim();
        if (str.indexOf("$") >= 0) str = str.replace("$", "&#36");

        if (str.indexOf("/") >= 0) {
          if (!UserHelper.hasGroupIdAndMembershipId(str)) {
            if (errorUser.length() == 0) errorUser.append(str);
            else errorUser.append(", ").append(str);
          }
        } else {// user
          if ((getUserHandler().findUserByName(str) == null)) {
            if (errorUser.length() == 0) errorUser.append(str);
            else errorUser.append(", ").append(str);
          }
        }
      }
    }
    return errorUser.toString();
  }

  public static boolean hasGroupIdAndMembershipId(String str) throws Exception {
    if(str.indexOf(":") >= 0) { //membership
      String[] array = str.split(":") ;
      try {
        getGroupHandler().findGroupById(array[1]).getId() ;
      }catch (Exception e) {
        return false ;
      }
      if(array[0].length() == 1 && array[0].charAt(0) == '*') {
        return true ;
      }else if(array[0].length() > 0){
        if(getOrganizationService().getMembershipTypeHandler().findMembershipType(array[0])== null) return false ;
      }else return false ;
    }else { //group
      try {
        getGroupHandler().findGroupById(str).getId() ;
      }catch (Exception e) {
        return false ;
      }
    }
    return true ;
  }

  public static boolean hasUserInGroup(String groupId, String userId) throws Exception {
    ListAccess<User> pageList = getUserHandler().findUsersByGroupId(groupId);
    int size = pageList.getSize();
    User[] users = pageList.load(0, size);

    for (User user : users) {
      if (user.getUserName().equals(userId)) {
        return true;
      }
    }

    return false;
  }

  public static User getUserByUserId(String userId) throws Exception {
    return getUserHandler().findUserByName(userId) ;
  }
  
  /**
   * Check user disable on system or not.
   *  
   * @param userName The user name 
   * @return
   */
  public static boolean isDisabledUser(String userName) {
    try {
      User user = getUserByUserId(userName);
      return (user == null || !user.isEnabled());
    } catch (Exception e) {
      return true;
    }
  }
  
  public static String[] getUserGroups() throws Exception {
    Object[] objGroupIds = getGroupHandler().findGroupsOfUser(UserHelper.getCurrentUser()).toArray();
    String[] groupIds = new String[objGroupIds.length];
    for (int i = 0; i < groupIds.length; i++) {
      groupIds[i] = ((GroupImpl) objGroupIds[i]).getId();
    }
    return groupIds;
  }

  public static List<String> getAllGroupId() throws Exception {
    List<String> grIds = new ArrayList<String>();
    for (Group gr : getAllGroup()) {
      grIds.add(gr.getId());
    }
    return grIds;
  }

  
  public static List<Group> findGroups(Group group) throws Exception {
    return (List<Group>) getGroupHandler().findGroups(group);
  }
  
  public static boolean isAnonim() {
    String userId = UserHelper.getCurrentUser();
    if (userId == null)
      return true;
    return false;
  }
  
  public static Collection<Membership> findMembershipsByUser(String userId) {
    try {
      return getMembershipHandler().findMembershipsByUser(userId);
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  /**
   * 
   * @param userId username
   * @return list of groups an user belong, and memberships of the user in each group. If userId is null, groups and memberships of the current
   * user will be returned.
   * @throws Exception
   */
  public static List<String> getAllGroupAndMembershipOfUser(String userId) {
    List<String> listOfUser = new ArrayList<String>();
    if (userId == null || userId.equals(getCurrentUser())) {
      ConversationState conversionState = ConversationState.getCurrent();
      Identity identity = conversionState.getIdentity();
      userId = identity.getUserId();
      if (userId != null) {
        listOfUser.add(userId);
        for (MembershipEntry membership : identity.getMemberships()) {
          listOfUser.add(membership.getGroup()); // its groups
          listOfUser.add(membership.getMembershipType() + ":" + membership.getGroup()); // its memberships
        }
      }
    } else {
      listOfUser.add(userId); // himself
      Collection<Membership> memberships = findMembershipsByUser(userId);
      for (Membership membership : memberships) {
        listOfUser.add(membership.getGroupId()); // its groups
        listOfUser.add(membership.getMembershipType() + ":" + membership.getGroupId()); // its memberships
      }
    }
    return listOfUser;
  }

  static public String getEmailUser(String userName) throws Exception {
    User user = getUserHandler().findUserByName(userName) ;
    String email = user.getEmail() ;
    return email;
  }

  static public String getCurrentUser() {
    try {
      ConversationState conversionState = ConversationState.getCurrent();
      Identity identity = conversionState.getIdentity();
      String userId = identity.getUserId();
      if (userId == null || IdentityConstants.ANONIM.equals(userId)) {
        return Util.getPortalRequestContext().getRemoteUser();
      }
      return userId;
    } catch (Exception e) {
      return null;
    }
  }
  
  /**
   * Get the display name of user or group or membership
   * 
   * @param owner The id of user or group or membership
   * @return The String value
   * @throws Exception
   */
  public static String getDisplayNameOfOwner(String owner) throws Exception {
    if (CommonUtils.isEmpty(owner) == true) {
      return CommonUtils.EMPTY_STR;
    }

    if (hasGroupIdAndMembershipId(owner)) {
      if (owner.contains(CommonUtils.COLON)) {
        String membership = owner.substring(0, owner.indexOf(CommonUtils.COLON));
        String groupId = owner.substring(membership.length() + 1);
        Group group = getGroupHandler().findGroupById(groupId);
        if (group != null) {
          return membership + " in " + group.getGroupName();
        }
      }
      Group group = getGroupHandler().findGroupById(owner);
      return (group != null) ? group.getGroupName() : CommonUtils.EMPTY_STR;
    } else {
      User user = getUserHandler().findUserByName(owner, UserStatus.BOTH);
      if (user != null) {
        String displayName = user.getDisplayName();
        if (CommonUtils.isEmpty(displayName) || owner.equals(displayName)) {
          displayName = user.getFirstName() + CommonUtils.SPACE + user.getLastName();
        }
        return displayName;
      }
    }
    return CommonUtils.EMPTY_STR;
  }

}
