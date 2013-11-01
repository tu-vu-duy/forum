package org.exoplatform.forum.service.cache.model.key;

import org.exoplatform.forum.common.cache.model.ScopeCacheKey;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;

public class TopicKey extends ScopeCacheKey {
  private static final long serialVersionUID = 1L;
  private final String id;
  private final String topicPath;
  private final boolean isLastPost;

  public TopicKey(String topicPath, boolean lastPost) {
    this.topicPath = topicPath;
    this.id = Utils.getTopicId(topicPath);
    isLastPost = lastPost;
  }

  public TopicKey(Topic topic) {
    this.id = topic.getId();
    this.topicPath = topic.getPath();
    this.isLastPost = false;
  }
  
  public TopicKey(String id) {
    this.id = id;
    this.topicPath = null;
    this.isLastPost = false;        
  }

  public String getTopicPath() {
    return topicPath;
  }

  public String getTopicId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TopicKey)) return false;
    if (!super.equals(o)) return false;

    TopicKey topicKey = (TopicKey) o;

    if (isLastPost != topicKey.isLastPost) return false;
    if (id == null || topicKey.id == null || !id.equals(topicKey.id) ) return false;
    if (topicPath != null && topicPath.equals(topicKey.topicPath) == false) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (id != null ? id.hashCode() : 0);
    return result;
  }

}
