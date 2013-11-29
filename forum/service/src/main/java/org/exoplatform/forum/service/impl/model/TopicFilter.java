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

public class TopicFilter {
  private String   categoryId = null;
  private String   forumId = null;
  private String   userLogin = null;

  private String   orderBy = null;

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
  
  private String userName = null;

  public TopicFilter(String categoryId, String forumId) {
    this.categoryId = categoryId;
    this.forumId = forumId;
  }

  public TopicFilter(long date, String forumPath) {
    this.forumPath = forumPath;
    this.date = date;
  }

  public TopicFilter(String userName, boolean isAdmin, String orderBy) {
    this.userName = userName;
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

  public String userName() {
    return userName;
  }

  public TopicFilter userName(String userName) {
    this.userName = userName;
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
        !equals(userName, f.userName) ||
        !equals(forumId, f.forumId) ||
        !equals(userLogin, f.userLogin) ||
        !equals(isApproved, f.isApproved) ||
        !equals(isWaiting, f.isWaiting) ||
        !equals(isActive, f.isActive) ||
        !equals(isClosed, f.isClosed) ||
        !equals(isLock, f.isLock) ||
        !equals(orderBy, f.orderBy)
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
        .append(", userName='").append(userName).append("'")
        .append(", date='").append(date).append("'")
        .append(", forumPath='").append(forumPath).append("'")
        .append('}').toString();
  }
  
  private int getHashCode(int current, Object key) {
    return (key != null ? (31 * current + key.hashCode()) : 0);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = getHashCode(result, categoryId);
    result = getHashCode(result, forumId);
    result = getHashCode(result, forumPath);
    result = getHashCode(result, isAdmin);
    result = getHashCode(result, isRequireApproved);
    result = getHashCode(result, userLogin);
    result = getHashCode(result, userName);
    result = getHashCode(result, date);
    result = getHashCode(result, isApproved);
    result = getHashCode(result, isWaiting);
    result = getHashCode(result, isActive);
    result = getHashCode(result, isClosed);
    result = getHashCode(result, isLock);
    return result;
  }
  
  
}
