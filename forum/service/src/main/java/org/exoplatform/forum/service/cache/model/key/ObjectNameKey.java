package org.exoplatform.forum.service.cache.model.key;

import org.exoplatform.forum.common.cache.model.ScopeCacheKey;
import org.exoplatform.forum.service.Utils;

public class ObjectNameKey extends ScopeCacheKey {
  private static final long serialVersionUID = 1L;
  private final String path;
  private final String id;
  private final String type;

  public ObjectNameKey(String path) {
    if (!Utils.isEmpty(path) && path.indexOf(Utils.CATEGORY) > 0) {
      path = path.substring(path.indexOf(Utils.CATEGORY));
    }
    this.path = path;
    this.id = path.substring(path.lastIndexOf("/") + 1);
    this.type = null;
  }

  public ObjectNameKey(String id, String type) {
    this.path = null;
    this.id = id;
    this.type = type;
  }
  
  public PostKey getPostKey() {
    if(path != null) {
      return new PostKey(Utils.getCategoryId(path), Utils.getForumId(path),
                          Utils.getTopicId(path), id);
    }
    return new PostKey(id);
  }
  
  public TopicKey getTopicKey() {
    if(path != null) {
      return new TopicKey(path, false);
    }
    return new TopicKey(id);
  }
  
  public CategoryKey getCategoryKey() {
    return new CategoryKey(id);
  }
  
  public ForumKey getForumKey() {
    if(path != null){
      return new ForumKey(Utils.getForumId(path), id);
    }
    return new ForumKey(id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ObjectNameKey)) return false;
    if (!super.equals(o)) return false;

    ObjectNameKey that = (ObjectNameKey) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (path != null ? !path.equals(that.path) : that.path != null) return false;
    if (type != null ? !type.equals(that.type) : that.type != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (path != null ? path.hashCode() : 0);
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }

}
