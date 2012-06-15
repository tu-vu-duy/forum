package org.exoplatform.forum.extras.injection.faq;

import java.util.HashMap;

import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.Question;

public class CommentInjector extends AbstractFAQInjector {
  
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
  
  /** . */
  private static final String COMMENT_PREFIX = "commentPrefix";
  
  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    int number = param(params, NUMBER);
    int to = param(params, TO_QUES);
    String userPrefix = params.get(USER_PREFIX);
    String catPrefix = params.get(CATEGORY_PREFIX);
    String questionPrefix = params.get(QUESTION_PREFIX);
    String answerPrefix = params.get(ANSWER_PREFIX);
    String commentPrefix = params.get(COMMENT_PREFIX);
    init(userPrefix, catPrefix, questionPrefix, answerPrefix, commentPrefix, 0);

    String questionName = questionBase + to;
    Question question = getQuestionByName(questionName);
    if (question == null) {
      getLog().info("Question name '" + questionName + "' is wrong. Aborting injection ..." );
      return;
    }
    
    String owner = null;
    String commentName = null;

    for (int i = 0; i < number; i++) {
      commentName = commentName();
      owner = USERS.get(random.nextInt(4));
      
      Comment comment = new Comment();
      comment.setCommentBy(owner);
      comment.setComments(lorem.getParagraphs(1));
      comment.setFullName(commentName);
      comment.setNew(true);
      comment.setPostId("");
      
      faqService.saveComment(question.getPath(), comment, true);
      commentNumber++;
      
      getLog().info("Comment '" + commentName + "' created by " + owner);
    }
  }
}
