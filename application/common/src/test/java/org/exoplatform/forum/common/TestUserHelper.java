/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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

import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;

public class TestUserHelper extends BaseCommonsTestCase {
  @Override
  public void setUp() throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    super.setUp();
    assertNotNull(container);
    assertNotNull(UserHelper.getOrganizationService());
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    RequestLifeCycle.end();
  }

  public void testCheckValueUser() throws Exception {
    String values = null;
    assertEquals("", UserHelper.checkValueUser(values));
    //
    values = "demo";
    assertEquals("", UserHelper.checkValueUser(values));
    values = "demo,/platform/users,test";
    assertEquals("test", UserHelper.checkValueUser(values));
    values = "demo,/platform/users,/platform/test,test";
    assertEquals("/platform/test, test", UserHelper.checkValueUser(values));
    values = "demo,member:/platform/users,test:/platform/users,test";
    assertEquals("test:/platform/users, test", UserHelper.checkValueUser(values));
  }

  public void testHasGroupIdAndMembershipId() throws Exception {
    String input = "";
    assertFalse(UserHelper.hasGroupIdAndMembershipId(input));
    input = "demo";
    assertFalse(UserHelper.hasGroupIdAndMembershipId(input));
    input = "/platform/test";
    assertFalse(UserHelper.hasGroupIdAndMembershipId(input));
    input = "test:/platform/users";
    assertFalse(UserHelper.hasGroupIdAndMembershipId(input));
    input = "/platform/users";
    assertTrue(UserHelper.hasGroupIdAndMembershipId(input));
    input = "member:/platform/users";
    assertTrue(UserHelper.hasGroupIdAndMembershipId(input));
  }

  public void testHasUserInGroup() throws Exception {
    assertFalse(UserHelper.hasUserInGroup("groupId", "user"));
    assertFalse(UserHelper.hasUserInGroup("/platform/users", "user"));
    assertFalse(UserHelper.hasUserInGroup("/platform/administrators", "demo"));
    //
    assertTrue(UserHelper.hasUserInGroup("/platform/users", "demo"));
    assertTrue(UserHelper.hasUserInGroup("/platform/administrators", "root"));
  }

  public void testGetDisplayNameOfOwner() throws Exception {
    String owner = null;
    assertEquals("", UserHelper.getDisplayNameOfOwner(owner));
    owner = "user";
    assertEquals("", UserHelper.getDisplayNameOfOwner(owner));
    owner = "demo";
    assertEquals("Demo gtn", UserHelper.getDisplayNameOfOwner(owner));
    owner = "/platform/test";
    assertEquals("", UserHelper.getDisplayNameOfOwner(owner));
    owner = "/platform/users";
    assertEquals("users", UserHelper.getDisplayNameOfOwner(owner));
    owner = "test:/platform/users";
    assertEquals("", UserHelper.getDisplayNameOfOwner(owner));
    owner = "member:/platform/users";
    assertEquals("member in users", UserHelper.getDisplayNameOfOwner(owner));
  }

}
