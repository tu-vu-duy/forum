/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.forum.service.ws.test;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.ws.AbstractResourceTest;
import org.exoplatform.forum.service.ws.BanIP;
import org.exoplatform.forum.service.ws.BeanToJsons;
import org.exoplatform.forum.service.ws.ForumWebservice;
import org.exoplatform.forum.service.ws.MessageBean;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tuvd@exoplatform.com
 * Oct 9, 2012  
 */

public class TestWebservice extends AbstractResourceTest {
  ForumWebservice     forumWebservice;

  static final String baseURI = "/ks/forum";
  static final String fullURI = "http://localhost:8080".concat(baseURI);
  

  public void setUp() throws Exception {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    super.setUp();
    forumWebservice = (ForumWebservice) getService(ForumWebservice.class);
    registry(forumWebservice);
    initDefaultData();
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }
  
  public ContainerResponse performTestCase(String path) throws Exception {
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", fullURI.concat(path), "", h, null, writer);
    return response;
  }

  public void testGetMessage() throws Exception {
    loginUser(USER_ROOT);
    for (int i = 0; i < 10; i++) {
      Post post = createdPost();
      post.setName("post " + i);
      forumService_.savePost(categoryId, forumId, topicId, post, true, new MessageBuilder());
    }
    String eventURI = "/getmessage/5";
    ContainerResponse response = performTestCase(eventURI);
    assertNotNull(response);
    assertEquals(response.getStatus(), 200);
    
    // assert data
    MessageBean bean = (MessageBean)response.getEntity();
    assertEquals(bean.getData().size(), 5);

  }
  
  public void testGetPulicMessage() throws Exception {
    loginUser(USER_DEMO);
    for (int i = 0; i < 10; i++) {
      Post post = createdPost();
      post.setOwner(USER_DEMO);
      post.setName("post of demo " + i);
      forumService_.savePost(categoryId, forumId, topicId, post, true, new MessageBuilder());
    }
    String eventURI = "/getpublicmessage/5";
    ContainerResponse response = performTestCase(eventURI);
    assertNotNull(response);
    assertEquals(response.getStatus(), 200);
    
    // assert data
    MessageBean bean = (MessageBean)response.getEntity();
    assertEquals(bean.getData().size(), 5);
  }
  
  public void testFilterIps() throws Exception {
    //TODO: save ban IPs of forum data
    String eventURI = "/filter/192.168.1.10";
    ContainerResponse response = performTestCase(eventURI);
    assertNotNull(response);
    assertEquals(response.getStatus(), 200);
  }
  
  public void testFilterIpBanForum() throws Exception {
    //TODO: create forum object, save ban IP of forum
    String eventURI = "/filterIpBanforum/"+forumId+"/192.168.1.10";
    ContainerResponse response = performTestCase(eventURI);
    assertNotNull(response);
    assertEquals(response.getStatus(), 200);
  }
  
  public void testFilterTagNameForum() throws Exception {
    List<Tag> tags = new ArrayList<Tag>(10);
    for (int i = 0; i < 10; i++) {
      Tag tag = new Tag();
      tag.setName("newfoo" + i);
      tags.add(tag);
    }
    Topic A = forumService_.getTopic(categoryId, forumId, topicId, USER_ROOT);
    forumService_.addTag(tags, USER_ROOT, A.getPath());
    tags = forumService_.getAllTags();
    // when user click on add Tag, list all tags 
    String userAndTopicId = USER_DEMO + "," + topicId;
    String eventURI = "/filterTagNameForum/"+userAndTopicId+"/onclickForm";
    
    ContainerResponse response = performTestCase(eventURI);
    assertNotNull(response);
    assertEquals(response.getStatus(), 200);
    BeanToJsons<BanIP> results =  (BeanToJsons<BanIP>) response.getEntity();
    // When other user filter tags on topic A
    
    userAndTopicId = USER_ROOT + "," + (new Topic()).getId();
    eventURI = "/filterTagNameForum/"+userAndTopicId+"/foo";
    
    response = performTestCase(eventURI);
    assertNotNull(response);
    assertEquals(response.getStatus(), 200);
    results =  (BeanToJsons<BanIP>) response.getEntity();;
    
    
  }

  public void testViewrss() throws Exception {
    //TODO: create topic 
    String eventURI = "/rss/" + topicId;
    ContainerResponse response = performTestCase(eventURI);
    assertNotNull(response);
    assertEquals(response.getStatus(), 200);
  }
  
  public void testCheckPublicRss() throws Exception {
    
    forumService_.addWatch(-1, forumId, null, USER_JOHN);
    // save watch rss by john and test
    String eventURI = "/rss/user/john";
    ContainerResponse response = performTestCase(eventURI);
    assertNotNull(response);
    assertEquals(response.getStatus(), 200);
    
//    HashMap<String, Object> entity = (HashMap<String, Object>) response.getEntity();
    //System.out.println("entity " + response.getEntity().toString());
  }

}
