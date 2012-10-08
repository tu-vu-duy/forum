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
package org.exoplatform.poll.service;

import org.exoplatform.poll.base.BaseTestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tuvd@exoplatform.com
 * Oct 8, 2012  
 */
public class PollServiceTestCase extends BaseTestCase {
  PollService pollService;
  @Override
  public void setUp() throws Exception {
    if(pollService == null) {
      pollService = (PollService)getContainer().getComponentInstanceOfType(PollService.class);
    }
    
    //TODO setup default data
    begin();
  }

  @Override
  public void tearDown() throws Exception {
    // TODO remove all data before end;
    end();
  }
  
  public void testPollService() throws Exception {
    assertNotNull(pollService);
  }

}
