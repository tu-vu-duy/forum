/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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

package org.exoplatform.forum.service;

import java.util.concurrent.Callable;

import org.exoplatform.commons.utils.CommonsUtils;

public class CalculateModerator implements Callable<Boolean> {
  
  private String path_;
  private boolean isNew = false;
  

  public CalculateModerator() throws Exception {
  }


  public CalculateModerator setPath(String path) {
    path_ = path;
    return this;
  }

  @Override
  public Boolean call() throws Exception {
    try {
      ForumService forumService = CommonsUtils.getService(ForumService.class);
      forumService.calculateModerator(path_, isNew);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  /**
   * @param isNew the isNew to set
   */
  public CalculateModerator setNew(boolean isNew) {
    this.isNew = isNew;
    return this;
  }
}
