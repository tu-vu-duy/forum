package org.exoplatform.forum.service.cache.model.key;

import org.exoplatform.forum.common.cache.model.ScopeCacheKey;
import org.exoplatform.forum.service.impl.model.PostFilter;

public class PostListKey extends ScopeCacheKey {

  private static final long serialVersionUID = 1L;

  private PostFilter filter;
  private int offset;
  private int limit;

  public PostListKey(PostFilter filter, int offset, int limit) {
    this.filter = filter;
    this.offset = offset;
    this.limit = limit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PostListKey)) return false;
    if (!super.equals(o)) return false;

    PostListKey that = (PostListKey) o;

    if (limit != that.limit) return false;
    if (offset != that.offset) return false;
    if(filter.equals(that.filter) == false) return false;

    return true;
  }
  

  @Override
  public int hashCode() {
    int result = super.hashCode();
    //
    result = 31 * result + filter.hashCode();
    result = 31 * result + offset;
    result = 31 * result + limit;
    return result;
  }
  
}
