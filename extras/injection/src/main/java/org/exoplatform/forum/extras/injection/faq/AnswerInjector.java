package org.exoplatform.forum.extras.injection.faq;

import java.util.HashMap;

import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Question;
import org.exoplatform.ks.common.jcr.KSDataLocation;

public class AnswerInjector extends AbstractFAQInjector {
  
  /** . */
  private static final String NUMBER = "number";
  
  /** . */
  private static final String TO_QUES = "toQues";
  
  /** . */
  private static final String USER_PREFIX = "userPrefix";
  
  /** . */
  private static final String CATEGORY_PREFIX = "catPrefix";
  
  /** . */
  private static final String QUESTION_PREFIX = "quesPrefix";
  
  /** . */
  private static final String ANSWER_PREFIX = "answerPrefix";
  
  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    int number = param(params, NUMBER);
    int to = param(params, TO_QUES);
    String userPrefix = params.get(USER_PREFIX);
    String catPrefix = params.get(CATEGORY_PREFIX);
    String questionPrefix = params.get(QUESTION_PREFIX);
    String answerPrefix = params.get(ANSWER_PREFIX);
    init(userPrefix, catPrefix, questionPrefix, answerPrefix, null, 0);

    String questionName = questionBase + to;
    Question question = getQuestionByName(questionName);
    if (question == null) {
      getLog().info("Question name '" + questionName + "' is wrong. Aborting injection ..." );
      return;
    }
    
    String owner = null;
    String answerName = null;
    
    for (int i = 0; i < number; i++) {
      answerName = answerName();
      owner = USERS.get(random.nextInt(4));
      
      Answer answer = new Answer();
      answer.setFullName(answerName);
      answer.setLanguage("English");
      answer.setMarksVoteAnswer(0.0);
      answer.setMarkVotes(0);
      answer.setNew(true);
      answer.setResponseBy(owner);
      answer.setResponses(lorem.getParagraphs(1));
      
      faqService.saveAnswer(question.getPath(), answer, true);
      answerNumber++;
      
      getLog().info("Answer '" + answerName + "' created by " + owner);
    }
  }
}
