package org.exoplatform.forum.extras.injection.faq;

import java.util.HashMap;

import org.exoplatform.faq.service.Category;

public class CategoryInjector extends AbstractFAQInjector {

  /** . */
  private static final String NUMBER = "number";
  
  /** . */
  private static final String USER_PREFIX = "userPrefix";

  /** . */
  private static final String CATEGORY_PREFIX = "catPrefix";

  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    int number = param(params, NUMBER);
    String userPrefix = params.get(USER_PREFIX);
    String categoryPrefix = params.get(CATEGORY_PREFIX);
    init(userPrefix, categoryPrefix, null, null, null, 0);
     
    Category rootCategory = getCategoryRoot(true);
    String categoryName = null;
    Category cat = null;
    
    for(int i = 0; i < number; i++) {
      categoryName = categoryName();

      cat = new Category();
      cat.setIndex(i);
      cat.setName(categoryName);
      cat.setDescription(lorem.getParagraphs(1));
      cat.setModerators(new String[] {USERS.get(0)});
      cat.setUserPrivate(new String[] {""});
      
      faqService.saveCategory(rootCategory.getPath(), cat, true);
      categoryNumber++;
      
      getLog().info("Category '" + categoryName + "' created with parent is '" + rootCategory.getId() + "'");
    }
  }
}
