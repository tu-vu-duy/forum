/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.forum.service;

import org.exoplatform.forum.base.BaseForumServiceTestCase;
import org.exoplatform.forum.common.UserHelper;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;

public class UserProfileTestCase extends BaseForumServiceTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();

  }
  
  @Override
  public void tearDown() throws Exception {
    //
    super.tearDown();
  }

  public void testUserProfile() throws Exception {
    // create user profile
    String userName = "user_bar";
    UserHandler userHandler = UserHelper.getUserHandler();
    User user = userHandler.createUserInstance(userName);
    user.setEmail("user_bar@plf.com");
    user.setFirstName(userName);
    user.setLastName(userName);
    user.setPassword("exo");
    //
    userHandler.createUser(user, true);

    // getUserInfo
    UserProfile userProfile = forumService_.getUserInfo(userName);
    assertNotNull("Get info UserProfile is null", userProfile);

    // get Default and storage this profile in ExoCache
    userProfile = forumService_.getDefaultUserProfile(userName, "");
    assertNotNull("Get default UserProfile is null", userProfile);

    // test cache user profile, get this profile is not null
    assertNotNull("Get default UserProfile is null", CacheUserProfile.getFromCache(userName));
    // remove this profile in ExoCache by update this profile
    forumService_.saveUserSettingProfile(userProfile);
    // get by ExoCache is null
    assertNull("Get default UserProfile is null", CacheUserProfile.getFromCache(userName));

    // getUserInformations
    userProfile = forumService_.getUserInformations(userProfile);
    assertNotNull("Get informations UserProfile is null", userProfile);

    // getUserSettingProfile
    userProfile = forumService_.getUserSettingProfile(userName);
    assertNotNull("Get Setting UserProfile is not null", userProfile);

    // saveUserSettingProfile
    assertEquals("Default AutoWatchMyTopics is false", userProfile.getIsAutoWatchMyTopics(), false);
    userProfile.setIsAutoWatchMyTopics(true);
    forumService_.saveUserSettingProfile(userProfile);
    userProfile = forumService_.getUserSettingProfile(userName);
    assertEquals("Edit AutoWatchMyTopics and can't save this property. AutoWatchMyTopics is false", userProfile.getIsAutoWatchMyTopics(), true);
  }

  public void testUserLogin() throws Exception {
    // Add user login
    forumService_.userLogin(USER_ROOT);
    forumService_.userLogin(USER_JOHN);
    forumService_.userLogin(USER_DEMO);

    // Get all user online:
    assertEquals("Get all user online", 3, forumService_.getOnlineUsers().size());

    // isOnline
    assertEquals("John is not Online", forumService_.isOnline(USER_JOHN), true);
    // get Last Login
    assertEquals("Demo can't last Login", forumService_.getLastLogin(), USER_DEMO);
    // userLogout
    forumService_.userLogout(USER_DEMO);
    assertEquals("Demo is online", forumService_.isOnline(USER_DEMO), false);
  }
  
  public void testSearchUserProfile() throws Exception {
    //
    int numberUser = 20;
    // create user
    UserHandler userHandler = UserHelper.getUserHandler();
    for (int i = 0; i < numberUser; i++) {
      User user = userHandler.createUserInstance("foo" + i);
      user.setEmail("foo" + i + "@plf.com");
      user.setFirstName("test");
      user.setLastName("abc user");
      user.setPassword("exo");
      //
      userHandler.createUser(user, true);
    }
    int allSize = UserHelper.getUserHandler().findAllUsers().getSize();
    
    // check get all user profile (8 users create by OrganizationDatabaseInitializer)
    assertEquals(allSize, forumService_.getPageListUserProfile().getAvailable());
    assertEquals(20, forumService_.searchUserProfile("foo").getAvailable());
    assertEquals(false, forumService_.getQuickProfile("foo0").isDisabled());
    // Disable 10 users
    for (int i = 0; i < 10; i++) {
      userHandler.setEnabled("foo" + i, false, true);
    }
    //
    assertEquals(allSize - 10, forumService_.getPageListUserProfile().getAvailable());
    assertEquals(10, forumService_.searchUserProfile("foo").getAvailable());
    assertEquals(true, forumService_.getQuickProfile("foo0").isDisabled());
    // Enable 10 users
    for (int i = 0; i < 10; i++) {
      userHandler.setEnabled("foo" + i, true, true);
    }
    //
    assertEquals(allSize, forumService_.getPageListUserProfile().getAvailable());
    assertEquals(20, forumService_.searchUserProfile("foo").getAvailable());
    assertEquals(false, forumService_.getQuickProfile("foo0").isDisabled());
  }


}
