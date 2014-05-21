/*
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
 */
package org.exoplatform.faq.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.forum.common.UserHelper;
import org.exoplatform.forum.common.cache.model.key.SimpleCacheKey;
import org.exoplatform.forum.common.jcr.KSDataLocation;
import org.exoplatform.forum.common.jcr.SessionManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;

public class FAQServiceUtils {

  private static final String COLON = ":".intern();

  private static final String SLASH = "/".intern();

  /**
   * Is the expression a group expression
   * @param expr
   * @return
   */
  public static boolean isGroupExpression(String expr) {
    return ((expr.indexOf(SLASH) >= 0) && !(expr.indexOf(COLON) >= 0));
  }

  /**
   * Is the expression a membership expression (MEMBERSHIPTYPE:GROUP)
   * @param expr
   * @return
   */
  public static boolean isMembershipExpression(String expr) {
    return ((expr.indexOf(SLASH) >= 0) && (expr.indexOf(COLON) >= 0));
  }

  private static ListAccess<User> getUserByGroup(String group) {
    try {
      return UserHelper.getUserHandler().findUsersByGroupId(group, UserStatus.ENABLED);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Find usernames matching membership expressions
   * @param organizationService 
   * @param memberShip the membership, ex: member:/platform/users , *:/platform/users. 
   * @return list of users that mach at least one of the membership
   * @throws Exception
   */

  private static List<String> getUserByMembershipType(String memberShip) throws Exception {
    List<String> users = getFromCache(new String[] { memberShip });
    if (users != null) {
      return users;
    }
    users = new ArrayList<String>();
    String[] array = memberShip.trim().split(COLON);
    String groupId = array[1];
    String memberShipType = array[0];
    //
    if (MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.equals(memberShipType)) {
      users.addAll(getUserByGroupId(groupId));
    } else {
      MembershipHandler membershipHandler = UserHelper.getMembershipHandler();
      ListAccess<Membership> listAccess = membershipHandler.findAllMembershipsByGroup(UserHelper.getGroupHandler().findGroupById(groupId));

      Membership[] mbs = listAccess.load(0, listAccess.getSize());
      for (Membership mb : mbs) {
        if (!UserHelper.isDisabledUser(mb.getUserName()) && 
              (mb.getMembershipType().equals(memberShipType) || mb.getMembershipType().equals(MembershipTypeHandler.ANY_MEMBERSHIP_TYPE))) {
          users.add(mb.getUserName());
        }
      }
    } 
    //
    storeInCache(new String[] { memberShip }, users);
    return users;
  }

  /**
   * Find usernames matching group id expressions
   * @param organizationService 
   * @param groupId the group Id, ex: /platform/users . 
   * @return list of users that mach at least one of the group id
   * @throws Exception
   */
  private static List<String> getUserByGroupId(String groupId) throws Exception {
    List<String> users = getFromCache(new String[]{groupId});
    if (users != null) {
      return users;
    }
    users = new ArrayList<String>();
    ListAccess<User> pageList = getUserByGroup(groupId);
    if (pageList == null){
      return users;
    }
    User[] userArray = (User[]) pageList.load(0, pageList.getSize());
    for (int i = 0; i < pageList.getSize(); i++) {
      users.add(userArray[i].getUserName());
    }
    storeInCache(new String[]{groupId}, users);
    return users;
  }

  /**
   * Find usernames matching user, group or membership expressions
   * @param userGroupMembership list that may contain usernames or group names or membership expressions in the form MEMBERSHIPTYPE:GROUP
   * @return list of users that mach at least one of the userGroupMembership
   * @throws Exception
   */
  public static List<String> getUserPermission(String[] userGroupMembership) throws Exception {
    if (userGroupMembership == null || userGroupMembership.length <= 0 || (userGroupMembership.length == 1 && userGroupMembership[0].equals(" "))) {
      return new ArrayList<String>();
    }
    List<String> list = getFromCache(userGroupMembership);
    if (list != null) {
      return list;
    }
    Set<String> users = new HashSet<String>();
    for (int j = 0; j < userGroupMembership.length; j++) {
      String inputValue = userGroupMembership[j].trim();
      if (isMembershipExpression(inputValue)) {
        users.addAll(getUserByMembershipType(inputValue));
      } else if (isGroupExpression(inputValue)) {
        users.addAll(getUserByGroupId(inputValue));
      } else if (!UserHelper.isDisabledUser(inputValue)) {
        users.add(inputValue);
      }
    }
    storeInCache(userGroupMembership, new ArrayList<String>(users));
    return new ArrayList<String>(users);
  }

  /**
   * Store the list of user for the permission expressions in cache
   * @param userGroupMembership
   * @param users
   * @throws Exception
   */
  private static void storeInCache(String[] userGroupMembership, List<String> users) throws Exception {
    ExoCache<Serializable, List<String>> cache = getCache();
    Serializable cacheKey = getCacheKey(userGroupMembership);
    cache.put(cacheKey, users);
  }

  /**
   * Load a list of user for the permission expressions in cache
   * @param userGroupMembership
   * @return
   * @throws Exception
   */
  private static List<String> getFromCache(String[] userGroupMembership) throws Exception {
    if (userGroupMembership == null || userGroupMembership.length == 0 || (userGroupMembership.length == 1 && userGroupMembership[0].length() == 0))
      return null;
    ExoCache<Serializable, List<String>> cache = getCache();
    Serializable cacheKey = getCacheKey(userGroupMembership);
    return cache.get(cacheKey);
  }

  private static SimpleCacheKey getCacheKey(String[] userGroupMembership) {
    StringBuilder sb = new StringBuilder();
    for (String item : userGroupMembership) {
      sb.append("#").append(item);
    }
    return new SimpleCacheKey(sb.toString());
  }

  private static ExoCache<Serializable, List<String>> getCache() throws Exception {
    CacheService cacheService = CommonsUtils.getService(CacheService.class);
    return cacheService.getCacheInstance("user.PermissionCache");
  }

  /**
   * Repare permission of node
   * @param node  Node which is repared permission
   * @param owner  permission will be added for this node
   * @throws Exception
   */
  public static void reparePermissions(Node node, String owner) throws Exception {
    ExtendedNode extNode = (ExtendedNode) node;
    if (extNode.canAddMixin("exo:privilegeable"))
      extNode.addMixin("exo:privilegeable");
    String[] arrayPers = { PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE };
    extNode.setPermission(owner, arrayPers);
    List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries();
    for (AccessControlEntry accessControlEntry : permsList) {
      extNode.setPermission(accessControlEntry.getIdentity(), arrayPers);
    }
  }

  public static SessionManager getSessionManager() {
    KSDataLocation location = CommonsUtils.getService(KSDataLocation.class);
    return location.getSessionManager();
  }
}
