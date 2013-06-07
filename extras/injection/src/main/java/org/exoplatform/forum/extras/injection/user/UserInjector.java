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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.extras.injection.user;

import java.util.HashMap;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.extras.injection.utils.ExoNameGenerator;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;

/**
 * @version $Revision$
 */
public class UserInjector extends DataInjector {

  private static String               PASSWORD = "gtn";

  private static String               DOMAIN   = "@exoplf.com";

  protected final UserHandler         userHandler;

  protected final OrganizationService organizationService;

  protected ExoNameGenerator          exoNameGenerator;

  public UserInjector() {
    PortalContainer c = PortalContainer.getInstance();
    this.organizationService = (OrganizationService) c.getComponentInstanceOfType(OrganizationService.class);
    this.userHandler = organizationService.getUserHandler();
    this.exoNameGenerator = new ExoNameGenerator();
  }

  @Override
  public void inject(HashMap<String, String> params) throws Exception {

    String users = params.get("users");
    String[] userNames = users.split(",");
    //
    for (int i = 0; i < userNames.length; ++i) {

      //
      String userName = userNames[i].trim().replace(" ", "_").toLowerCase();

      if (userHandler.findUserByName(userName) == null) {
        User user = userHandler.createUserInstance(userName);
        user.setEmail(userName + DOMAIN);
        user.setFirstName(exoNameGenerator.compose(3));
        user.setLastName(exoNameGenerator.compose(4));
        user.setPassword(PASSWORD);

        try {

          //
          userHandler.createUser(user, true);

          //
          // ++userNumber;

        } catch (Exception e) {
          getLog().error(e);
        }
        getLog().info("User '" + userName + "' generated");
      } else {
        getLog().info("User '" + userName + "' has been created.");
        
      }

      //
    }
  }

  @Override
  public Log getLog() {
    return ExoLogger.getExoLogger(this.getClass());
  }

  @Override
  public Object execute(HashMap<String, String> params) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void reject(HashMap<String, String> params) throws Exception {
    // TODO Auto-generated method stub

  }

}
