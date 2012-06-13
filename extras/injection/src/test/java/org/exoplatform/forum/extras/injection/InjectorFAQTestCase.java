/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.forum.extras.injection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.forum.extras.injection.faq.AnswerInjector;
import org.exoplatform.forum.extras.injection.faq.CategoryInjector;
import org.exoplatform.forum.extras.injection.faq.CommentInjector;
import org.exoplatform.forum.extras.injection.faq.ProfileInjector;
import org.exoplatform.forum.extras.injection.faq.QuestionInjector;

import org.exoplatform.services.organization.OrganizationService;

/**
 * @author <a href="mailto:thanhvc@exoplatform.com">Thanh Vu</a>
 * @version $Revision$
 */
public class InjectorFAQTestCase extends BaseTestCase {

  private OrganizationService organizationService;
  private FAQService faqService;
 
  //
  private AnswerInjector answerInjector;
  private CategoryInjector categoryInjector;
  private CommentInjector commentInjector;
  private ProfileInjector profileInjector;
  private QuestionInjector questionInjector;
  
  
  
  private HashMap<String, String> params;
  private List<String> users;
  
  
  
  @Override
  public void setUp() throws Exception {

    super.setUp();
    
    //
    profileInjector = (ProfileInjector) getContainer().getComponentInstanceOfType(ProfileInjector.class);
    categoryInjector = (CategoryInjector) getContainer().getComponentInstanceOfType(CategoryInjector.class);
    answerInjector = (AnswerInjector) getContainer().getComponentInstanceOfType(AnswerInjector.class);
    commentInjector = (CommentInjector) getContainer().getComponentInstanceOfType(CommentInjector.class);
    questionInjector = (QuestionInjector) getContainer().getComponentInstanceOfType(QuestionInjector.class);
    
    
    //
    organizationService = (OrganizationService) getContainer().getComponentInstanceOfType(OrganizationService.class);
    faqService = (FAQService) getContainer().getComponentInstanceOfType(FAQService.class);
    
    
    assertNotNull(profileInjector);
    assertNotNull(categoryInjector);
    assertNotNull(answerInjector);
    assertNotNull(commentInjector);
    assertNotNull(questionInjector);
    assertNotNull(organizationService);
    assertNotNull(faqService);
    
    //
    params = new HashMap<String, String>();
    users = new ArrayList<String>();
  }

  @Override
  public void tearDown() throws Exception {
    //
    List<Category> list =  faqService.getAllCategories();
    for(Category cat : list) {
      faqService.removeCategory(cat.getId());
    }
    
    //
    for(String user : users) {
      organizationService.getUserHandler().removeUser(user, true);
    }
    
    super.tearDown();
  }
  
  public void testDefaultProfile() throws Exception {
    performProfileTest(null);
  }
  
  public void testPrefixProfile() throws Exception {
    performProfileTest("foo");
  }
  
  public void testDefaultCategory() throws Exception {
    performCategoryTest(null, null);
  }
  
  public void testPrefixCategory() throws Exception {
    performCategoryTest("foo", "bar");
  }
  
  
  
  private void performProfileTest(String prefix) throws Exception {
    //
    String baseName = (prefix == null ? "bench.user" : prefix);
    assertClean(baseName, null, null);
    
    //
    params.put("number", "5");
    if (prefix != null) {
      params.put("prefix", prefix);      
    }

    profileInjector.inject(params);
    
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "0"));
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "1"));
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "2"));
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "3"));
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "4"));
    
    //
    assertEquals(5, profileInjector.userNumber(baseName));
    
    //
    cleanProfile(baseName, 5);
  }
  
  private void performCategoryTest(String userPrefix, String catPrefix) throws Exception {

    //
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String catBaseName = (catPrefix == null ? "bench.cat" : catPrefix);
    assertClean(userBaseName, catBaseName, null);

    //
    params.put("number", "3");
    if (userPrefix != null) {
      params.put("prefix", userPrefix);      
    }
    
    profileInjector.inject(params);
    
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "0"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "1"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "2"));
    

    //
    params.put("number", "2");
    params.put("fromUser", "0");
    params.put("toUser", "2");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    categoryInjector.inject(params);
    //
    assertEquals(6, categoryInjector.categoryNumber(catBaseName));
    
    categoryInjector.inject(params);
    //
    assertEquals(12, categoryInjector.categoryNumber(catBaseName));

    //
    cleanProfile(userBaseName, 3);
  }
  
    
  
  private void assertClean(String userBaseName, String categoryBaseName, String forumBaseName) throws Exception {
    if (userBaseName != null) {
      assertEquals(null, organizationService.getUserHandler().findUserByName(userBaseName + "0"));
    }
  }
  
  private void cleanProfile(String prefix, int number) {

    for (int i = 0; i < number; ++i) {
      users.add(prefix + i);
    }

  }
  
 
}
