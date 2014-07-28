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
package org.exoplatform.forum.service.rest;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.DataStorage;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("forum")
public class ForumRestService implements ResourceContainer {
  private static Log LOG = ExoLogger.getLogger(ForumRestService.class);
  private static final CacheControl cc;

  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cc = new CacheControl();
    cc.setNoCache(true);
    cc.setNoStore(true);
  }

  private ForumService getForumService() {
    return CommonsUtils.getService(ForumService.class);
  }

  private DataStorage getForumDataStorage() {
    return CommonsUtils.getService(DataStorage.class);
  }
  
  /**
   * Gets RSS feeds of a category, forum or topic.
   *
   * @param categoryId The object Id to get RSS feeds.
   * 
   * @anchor ForumWebservice.viewrss
   * 
   * @return The response is xml data which contains returned RSS.
   * 
   * @throws Exception
   * 
   * @LevelAPI Platform
   */
  @GET
  @Path("categories/{id}")
  @Produces(MediaType.APPLICATION_XML)
  public Response getCategory(@PathParam("id") String categoryId) throws Exception {
    try {
      
      
      return Response.ok(null, MediaType.APPLICATION_XML).cacheControl(cc).build();
    } catch (Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }
  
}
