package org.exoplatform.forum.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.exoplatform.commons.utils.ISO8601;

public class ForumEventQuery implements ForumNodeTypes {
  public static final String VALUE_IN_ENTIRE = "entire";

  public static final String VALUE_IN_TITLE  = "title";

  long             userPermission = 0;

  List<String>     listOfUser     = null;

  private String   type;

  private String   keyValue;

  private String   valueIn;

  private String   path;

  private String   byUser;

  private String   isLock;

  private String   isClosed;

  private String   topicCountMin  = "0";

  private String   postCountMin   = "0";

  private String   viewCountMin   = "0";

  private String   moderator;

  private String   remain;

  private Calendar fromDateCreated;

  private Calendar toDateCreated;

  private Calendar fromDateCreatedLastPost;

  private Calendar toDateCreatedLastPost;

  private boolean  isStartWith    = false;

  private boolean  isEmpty        = true;

  public void setListOfUser(List<String> listOfUser) {
    this.listOfUser = new ArrayList<String>(listOfUser);
  }

  public List<String> getListOfUser() {
    return listOfUser;
  }

  public long getUserPermission() {
    return userPermission;
  }

  public void setUserPermission(long userPermission) {
    this.userPermission = userPermission;
  }

  public ForumEventQuery() {
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getKeyValue() {
    return keyValue;
  }

  public void setKeyValue(String keyValue) {
    this.keyValue = keyValue;
  }

  public String getValueIn() {
    return valueIn;
  }

  public void setValueIn(String valueIn) {
    this.valueIn = valueIn;
  }


  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getByUser() {
    return byUser;
  }

  public void setByUser(String byUser) {
    this.byUser = byUser;
  }

  public String getIsLock() {
    return isLock;
  }

  public void setIsLock(String isLock) {
    this.isLock = isLock;
  }

  public String getIsClose() {
    return isClosed;
  }

  public void setIsClose(String isClosed) {
    this.isClosed = isClosed;
  }

  public String getTopicCountMin() {
    return topicCountMin;
  }

  public void setTopicCountMin(String topicCountMin) {
    this.topicCountMin = topicCountMin;
  }

  public String getPostCountMin() {
    return postCountMin;
  }

  public void setPostCountMin(String postCountMin) {
    this.postCountMin = postCountMin;
  }

  public String getViewCountMin() {
    return viewCountMin;
  }

  public void setViewCountMin(String viewCountMin) {
    this.viewCountMin = viewCountMin;
  }

  public String getModerator() {
    return moderator;
  }

  public void setModerator(String moderator) {
    this.moderator = moderator;
  }

  public String getRemain() {
    return remain;
  }

  public void setRemain(String remain) {
    this.remain = remain;
  }

  public Calendar getFromDateCreated() {
    return fromDateCreated;
  }

  public void setFromDateCreated(Calendar fromDateCreated) {
    this.fromDateCreated = fromDateCreated;
  }

  public Calendar getToDateCreated() {
    return toDateCreated;
  }

  public void setToDateCreated(Calendar toDateCreated) {
    this.toDateCreated = toDateCreated;
  }

  public Calendar getFromDateCreatedLastPost() {
    return fromDateCreatedLastPost;
  }

  public void setFromDateCreatedLastPost(Calendar fromDateCreatedLastPost) {
    this.fromDateCreatedLastPost = fromDateCreatedLastPost;
  }

  public Calendar getToDateCreatedLastPost() {
    return toDateCreatedLastPost;
  }

  public void setToDateCreatedLastPost(Calendar toDateCreatedLastPost) {
    this.toDateCreatedLastPost = toDateCreatedLastPost;
  }

  public boolean isStartWith() {
    return isStartWith;
  }

  public void setStartWith(boolean isStartWith) {
    this.isStartWith = isStartWith;
  }

  public boolean getIsEmpty() {
    return this.isEmpty;
  }
  
  private String and(StringBuilder statements) {
    if (statements.length() > 0) {
      return " AND ";
    }
    return "";
  }

  public String getPathQuery(List<String> listIds) {
    String nodeType = (Utils.CATEGORY.equals(type)) ? EXO_FORUM_CATEGORY :
                      (Utils.FORUM.equals(type)) ? EXO_FORUM :
                      (Utils.TOPIC.equals(type)) ? EXO_TOPIC : EXO_POST;
    String userLogin = (listOfUser != null && listOfUser.size() > 0) ? listOfUser.get(0) : UserProfile.USER_GUEST;

    StringBuilder sqlQuery = new StringBuilder();
    
    if(Utils.CATEGORY.equals(type) ||
        type.equals(Utils.FORUM) && path.indexOf(Utils.CATEGORY) > 0 ||
        type.equals(Utils.TOPIC) && (path.lastIndexOf(Utils.FORUM) > path.indexOf(Utils.CATEGORY)) ||
        type.equals(Utils.POST) && path.indexOf(Utils.TOPIC) > 0){
       //
       sqlQuery = Utils.jcrPathLikeAndNotLike(nodeType, path);
     } else {
       sqlQuery.append("SELECT * FROM exo:").append(type).append(" WHERE (")
               .append(JCR_PATH).append(" LIKE '").append(path).append("/%')");
     }    
    
    StringBuilder statements = new StringBuilder();
    
    if (keyValue != null && keyValue.length() > 0) {
      if (VALUE_IN_TITLE.equals(valueIn)) {
        statements.append("CONTAINS(").append(EXO_NAME).append(", '").append(keyValue).append("')");
      } else {
        statements.append("CONTAINS(*, '").append(keyValue).append("')");
      }
    }

    if (byUser != null && byUser.length() > 0) {
      StringBuilder builder = setArrays(byUser, EXO_OWNER);
      if (builder.length() > 0) {
        statements.append(and(statements)).append("(").append(builder).append(")");
      }
    }
    
    if(type.equals(Utils.FORUM) || type.equals(Utils.TOPIC)) {
      if (isClosed != null && isClosed.length() > 0) {
        if (userPermission == 1) {
          if (type.equals(Utils.FORUM)) {
            if (isClosed.equals("all")) {
              statements.append(and(statements)).append("(").append(Utils.getSQLQueryByProperty("", EXO_IS_CLOSED, "false"))
                      .append(" OR ").append(Utils.buildSQLByUserInfo(EXO_MODERATORS, listOfUser))
                      .append(")");
            } else if (isClosed.equals("false")) {
              statements.append(Utils.getSQLQueryByProperty(and(statements).trim(), EXO_IS_CLOSED, "false"));
            } else if (isClosed.equals("true")) {
              statements.append(and(statements)).append("(").append(Utils.getSQLQueryByProperty("", EXO_IS_CLOSED, "true"))
                       .append(" AND ").append(Utils.buildSQLByUserInfo(EXO_MODERATORS, listOfUser))
                       .append(")");
            }
          } else {
            if (!isClosed.equals("all")) {
              statements.append(Utils.getSQLQueryByProperty(and(statements).trim(), EXO_IS_CLOSED, isClosed));
            }
          }
        } else {
          if (!isClosed.equals("all")) {
            statements.append(Utils.getSQLQueryByProperty(and(statements).trim(), EXO_IS_CLOSED, isClosed));
          }
        }
      }
      if (Utils.isEmpty(isLock) == false && !isLock.equals("all")) {
        statements.append(Utils.getSQLQueryByProperty(and(statements).trim(), EXO_IS_LOCK, isLock));
      }
    }
    if (remain != null && remain.length() > 0) {
      statements.append(and(statements)).append("(").append(remain).append(")");
    }
    StringBuilder builder;
    if (Utils.isEmpty(moderator) == false && (Utils.FORUM.equals(type) || Utils.CATEGORY.equals(type))) {
      builder = setArrays(moderator, EXO_MODERATORS);
      if (builder.length() > 0) {
        statements.append(and(statements)).append("(").append(builder).append(")");
      }
    }
    
    String element;
    if(Utils.FORUM.equals(type) || Utils.TOPIC.equals(type)) {
      element = setValueMin(topicCountMin, EXO_TOPIC_COUNT);
      if (Utils.isEmpty(element) == false) {
        statements.append(and(statements)).append(element);
      }
      element = setValueMin(postCountMin, EXO_POST_COUNT);
      if (Utils.isEmpty(element) == false) {
        statements.append(and(statements)).append(element);
      }
      if(Utils.TOPIC.equals(type)) {
        element = setValueMin(viewCountMin, EXO_VIEW_COUNT);
        if (Utils.isEmpty(element) == false) {
          statements.append(and(statements)).append(element);
        }
        element = setDateFromTo(fromDateCreatedLastPost, toDateCreatedLastPost, EXO_LAST_POST_DATE);
        if (Utils.isEmpty(element) == false) {
          statements.append(and(statements)).append(element);
        }
      }
    }

    element = setDateFromTo(fromDateCreated, toDateCreated, EXO_CREATED_DATE);
    if (Utils.isEmpty(element) == false) {
      statements.append(and(statements)).append(element);
    }
    
    // check input empty
    isEmpty = (statements.length() == 0);

    // add to search for user and moderator:
    if (type.equals(Utils.TOPIC) && userPermission > 1) {
      statements.append(Utils.getSQLQueryByProperty(and(statements).trim(), EXO_IS_WAITING, "false"))
                .append(Utils.getSQLQueryByProperty("AND", EXO_IS_ACTIVE, "true"))
                .append(Utils.getSQLQueryByProperty("AND", EXO_IS_APPROVED, "true"))
                .append(Utils.getSQLQueryByProperty("AND", EXO_IS_ACTIVE_BY_FORUM, "true"));
      
      String str = Utils.buildSQLByUserInfo(EXO_CAN_VIEW, listOfUser);
      if(Utils.isEmpty(str) == false) {
        statements.append(and(statements)).append("(").append(Utils.buildSQLHasProperty(EXO_CAN_VIEW)).append(" OR ")
        .append(str).append(" OR ").append(EXO_OWNER).append("='").append(userLogin).append("'").append(")");
      }
    } 
    
    if (type.equals(Utils.POST)) {
      statements.append(and(statements)).append("(");
      if (userPermission > 1) {
        statements.append(Utils.getSQLQueryPosts("true", "false", "false", userLogin));
      } else {
        statements.append(Utils.getSQLQueryPosts(null, null, null, userLogin));
      }
      statements.append(Utils.getSQLQueryByProperty("AND", EXO_IS_FIRST_POST, "false"));
      statements.append(")");
    }

    if (listIds != null && listIds.size() > 0) {
      StringBuilder sqlQr = new StringBuilder();
      
      String searchBy = null;
      if (type.equals(Utils.CATEGORY) || type.equals(Utils.FORUM)) {
        searchBy = "fn:name()";
      } else {
        searchBy = EXO_PATH;
      }
      for (String id : listIds) {
        sqlQr.append(Utils.getSQLQueryByProperty((sqlQr.length() > 0) ? "OR" : "", searchBy, id));
      }
      statements.append(and(statements)).append("(").append(sqlQr).append(")");
    }
    
    if(statements.length() > 0) {
      sqlQuery.append(" AND (").append(statements).append(")");
    }
    
    return sqlQuery.toString();
  }

  private StringBuilder setArrays(String values, String property) {
    StringBuilder builder = new StringBuilder();
    values = values.replaceAll(";", ",");
    if (values.indexOf(",") > 0) {
      String[] vls = values.split(",");
      for (String string : vls) {
        if (Utils.isEmpty(string) == false) {
          if (builder.length() > 0){
            builder.append(" OR ");
          }
          builder.append(property).append("='").append(string.trim()).append("'");
        }
      }
    } else if (Utils.isEmpty(values) == false) {
      builder.append(property).append("='").append(values.trim()).append("'");
    }
    return builder;
  }

  private String setValueMin(String min, String property) {
    StringBuffer queryString = new StringBuffer();
    if (Integer.parseInt(min) > 0) {
      queryString.append("(").append(property).append(">=").append(min).append(")");
    }
    return queryString.toString();
  }

  private String setDateFromTo(Calendar fromDate, Calendar toDate, String property) {
    StringBuilder queryString = new StringBuilder();
    if (fromDate != null && toDate != null) {
      queryString.append("(").append(property).append(" >= TIMESTAMP '").append(ISO8601.format(fromDate))
                 .append("' AND ").append(property).append(" <= TIMESTAMP '").append(ISO8601.format(toDate)).append("')");
    } else if (fromDate != null) {
      queryString.append("(").append(property).append(" >= TIMESTAMP '").append(ISO8601.format(fromDate)).append("')");
    } else if (toDate != null) {
      queryString.append("(").append(property).append(" <= TIMESTAMP '").append(ISO8601.format(toDate)).append("')");
    }
    return queryString.toString();
  }
}
