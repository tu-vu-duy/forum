/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.forum.service.impl;

import java.util.concurrent.Callable;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public abstract class ForumTaskCompletion implements Callable<Boolean> {
  protected static final Log LOG = ExoLogger.getLogger(JCRDataStorage.class);
  private static JCRDataStorage     storage;

  private static JCRDataStorage getJCRDataStorage() {
    if (storage == null) {
      storage = CommonsUtils.getService(JCRDataStorage.class);
    }
    return storage;
  }
  
  public static class SendNotificationTask extends ForumTaskCompletion {
    private final Topic          topic;
    private final Post           post;
    private final String         nodePath;
    private final boolean        isApprovePost;
    private final MessageBuilder messageBuilder;

    public SendNotificationTask(String nodePath, Topic topic, Post post, MessageBuilder messageBuilder, boolean isApprovePost) {
      this.topic = topic;
      this.post = post;
      this.nodePath = nodePath;
      this.messageBuilder = messageBuilder;
      this.isApprovePost = isApprovePost;
    }

    @Override
    public Boolean call() throws Exception {
      getJCRDataStorage().sendNotification(nodePath, topic, post, messageBuilder, isApprovePost);
      return true;
    }
  }

  public static class QueryLastPostTask extends ForumTaskCompletion {
    private final String forumPath;

    public QueryLastPostTask(String forumPath) {
      this.forumPath = forumPath;
    }

    @Override
    public Boolean call() throws Exception {
      getJCRDataStorage().queryLastPostForum(forumPath);
      return true;
    }
  }
  
  
  
}
