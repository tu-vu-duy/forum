package org.exoplatform.forum.service.cache.model.key;

import org.exoplatform.forum.common.cache.model.ScopeCacheKey;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Utils;

public class PostKey extends ScopeCacheKey {
  private static final long serialVersionUID = 1L;

  private final String category;
  private final String forum;
  private final String topic;
  private final String post;

  public PostKey(String category, String forum, String topic, String post) {
    if (post.lastIndexOf("/") > 0) {
      this.category = Utils.getCategoryId(post);
      this.forum = Utils.getCategoryId(post);
      this.topic = Utils.getCategoryId(post);
      this.post = Utils.getIdByType(post, Utils.POST);
    } else {
      this.category = category;
      this.forum = forum;
      this.topic = topic;
      this.post = post;
    }
  }

  public PostKey(Post post) {
    this.category = post.getCategoryId();
    this.forum = post.getForumId();
    this.topic = post.getTopicId();
    this.post = post.getId();
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

    if (category != null ? !category.equals(postKey.category) : postKey.category != null) return false;
    if (forum != null ? !forum.equals(postKey.forum) : postKey.forum != null) return false;
    if (post != null ? !post.equals(postKey.post) : postKey.post != null) return false;
    if (topic != null ? !topic.equals(postKey.topic) : postKey.topic != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (category != null ? category.hashCode() : 0);
    result = 31 * result + (forum != null ? forum.hashCode() : 0);
    result = 31 * result + (topic != null ? topic.hashCode() : 0);
    result = 31 * result + (post != null ? post.hashCode() : 0);
    return result;
  }
}
