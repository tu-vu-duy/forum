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
package org.exoplatform.forum.service.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.SerializationUtils;
import org.exoplatform.forum.common.cache.model.ScopeCacheKey;
import org.exoplatform.management.ManagementAware;
import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;

@Managed
@ManagedDescription("Byte Static Manager")
@NameTemplate({ 
  @Property(key = "service", value = "forum"), 
  @Property(key = "view", value = "bytestatic")
})
public class ByteStaticManager implements ManagementAware {
  
  private Map<TYPE, HashMap<ScopeCacheKey, Object>> data;
  
  private CachedDataStorage cachedDataStorage;
  
  private ManagementContext context;
  
  public enum TYPE {
    category, cateList, forum, forumList, topic, topicList, post, postList, watched, miscData, objectName
  }
  public ByteStaticManager(CachedDataStorage cachedDataStorage) {
    this.cachedDataStorage = cachedDataStorage;
    cachedDataStorage.setViewBean(this);
    
    data = new HashMap<ByteStaticManager.TYPE, HashMap<ScopeCacheKey, Object>>();
  }
  
  public void put(TYPE type, Object obj, ScopeCacheKey key) {
    HashMap<ScopeCacheKey, Object> ol = data.get(type);

    if (ol == null) {
      ol = new HashMap<ScopeCacheKey, Object>();
    }
    //
    ol.put(key, obj);
    //
    data.put(type, ol);
  }
  
  public void registerManager(Object o) {
    if (context != null) {
      context.register(o);
    }
  }

  @Override
  public void setContext(ManagementContext context) {
    this.context = context;
  }

  public HashMap<ScopeCacheKey, Object> getCategories() {
    return data.get(TYPE.category);
  }
  
  public HashMap<ScopeCacheKey, Object> getCateList() {
    return data.get(TYPE.cateList);
  }
  
  public HashMap<ScopeCacheKey, Object> getForums() {
    return data.get(TYPE.forum);
  }
  
  public HashMap<ScopeCacheKey, Object> getForumList() {
    return data.get(TYPE.forumList);
  }
  
  public HashMap<ScopeCacheKey, Object> getTopics() {
    return data.get(TYPE.topic);
  }
  
  public HashMap<ScopeCacheKey, Object> getTopicList() {
    return data.get(TYPE.topicList);
  }
  
  public HashMap<ScopeCacheKey, Object> getPosts() {
    return data.get(TYPE.post);
  }
  
  public HashMap<ScopeCacheKey, Object> getPostList() {
    return data.get(TYPE.postList);
  }
  
  public HashMap<ScopeCacheKey, Object> getWatcheds() {
    return data.get(TYPE.watched);
  }
  
  public HashMap<ScopeCacheKey, Object> getMicDatas() {
    return data.get(TYPE.miscData);
  }
  //

  @Managed
  @ManagedDescription("Turn on the infomation.")
  @Impact(ImpactType.READ)
  public String getInfoCategories() {
    return getInfo(getCategories());
  }

  @Managed
  @ManagedDescription("Turn on the infomation.")
  @Impact(ImpactType.READ)
  public String getInfoCateList() {
    return getInfo(getCateList());
  }

  @Managed
  @ManagedDescription("Turn on the infomation.")
  @Impact(ImpactType.READ)
  public String getInfoForums() {
    return getInfo(getForums());
  }

  @Managed
  @ManagedDescription("Turn on the infomation.")
  @Impact(ImpactType.READ)
  public String getInfoForumList() {
    return getInfo(getForumList());
  }

  @Managed
  @ManagedDescription("Turn on the infomation.")
  @Impact(ImpactType.READ)
  public String getInfoTopics() {
    return getInfo(getTopics());
  }

  @Managed
  @ManagedDescription("Turn on the infomation.")
  @Impact(ImpactType.READ)
  public String getInfoTopicList() {
    return getInfo(getTopicList());
  }

  @Managed
  @ManagedDescription("Turn on the infomation.")
  @Impact(ImpactType.READ)
  public String getInfoPosts() {
    return getInfo(getPosts());
  }

  @Managed
  @ManagedDescription("Turn on the infomation.")
  @Impact(ImpactType.READ)
  public String getInfoPostList() {
    return getInfo(getPostList());
  }

  @Managed
  @ManagedDescription("Turn on the infomation.")
  @Impact(ImpactType.READ)
  public String getInfoWatcheds() {
    return getInfo(getWatcheds());
  }

  @Managed
  @ManagedDescription("Turn on the infomation.")
  @Impact(ImpactType.READ)
  public String getInfoMicDatas() {
    return getInfo(getMicDatas());
  }

  @Managed
  @ManagedDescription("Turn on the infomation.")
  @Impact(ImpactType.READ)
  public String getInfoObjectNames() {
    return getInfo(data.get(TYPE.objectName));
  }

  private byte[] getbyteObject(Serializable o) {
    return SerializationUtils.serialize(o);
  }

  private String getInfo(HashMap<ScopeCacheKey, Object> os) {
    if (os == null || os.isEmpty()) {
      return "{ Size: 0, capacity: 0}";
    }
    byte[] b = getbyteObject(os);
    //
    return "{ Size: " + String.valueOf(os.size()) + ", capacity: " + b.length + "}";
  }

}
