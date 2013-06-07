package org.exoplatform.forum.extras.injection.faq;

import java.util.HashMap;

import org.exoplatform.faq.service.Category;

public class CategoryInjector extends AbstractFAQInjector {

  /** . */
  private static final String NUMBER = "number";
  
  /** . */
  private static final String CATEGORY_PREFIX = "catPrefix";
  
  int rootLever = 1;

  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    //
    int number = param(params, NUMBER);
    String categoryPrefix = params.get(CATEGORY_PREFIX);
    init(null, categoryPrefix, null, null, null, 0);
    
    //
    Category rootCategory = getCategoryRoot(true);
    
    rootLever = param(params, "lever");
    
    addCategory(rootCategory.getPath(), number, 0);
  }
  
  private void addCategory(String path, int number, int lever) {
    System.out.println("\nrootLever " + rootLever + "  lever: " + lever);
    for(int i = 0; i < number; i++) {
      //
      String categoryName;
      if(rootLever == 0 || lever == 0) {
        categoryName = categoryName();
        categoryNumber++;
      } else {
        categoryName = categoryName() + "_" + lever + "_" + i;
      }
      Category cat = new Category();
      cat.setIndex(i);
      cat.setName(categoryName);
      cat.setDescription(lorem.getParagraphs(1));
      cat.setModerators(new String[] {"root"});
      cat.setUserPrivate(new String[] {""});
      
      //
      faqService.saveCategory(path, cat, true);
      //
      getLog().info("Created Category '" + categoryName );

      int nlever = lever + 1;
      if(nlever < rootLever) {
        addCategory(path + "/" + cat.getId(), number, nlever);
      }
      
    }
  }
}
