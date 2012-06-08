package org.exoplatform.forum.extras.injection.faq;

import java.util.HashMap;

import org.exoplatform.services.organization.User;

public class ProfileInjector extends AbstractFAQInjector {

  /** . */
  private static final String NUMBER = "number";

  private static final String PREFIX = "prefix";
  
  @Override
  public void inject(HashMap<String, String> params) throws Exception {

    //
    int number = param(params, NUMBER);
    String prefix = params.get(PREFIX);
    init(prefix, null, null, null, null, 0);

    //
    for (int i = 0; i < number; ++i) {

      //
      String username = userName();
      User user = userHandler.createUserInstance(username);
      user.setEmail(username + "@" + DOMAIN);
      user.setFirstName(nameGenerator.compose(3));
      user.setLastName(nameGenerator.compose(4));
      user.setPassword(PASSWORD);

      try {

        //
        userHandler.createUser(user, true);

        //
        ++userNumber;

      } catch (Exception e) {
        getLog().error(e);
      }

      //
      getLog().info("User '" + username + "' generated");
    }
  }
  
}
