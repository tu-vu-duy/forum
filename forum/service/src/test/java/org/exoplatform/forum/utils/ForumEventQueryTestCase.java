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
package org.exoplatform.forum.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.Utils;

public class ForumEventQueryTestCase extends TestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testQuerySearchCategory() {
    List<String> categoryIds = new ArrayList<String>();
    String selector = "SELECT * FROM exo:forumCategory WHERE (jcr:path LIKE '/root/categoryHome/%' AND NOT jcr:path LIKE '/root/categoryHome/%/%')";
    ForumEventQuery eventQuery = new ForumEventQuery();
    String predicate = "", first = "(", end = ")";
    eventQuery.setType(Utils.CATEGORY);
    eventQuery.setPath("/root/categoryHome");

    // not conditions
    assertEquals(selector + predicate, eventQuery.getPathQuery(categoryIds));
    assertEquals(true, eventQuery.getIsEmpty());

    selector += " AND ";
    // set text search
    eventQuery.setKeyValue("text search");
    // only category name
    eventQuery.setValueIn("title");
    predicate = "CONTAINS(exo:name, 'text search')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));
    // all value of category
    eventQuery.setValueIn("all");
    predicate = "CONTAINS(*, 'text search')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));

    eventQuery.setByUser("root");
    predicate += " AND (exo:owner='root')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));

    eventQuery.setModerator("demo");
    predicate += " AND (exo:moderators='demo')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));

    String tempPredicate = predicate;
    // case 1: only from date
    Calendar calendar = GregorianCalendar.getInstance();
    eventQuery.setFromDateCreated(calendar);
    predicate += " AND (exo:createdDate >= TIMESTAMP '" + ISO8601.format(calendar) + "')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));
    // case 2: only to date
    predicate = tempPredicate;
    eventQuery.setFromDateCreated(null);
    eventQuery.setToDateCreated(calendar);
    predicate += " AND (exo:createdDate <= TIMESTAMP '" + ISO8601.format(calendar) + "')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));
    // case 3: from date to date
    predicate = tempPredicate;
    eventQuery.setFromDateCreated(calendar);
    predicate += " AND (exo:createdDate >= TIMESTAMP '" + ISO8601.format(calendar) + "'" +
                 " AND exo:createdDate <= TIMESTAMP '" + ISO8601.format(calendar) + "')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));

