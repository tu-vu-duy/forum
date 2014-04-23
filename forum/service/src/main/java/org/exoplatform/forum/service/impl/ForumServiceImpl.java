/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.forum.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jcr.NodeIterator;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.forum.common.CommonUtils;
import org.exoplatform.forum.common.conf.RoleRulesPlugin;
import org.exoplatform.forum.common.lifecycle.LifeCycleCompletionService;
import org.exoplatform.forum.service.CacheUserProfile;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.DataStorage;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumEventLifeCycle;
import org.exoplatform.forum.service.ForumEventListener;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumSearchResult;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.ForumStatisticsService;
import org.exoplatform.forum.service.ForumSubscription;
import org.exoplatform.forum.service.InitializeForumPlugin;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.LazyPageList;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.PruneSetting;
import org.exoplatform.forum.service.SendMessageInfo;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserLoginLogEntry;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.Watch;
import org.exoplatform.forum.service.filter.model.CategoryFilter;
import org.exoplatform.forum.service.impl.model.PostFilter;
import org.exoplatform.forum.service.impl.model.PostListAccess;
import org.exoplatform.forum.service.impl.model.TopicFilter;
import org.exoplatform.forum.service.impl.model.TopicListAccess;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.picocontainer.Startable;
import org.quartz.JobDetail;

@ManagedBy(ForumServiceManaged.class)
public class ForumServiceImpl implements ForumService, Startable {

  private static final Log           log             = ExoLogger.getLogger(ForumServiceImpl.class);

  private DataStorage                storage;

  private ForumServiceManaged        managementView;                                                  // will be automatically set at @ManagedBy processing

  private Map<String, List<String>>  onlineUserMap   = new HashMap<String, List<String>>();

  final Queue<UserLoginLogEntry>     queue           = new ConcurrentLinkedQueue<UserLoginLogEntry>();

  private Map<String, String>        lastLoginMap      = new HashMap<String, String>();

  private ForumStatisticsService     forumStatisticsService;

  private LifeCycleCompletionService completionService;

  private JobSchedulerService        jobSchedulerService;

  protected List<ForumEventListener> listeners_      = new ArrayList<ForumEventListener>(3);
  
  public ForumServiceImpl(InitParams params, ExoContainerContext context, DataStorage dataStorage,
                          ForumStatisticsService staticsService, JobSchedulerService jobService, LifeCycleCompletionService completionService) {
    this.storage = dataStorage;
    this.forumStatisticsService = staticsService;
    this.jobSchedulerService = jobService;
    this.completionService = completionService;
  }

  /**
   * {@inheritDoc}
   */
  public void addPlugin(ComponentPlugin plugin) throws Exception {
    storage.addPlugin(plugin);
  }

  /**
   * {@inheritDoc}
   */
  public void addRolePlugin(ComponentPlugin plugin) throws Exception {
    storage.addRolePlugin(plugin);
  }

  /**
   * {@inheritDoc}
   */
  public void addInitialDefaultDataPlugin(ComponentPlugin plugin) throws Exception {
    storage.addInitialDefaultDataPlugin(plugin);
  }

  public void addInitialDataPlugin(ComponentPlugin plugin) throws Exception {
    storage.addInitialDataPlugin(plugin);
  }

  public void start() {

    try {
      log.info("initializing category listeners...");
      storage.initCategoryListener();
    } catch (Exception e) {
      log.error("Error while updating category listeners " + e.getMessage());
    }

    try {
      log.info("initializing default data...");
      storage.initDefaultData();

    } catch (Exception e) {
      log.error("Error while initializing default data: " + e.getMessage());
    }

    try {
      log.info("initializing data...");
      storage.initDataPlugin();
    } catch (Exception e) {
      log.error("Error while initializing data plugin: " + e.getMessage());
    }

    try {
      log.info("Calculating active users...");
      storage.evaluateActiveUsers("");
    } catch (Exception e) {
      log.error("Error while calculating active users: " + e.getMessage());
    }

    // init Calculate Moderators listeners
    try {
      log.info("initializing Calculate Moderators listeners...");
      storage.addCalculateModeratorEventListener();
    } catch (Exception e) {
      log.error("Error while initializing Moderators listeners: " + e.getMessage());
    }

    // initialize auto prune schedules
    try {
      log.info("initializing prune schedulers...");
      storage.initAutoPruneSchedules();
    } catch (Exception e) {
      log.error("Error while initializing Prune schedulers: " + e.getMessage());
    }

    // init deleted user listeners
    try {
      log.info("initializing deleted user listener...");
      storage.addDeletedUserCalculateListener();
    } catch (Exception e) {
      log.error("Error while initializing Prune schedulers: " + e.getMessage());
    }

    // management views
    try {
      log.info("initializing management view...");
      managePlugins();
      manageStorage();
      manageJobs();
    } catch (Exception e) {
      log.error("Error while initializing Management view: " + e.getMessage());
    }
    //
//    removeActivity();
  }

  private void manageStorage() {
    managementView.registerStorageManager(storage);
  }

  private void manageJobs() {
    try {
      List<JobDetail> jobs = jobSchedulerService.getAllJobs();
      for (JobDetail jobDetail : jobs) {
        managementView.registerJobManager(new JobManager(jobDetail));
      }
    } catch (Exception e) {
      log.error("failed to register jobs manager", e);
    }
  }

  private void managePlugins() {
    List<RoleRulesPlugin> plugins = storage.getRulesPlugins();
    for (RoleRulesPlugin plugin2 : plugins) {
      managementView.registerPlugin(plugin2);
    }

    List<InitializeForumPlugin> defaultPlugins = storage.getDefaultPlugins();
    for (InitializeForumPlugin plugin2 : defaultPlugins) {
      managementView.registerPlugin(plugin2);
    }

  }

  public void stop() {
  }

  public void addMember(User user, UserProfile profileTemplate) throws Exception {
    boolean added = storage.populateUserProfile(user, profileTemplate, true);
    if (added) {
      forumStatisticsService.addMember(user.getUserName());
    }
  }

  public void calculateDeletedUser(String userName) throws Exception {
    storage.calculateDeletedUser(userName);
  }

  /**
   * {@inheritDoc}
   */
  public void removeMember(User user) throws Exception {
    String userName = user.getUserName();
    if (storage.deleteUserProfile(userName))
      forumStatisticsService.removeMember(userName);
    UserProfile userProfile = CacheUserProfile.getFromCache(userName);
    if (userProfile != null) {
      UserProfile profile = new UserProfile();
      profile.setUserId(userName);
      profile.setUserTitle(UserProfile.USER_REMOVED);
      profile.setUserRole(UserProfile.USER_DELETED);
      profile.setIsBanned(true);
      CacheUserProfile.storeInCache(userName, profile);
    }
  }

  public void createUserProfile(User user) throws Exception {

  }

  
  public void calculateDeletedGroup(String groupId, String groupName) throws Exception {
    storage.calculateDeletedGroup(groupId, groupName);
  }
  
  /**
   * {@inheritDoc}
   */
  public void updateUserProfile(User user) throws Exception {
    storage.populateUserProfile(user, null, false);
  }

  /**
   * @deprecated use {@link #updateUserProfile(User)}
   */
  public void saveEmailUserProfile(String userId, String email) throws Exception {
  }

