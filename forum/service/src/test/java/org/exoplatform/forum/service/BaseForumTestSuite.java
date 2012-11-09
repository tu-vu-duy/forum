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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestSuite;

import org.exoplatform.component.test.KernelBootstrap;
import org.exoplatform.forum.base.BaseForumServiceTestCase;
import org.exoplatform.forum.base.BaseTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tuvd@exoplatform.com
 * Nov 8, 2012  
 */
@RunWith(Suite.class)
@SuiteClasses({ 
  ForumServiceTestCase.class, 
  UserProfileTestCase.class,
  CategoryForumTestCase.class,
  TopicTestCase.class,
  PostTestCase.class,
  SearchTestCase.class
})

public class BaseForumTestSuite {

  /** . */
  private static KernelBootstrap                 bootstrap;

  /** . */
  private static final Map<Class<?>, AtomicLong> counters       = new HashMap<Class<?>, AtomicLong>();

  @BeforeClass
  public static void setUp() throws Exception {
    beforeRunBare();
  }

  @AfterClass
  public static void tearDown() {
    afterRunBare();
  }

  public static void beforeRunBare() throws Exception {
    Class<?> key = BaseForumServiceTestCase.class;
    //
    if (!counters.containsKey(key)) {
      counters.put(key, new AtomicLong(new TestSuite(key).testCount()));

      //
      bootstrap = new KernelBootstrap(Thread.currentThread().getContextClassLoader());

      // Configure ourselves
      bootstrap.addConfiguration(key);

      //
      bootstrap.boot();
    }
    BaseTestCase.bootstrap = bootstrap;

  }

  public static void afterRunBare() {
    Class<?> key = BaseForumServiceTestCase.class;

    //
    if (counters.get(key).decrementAndGet() == 0) {
      bootstrap.dispose();

      //
      bootstrap = null;
    }
  }
}