    // set category Scoping
    categoryIds.addAll(Arrays.asList(new String[] { "CategoryId1", "CategoryId2", "CategoryId3" }));
    predicate += " AND ((fn:name()='CategoryId1') OR (fn:name()='CategoryId2') OR (fn:name()='CategoryId3'))";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));

  }

  public void testQuerySearchForum() {
    List<String> categoryIds = new ArrayList<String>();
    String selector = "SELECT * FROM exo:forum WHERE (jcr:path LIKE '/root/categoryHome/forumCategoryabc/%' " +
    		              "AND NOT jcr:path LIKE '/root/categoryHome/forumCategoryabc/%/%')";
    ForumEventQuery eventQuery = new ForumEventQuery();
    String predicate = "", first = "(", end = ")";
    eventQuery.setType(Utils.FORUM);
    // get forums on only one category
    eventQuery.setPath("/root/categoryHome/forumCategoryabc");
    // not conditions
    assertEquals(selector + predicate, eventQuery.getPathQuery(categoryIds));
    assertEquals(true, eventQuery.getIsEmpty());
    // get forums on all categories
    eventQuery.setPath("/root/categoryHome");
    selector = "SELECT * FROM exo:forum WHERE (jcr:path LIKE '/root/categoryHome/%')";
    assertEquals(selector + predicate, eventQuery.getPathQuery(categoryIds));
    assertEquals(true, eventQuery.getIsEmpty());
    
    selector += " AND ";
    eventQuery.setKeyValue("text search");
    // only forum name
    eventQuery.setValueIn("title");
    predicate = "CONTAINS(exo:name, 'text search')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));
    // all value of forums
    eventQuery.setValueIn("all");
    predicate = "CONTAINS(*, 'text search')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));
    // set User
    eventQuery.setByUser("root");
    predicate += " AND (exo:owner='root')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));

    // Set Close
    String tempPredicate = predicate;
    // Case 1: With Administrator search
    eventQuery.setUserPermission(0);
    eventQuery.setIsClose("true"); // or false, if value is 'all', not new X-path
    predicate += " AND (exo:isClosed='true')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));
    // With normal user or anonim-user not use value isClose.
    // Case 2: With moderator - add moderator search
    eventQuery.setUserPermission(1);
    // sub case 1: close is false;
    eventQuery.setIsClose("false"); // same for case search Administrator with isClose = false.
    predicate = tempPredicate + " AND (exo:isClosed='false')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));
    // sub case 2: close is true
    eventQuery.setIsClose("true");
    List<String> listOfUser = Arrays.asList(new String[] { "john", "/foo/bar", "bez:/foo/dez" });// userName, group and membership of this user.
    eventQuery.setListOfUser(listOfUser);
    predicate = tempPredicate + " AND ((exo:isClosed='true') AND exo:moderators='john' OR exo:moderators='/foo/bar' OR exo:moderators='*:/foo/bar' OR exo:moderators='bez:/foo/dez' OR exo:moderators='*:/foo/dez')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));
    // sub case 3: close is all
    eventQuery.setIsClose("all");
    predicate = tempPredicate + " AND ((exo:isClosed='false') OR exo:moderators='john' OR exo:moderators='/foo/bar' OR exo:moderators='*:/foo/bar' OR exo:moderators='bez:/foo/dez' OR exo:moderators='*:/foo/dez')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));

    // set Lock, if isLock = 'all', not build new query
    eventQuery.setIsLock("all");
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));
    // isLock != 'all'
    eventQuery.setIsLock("false");
    predicate += " AND (exo:isLock='false')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));

    // set moderator
    eventQuery.setModerator("demo");
    predicate += " AND (exo:moderators='demo')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));

    // set topic count
    eventQuery.setTopicCountMin("50");
    predicate += " AND (exo:topicCount>=50)";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));

    // set post count
    eventQuery.setPostCountMin("100");
    predicate += " AND (exo:postCount>=100)";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));

    // set from date
    tempPredicate = predicate;
    Calendar calendar = GregorianCalendar.getInstance();
    eventQuery.setFromDateCreated(calendar);
    predicate += " AND (exo:createdDate >= TIMESTAMP '" + ISO8601.format(calendar) + "')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));

    // set to date but not from date
    eventQuery.setFromDateCreated(null);
    eventQuery.setToDateCreated(calendar);

    // set from date to date
    eventQuery.setFromDateCreated(calendar);
    predicate = tempPredicate + " AND (exo:createdDate >= TIMESTAMP '" + ISO8601.format(calendar) + "'" +
    		                        " AND exo:createdDate <= TIMESTAMP '" + ISO8601.format(calendar) + "')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));

    // set category Scoping
    categoryIds.addAll(Arrays.asList(new String[] { "CategoryId1", "CategoryId2", "CategoryId3" }));
    predicate += " AND ((fn:name()='CategoryId1') OR (fn:name()='CategoryId2') OR (fn:name()='CategoryId3'))";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));
  }

  public void testQuerySearchTopic() {
    List<String> forumIds = new ArrayList<String>();
    String selector = "SELECT * FROM exo:topic WHERE (jcr:path LIKE '/root/categoryHome/forumCategoryabc/forumfoo/%' " +
    		              "AND NOT jcr:path LIKE '/root/categoryHome/forumCategoryabc/forumfoo/%/%')";
    ForumEventQuery eventQuery = new ForumEventQuery();
    String predicate = "", first = "(", end = ")";
    eventQuery.setType(Utils.TOPIC);
    // get topics on only one forum
    eventQuery.setPath("/root/categoryHome/forumCategoryabc/forumfoo");
    // not conditions
    assertEquals(selector + predicate, eventQuery.getPathQuery(forumIds));
    assertEquals(true, eventQuery.getIsEmpty());
    // get topics on all forums
    eventQuery.setPath("/root/categoryHome");
    selector = "SELECT * FROM exo:topic WHERE (jcr:path LIKE '/root/categoryHome/%')";
    assertEquals(selector + predicate, eventQuery.getPathQuery(forumIds));
    assertEquals(true, eventQuery.getIsEmpty());
    
    selector += " AND ";

    eventQuery.setKeyValue("text search");
    // only forum name
    eventQuery.setValueIn("title");
    predicate = "CONTAINS(exo:name, 'text search')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(forumIds));
    // all value of forums
    eventQuery.setValueIn("all");
    predicate = "CONTAINS(*, 'text search')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(forumIds));
    // set User
    eventQuery.setByUser("root");
    predicate += " AND (exo:owner='root')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(forumIds));

    // Set Close, only use for administrator or moderator. they same query
    eventQuery.setUserPermission(0);
    // if value is 'all', not new query
    eventQuery.setIsClose("all");
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(forumIds));
    // isClose != 'all'
    eventQuery.setIsClose("true"); // or false.
    predicate += " AND (exo:isClosed='true')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(forumIds));

    // set Lock, if isLock = 'all', not build new query
    eventQuery.setIsLock("all");
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(forumIds));
    // isLock != 'all'
    eventQuery.setIsLock("false");
    predicate += " AND (exo:isLock='false')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(forumIds));

    // set post count
    eventQuery.setPostCountMin("100");
    predicate += " AND (exo:postCount>=100)";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(forumIds));

    // set view count
    eventQuery.setViewCountMin("200");
    predicate += " AND (exo:viewCount>=200)";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(forumIds));

    // set from date
    String tempPredicate = predicate;
    Calendar calendar = GregorianCalendar.getInstance();
    eventQuery.setFromDateCreated(calendar);
    predicate += " AND (exo:createdDate >= TIMESTAMP '" + ISO8601.format(calendar) + "')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(forumIds));

    // set to date but not from date
    eventQuery.setFromDateCreated(null);
    eventQuery.setToDateCreated(calendar);

    // set from date to date
    eventQuery.setFromDateCreated(calendar);
    predicate = tempPredicate + " AND (exo:createdDate >= TIMESTAMP '" + ISO8601.format(calendar) + "'" +
                                " AND exo:createdDate <= TIMESTAMP '" + ISO8601.format(calendar) + "')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(forumIds));

    // check can view for normal user and guest
    List<String> listOfUser = Arrays.asList(new String[] { "john", "/foo/bar", "bez:/foo/dez" });// userName, group and membership of this user.
    eventQuery.setListOfUser(listOfUser);
    eventQuery.setUserPermission(2);
    predicate += " AND (exo:isWaiting='false') AND (exo:isActive='true') AND (exo:isApproved='true') AND (exo:isActiveByForum='true')" +
    		         " AND (exo:canView='' OR exo:canView=' ' OR exo:canView IS NULL OR exo:canView='john'" +
    		         " OR exo:canView='/foo/bar' OR exo:canView='*:/foo/bar' OR exo:canView='bez:/foo/dez' OR exo:canView='*:/foo/dez' OR exo:owner='john')";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(forumIds));

    // set category Scoping
    forumIds.addAll(Arrays.asList(new String[] { "CategoryId1", "CategoryId2", "CategoryId3" }));
    predicate += " AND ((exo:path='CategoryId1') OR (exo:path='CategoryId2') OR (exo:path='CategoryId3'))";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(forumIds));
  }

  public void testQuerySearchPost() {
    List<String> categoryIds = new ArrayList<String>();
    String selector = "SELECT * FROM exo:post WHERE (jcr:path LIKE '/forumPath/%') AND ";
    String postPrivate = "(((exo:userPrivate='john') OR (exo:userPrivate='exoUserPri')) AND (exo:isFirstPost='false'))";
    String first = "(", end = ")";
    ForumEventQuery eventQuery = new ForumEventQuery();
    String predicate = "";
    eventQuery.setType(Utils.POST);
    eventQuery.setPath("/forumPath");
    List<String> listOfUser = Arrays.asList(new String[] { "john", "/foo/bar", "bez:/foo/dez" });// userName, group and membership of this user.
    eventQuery.setListOfUser(listOfUser);
    eventQuery.setUserPermission(0);
    // not conditions
    assertEquals(selector + first + predicate + postPrivate + end, eventQuery.getPathQuery(categoryIds));
    assertEquals(true, eventQuery.getIsEmpty());

    eventQuery.setKeyValue("text search");
    // only forum name
    eventQuery.setValueIn("title");
    predicate = "CONTAINS(exo:name, 'text search') AND ";
    assertEquals(selector + first + predicate + postPrivate + end, eventQuery.getPathQuery(categoryIds));
    // all value of forums
    eventQuery.setValueIn("all");
    predicate = "CONTAINS(*, 'text search') AND ";
    assertEquals(selector + first + predicate + postPrivate + end, eventQuery.getPathQuery(categoryIds));
    // set User
    eventQuery.setByUser("root");
    predicate += "(exo:owner='root') AND ";
    assertEquals(selector + first + predicate + postPrivate + end, eventQuery.getPathQuery(categoryIds));

    String keepPredicate = predicate;
    // case 1: only from date
    Calendar calendar = GregorianCalendar.getInstance();
    eventQuery.setFromDateCreated(calendar);
    predicate += "(exo:createdDate >= TIMESTAMP '" + ISO8601.format(calendar) + "') AND ";
    assertEquals(selector + first + predicate + postPrivate + end, eventQuery.getPathQuery(categoryIds));
    // case 2: only to date
    predicate = keepPredicate;
    eventQuery.setFromDateCreated(null);
    eventQuery.setToDateCreated(calendar);
    
    predicate += "(exo:createdDate <= TIMESTAMP '" + ISO8601.format(calendar) + "') AND ";
    assertEquals(selector + first + predicate + postPrivate + end, eventQuery.getPathQuery(categoryIds));
    // case 3: from date to date
    predicate = keepPredicate;
    eventQuery.setFromDateCreated(calendar);
    predicate += "(exo:createdDate >= TIMESTAMP '" + ISO8601.format(calendar) + "' AND exo:createdDate <= TIMESTAMP '" + ISO8601.format(calendar) + "') AND ";
    assertEquals(selector + first + predicate + postPrivate + end, eventQuery.getPathQuery(categoryIds));
    // set category Scoping
    categoryIds.addAll(Arrays.asList(new String[] { "CategoryId1", "CategoryId2", "CategoryId3" }));
    predicate += postPrivate + " AND ((exo:path='CategoryId1') OR (exo:path='CategoryId2') OR (exo:path='CategoryId3'))";
    assertEquals(selector + first + predicate + end, eventQuery.getPathQuery(categoryIds));
  }
}
