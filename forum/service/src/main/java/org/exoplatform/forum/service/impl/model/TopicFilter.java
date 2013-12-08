/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.forum.service.impl.model;

import org.exoplatform.forum.service.Utils;

public class TopicFilter {
  private String categoryId = null;
  private String forumId = null;
  private String userLogin = null;

  private String orderBy = null;

  private String[] viewers;
  private boolean isRequireApproved = false;
  private boolean isAdmin = false;

  private String isApproved = null;
  private String isWaiting = null;
  private String isActive = null;
  private String isClosed = null;
  private String isLock = null;
  
  private long date = 0l;
  private String forumPath = null;
  
  private String byUser = null;
  private String tagId = null;

  public TopicFilter(String categoryId, String forumId) {
    this.categoryId = categoryId;
    this.forumId = forumId;
  }

  public TopicFilter(String userLogin, String tagId, String orderBy) {
    this.userLogin = userLogin;
    this.tagId = tagId;
    this.orderBy = orderBy;
  }

  public TopicFilter(long date, String forumPath) {
    this.forumPath = forumPath;
    this.date = date;
  }

  public TopicFilter(String byUser, boolean isAdmin, String orderBy) {
    this.byUser = byUser;
    this.orderBy = orderBy;
    this.isAdmin = isAdmin;
  }

  public String categoryId() {
    return categoryId;
  }

  public TopicFilter categoryId(String categoryId) {
    this.categoryId = categoryId;
    return this;
  }

  public String forumId() {
    if (forumId == null) {
      return Utils.getForumId(forumPath);
    }
    return forumId;
  }

  public TopicFilter forumId(String forumId) {
    this.forumId = forumId;
    return this;
  }

  public String forumPath() {
    return forumPath;
  }
  
  public TopicFilter forumPath(String forumPath) {
    this.forumPath = forumPath;
    return this;
  }

  public String byUser() {
    return byUser;
  }

  public TopicFilter byUser(String byUser) {
    this.byUser = byUser;
    return this;
  }

  public String userLogin() {
    return userLogin;
  }
  
  public TopicFilter userLogin(String userLogin) {
    this.userLogin = userLogin;
    return this;
  }

  public boolean isRequireApproved() {
    return isRequireApproved;
  }

  public TopicFilter isRequireApproved(boolean isApproved) {
    this.isRequireApproved = isApproved;
    return this;
  }

  public String orderBy() {
    return orderBy;
  }

  public TopicFilter orderBy(String orderBy) {
    this.orderBy = orderBy;
    return this;
  }

  public String tagId() {
    return tagId;
  }
  
  public TopicFilter tagId(String tagId) {
    this.tagId = tagId;
    return this;
  }

  public long date() {
    return date;
  }
  
  public TopicFilter date(long date) {
    this.date = date;
    return this;
  }

  public String[] viewers() {
    return viewers;
  }

  public TopicFilter viewers(String[] viewers) {
    this.viewers = viewers;
    return this;
  }

  public boolean isAdmin() {
    return isAdmin;
  }

  public TopicFilter isAdmin(boolean isAdmin) {
    this.isAdmin = isAdmin;
    return this;
  }

  public String isApproved() {
    return isApproved;
  }

  public TopicFilter isApproved(String isApproved) {
    this.isApproved = isApproved;
    return this;
  }

  public String isLock() {
    return isLock;
  }

  public TopicFilter isLock(String isLock) {
    this.isLock = isLock;
    return this;
  }

  public String isClosed() {
    return isClosed;
  }

  public TopicFilter isClosed(String isClosed) {
    this.isClosed = isClosed;
    return this;
  }

  public String isActive() {
    return isActive;
  }

  public TopicFilter isActive(String isActive) {
    this.isActive = isActive;
    return this;
  }

  public String isWaiting() {
    return isWaiting;
  }

  public TopicFilter isWaiting(String isWaiting) {
    this.isWaiting = isWaiting;
    return this;
  }

  private static boolean equals(String s1, String s2) {
    if (s1 == null) {
      return (s2 == null) ? true : false;
    }
    return s1.equals(s2);
  }

  @Override
  public boolean equals(Object o) {
    if (super.equals(o)) {
      return true;
    }
    if ((o instanceof TopicFilter) == false) {
      return false;
    }
    TopicFilter f = (TopicFilter) o;
    if(isAdmin != f.isAdmin || 
        isRequireApproved != f.isRequireApproved ||
        date != f.date ||
        !equals(forumPath, f.forumPath) ||
        !equals(byUser, f.byUser) ||
        !equals(forumId, f.forumId) ||
        !equals(userLogin, f.userLogin) ||
        !equals(isApproved, f.isApproved) ||
        !equals(isWaiting, f.isWaiting) ||
        !equals(isActive, f.isActive) ||
        !equals(isClosed, f.isClosed) ||
        !equals(isLock, f.isLock) ||
        !equals(orderBy, f.orderBy) ||
        !equals(tagId, f.tagId)
        ) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return new StringBuilder("PostFilter{")
        .append("categoryId='").append(categoryId).append("'")
        .append(", forumId='").append(forumId).append("'")
        .append(", isAdmin='").append(isAdmin).append("'")
        .append(", isRequireApproved='").append(isRequireApproved).append("'")
        .append(", userLogin='").append(userLogin).append("'")
        .append(", isApproved='").append(isApproved).append("'")
        .append(", isWaiting='").append(isWaiting).append("'")
        .append(", isActive='").append(isActive).append("'")
        .append(", isClosed='").append(isClosed).append("'")
        .append(", isLock='").append(isLock).append("'")
        .append(", byUser='").append(byUser).append("'")
        .append(", tagId='").append(tagId).append("'")
        .append(", date='").append(date).append("'")
        .append(", forumPath='").append(forumPath).append("'")
        .append('}').toString();
  }
  
  @Override
  public int hashCode() {
    int result = super.hashCode();
    //
    result = 31 * result + ((isAdmin == true) ? 1 : 0);
    result = 31 * result + ((date == 0) ? 0 : String.valueOf(date).hashCode());
    result = 31 * result + ((isRequireApproved == true) ? 1 : 0);
    result = categoryId != null ? (31 * result + categoryId.hashCode()) : 0;
    result = forumId != null ? (31 * result + forumId.hashCode()) : 0;
    result = userLogin != null ? (31 * result + userLogin.hashCode()) : 0;
    result = byUser != null ? (31 * result + byUser.hashCode()) : 0;
    result = tagId != null ? (31 * result + tagId.hashCode()) : 0;
    result = isApproved != null ? (31 * result + isApproved.hashCode()) : 0;
    result = isWaiting != null ? (31 * result + isWaiting.hashCode()) : 0;
    result = isActive != null ? (31 * result + isActive.hashCode()) : 0;
    result = isClosed != null ? (31 * result + isClosed.hashCode()) : 0;
    result = isLock != null ? (31 * result + isLock.hashCode()) : 0;

    return result;
  }
  
  
}
