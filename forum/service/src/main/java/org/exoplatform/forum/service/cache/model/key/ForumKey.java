package org.exoplatform.forum.service.cache.model.key;

import org.exoplatform.forum.common.cache.model.ScopeCacheKey;
import org.exoplatform.forum.service.Forum;

public class ForumKey extends ScopeCacheKey {
  private static final long serialVersionUID = 1L;

  private final String categoryId;
  private final String forumId;

  public ForumKey(String categoryId, String forumId) {
    this.categoryId = categoryId;
    this.forumId = forumId;
  }

  public ForumKey(String forumId) {
    this.categoryId = null;
    this.forumId = forumId;
  }

  public ForumKey(Forum forum) {
    this.categoryId = forum.getCategoryId();
    this.forumId = forum.getId();
  }

  public String getCategoryId() {
    return categoryId;
  }

  public String getForumId() {
    return forumId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ForumKey)) return false;
    if (!super.equals(o)) return false;

    ForumKey forumKey = (ForumKey) o;

    if (forumId == null || forumKey.forumId == null || !forumId.equals(forumKey.forumId) ) return false;
    if (categoryId != null) return categoryId.equals(forumKey.categoryId);

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (forumId != null ? forumId.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ForumKey { forumId: " + forumId + ", categoryId: " + categoryId + "}";
  }
  
}
