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
package org.exoplatform.forum.service.rest.model;

import org.exoplatform.forum.service.Forum;

public class ForumJson extends AbstractJson {
  private parent category;
  private String position;
  private String closed;
  private String locked;
  private String autoAddEmailNotify;
  private String moderateTopic;
  private String moderatePost;

  private String[] notifyWhenAddTopic;
  private String[] notifyWhenAddPost;

  private String[] moderators;
  private String[] topicCreators;
  private String[] posters;
  private String[] viewers;
  private String[] bannedIPs;

  public ForumJson(Forum forum) {
  }
  
}
