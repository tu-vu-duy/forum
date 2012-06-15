package org.exoplatform.forum.extras.injection.faq;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Question;

public class QuestionInjector extends AbstractFAQInjector {
  
  /** . */
  private static final String NUMBER = "number";
  
  /** . */
  private static final String TO_CAT = "toCat";
  
  /** . */
  private static final String USER_PREFIX = "userPrefix";
  
  /** . */
  private static final String CATEGORY_PREFIX = "catPrefix";
  
  /** . */
  private static final String QUESTION_PREFIX = "quesPrefix";
  
  /** . */
  private List<String> users   = Arrays.asList(new String[] { "root", "demo", "mary", "john" });
  
  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    int number = param(params, NUMBER);
    int to = param(params, TO_CAT);
    String userPrefix = params.get(USER_PREFIX);
    String categoryPrefix = params.get(CATEGORY_PREFIX);
    String questionPrefix = params.get(QUESTION_PREFIX);
    init(userPrefix, categoryPrefix, questionPrefix, null, null, 0);

    String categoryName = categoryBase + to;
    Category cat = getCategoryByName(categoryName);
    if (cat == null) {
      getLog().info("Category name '" + categoryName + "' is wrong. Aborting injection ..." );
      return;
    }

    String owner = null;
    String questionName = null;

    for (int i = 0; i < number; i++) {
      questionName = questionName();
      owner = users.get(random.nextInt(4));
      
      Question question = new Question();
      question.setAuthor(owner);
      question.setCategoryId(cat.getId());
      question.setCategoryPath(cat.getPath());
      question.setDetail(lorem.getParagraphs(1));
      question.setEmail("noreply@exoplatform.com");
      question.setEmailsWatch(new String[] {""});
      question.setLanguage("English");
      question.setLink("");
      question.setMarkVote(0.0);
      question.setQuestion(questionName);
      question.setRelations(new String[] {""});
      question.setTopicIdDiscuss("");
      question.setUsersWatch(new String[] {""});
      
      faqService.saveQuestion(question, true, faqSetting);
      questionNumber++;
      
      getLog().info("Question '" + questionName + "' created by " + owner);
    }
  }
}