  /**
   * {@inheritDoc}
   */
  public void saveCategory(Category category, boolean isNew) throws Exception {
    storage.saveCategory(category, isNew);
    for (ForumEventLifeCycle f : listeners_) {
      try {
        f.saveCategory(category);
      } catch (Exception e) {
        log.debug("Failed to run function saveCategory in the class ForumEventLifeCycle. ", e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void calculateModerator(String categoryPath, boolean isNew) throws Exception {
    storage.calculateModerator(categoryPath, false);
  }

  /**
   * {@inheritDoc}
   */
  public Category getCategory(String categoryId) {
    return storage.getCategory(categoryId);
  }
  
  public Category getCategoryIncludedSpace() {
    return storage.getCategoryIncludedSpace();
  }

  /**
   * {@inheritDoc}
   */
  public String[] getPermissionTopicByCategory(String categoryId, String type) throws Exception {
    return storage.getPermissionTopicByCategory(categoryId, type);
  }

  /**
   * {@inheritDoc}
   */
  public List<Category> getCategories() {
    return storage.getCategories();
  }

  /**
   * {@inheritDoc}
   */
  public Category removeCategory(String categoryId) throws Exception {
    List<Forum> listForums = getForums(categoryId, null);
    for (Forum forum : listForums) {
      String forumId = forum.getId();
      List<Topic> listTopics = getTopics(categoryId, forumId);
      for (Topic topic : listTopics) {
        String topicId = topic.getId();
        String topicActivityId = storage.getActivityIdForOwner(categoryId.concat("/").concat(forumId).concat("/").concat(topicId));
        for (ForumEventLifeCycle f : listeners_) {
          try {
            if (topic.getIsPoll()) {
              String pollActivityId = getActivityIdForOwnerPath(categoryId.concat("/").concat(forumId).concat("/").concat(topicId).concat("/").concat(topicId.replace(Utils.TOPIC, Utils.POLL)));
              f.removeActivity(pollActivityId);
            }
            f.removeActivity(topicActivityId);
          } catch (Exception e) {
            log.debug("Failed to run function removeActivity in the class ForumEventLifeCycle. ", e);
          }
        }
      }
    }
    return storage.removeCategory(categoryId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveModOfCategory(List<String> moderatorCate, String userId, boolean isAdd) {
    storage.saveModOfCategory(moderatorCate, userId, isAdd);
  }

  /**
   * {@inheritDoc}
   */
  public void modifyForum(Forum forum, int type) throws Exception {
    storage.modifyForum(forum, type);
    List<Topic> topics = getTopics(forum.getCategoryId(), forum.getId());
    for (ForumEventLifeCycle f : listeners_) {
      try {
        f.updateTopics(topics, forum.getIsLock());
      } catch (Exception e) {
        log.debug("Failed to run function updateTopic in the class ForumEventLifeCycle. ", e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception {
    storage.saveForum(categoryId, forum, isNew);
    for (ForumEventLifeCycle f : listeners_) {
      try {
        f.saveForum(forum);
      } catch (Exception e) {
        log.debug("Failed to run function saveForum in the class ForumEventLifeCycle. ", e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void saveModerateOfForums(List<String> forumPaths, String userName, boolean isDelete) throws Exception {
    storage.saveModerateOfForums(forumPaths, userName, isDelete);
  }

  /**
   * {@inheritDoc}
   */
  public void moveForum(List<Forum> forums, String destCategoryPath) throws Exception {
    storage.moveForum(forums, destCategoryPath);
  }

  public List<CategoryFilter> filterForumByName(String filterKey, String userName, int maxSize) throws Exception {
    return storage.filterForumByName(filterKey, userName, maxSize);
  }
  /**
   * {@inheritDoc}
   */
  public Forum getForum(String categoryId, String forumId){
    return storage.getForum(categoryId, forumId);
  }

  /**
   * {@inheritDoc}
   */
  public List<Forum> getForums(String categoryId, String strQuery) throws Exception {
    return storage.getForums(categoryId, strQuery);
  }

  /**
   * {@inheritDoc}
   */
  public List<Forum> getForumSummaries(String categoryId, String strQuery) throws Exception {
    return storage.getForumSummaries(categoryId, strQuery);
  }

  /**
   * {@inheritDoc}
   */
  public Forum removeForum(String categoryId, String forumId) throws Exception {
    List<Topic> listTopics = getTopics(categoryId, forumId);
    if (listTopics == null)
      return null;
    for (Topic topic : listTopics) {
      String topicId = topic.getId();
      String topicActivityId = storage.getActivityIdForOwner(categoryId.concat("/").concat(forumId).concat("/").concat(topicId));
      for (ForumEventLifeCycle f : listeners_) {
        try {
          if (topic.getIsPoll()) {
            String pollActivityId = getActivityIdForOwnerPath(categoryId.concat("/").concat(forumId).concat("/").concat(topicId).concat("/").concat(topicId.replace(Utils.TOPIC, Utils.POLL)));
            f.removeActivity(pollActivityId);
          }
          f.removeActivity(topicActivityId);
        } catch (Exception e) {
          log.debug("Failed to run function removeActivity in the class ForumEventLifeCycle. ", e);
        }
      }
    }
    return storage.removeForum(categoryId, forumId);
  }

  /**
   * {@inheritDoc}
   */
  public void modifyTopic(List<Topic> topics, int type) {
    //update case
    List<Topic> editeds = new ArrayList<Topic>();
    Topic edited = null;
    for(Topic topic : topics) {
      //
      try {
        edited = getTopic(topic.getCategoryId(), topic.getForumId(), topic.getId(), "");
      } catch (Exception e) {
       log.warn("Ca not get Topic for " + topic.getId());
      }
      
      //
      switch (type) {
        case Utils.CLOSE: {
          edited.setEditedIsClosed(topic.getIsClosed());
          editeds.add(edited);
          break;
        }
        case Utils.LOCK: {
          edited.setEditedIsLock(topic.getIsLock());
          editeds.add(edited);
          break;
        }
        case Utils.WAITING: {//CENSORING
          edited.setEditedIsWaiting(topic.getIsWaiting());
          editeds.add(edited);
          break;
        }
        case Utils.ACTIVE: {//HIDDEN & Showing
          edited.setEditedIsActive(topic.getIsActive());
          editeds.add(edited);
          break;
        }
        case Utils.APPROVE: {
          edited.setEditedIsApproved(topic.getIsApproved());
          editeds.add(edited);
          break;
        }
        case Utils.CHANGE_NAME: {
          edited.setEditedTopicName(topic.getTopicName());
          editeds.add(edited);
          break;
        }
        case Utils.VOTE_RATING: {
          edited.setEditedVoteRating(topic.getVoteRating());
          editeds.add(edited);
          break;
        }
      }
    }
    
    storage.modifyTopic(topics, type);
    for (ForumEventLifeCycle f : listeners_) {
      for(Topic topic : editeds) {
        try {
          f.updateTopic(topic);
        } catch (Exception e) {
          log.debug("Failed to run function updateTopic in the class ForumEventLifeCycle. ", e);
        }
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void modifyMergedTopic(List<Topic> topics, int type) {
    storage.modifyTopic(topics, type);
  }

  /**
   * 
   * @deprecated use {@link #saveTopic(String, String, Topic, boolean, boolean, MessageBuilder)}
   */
  @Deprecated
  public void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, String defaultEmailContent) throws Exception {
    saveTopic(categoryId, forumId, topic, isNew, isMove, new MessageBuilder());
  }

  public void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, MessageBuilder messageBuilder) throws Exception {
    //update case
    Topic edited = null;
    if(isNew == false) {
      edited = getTopic(categoryId, forumId, topic.getId(), "");
      edited.setEditedDescription(topic.getDescription());
      edited.setEditedTopicName(topic.getTopicName());
      edited.setEditedIsClosed(topic.getIsClosed());
      edited.setEditedIsLock(topic.getIsLock());
      edited.setEditedIsWaiting(topic.getIsWaiting());
    }
    storage.saveTopic(categoryId, forumId, topic, isNew, isMove, messageBuilder);
    //
    Callable<Boolean> callAble = new ForumEventCompletion.ProcessTopic(((isNew) ? topic : edited), isNew).setListeners(listeners_);
    completionService.addTask(callAble);
  }

  /**
   * {@inheritDoc}
   */
  public Topic getTopic(String categoryId, String forumId, String topicId, String userRead) throws Exception {
    return storage.getTopic(categoryId, forumId, topicId, userRead);
  }

  /**
   * {@inheritDoc}
   */
  public void setViewCountTopic(String path, String userRead){
    storage.setViewCountTopic(path, userRead);
  }

  /**
   * {@inheritDoc}
   */
  public void writeReads() {
    storage.writeReads();
  }

  /**
   * {@inheritDoc}
   */
  public Topic getLastPostOfForum(String topicPath) throws Exception {
    return storage.getTopicSummary(topicPath, true);
  }

  /**
   * {@inheritDoc}
   */
  public Topic getTopicSummary(String topicPath) throws Exception {
    return storage.getTopicSummary(topicPath);
  }

  /**
   * {@inheritDoc}
   */
  public Topic getTopicByPath(String topicPath, boolean isLastPost) throws Exception {
    return storage.getTopicByPath(topicPath, isLastPost);
  }

  public Topic getTopicUpdate(Topic topic, boolean isSummary) throws Exception {
    return storage.getTopicUpdate(topic, isSummary);
  }

  /**
   * @deprecated use {@link ForumServiceImpl#getTopics(TopicFilter);
   */
  public LazyPageList<Topic> getTopicList(String categoryId, String forumId, String strQuery, String strOrderBy, int pageSize) throws Exception {
    return storage.getTopicList(categoryId, forumId, strQuery, strOrderBy, pageSize);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPageTopic(String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception {
    return storage.getPageTopic(categoryId, forumId, strQuery, strOrderBy);
  }

  /**
   * @deprecated use {@link ForumServiceImpl#getTopics(TopicFilter);
   */
  public List<Topic> getTopics(String categoryId, String forumId) throws Exception {
    return storage.getTopics(categoryId, forumId);
  }
  

  @Override
  public ListAccess<Topic> getTopics(TopicFilter filter) throws Exception {
    return new TopicListAccess(storage, filter);
  }

  /**
   * {@inheritDoc}
   */
  public void moveTopic(List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception {
    storage.moveTopic(topics, destForumPath, mailContent, link);
    String toForumName = ((Forum) storage.getObjectNameByPath(destForumPath)).getForumName();
    String toCategoryName = ((Category) storage.getObjectNameByPath(Utils.getCategoryPath(destForumPath))).getCategoryName();
    for (ForumEventLifeCycle f : listeners_) {
      for (Topic topic : topics) {
        topic.setPath(destForumPath.concat("/").concat(topic.getId()));
        try {
          f.moveTopic(topic, toCategoryName, toForumName);
        } catch (Exception e) {
          log.debug("Failed to run function moveTopic in the class ForumEventLifeCycle. ", e);
        }
      }
    }
    CacheUserProfile.clearCache();
  }

  /**
   * {@inheritDoc}
   */
  public Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception {
    String topicActivityId = storage.getActivityIdForOwner(categoryId.concat("/").concat(forumId).concat("/").concat(topicId));
    Topic topic = getTopic(categoryId, forumId, topicId, "");
    String pollActivityId = null;
    if (topic.getIsPoll())
      pollActivityId = getActivityIdForOwnerPath(categoryId.concat("/").concat(forumId).concat("/").concat(topicId).concat("/").concat(topicId.replace(Utils.TOPIC, Utils.POLL)));
    topic = storage.removeTopic(categoryId, forumId, topicId);
    for (ForumEventLifeCycle f : listeners_) {
      try {
        if (pollActivityId != null) {
          f.removeActivity(pollActivityId);
        }
        f.removeActivity(topicActivityId);
      } catch (Exception e) {
        log.debug("Failed to run function removeActivity in the class ForumEventLifeCycle. ", e);
      }
    }
    CacheUserProfile.clearCache();
    return topic;
  }

  /**
   * {@inheritDoc}
   */
  public Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    return storage.getPost(categoryId, forumId, topicId, postId);
  }

  /**
   * {@inheritDoc}
   */
  public long getLastReadIndex(String path, String isApproved, String isHidden, String userLogin) throws Exception {
    return storage.getLastReadIndex(path, isApproved, isHidden, userLogin);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPostForSplitTopic(String topicPath) throws Exception {
    return storage.getPostForSplitTopic(topicPath);
  }

  /**
   * @deprecated use {@link ForumServiceImpl#getPosts(PostFilter filter);
   */
  public JCRPageList getPosts(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin) throws Exception {
    return storage.getPosts(categoryId, forumId, topicId, isApproved, isHidden, strQuery, userLogin);
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Post> getPosts(PostFilter filter) throws Exception {
    return new PostListAccess(PostListAccess.Type.POSTS, storage, filter);
  }

  /**
   * {@inheritDoc}
   */
  public long getAvailablePost(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String userLogin) throws Exception {
    PostFilter filter = new PostFilter(categoryId, forumId, topicId, isApproved, isHidden, isHidden, userLogin);
    return Long.valueOf(storage.getPostsCount(filter));
  }

  /**
   * 
   * @deprecated use {@link #savePost(String, String, String, Post, boolean, MessageBuilder)}
   */
  @Deprecated
  public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew, String defaultEmailContent) throws Exception {
    savePost(categoryId, forumId, topicId, post, isNew, new MessageBuilder());
  }

  public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew, MessageBuilder messageBuilder) throws Exception {
    storage.savePost(categoryId, forumId, topicId, post, isNew, messageBuilder);
    //
    Callable<Boolean> callAble = new ForumEventCompletion.ProcessPost(post, isNew).setListeners(listeners_);
    completionService.addTask(callAble);
  }

  /**
   * {@inheritDoc}
   */
  public void modifyPost(List<Post> posts, int type){
    storage.modifyPost(posts, type);
    for (ForumEventLifeCycle f : listeners_) {
      for(Post post : posts) {
        try {
          f.updatePost(post, type);
        } catch (Exception e) {
          log.debug("Failed to run function updatePost in the class ForumEventLifeCycle. ", e);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void movePost(List<Post> posts, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link) throws Exception {
    List<String> postPaths = new ArrayList<String>();
    for (Post p : posts) {
      postPaths.add(p.getPath());
    }
    movePost(postPaths.toArray(new String[postPaths.size()]), destTopicPath, isCreatNewTopic, mailContent, link);
  }

  /**
   * {@inheritDoc}
   */
  public void movePost(String[] postPaths, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link) throws Exception {
    storage.movePost(postPaths, destTopicPath, isCreatNewTopic, mailContent, link);
    CacheUserProfile.clearCache();
  }

  /**
   * {@inheritDoc}
   */
  public void mergeTopic(String srcTopicPath, String destTopicPath, String mailContent, String link, String topicMergeTitle) throws Exception {
    String srcActivityId = storage.getActivityIdForOwner(srcTopicPath);
    String destActivityId = storage.getActivityIdForOwner(destTopicPath);
    //
    storage.mergeTopic(srcTopicPath, destTopicPath, mailContent, link);
    //
    CacheUserProfile.clearCache();

    Topic newTopic = storage.getTopicByPath(destTopicPath, false);
    newTopic.setTopicName(topicMergeTitle);
    for (ForumEventLifeCycle f : listeners_) {
      try {
        f.mergeTopic(newTopic, srcActivityId, destActivityId);
      } catch (Exception e) {
        log.debug("Failed to run function mergeTopic in the class ForumEventLifeCycle. ", e);
      }
    }
  }

  public void splitTopic(Topic newTopic, Post fistPost, List<String> postPathMove, String mailContent, String link) throws Exception {
    String srcTopicPath = Utils.getTopicPath(postPathMove.get(0));
    storage.splitTopic(newTopic, fistPost, postPathMove, mailContent, link);
    String srcActivityId = storage.getActivityIdForOwner(srcTopicPath);
    Topic srcTopic = storage.getTopicByPath(srcTopicPath, false);
    for (ForumEventLifeCycle f : listeners_) {
      try {
        f.splitTopic(newTopic, srcTopic, srcActivityId);
      } catch (Exception e) {
        log.debug("Failed to run function splitTopic in the class ForumEventLifeCycle. ", e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public Post removePost(String categoryId, String forumId, String topicId, String postId) {
    CacheUserProfile.clearCache();
    String topicActivityId = storage.getActivityIdForOwner(categoryId.concat("/").concat(forumId).concat("/").concat(topicId));
    String postActivityId = storage.getActivityIdForOwner(categoryId.concat("/").concat(forumId).concat("/").concat(topicId).concat("/").concat(postId));
    Post deleted = storage.removePost(categoryId, forumId, topicId, postId);
    
    //
    for (ForumEventLifeCycle f : listeners_) {
      try {
        f.removeComment(topicActivityId, postActivityId);
      } catch (Exception e) {
        log.debug("Failed to run function removeComment in the class ForumEventLifeCycle. ", e);
      }
    }
    
    return deleted;
  }

  /**
   * {@inheritDoc}
   */
  public Object getObjectNameByPath(String path) throws Exception {
    return storage.getObjectNameByPath(path);
  }

  /**
   * {@inheritDoc}
   */
  public Object getObjectNameById(String path, String type) throws Exception {
    return storage.getObjectNameById(path, type);
  }

  /**
   * {@inheritDoc}
   */
  public List<ForumLinkData> getAllLink(String strQueryCate, String strQueryForum) throws Exception {
    return storage.getAllLink(strQueryCate, strQueryForum);
  }

  /**
   * {@inheritDoc}
   */
  public String getForumHomePath() throws Exception {
    return storage.getDataLocation().getForumHomeLocation();
  }

  /**
   * {@inheritDoc}
   */
  /*
   * public Poll getPoll(String categoryId, String forumId, String topicId) throws Exception { return storage.getPoll(categoryId, forumId, topicId) ; }
   *//**
     * {@inheritDoc}
     */
  /*
   * public Poll removePoll(String categoryId, String forumId, String topicId) throws Exception { return storage.removePoll(categoryId, forumId, topicId); }
   *//**
     * {@inheritDoc}
     */
  /*
   * public void savePoll(String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote) throws Exception { storage.savePoll(categoryId, forumId, topicId, poll, isNew, isVote) ; }
   *//**
     * {@inheritDoc}
     */
  /*
   * public void setClosedPoll(String categoryId, String forumId, String topicId, Poll poll) throws Exception { storage.setClosedPoll(categoryId, forumId, topicId, poll) ; }
   */
  /**
   * {@inheritDoc}
   */
  public void addTag(List<Tag> tags, String userName, String topicPath) throws Exception {
    storage.addTag(tags, userName, topicPath);
  }

  /**
   * {@inheritDoc}
   */
  public List<Tag> getAllTags() throws Exception {
    return storage.getAllTags();
  }

  /**
   * {@inheritDoc}
   */
  public List<Tag> getMyTagInTopic(String[] tagIds) throws Exception {
    return storage.getMyTagInTopic(tagIds);
  }

  /**
   * {@inheritDoc}
   */
  public Tag getTag(String tagId) throws Exception {
    return storage.getTag(tagId);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getAllTagName(String strQuery, String userAndTopicId) throws Exception {
    return storage.getAllTagName(strQuery, userAndTopicId);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getTagNameInTopic(String userAndTopicId) throws Exception {
    return storage.getTagNameInTopic(userAndTopicId);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getTopicByMyTag(String userIdAndtagId, String strOrderBy) throws Exception {
    return storage.getTopicByMyTag(userIdAndtagId, strOrderBy);
  }

  /**
   * {@inheritDoc}
   */
  public void saveTag(Tag newTag) throws Exception {
    storage.saveTag(newTag);
  }

  /**
   * {@inheritDoc}
   */
  public void unTag(String tagId, String userName, String topicPath) {
    storage.unTag(tagId, userName, topicPath);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserModerator(String userName, List<String> ids, boolean isModeCate) throws Exception {
    storage.saveUserModerator(userName, ids, isModeCate);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserProfile(UserProfile userProfile, boolean isOption, boolean isBan) throws Exception {
    String userId = userProfile.getUserId();
    storage.saveUserProfile(userProfile, isOption, isBan);
    removeCacheUserProfile(userId);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile getUserInfo(String userName) throws Exception {
    return storage.getUserInfo(userName);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getUserModerator(String userName, boolean isModeCate) throws Exception {
    return storage.getUserModerator(userName, isModeCate);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile getUserProfileManagement(String userName) throws Exception {
    return storage.getUserProfileManagement(userName);
  }

  /**
   * {@inheritDoc}
   */
  public void saveLastPostIdRead(String userId, String[] lastReadPostOfForum, String[] lastReadPostOfTopic) throws Exception {
    storage.saveLastPostIdRead(userId, lastReadPostOfForum, lastReadPostOfTopic);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserBookmark(String userName, String bookMark, boolean isNew) throws Exception {
    storage.saveUserBookmark(userName, bookMark, isNew);
    removeCacheUserProfile(userName);
  }

  /**
   * {@inheritDoc}
   */
  public void saveCollapsedCategories(String userName, String categoryId, boolean isAdd) throws Exception {
    storage.saveCollapsedCategories(userName, categoryId, isAdd);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPageListUserProfile() throws Exception {
    return storage.getPageListUserProfile();
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPrivateMessage(String userName, String type) throws Exception {
    return storage.getPrivateMessage(userName, type);
  }

  /**
   * {@inheritDoc}
   */
  public long getNewPrivateMessage(String userName) throws Exception {
    return storage.getNewPrivateMessage(userName);
  }

  /**
   * {@inheritDoc}
   */
  public void removePrivateMessage(String messageId, String userName, String type) throws Exception {
    storage.removePrivateMessage(messageId, userName, type);
  }

  /**
   * {@inheritDoc}
   */
  public void saveReadMessage(String messageId, String userName, String type) throws Exception {
    storage.saveReadMessage(messageId, userName, type);
  }

  /**
   * {@inheritDoc}
   */
  public void savePrivateMessage(ForumPrivateMessage privateMessage) throws Exception {
    storage.savePrivateMessage(privateMessage);
  }

  /**
   * {@inheritDoc}
   */
  public ForumSubscription getForumSubscription(String userId) {
    return storage.getForumSubscription(userId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveForumSubscription(ForumSubscription forumSubscription, String userId) throws Exception {
    storage.saveForumSubscription(forumSubscription, userId);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPageTopicOld(long date, String forumPatch) throws Exception {
    return storage.getPageTopicOld(date, forumPatch);
  }

  /**
   * {@inheritDoc}
   */
  public List<Topic> getAllTopicsOld(long date, String forumPatch) throws Exception {
    return storage.getAllTopicsOld(date, forumPatch);
  }

  /**
   * {@inheritDoc}
   */
  public long getTotalTopicOld(long date, String forumPatch) {
    return storage.getTotalTopicOld(date, forumPatch);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPageTopicByUser(String userName, boolean isMod, String strOrderBy) throws Exception {
    return storage.getPageTopicByUser(userName, isMod, strOrderBy);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPagePostByUser(String userName, String userId, boolean isMod, String strOrderBy) throws Exception {
    return storage.getPagePostByUser(userName, userId, isMod, strOrderBy);
  }

  /**
   * {@inheritDoc}
   */
  public ForumStatistic getForumStatistic() throws Exception {
    return storage.getForumStatistic();
  }

  /**
   * {@inheritDoc}
   */
  public void saveForumStatistic(ForumStatistic forumStatistic) throws Exception {
    storage.saveForumStatistic(forumStatistic);
  }

  /**
   * {@inheritDoc}
   */
  public void updateStatisticCounts(long topicCount, long postCount) throws Exception {
    storage.updateStatisticCounts(topicCount, postCount);
  }

  /**
   * {@inheritDoc}
   */
  public List<ForumSearchResult> getQuickSearch(String textQuery, String type, String pathQuery, String userId, List<String> listCateIds, List<String> listForumIds, List<String> forumIdsOfModerator) throws Exception {
    return storage.getQuickSearch(textQuery, type, pathQuery, userId, listCateIds, listForumIds, forumIdsOfModerator);
  }

  /**
   * {@inheritDoc}
   */
  public List<ForumSearchResult> getAdvancedSearch(ForumEventQuery eventQuery, List<String> listCateIds, List<String> listForumIds){
    return storage.getAdvancedSearch(eventQuery, listCateIds, listForumIds);
  }

  /**
   * {@inheritDoc}
   */
  public ForumAdministration getForumAdministration() throws Exception {
    return storage.getForumAdministration();
  }

  /**
   * {@inheritDoc}
   */
  public void saveForumAdministration(ForumAdministration forumAdministration) throws Exception {
    storage.saveForumAdministration(forumAdministration);
  }

  /**
   * {@inheritDoc}
   */
  public void addWatch(int watchType, String path, List<String> values, String currentUser) throws Exception {
    storage.addWatch(watchType, path, values, currentUser);
  }

  /**
   * {@inheritDoc}
   */
  public void removeWatch(int watchType, String path, String values) throws Exception {
    storage.removeWatch(watchType, path, values);
  }

  /**
   * {@inheritDoc}
   */
  public List<ForumSearchResult> getJobWattingForModerator(String[] paths){
    return storage.getJobWattingForModerator(paths);
  }

  /**
   * {@inheritDoc}
   */
  public int getJobWattingForModeratorByUser(String userId) throws Exception {
    return storage.getJobWattingForModeratorByUser(userId);
  }

  /**
   * {@inheritDoc}
   */
  public void updateLoggedinUsers() throws Exception {
    UserLoginLogEntry loginEntry = queue.poll();
    if (loginEntry == null)
      return;
    int maxOnline = 1;
    Calendar timestamp = loginEntry.loginTime;
    while (loginEntry != null) {
      if(Utils.getCurrentTenantName().equals(loginEntry.tenantName)) {
        storage.updateLastLoginDate(loginEntry.userName);
        if (loginEntry.totalOnline > maxOnline) {
          maxOnline = loginEntry.totalOnline;
          timestamp = loginEntry.loginTime;
        }
      }
      loginEntry = queue.poll();
    }

    ForumStatistic stats = storage.getForumStatistic();
    int mostOnline = 0;
    String mostUsersOnline = stats.getMostUsersOnline();
    if (mostUsersOnline != null && mostUsersOnline.length() > 0) {
      String[] array = mostUsersOnline.split(",");
      try {
        mostOnline = Integer.parseInt(array[0].trim());
      } catch (NumberFormatException e) {
        mostOnline = 0;
      }
    }
    if (maxOnline > mostOnline) {
      stats.setMostUsersOnline(maxOnline + "," + timestamp.getTimeInMillis());
    } else if (mostOnline == 0) {
      // this case is expected to appear when the first user logins to system and the statistic is not activated before.
      // the maximum number of online users will jump from N/A to 1
      stats.setMostUsersOnline("1," + timestamp.getTimeInMillis());
    }
    storage.saveForumStatistic(stats);

  }

  /**
   * {@inheritDoc}
   */
  public void userLogin(String userId) throws Exception {
    // Note: login and onlineUserlist shoudl be anaged by forumStatisticsService.memberIn();
    String currentTenant = Utils.getCurrentTenantName();
    lastLoginMap.put(currentTenant, userId);
    List<String> onlinUsers = Utils.getOnlineUserByTenantName(onlineUserMap);
    if (!onlinUsers.contains(userId)) {
      onlinUsers.add(userId);
      onlineUserMap.put(currentTenant, onlinUsers);
    }
    UserLoginLogEntry loginEntry = new UserLoginLogEntry(currentTenant, userId, onlinUsers.size(), 
                                                         CommonUtils.getGreenwichMeanTime());
    queue.add(loginEntry);
    CacheUserProfile.storeInCache(userId, storage.getDefaultUserProfile(userId, null));
  }

  /**
   * {@inheritDoc}
   */
  public void userLogout(String userId) throws Exception {
    List<String> onlinUsers = Utils.getOnlineUserByTenantName(onlineUserMap);
    if (onlinUsers.contains(userId)) {
      onlinUsers.remove(userId);
      onlineUserMap.put(Utils.getCurrentTenantName(), onlinUsers);
    }
    removeCacheUserProfile(userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isOnline(String userId) throws Exception {
    return Utils.getOnlineUserByTenantName(onlineUserMap).contains(userId);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getOnlineUsers() throws Exception {
    return Utils.getOnlineUserByTenantName(onlineUserMap);
  }

  /**
   * {@inheritDoc}
   */
  public String getLastLogin() throws Exception {
    return lastLoginMap.get(Utils.getCurrentTenantName());
  }

  /**
   * {@inheritDoc}
   */
  public SendMessageInfo getMessageInfo(String name) throws Exception {
    return storage.getMessageInfo(name);
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<SendMessageInfo> getPendingMessages() throws Exception {
    return storage.getPendingMessages();
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList searchUserProfile(String userSearch) throws Exception {
    return storage.searchUserProfile(userSearch);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAdminRole(String userName) throws Exception {
    return storage.isAdminRole(userName);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isAdminRoleConfig(String userName) throws Exception {
    return storage.isAdminRoleConfig(userName);
  }

  /**
   * {@inheritDoc}
   */
  public List<Post> getNewPosts(int number) throws Exception {
    return storage.getNewPosts(number);
  }

  /**
   * {@inheritDoc}
   */
  public List<Post> getRecentPostsForUser(String userName, int number) throws Exception {
    return storage.getRecentPostsForUser(userName, number);
  }

  public NodeIterator search(String queryString) throws Exception {
    return storage.search(queryString);
  }

  /**
   * {@inheritDoc}
   */
  public void evaluateActiveUsers(String query) {
    storage.evaluateActiveUsers(query);
  }

  /**
   * {@inheritDoc}
   */
  public void updateTopicAccess(String userId, String topicId) {
    storage.updateTopicAccess(userId, topicId);
  }

  /**
   * {@inheritDoc}
   */
  public void updateForumAccess(String userId, String forumId){
    storage.updateForumAccess(userId, forumId);
  }

  /**
   * {@inheritDoc}
   */
  public void writeViews() {
    storage.writeViews();
  }

  /**
   * {@inheritDoc}
   */
  public Object exportXML(String categoryId, String forumId, List<String> objectIds, String nodePath, ByteArrayOutputStream bos, boolean isExportAll) throws Exception {
    return storage.exportXML(categoryId, forumId, objectIds, nodePath, bos, isExportAll);
  }

  /**
   * {@inheritDoc}
   */
  public List<UserProfile> getQuickProfiles(List<String> userList) throws Exception {
    return storage.getQuickProfiles(userList);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile getQuickProfile(String userName) throws Exception {
    return storage.getQuickProfile(userName);
  }

  /**
   * {@inheritDoc}
   */
  public String getScreenName(String userName) throws Exception {
    return storage.getScreenName(userName);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile getUserInformations(UserProfile userProfile) throws Exception {
    return storage.getUserInformations(userProfile);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile getDefaultUserProfile(String userName, String ip) throws Exception {
    UserProfile userProfile = CacheUserProfile.getFromCache(userName);
    if (userProfile == null) {
      userProfile = storage.getDefaultUserProfile(userName, null);
      CacheUserProfile.storeInCache(userName, userProfile);
    }
    if (!userProfile.getIsBanned() && ip != null) {
      userProfile.setIsBanned(storage.isBanIp(ip));
    }
    return userProfile;
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile updateUserProfileSetting(UserProfile userProfile) throws Exception {
    return storage.updateUserProfileSetting(userProfile);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getBookmarks(String userName) throws Exception {
    return storage.getBookmarks(userName);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile getUserSettingProfile(String userName) throws Exception {
    return storage.getUserSettingProfile(userName);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserSettingProfile(UserProfile userProfile) throws Exception {
    storage.saveUserSettingProfile(userProfile);
    removeCacheUserProfile(userProfile.getUserId());
  }

  /**
   * {@inheritDoc}
   */
  public void importXML(String nodePath, ByteArrayInputStream bis, int typeImport) throws Exception {
    storage.importXML(nodePath, bis, typeImport);
  }

  /**
   * {@inheritDoc}
  public void updateDataImported() throws Exception{
    storage.updateDataImported();
  }
   */

  /**
   * {@inheritDoc}
   */
  public void updateForum(String path) throws Exception {
    storage.updateForum(path);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getBanList() throws Exception {
    return storage.getBanList();
  }

  /**
   * {@inheritDoc}
   */
  public boolean addBanIP(String ip) throws Exception {
    return storage.addBanIP(ip);
  }

  /**
   * {@inheritDoc}
   */
  public void removeBan(String ip) throws Exception {
    storage.removeBan(ip);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getListPostsByIP(String ip, String strOrderBy) throws Exception {
    return storage.getListPostsByIP(ip, strOrderBy);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getForumBanList(String forumId) throws Exception {
    return storage.getForumBanList(forumId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean addBanIPForum(String ip, String forumId) throws Exception {
    return storage.addBanIPForum(ip, forumId);
  }

  /**
   * {@inheritDoc}
   */
  public void removeBanIPForum(String ip, String forumId) throws Exception {
    storage.removeBanIPForum(ip, forumId);
  }

  /**
   * {@inheritDoc}
   */
  public void registerListenerForCategory(String categoryId) throws Exception {
    storage.registerListenerForCategory(categoryId);
  }

  /**
   * {@inheritDoc}
   */
  public void unRegisterListenerForCategory(String path) throws Exception {
    storage.unRegisterListenerForCategory(path);
  }

  /**
   * {@inheritDoc}
   */
  public ForumAttachment getUserAvatar(String userName) throws Exception {
    return storage.getUserAvatar(userName);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserAvatar(String userId, ForumAttachment fileAttachment) throws Exception {
    storage.saveUserAvatar(userId, fileAttachment);
  }

  /**
   * {@inheritDoc}
   */
  public void setDefaultAvatar(String userName) {
    storage.setDefaultAvatar(userName);
  }

  /**
   * {@inheritDoc}
   */
  public List<Watch> getWatchByUser(String userId) throws Exception {
    return storage.getWatchByUser(userId);
  }

  /**
   * {@inheritDoc}
   */
  public void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId) throws Exception {
    storage.updateEmailWatch(listNodeId, newEmailAdd, userId);
  }

  /**
   * {@inheritDoc}
   */
  public List<PruneSetting> getAllPruneSetting() throws Exception {
    return storage.getAllPruneSetting();
  }

  /**
   * {@inheritDoc}
   */
  public void savePruneSetting(PruneSetting pruneSetting) throws Exception {
    storage.savePruneSetting(pruneSetting);
  }

  /**
   * {@inheritDoc}
   */
  public PruneSetting getPruneSetting(String forumPath) throws Exception {
    return storage.getPruneSetting(forumPath);
  }

  /**
   * {@inheritDoc}
   */
  public void runPrune(PruneSetting pSetting) throws Exception {
    storage.runPrune(pSetting);
  }

  /**
   * {@inheritDoc}
   */
  public void runPrune(String forumPath) throws Exception {
    storage.runPrune(forumPath);
  }

  /**
   * {@inheritDoc}
   */
  public long checkPrune(PruneSetting pSetting) throws Exception {
    return storage.checkPrune(pSetting);
  }

  /**
   * {@inheritDoc}
   */
  public void updateUserProfileInfo(String name) throws Exception {
    storage.updateUserProfileInfo(name);
  }

  public DataStorage getStorage() {
    return storage;
  }

  public void setStorage(DataStorage storage) {
    this.storage = storage;
  }

  public ForumServiceManaged getManagementView() {
    return managementView;
  }

  public void setManagementView(ForumServiceManaged managementView) {
    this.managementView = managementView;
  }

  public ForumStatisticsService getForumStatisticsService() {
    return forumStatisticsService;
  }

  public void setForumStatisticsService(ForumStatisticsService forumStatisticsService) {
    this.forumStatisticsService = forumStatisticsService;
  }

  public JobSchedulerService getJobSchedulerService() {
    return jobSchedulerService;
  }

  public void setJobSchedulerService(JobSchedulerService jobSchedulerService) {
    this.jobSchedulerService = jobSchedulerService;
  }

  public InputStream createForumRss(String objectId, String link) throws Exception {
    return storage.createForumRss(objectId, link);
  }

  public InputStream createUserRss(String userId, String link) throws Exception {
    return storage.createUserRss(userId, link);
  }

  public void addListenerPlugin(ForumEventListener listener) throws Exception {
    listeners_.add(listener);
  }
  
  public void removeCacheUserProfile(String userName) {
    UserProfile userProfile = CacheUserProfile.getFromCache(userName);
    if (userProfile != null && UserProfile.USER_DELETED != userProfile.getUserRole()) {
      CacheUserProfile.removeInCache(userName);
    }
  }

  public void saveActivityIdForOwnerId(String ownerId,  String activityId) {
    storage.saveActivityIdForOwner(ownerId, Utils.TOPIC, activityId);
  }

  public void saveActivityIdForOwnerPath(String ownerPath, String activityId) {
    storage.saveActivityIdForOwner(ownerPath, activityId);
  }

  public String getActivityIdForOwnerId(String ownerId) {
    return storage.getActivityIdForOwner(ownerId, Utils.TOPIC);
  }

  public String getActivityIdForOwnerPath(String ownerPath) {
    return storage.getActivityIdForOwner(ownerPath);
  }
  
  public void saveCommentIdForOwnerId(String ownerId,  String commentId) {
    storage.saveActivityIdForOwner(ownerId, Utils.POST, commentId);
  }

  public void saveCommentIdForOwnerPath(String ownerPath, String commentId) {
    storage.saveActivityIdForOwner(ownerPath, commentId);
  }

  public String getCommentIdForOwnerId(String ownerId) {
    return storage.getActivityIdForOwner(ownerId, Utils.POST);
  }

  public String getCommentIdForOwnerPath(String ownerPath) {
    return storage.getActivityIdForOwner(ownerPath);
  }

  private String ids = "topic8402a0d0c0a805095625162ecf2e4c5c,topic8402a2d2c0a8050901f81ac84fd9c9d9,topic840b0d69c0a805096d7109a884b57324,topic840b0f4fc0a805091bc5086f0408b8b6,topic840b1100c0a805097743bdfbb99a3cd3,topic840b12d6c0a8050961e0c71cdfa4f238,topic840b15a1c0a80509679a3d5568f5f0bb,topic840b1785c0a80509369942f6598432c4,topic840b192dc0a8050960f9b7a90c658df2,topic840b1b6cc0a805093d1d11de82a7b6e1,topic840b1e6ec0a805095de89ccd57dc0732,topic840b2014c0a80509298476035da4e98e,topic840b22fec0a805095581c5b69a692a43,topic840b24ccc0a80509073db82a909715bd,topic840b25eec0a805093e5bc7c07490cfe3,topic840b271ac0a805093fc502a254e10654,topic840b284bc0a805096f031e16dd6dc82e,topic840b2958c0a8050910e4edd299e91be4,topic840b2a8ac0a805095ffec59dd27e95a4,topic840b2bb5c0a805095e02afe3e25cbfed,topic840b2cb2c0a805096f08c4318df644a4,topic840b2d9dc0a8050939f07d8666bd9517,topic840b2ff1c0a8050951601c1195d34a51,topic840b30f2c0a80509158b3466a900e42a,topic840b3284c0a805092b697541c173478a,topic840b33a1c0a8050951ea0eace9431f6a,topic840b34bcc0a805094fc9d9ca6085c531,topic840b359ec0a80509733222778a7ab9c7,topic840b36cfc0a805096c05659623d1f73f,topic840b37f9c0a80509087f222cf115cccd,topic840b3911c0a805095e964b273e9d0940,topic840b3a0dc0a805090742b7b5c8825236,topic840b3c53c0a8050959e56bcabda8cbdb,topic840b3d61c0a80509359c6ed4a75c3009,topic840b3eb2c0a8050916f38fd0de39499a,topic840b3fc2c0a805091341026245476bd0,topic840b40f6c0a80509488b0bc14f47cda2,topic840b4205c0a8050913c6bf9aaf260939,topic840b44a9c0a805094d440aa650cc7b40,topic840b45f5c0a805091e6466fec718d5e6,topic840b4832c0a805092569462a8db95f8e,topic840b4940c0a805097f011ce6de818101,topic840b4cc2c0a8050947d7b5f931f7f77e,topic840b4de8c0a80509219de45f55110710,topic840b4ef8c0a805091c6877e0f259c84b,topic840b5023c0a8050912d854dba81497f1,topic840b5142c0a80509440d0016b1d311bb,topic840b5263c0a8050957b949f89aed4a3d,topic840b5372c0a8050925cf17041982b2c0,topic840b55aac0a805093b04535401d3b51f,topic840b56bdc0a8050942e41839ded547d1,topic840b57d9c0a80509609a439ee9474797,topic840b5a65c0a805095e5836e41aa08574,topic840b5b9fc0a805091e03a1e609e140ad,topic840b5cc1c0a805090e4e8a085aa0aef0,topic840b5de5c0a805090a53fe2f92bb1dcb,topic840b6022c0a8050914de1a407ab3faa6,topic840b613fc0a805092099e272991ad03b,topic840b6291c0a805097b61249f48225211,topic840b63a6c0a805091dec17108713ae82,topic840b64bec0a8050936f856a9237e0692,topic840b663cc0a805097c5cbd579066e4ad,topic840b68a9c0a805091452dd42d0564f43,topic840b69c7c0a8050936239a6f529e36eb,topic840b6be6c0a8050940f2129bccbc50ca,topic840b6d2dc0a805094d6f56363fd6b6e0,topic840b6e45c0a805090d5cfd132fb518ad,topic840b6f5dc0a80509136ab0f8c9eaff53,topic840b7158c0a8050968515b3fc24d6ce2,topic840b7279c0a805090f811e4c3fdb98e7,topic840b73c6c0a805096641171b80367f0d,topic840b74dfc0a805096846137ed7205862,topic840b774ac0a80509493c3b75f64af9dc,topic840b7953c0a80509018d227538234bcf,topic840b7a8bc0a8050921b8c8ddf738a5af,topic840b7ba1c0a80509097eff82c98b7601,topic840b7d52c0a8050926bca36016328200,topic840b7e9fc0a805096132e50c73517c33,topic840b7fbfc0a8050943fbbd7d41dc20a6,topic840b80e9c0a805092b38c8d412d06fa6,topic840b822ac0a805095d55e26bdd38bd1f,topic840b839cc0a8050917f29bddb25790c8,topic840b85eec0a80509513c39862a95040b,topic840b87b7c0a805090b786b1f6399c864,topic840b88f0c0a805094342f4d37cd9f1c3,topic840b8a0ec0a805093436bbb0514e706d,topic840b8c6fc0a805097c80545518a29dfa,topic840b8da5c0a8050968ebc3e419eefdca,topic840b8ebec0a8050954a9d124af4abc39,topic840b8ff1c0a805091396550af5e0c25b,topic840b9136c0a805093dbe0b6a062d35c0,topic840b9363c0a8050947185591c5f324b8,topic840b95f9c0a8050977530fcde6076882,topic840b9726c0a8050915b26bada2405e2a,topic840b985dc0a805096244288e0f3cf79e,topic840b9996c0a80509583ebd3df26957ed,topic840b9ad2c0a805094b17bdb4cc884e2f,topic840b9c07c0a805096da277d20341cc6b,topic840b9d6dc0a805096d418ae6c9571887,topic840b9e99c0a8050952c8bea092a3606e,topic840b9fc5c0a805091dde5d2b423e8d1c,topic840ba1ebc0a80509169618bf17775d7f,topic841423abc0a80509309be7bc27dc4c45,topic84142551c0a805095637b7229ebb209a,topic841427f1c0a8050914838ada4b16e832,topic841429adc0a80509425e317a52186bfe,topic84142debc0a805095de195b43bdb26fa,topic84142fb2c0a805096fa1265af7b335cc,topic8414315ac0a80509738c40cfd8bbc244,topic84143347c0a805095f6c7d40ba429256,topic841434fbc0a8050948ef3fb4d83b6434,topic841436fbc0a8050902908cd91ba085d9,topic841439c3c0a8050956ab8a4ed98314d1,topic84143affc0a8050913dac2b4235d9c99,topic84143bf7c0a80509096287fa5db2997d,topic84143d1ac0a8050914b90d7edb0cb8d5,topic84143f1cc0a8050974ef7cdf3cb4a7de,topic84144048c0a80509541b12e3895a5743,topic84144173c0a80509347e6658595c5233,topic8414439ac0a8050939474520d5cf9f77,topic8414457fc0a805092917856379dbea5f,topic8414468fc0a8050904476d0011dfec7d,topic8414494fc0a805097c28e4dbc7967dcd,topic84144a5bc0a805093232545ff7adf39b,topic84144b92c0a805091c049b5a2735cbb2,topic84144ca7c0a805095081a5fbb77af9f1,topic84144ed1c0a805091365efbb00e0de62,topic84144ff2c0a80509267f76b36ff23c4b,topic84145108c0a8050908fdee85a2c418d9,topic8414524ac0a8050971097a615a93ab08,topic8414535dc0a8050967a0af57aba8b8df,topic8414546fc0a8050962983fbcc48711ed,topic841456b4c0a805096e1932e6ca99d0e6,topic841457e3c0a8050911a25bad5bf05d3e,topic8414591dc0a8050910b2ffc152b07f56,topic84145a3dc0a805093c1f0b4cd59be6da,topic84145b6fc0a805091df1855e913d7e47,topic84145c85c0a805094f2f6be8b8f4374f,topic84145de8c0a8050908284efa2cf9c782,topic84145efac0a80509651be2d176a8da2c,topic8414600ac0a8050977e4b42dcddb6686,topic84146140c0a80509298931c021fad767,topic84146380c0a805093e27c2bbd1fba8c6,topic84146494c0a805094259ae71462eb6a7,topic8414671dc0a8050948fcb967068bade1,topic84146849c0a8050954f98ff47e539cc3,topic8414695fc0a805090a13edeaa3cd9d4e,topic84146ab4c0a805094a466b3b20efa853,topic84146bc9c0a805094e622332a6bee99a,topic84146e2ec0a805093a934a44cb41176f,topic84146f8cc0a805097e0a029075692a77,topic841470a6c0a805092cb9b2c6ea883598,topic841472f3c0a8050937fe5c2f21645a17,topic84147496c0a805091b627772b60c016f,topic841476bec0a8050941d3b43119123a6e,topic841477e2c0a805097bf3772d8c5e84f7,topic84147935c0a80509793881a404f639ad,topic84147a4bc0a80509670d2dcd645981f1,topic84147b8ac0a8050976f056667a6c8142,topic84147cb6c0a8050900379623cf2ab390,topic84147dd0c0a8050974f0a27aa5c2fb26,topic84147f3bc0a80509116830079f2bf41b,topic841481a1c0a8050962ad649b51293eae,topic841482cec0a8050961435e375829737b,topic841484e2c0a8050940c2bad75a53294b,topic84148629c0a805093b5a48eea001a20e,topic8414873bc0a805090472e9d76b3933d1,topic8414884cc0a805094e54d6603d4f43fb,topic84148967c0a8050939e0d433e2420705,topic84148b97c0a805093b1034a963e38c40,topic84148cd4c0a805094a535231bb9bcfc1,topic84148debc0a805092a3184f82770e1fc,topic841491f4c0a8050973c9eb9e16e1dcf9,topic8414931ac0a8050931356d60c8eeb6c0,topic84149467c0a805095d7c8f1e8b960f7f,topic8414957ec0a8050912b0ac900bc49a89,topic841496bbc0a805094771bdbda675abcf,topic841497dfc0a8050914f2653ba4cb0a42,topic84149903c0a8050945726b1fa58fa233,topic84149acfc0a805095ea911c6858ff4bb,topic84149c32c0a805091083c425466d7e5c,topic84149e8fc0a8050947268864fc5343c0,topic8414a0e3c0a805094d33ab4d37581554,topic8414a248c0a805094b943baab8dd2a55,topic8414a379c0a805095ceb947141a664d2,topic8414a4c3c0a8050974bb5e7a9396f462,topic8414a68dc0a8050921e1d52c92ea8aa0,topic8414a83cc0a8050958d2a1dabd5024d6,topic8414a96ac0a805091e04a32783e98c6c,topic8414aac0c0a805092b906002ebf2d04a,topic8414abf5c0a805090b061d1517b5a945,topic8414ad2cc0a805091192c7cb82645880,topic8414af9bc0a805096d4f21ea8683ba01,topic8414b0e1c0a8050978f234edd5b5c3c7,topic8414b212c0a805091343226b07cce111,topic8414b36bc0a8050905120bdbe749895b,topic8414b578c0a805090ee5f897b11f6f63,topic8414b69cc0a805090704ebba66685380,topic8414b7d2c0a8050968bc0ba070a5cc9d,topic8414b8f6c0a80509139ef557800e2ab1,topic8414bb5dc0a805093f8aeaf4fc7f4519,topic8414bc6cc0a805090635b057155245a1,topic841db2fcc0a805093cd645a59cc5e742,topic841db4c1c0a805097605113232123137,topic841db6a0c0a805094596de252c24ce78,topic841db848c0a805093c4a80b0692ab9c0,topic841dba10c0a8050943e35b5ac618a79e,topic841dbbc9c0a805090b9db3a3a86c60f6,topic841dbdf6c0a80509161be339d3e2c6bd,topic841dc292c0a80509754877be259b2c1c,topic841dc469c0a805094366f8717257c025,topic841dc685c0a8050975a7892e128eb8bf,topic841dc9f3c0a80509199f1c1fd1fa664c,topic841dcb2dc0a805095de7772a54f4adfb,topic841dcd39c0a80509742a5a571feafa7f,topic841dce74c0a80509405676dc8f3b5bea,topic841dcf9fc0a80509163f5778ca799c02,topic841dd0e6c0a8050942580c05aa78b744,topic841dd209c0a805092e2ed80ea343a985,topic841dd342c0a805090a67e1ddfdfdc635,topic841dd470c0a8050935a5df1d6911b60d,topic841dd66cc0a805095d3a96052a0d22b9,topic841dd905c0a805090b8acb34262d5395,topic841ddb10c0a80509634cbf95dad28da8,topic841dddeac0a8050924c9e008443dbfdb,topic841ddf0dc0a805093637e3d5233f9203,topic841de039c0a80509734ce11d27067709,topic841de161c0a80509184a11bcb709bf78,topic841de290c0a805093996da2b995c0574,topic841de6a3c0a805094966f5b5b10a7536,topic841dfb0bc0a80509475e864b34d5199a,topic841dfd55c0a8050920c649eedcb9cf8f,topic841dffd4c0a8050942605de315fb4d7f,topic841e00efc0a8050940241a6b994a23fa,topic841e0222c0a80509329383e2ef4a8451,topic841e036bc0a805096225cb4a89aba990,topic841e05dbc0a80509562e2dbb43b81fab,topic841e06f4c0a8050978be981ce1d1ac03,topic841e0823c0a8050920ceff2d05c87514,topic841e0b36c0a80509325c10d9f28f5b6a,topic841e0c51c0a80509041af950a7b0a4eb,topic841e0d89c0a805096af9cc5847977087,topic841e10dec0a80509539158ad2b76beca,topic841e11fdc0a805092a1c154560ae4512,topic841e1427c0a805097e5077b9b49e67ac,topic841e1542c0a805095f17f19e09e2802c,topic841e165ec0a805090c71a39fcd5c8bb4,topic841e17d2c0a805095e0f0026c2a7c8bb,topic841e18fcc0a805091bd9229db07c15cd,topic841e1b49c0a8050952acf3427e07b2e0,topic841e1cafc0a805090e8343e09d04695e,topic841e1dcbc0a805093de0cb0d1d9e0aee,topic841e20a5c0a805095b66788639c37f8a,topic841e21cdc0a805090d871b449fd138fe,topic841e22e9c0a8050962c5b8be93caacca,topic841e2434c0a80509048e1ecfb30f6fd3,topic841e257ec0a8050956a961fef02734c0,topic841e26b3c0a8050925e507f57cd83ee4,topic841e27e0c0a8050917cae3f802fe3a38,topic841e2911c0a8050948b3e8f78f90e08f,topic841e2a58c0a8050962c1757134555c1b,topic841e2c55c0a8050973d1e2c0b451d04a,topic841e2f0ac0a805096ce3f4d57f1cff3e,topic841e3026c0a80509660e8bd07abc1eac,topic841e3142c0a8050952a78bda26e19bc4,topic841e3287c0a805093da39f808e510bdb,topic841e33f5c0a805095c9322307cbdfbbd,topic841e3539c0a805092e0204774ab3d8ff,topic841e375ec0a8050942264252eefff5a9,topic841e387fc0a8050902ddc63f5325d012,topic841e39e0c0a80509606a61dd1587884d,topic841e3aecc0a805097ac129762541204c,topic841e3d3ac0a805092f466ce3d784a1d0,topic841e3e68c0a805097d76f357988f775c,topic841e40c7c0a80509409281af05c022d5,topic841e4235c0a805096c60524297f79c36,topic841e435bc0a805096c211bf18ada1634,topic841e449fc0a805091da9a9844902fcb4,topic841e45c5c0a805090719aaa7c3e6b3e3,topic841e46fac0a805092df2a4eb5db3b744,topic841e4843c0a8050969ef399d28f0a622,topic841e4983c0a805092ee307061abd6460,topic841e4c0fc0a80509088014e73b488dce,topic841e4d58c0a80509088160feb0f2cbf3,topic841e4ebcc0a805095d38589cce76f6e0,topic841e4fd9c0a805094734b2d8e4d1cf2c,topic841e50f8c0a805090621e278afeebd7a,topic841e522ac0a805097667c4c52dd97b51,topic841e534ec0a805091981137a6a55ffac,topic841e5599c0a805096134f2452938e7cd,topic841e5710c0a8050907bd17f48b48ce11,topic841e5847c0a805095d193e46cb56024c,topic841e5b2ec0a805094d6c47044c25e9ec,topic841e5dbdc0a80509129ca14ac2c88f81,topic841e5ef4c0a805096252acf14fe451eb,topic841e612fc0a805096480d2d116355c9b,topic841e624fc0a805091b28f7f31c93ed5b,topic841e6393c0a805095e6b7491e42aedcd,topic841e64b4c0a805096392f9e2635100fd,topic841e65d4c0a8050956455c44f9c7d996,topic841e672bc0a805097a65456e44ce39a2,topic841e698ac0a8050947f5cf5a835f6210,topic8426eafdc0a805096e8095106c67ce7f,topic8426f265c0a8050906651341249598bc,topic8426f828c0a8050928142eaa726ea186,topic8426fe63c0a8050919453578d3a7917c,topic84270352c0a8050955c98310238f91d4,topic84270871c0a80509135ec272a53ed2ba,topic84270d30c0a805095a823c3264940705,topic84271164c0a8050965ca81c557ea4c7a,topic8427159bc0a80509705dcba87786d5c4,topic842719adc0a8050940e5276460ad973e,topic84271fe4c0a805095435ff43eb7b13fb,topic8427213dc0a805095828ebb17c086296,topic8427229fc0a8050913b4153e5b5464be,topic842724d5c0a805091c500af96de63cf2,topic84272628c0a805096ce8268e1aaaae93,topic8427275bc0a805092528b04b8ac46e43,topic84272891c0a8050962d1dcb01a58b9fc,topic842729e4c0a80509322b5edda59be015,topic84272b12c0a805090d88c39ff006a7dc,topic84272c62c0a805096c12ffae1faa9c29,topic84272ecec0a80509389776d1c447d3d5,topic8427302fc0a80509410da442c4fe93b1,topic842731bec0a80509028479b4126cd1fb,topic8427337dc0a8050925bc4d6f55445569,topic8427350bc0a805091b2bffe2ab8dc5ed,topic84273676c0a805095b9bba02a7bb4fe4,topic842739c0c0a805093e5f137a0bc4919b,topic84273c07c0a80509570f4876806a1fb3,topic84273d47c0a805091994888f7cd2a1bf,topic84273ea5c0a80509617a381ee5ad2483,topic84274202c0a8050905ffb0ea3ae9d3ef,topic84274353c0a805095fba04a611bff7aa,topic84274510c0a8050924cee544f562fe7a,topic8427463ec0a805090d3e3864e1c08f8c,topic842747b4c0a805094a6883c46e499f0e,topic842749dec0a805096cb85726c4a1ef5a,topic84274b4fc0a8050971dd867550bd63ef,topic84274c92c0a8050962b28d8c1c3f5f71,topic84274dd6c0a80509712146bf29711201,topic84274f4bc0a805097ba1704a96f8e392,topic8427521ec0a80509140a3b6d09335cda,topic8427547fc0a805090ba68365f98aa95d,topic84275611c0a80509226b6466ca69b116,topic8427574ac0a805092be4f938d277242e,topic8427589ac0a80509622ea622e5bda4cc,topic842759eec0a805091b9a6e723dfd325a,topic84275b42c0a8050906930dc4984b8899,topic84275c8fc0a805097800a8e95fc25a8d,topic84275ee5c0a805093482700552347919,topic84276034c0a805092363e613a2fb6326,topic84276295c0a8050965b25d4f0290a30b,topic84276406c0a805097b760e444b0f0841,topic8427657fc0a8050964ac966976c67263,topic842766b9c0a8050909e48a588e07d244,topic84276801c0a805092920134b2f006454,topic84276a1ec0a8050916cba20157faf6eb,topic84276c7dc0a80509712d73a9e44c6b2f,topic84276e0bc0a805092c8792dec719abf8,topic84276f42c0a80509197a21504cfade5d,topic84277186c0a80509022ac4c8d8b98566,topic84277402c0a80509700516f1a41948f2,topic84277541c0a805094df15b45bfd78550,topic842776abc0a8050979c1ca664bfbab31,topic842777e5c0a8050905040c864a376699,topic84277a71c0a805092a0f49c85bd083c6,topic84277bb3c0a805097c526e3045c7c2dc,topic84277d09c0a805097223969672887ace,topic84277ef5c0a8050973b409e2ed02aa79,topic8427802ac0a80509567d99d38b064ac5,topic84278181c0a805097487b7fde2528f24,topic8427850ac0a8050962d74f7ef919909f,topic8427866ac0a805091d2456cade56283b,topic842787b3c0a805092de1a3207644adf7,topic842788f8c0a805097d18c4c0774eb6fc,topic84278b79c0a805090d245688e51752a8,topic84278ce3c0a8050933560f0c689a3420,topic84278e1bc0a805093c3c7eaf5fe36aea,topic84278f98c0a805092e2146da93ef1e88,topic842791d1c0a805093769b9996ed18b3b,topic8427934dc0a805092305c2ea41707460,topic842795c1c0a805091d52b8fcea4d02bc,topic84279724c0a8050910c0b4a3ebdcfe58,topic8427985bc0a805097300489d2144beb5,topic84279ad9c0a8050949ac0f7946fa2ab3,topic84279c30c0a805091c8eef183e94a62d,topic84279d86c0a805090e06ec0724cb02a1,topic84279fe6c0a805090550a0a383657570,topic8427a138c0a80509774e3553d8d2dc96,topic8427a291c0a805091aecf7f757aa9194,topic8427a47dc0a805095f3e5aa99439767c,topic8427a6f3c0a805093d072ca5ff0b6361,topic8427a863c0a80509523e4980a77e359f,topic8427a9b3c0a8050971077fcfd96e843c,topic8427ab48c0a80509351d292306e8531a,topic8427acb4c0a805096ea6f3c71f31c630,topic8427adeec0a805091dec17ba979992ea,topic8427b1c0c0a805096d40fdde5dffe902,topic8427b562c0a805095dc18e5f195344c0,topic8427b92cc0a8050929ec53160e359717,topic8427bad7c0a805097c935f3aaff38341,topic84314995c0a80509609933033cb1f978,topic84315106c0a805093829cd8f4d880989,topic8431564cc0a805097bc32ea0983af6b0,topic84315ae9c0a805096fa92e3bc5935018,topic84315f69c0a805090a4a0a8b70350efd,topic843164ffc0a8050930fd84c40340dd7e,topic84316929c0a805090d24a5039f7d816e,topic84316d7fc0a805091f3bea636d05df15,topic843171a9c0a80509644f83adc64e9f47,topic84317580c0a8050976999a74493dbebb,topic84317ae5c0a80509606e2b3998e8cdfa,topic84317c38c0a8050915f5e86a354be0e8,topic84317d7fc0a8050947b920a42aa29924,topic84317ecdc0a805091ee31a1b983f484a,topic843180dbc0a805094397e6e4d80d5625,topic84318292c0a805093bc994592c030f35,topic843185dec0a8050952b272d367df0999,topic84318733c0a8050964cbd51c7cfd3166,topic8431899cc0a805094091311c330e1de6,topic84318aebc0a8050951f5955daa9afba2,topic84318d65c0a8050943f3eb6d2cd8ed17,topic84318ed6c0a805096fce6271f2b69017,topic84319016c0a80509481bec5ea32344b5,topic84319178c0a805096b3e998776df4291,topic843192d1c0a805097e2d2224e556782a,topic84319425c0a805094b17e4b871396c2d,topic8431963ec0a8050943bf5f138ccd8759,topic843197a7c0a8050951b27e207ef9434b,topic8431990dc0a80509622c399ff9d86d2e,topic84319a51c0a805091fefb05bae29a84a,topic84319ccfc0a805097e0534c04fbd0a2c,topic84319e19c0a8050920babfcb1fdfb374,topic8431a0bec0a80509722010f809b2faa3,topic8431a1efc0a8050956458189d719bc58,topic8431a33ac0a805097fb0caf8e1379257,topic8431a48ac0a8050902052707ee0e69b0,topic8431a5e8c0a80509752c04ec9b41ec11,topic8431a740c0a805094d1255fe812b3739,topic8431a9a8c0a8050933b92e0befb46fbd,topic8431aae7c0a80509233991d63974fd79,topic8431ad88c0a805091c7150bfa82e7c32,topic8431af20c0a805090d413d4895ca269b,topic8431b1b4c0a805097d34fedff5458091,topic8431b301c0a8050957965bb517fef637,topic8431b44cc0a80509317b5d93d6acd21d,topic8431b5c5c0a805091772b027806f5c9e,topic8431b700c0a805090a02e25609f6828e,topic8431b962c0a80509384a756bf5bc7d79,topic8431baa3c0a805096d54419a3fbad68e,topic8431bcdac0a8050938588e3cf952224a,topic8431bf75c0a80509580118bb8bf3b4c8,topic8431c1ccc0a805092bd2878cd1cc3c6e,topic8431c30dc0a8050929c588700577efbb,topic8431c471c0a805092a4bedf7d8668906,topic8431c60cc0a805094b557196cbf3aee3,topic8431c76bc0a805090bc02131433ae95b,topic8431c96ac0a805091a008950300e9cd4,topic8431cbb7c0a80509456ecbb0043ca181,topic8431cd61c0a805095e9990cfa3f218d2,topic8431ceafc0a8050930edd7c9fd6a81e2,topic8431d12dc0a805091396edcfe03b38f2,topic8431d3c1c0a805095eca1f254485ef19,topic8431d5e9c0a8050934baee097b606383,topic8431d7bac0a80509719f15aa4f866a8d,topic8431d911c0a805092044728a076b1bd6,topic8431da59c0a805096b748b4d994bb39a,topic8431dbddc0a80509193f91bece69ede8,topic8431dd3cc0a805091f3c8772ddf761de,topic8431de8dc0a805091cf08a763eb2db6c,topic8431e014c0a805090031e88f3f093328,topic8431e394c0a8050960cefde823ae3b23,topic8431e4fdc0a80509597ceedbd438a62a,topic8431e665c0a805096c02179296a5141b,topic8431e8c4c0a805097446af2bf071a479,topic8431ea07c0a805092d9a789a768e6f98,topic8431ec76c0a805091971e76485e905cd,topic8431ede1c0a805093a185865ff2e6554,topic8431ef12c0a8050968466b8ce7770100,topic8431f04fc0a8050961098748e3319c82,topic8431f1c8c0a8050974912f4a1b2f21d0,topic8431f56cc0a805094d2b1e00c52d9948,topic8431f6bec0a80509374f3ec32e3adb30,topic8431f94cc0a805094263398f994e61ad,topic8431faacc0a805093813b54eb881a6b9,topic8431fbf5c0a805090901647e2768bf0b,topic8431fd5dc0a8050900df3826c8c901db,topic8431feaac0a80509533e3cc3f1a3fb3b,topic8431fffcc0a80509047d1063e0ea946e,topic84320251c0a8050939ab68661e97b21e,topic84320399c0a8050912deabf2e7d17083,topic843207b2c0a805093fbeabfb3df2bee3,topic84320904c0a805090caaadbfac8154f3,topic84320a8bc0a805090ba503db04170656,topic84320bddc0a805090f22018696fb13f8,topic84320d56c0a805094cf23edd9271c79f,topic84320ebfc0a80509224787b831c4ad6c,topic8432101cc0a805096982f1026a792750,topic84321257c0a8050938bb03ce321b5bea,topic843213aac0a8050976d44112450558e1,topic84321517c0a805093af31606db1ba269,topic843baa03c0a805092df77b26dcaa374f,topic843bb0c1c0a805092af16ff2f93b1d2d,topic843bb663c0a80509751ccebe32051501,topic843bbaecc0a8050933e99fb79b1454b3,topic843bc086c0a805096843df9ed519170e,topic843bc538c0a805096ab12edb5aac9671,topic843bc9e6c0a80509659bf89fc8a66496,topic843bcf1ec0a8050920d672448213217a,topic843bd327c0a8050932e0e123ca816025,topic843bd766c0a8050971fe850195b773bd,topic843bdc2dc0a805091a7bfb52b9bef2b2,topic843bdd97c0a8050946677a1ef4e57b73,topic843bdedfc0a805091af0a17b07ca0af6,topic843be034c0a80509643d1645e0be10bc,topic843be188c0a8050922cd20e4ed1cb8d4,topic843be2e5c0a805096d4919815303ef9e,topic843be508c0a8050936b62106732ad2cc,topic843be63fc0a805091fe687737ca68ea7,topic843be7cbc0a805090f129b23eb2a1a3d,topic843be90bc0a8050907fe0937c61a17ec,topic843bec60c0a805095a44d3ea8597679c,topic843bedbdc0a805090ed4bde618b3528b,topic843bef29c0a805096537d64eef120dd1,topic843bf07fc0a80509626dad2a0164b768,topic843bf21bc0a805094fc0d8ddc7114550,topic843bf35ac0a805094e0689f0cb8f51f1,topic843bf4b0c0a80509067a9b3e1a1b44fb,topic843bf726c0a805097081a38415c4c76f,topic843bf886c0a8050971e3968fc84123e3,topic843bf9d9c0a805092d1d825c944e7cc8,topic843bfc16c0a805093ea31a6b31ce5cd2,topic843bfe5ec0a80509299e8f68d4617ff5,topic843c00acc0a80509242704ba8b7b8e54,topic843c0203c0a805094cc13ecfc857c72a,topic843c0341c0a805090513da4b98856f9c,topic843c04bac0a80509205bddadfa865fa4,topic843c063ec0a805096683d08159dfee9e,topic843c0791c0a805093dc85062d91a54f1,topic843c08ddc0a805097a2a7e894b81c9d8,topic843c0a2ec0a805094ff603245d96406c,topic843c0d85c0a80509077b8da602c805b2,topic843c0ee5c0a80509068edf49c216e944,topic843c1042c0a80509555abc42ce53f8d0,topic843c1293c0a8050916a888820f352fd0,topic843c13cbc0a8050931692709e26d3fbd,topic843c1636c0a805092d3be6542ef8cb14,topic843c193bc0a805092cf33841c4b1ea10,topic843c1abbc0a8050921ca8263368b4b46,topic843c1bfec0a8050972524dceb736d7c7,topic843c1e50c0a805093ab7058926d78ba5,topic843c20a1c0a805090ed535e98cb8fc26,topic843c220ac0a8050916bc8a16fc0b91dd,topic843c2346c0a805091a7da0a39d2fc114,topic843c2498c0a80509553c7c9c83a7b4d8,topic843c25f5c0a805094be90f54dc55c6f6,topic843c275dc0a805091baebbf6977204aa,topic843c29c5c0a80509276f66995929ad3e,topic843c2c41c0a805091bd730ada8b198a0,topic843c2d87c0a8050962434939ae768c48,topic843c2ed8c0a805092e04ef3bb70b5114,topic843c329bc0a8050912eb2094ff828255,topic843c3407c0a805097ec3e9921016682f,topic843c3556c0a805097d82cbbc4395e9c6,topic843c3694c0a8050970897ded8abb5961,topic843c38e0c0a805095fa89c48297f9d8f,topic843c3b0fc0a805096e9bb513ffe6e3d5,topic843c3d42c0a805091b893e41d0b0a918,topic843c3f15c0a805090c87f2ffefe2640d,topic843c4051c0a805091b7362c352bee95d,topic843c41aec0a805096ba98ee257fa04d5,topic843c442cc0a8050932e6b6e9b2d5b539,topic843c458dc0a80509284866caeefe6c86,topic843c46e8c0a805095a214b95f9431e41,topic843c493ec0a805092888555a3bf0c709,topic843c4a8ac0a805094decaed65076893d,topic843c4c15c0a805090f7419252ee74179,topic843c4e26c0a8050932196324a2fbe301,topic843c4f82c0a80509415bda212e8f9550,topic843c50e2c0a8050931c0d025d8f39d35,topic843c5229c0a805093f19df4a0083bc5b,topic843c54c2c0a8050947986dc119c6e514,topic843c560ec0a805095ead64eb41a33f03,topic843c5783c0a805097035f66c7e255e99,topic843c59aec0a805092e57cb0612c1c279,topic843c5b4fc0a80509517c18f912fd8f69,topic843c5c9cc0a805092425408efc2c2965,topic843c5f24c0a805097e519db76b56a61d,topic843c6097c0a80509466b87eb4caa9f5c,topic843c61dcc0a80509757c084f8d758a21,topic843c6387c0a805095cf3c53a8cff02d6,topic843c65dcc0a805097d6e5938c60c41ce,topic843c6870c0a8050944e2ba131263288d,topic843c69bbc0a8050926da4ce705c95570,topic843c6b30c0a805091edc9f25a4513879,topic843c6ca0c0a80509545f2ec89ed0abb0,topic843c6dffc0a80509271d4dc2cd0b4236,topic843c6f7ac0a805096f10ef18e553c2b4,topic843c71d9c0a8050929a8ad0294774066,topic843c7346c0a805092487553d8eeefbe8,topic843c75dec0a805091e6f09d4068b7eac,topic84462295c0a805095d8b6934a82fa905,topic84462a3dc0a8050902a3ab5bac76684d,topic84462fa3c0a805095e2716558e23822f,topic844634b2c0a80509170e92578c142aeb,topic844638f7c0a8050962e18d64e2212352,topic84463e21c0a805092aa0b89d2b2e477f,topic84464263c0a805093656453332c43be4,topic84464691c0a805095913eaee0f66d326,topic84464a9ec0a805092155b90e598b26d0,topic84464ebec0a805091443b81afc12cabb,topic844653aac0a805093f243018f7da6c62,topic844654f8c0a8050930846410654687e6,topic84465629c0a805097c2fc44859976f04,topic8446577cc0a8050966b83021694adc9e,topic844658dcc0a80509777a81dd841d951f,topic84465a1dc0a80509299c314c10847def,topic84465c0ec0a805092244edac5205d793,topic84465dbac0a805092b9af77f6fe4a16e,topic84465f93c0a805091ff6a23dcdde2199,topic844660d2c0a805093c126225d1c3271b,topic8446642bc0a8050917069aab885ef1f4,topic844665aac0a80509122f6b7993343807,topic844666ffc0a8050934ebac569fab5729,topic8446684bc0a8050964f9dae2bad2c5eb,topic844669c5c0a805094b6961f2e7e8cc72,topic84466afdc0a805093b9846ad45a8d99c,topic84466c9fc0a805090c4611e6768f3ea1,topic84466f32c0a805091b8016de93577c89,topic8446716fc0a8050950e623d4afdeabb2,topic844672f9c0a805093968c06427263d77,topic84467554c0a805090944a9a78c7e465b,topic8446768ec0a8050943233cf94d5e9274,topic8446791dc0a805093a1bd03c3ef41bd2,topic84467a52c0a805094dae0adbf4d6b8a1,topic84467c97c0a805092e5c199b55374798,topic84467ddbc0a8050974e5292b059db5b7,topic84467f44c0a80509227ca3e929b5cbb4,topic844680afc0a805096a1e5803e7940713,topic844682bbc0a805095069b69320ce2644,topic84468429c0a8050966e67e904b4046a2,topic844686a6c0a8050936a0c35a7609c845,topic844688f2c0a80509742d7fa330241e0a,topic84468a3cc0a805093d7ac4d24367a83d,topic84468b94c0a80509454129bebaeeb3b4,topic84468cc7c0a805097ef0d5132f6bdb8c,topic84468ef7c0a805090f1465a42bff7419,topic84469059c0a805093d1751053e88a23f,topic844692bcc0a805097566f7549a73854e,topic8446940bc0a8050952dc8b2fa98c322d,topic8446954dc0a805091229c1e846bba374,topic844697c6c0a80509521436540e79c67d,topic8446992fc0a8050932e726010dc887c5,topic84469ae2c0a8050902398689fc3d992b,topic84469c4dc0a805097a7e33d84dd7f0e4,topic84469db8c0a805090446271c5ea188f1,topic84469f27c0a805092b135d021e694a3e,topic8446a0cac0a80509259aea9399705e20,topic8446a229c0a80509429c84390dd0d8f1,topic8446a38fc0a8050952ddc665d3192744,topic8446a4edc0a805094a5038e5345764ee,topic8446a755c0a805091d997aadd6545402,topic8446a9f8c0a805095e519277945113a7,topic8446ac3bc0a805091d4118a878c0269e,topic8446ad91c0a805092bb09b74d6657b86,topic8446aeffc0a8050956bee5d6e6cb4faa,topic8446b080c0a8050952fe70c01f4b6134,topic8446b1d6c0a8050967b49bda54763845,topic8446b353c0a805094c62e88655241909,topic8446b4a3c0a805097b3b62c6319b50b8,topic8446b613c0a805095f1c81475893f679,topic8446b8aec0a805091e594b25b9d0df9c,topic8446ba34c0a8050929e9cfd6773dd317,topic8446bc98c0a8050960dfae2a9125433b,topic8446bde7c0a80509156041fa92b8d670,topic8446bf64c0a8050906680f973ffa5504,topic8446c182c0a8050906aa765b60dc3ef2,topic8446c2d6c0a805094bfa8c3d4322b033,topic8446c541c0a8050945fbacff155896c3,topic8446c686c0a805094d8b42460516fb0b,topic8446c81bc0a80509766749714acdf9c5,topic8446ca70c0a8050940ea2591f4108d63,topic8446cbe9c0a805095793bd2a68512e2c,topic8446cd3bc0a805091c1bcb2c671f2b60,topic8446ceb5c0a805093327c92855cdf8bb,topic8446d014c0a805096b8a44982b53f4a9,topic8446d176c0a805095bddfe50ab714079,topic8446d318c0a805097eeb62797f2d66ff,topic8446d45fc0a80509771f462403ce85d9,topic8446d7eac0a805094120feab4d4b6f19,topic8446d942c0a805090b39d56837ef3167,topic8446dbc8c0a8050964347dbc85812655,topic8446de51c0a8050943ba898a19a368e0,topic8446dfa1c0a8050954f8eabf02e1f845,topic8446e108c0a8050942df42d1ca78f496,topic8446e288c0a80509266d09a33a61b24c,topic8446e41bc0a805092f45e495e4f081d5,topic8446e57cc0a805094c1639ac21cd192d,topic8446e76dc0a805097bb5d033c66dcb4b,topic8446e8cfc0a80509482a57f32e1ca8d9,topic8446eb79c0a805094e43431b46efaf7a,topic84506599c0a80509406ed8e4d39182b3,topic84506e78c0a805094b62afd76fcc3719,topic845074c6c0a805093d0a1b48702ab4d0,topic8450798fc0a805094659c1d561119cce,topic84507df6c0a8050955624fef13c36262,topic845082b7c0a805092cb32fb6450abd79,topic84508721c0a8050910615b3ef165a023,topic84508bd3c0a80509214b446372014a1b,topic84508ff7c0a80509027777da00998253,topic8450942bc0a8050914ba97da6c7fc9df,topic84509adbc0a80509677f821cebed1671,topic84509c35c0a805090d8dc8b27ce4d717,topic84509da6c0a8050900650efa9777b59f,topic84509febc0a8050920206fc073e2bab8,topic8450a127c0a805094dcfdad77dec773e,topic8450a2a2c0a805092ff5c03e8c10f2f4,topic8450a3f4c0a805091043fa2c2d6a71bd,topic8450a60fc0a805096b624b1b8dd9ad45,topic8450a8dfc0a805095c9dfe81295bca85,topic8450aa41c0a8050945bb681618a4cfc8,topic8450ad11c0a8050944769c1062c15081,topic8450ae95c0a8050936ddf6a3a4332fcc,topic8450affac0a805094d1f373ad68c5c16,topic8450b15fc0a805091d89ac6fc668e141,topic8450b3c2c0a80509712389f758e09620,topic8450b519c0a805093d036139295edaa7,topic8450b678c0a805091fb1809388b0628c,topic8450b7d6c0a805095c4fb30d5ab91bbe,topic8450b92dc0a8050955a0c8dcf2ee12b5,topic8450ba99c0a805096bd3967f27725e01,topic8450bd3bc0a805096dac120344d9055e,topic8450be89c0a805093265f38c511abfce,topic8450bff1c0a80509017aadeb8734ae89,topic8450c3aec0a8050957fb81a92a58d91c,topic8450c50bc0a80509130b7dd4e0ef11da,topic8450c66ec0a8050939696f85f08aa68b,topic8450c84bc0a8050952212c4857985ec6,topic8450c98dc0a805090f96072f4c2ef6d4,topic8450cae4c0a8050914569dd127a951e5,topic8450cc45c0a80509697d381a8460d3f0,topic8450cf1ac0a8050935949a455ddf5a46,topic8450d0dcc0a80509718b7ee2c80bc49c,topic8450d247c0a8050977adfc4d9b1bb255,topic8450d49cc0a805091f4daeb14fd9e0c1,topic8450d5f0c0a805095443c15cdb36fea4,topic8450d74fc0a805097b1deb6e3f985e38,topic8450d9a0c0a805091b1692bf64e8e7d6,topic8450daedc0a80509138b54ffa0db0a2f,topic8450dd31c0a80509776f6a5e28d54e1b,topic8450dfa3c0a8050917330426280bfb04,topic8450e2a0c0a805093cce1b4bbaa30b97,topic8450e4dec0a8050921f57400f6beaeb1,topic8450e65ec0a8050911cac4886eb7777f,topic8450e7ccc0a805097271accb88ab125c,topic8450ea58c0a805096740ec1ff239a8f5,topic8450ebbfc0a8050950b367270532372b,topic8450ed1ec0a805090e353723fd6ee736,topic8450eec8c0a805094e4abf687f728dd3,topic8450f02bc0a8050970cceeb37bcf198e,topic8450f19fc0a805091f6b422efd403313,topic8450f43bc0a805091d7e2899f8f7ced1,topic8450f6bac0a8050953f6f57dcdb6ad14,topic8450f82bc0a805092ca8661959fb5a6a,topic8450f9aac0a80509252571eb076d460b,topic8450fafac0a80509560273414aa1b07d,topic8450fd66c0a80509272105d9f77ff692,topic8450fed1c0a80509658a4830fed475e8,topic84510247c0a805091107c2bcd460e47a,topic845103c8c0a8050942756afa6aa3fa6b,topic84510608c0a805095b1438aafabf696e,topic845108d1c0a805090accf78270dc50c7,topic84510a42c0a80509740a19c6d0a285cf,topic84510bc0c0a8050921b96fc8f29842b9,topic84510d3dc0a80509034b61276f88e22f,topic84510eedc0a805093f0482f37705b22f,topic84511132c0a80509139be475a879dac4,topic845112b1c0a80509329f9b34cbc7d464,topic8451143dc0a805094ad13dde59c0623c,topic84511652c0a8050946246468cb650d67,topic845118c9c0a8050944835f882c228ea4,topic84511b94c0a8050948584cf75d76cbf3,topic84511df4c0a805092a14a786051eccd4,topic84511f61c0a805097f9769e7171639fb,topic84512180c0a80509648b81e94db56db3,topic845122f2c0a8050956d2bdc271e2c135,topic84512472c0a8050900d02ffdf7c6014b,topic845125ecc0a8050932f1b577958bc317,topic84512745c0a80509323312b10f67e1ce,topic84512ac8c0a8050955b02b6b07ae4bc9,topic84512caac0a805095ea4a8f6f90ad4a5,topic84512f63c0a80509001cbea53b6fa42d,topic8451310dc0a8050919128055f01dd263,topic8451328bc0a80509305c9cfba5c8dee7,topic84513409c0a80509276eebb330c61842,topic84513569c0a8050901bd3f8198edbef2,topic845136e7c0a805093c09852f40b90717,topic84513929c0a8050904fc4c244dcbb39a,topic84513a96c0a805092b6edcd2c247a561,topic84513c08c0a8050956e719924464894c,topic84513d6cc0a8050905514888221830ef";
  
  private void removeActivity() {
    
    try {
      String[] ids_ = ids.split(",");
      System.out.println("\n\nAll topic: "+ids.length() + "\n\n");
      int i = 0;
      for (String topicId : ids_) {
        try {
          System.out.println("\n\n process with topic: " + topicId + "\n\n");
          Topic topic = (Topic)getObjectNameById(topicId, Utils.TOPIC);
          //
          String topicActivityId = getActivityIdForOwnerPath(topic.getPath());
          for (ForumEventLifeCycle f : listeners_) {
            try {
              f.removeActivity(topicActivityId);
              System.out.println(String.format("Removed activity of topic %s with activityId %s ", topic.getId(), topicActivityId));
            } catch (Exception e) {
              System.out.println("Failed to run function removeActivity in the class ForumEventLifeCycle. " + e.getMessage());
            }
          }
          
          storage.saveActivityIdForOwner(topic.getPath(), "");
        } catch (Exception e) {
          e.printStackTrace();
        }
        //
        if(i == 200) {
          break;
        }
        ++i;
      }
      
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
}
