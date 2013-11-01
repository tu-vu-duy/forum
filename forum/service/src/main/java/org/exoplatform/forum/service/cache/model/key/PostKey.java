package org.exoplatform.forum.service.cache.model.key;

import org.exoplatform.forum.common.cache.model.ScopeCacheKey;
import org.exoplatform.forum.service.Post;

public class PostKey extends ScopeCacheKey {
  private static final long serialVersionUID = 1L;
  private final String category;
  private final String forum;
  private final String topic;
  private final String post;

  public PostKey(String category, String forum, String topic, String post) {
    this.category = category;
    this.forum = forum;
    this.topic = topic;
    this.post = post;
  }

  public PostKey(Post post) {
    this.category = post.getCategoryId();
    this.forum = post.getForumId();
    this.topic = post.getTopicId();
    this.post = post.getId();
  }
  
  public PostKey(String postId) {
    this.post = postId;
    this.category = null;
    this.forum = null;
    this.topic = null;
  }

  public String getCategory() {
    return category;
  }

  public String getForum() {
    return forum;
  }

  public String getTopic() {
    return topic;
  }

  public String getPost() {
    return post;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PostKey)) return false;
    if (!super.equals(o)) return false;

    PostKey postKey = (PostKey) o;

    if (post == null || postKey.post == null || !post.equals(postKey.post)) return false;
    if (topic != null && topic.equals(postKey.topic) == false) return false;
    if (forum != null && forum.equals(postKey.forum) == false) return false;
    if (category != null && category.equals(postKey.category) == false) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (post != null ? post.hashCode() : 0);
    return result;
  }
}
