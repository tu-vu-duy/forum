/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
 * along with this program; if not, see&lt;http://www.gnu.org/licenses/&gt;.
 */
package org.exoplatform.forum.statistics;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.ArrayList;
import java.util.List;

public class JCRAPIAspectConfig
{

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger(JCRAPIAspectConfig.class);

   /**
    * The list of interfaces that we want to monitor
    */
   private Class<?>[] targetInterfaces;

   /**
    * Default constructor
    */
   public JCRAPIAspectConfig(InitParams params)
   {
      this.targetInterfaces = loadTargetInterfaces(params.getValuesParam("targetInterfaces"));
   }

   /**
    * @return the list of interfaces to monitor
    */
  private Class<?>[] loadTargetInterfaces(ValuesParam params) {
    List<String> values = params.getValues();
    List<Class<?>> lTargetInterfaces = new ArrayList<Class<?>>();
    if (values != null) {
      for (String className : values) {
        try {
          lTargetInterfaces.add(Class.forName(className));
        } catch (Exception e) {
          LOG.warn("Cannot find the target interface " + className, e);
        }
      }
    }
    Class<?>[] targetInterfaces = new Class<?>[lTargetInterfaces.size()];
    return (Class<?>[]) lTargetInterfaces.toArray(targetInterfaces);
  }
   
   /**
    * @return the list of target interfaces
    */
  public Class<?>[] getTargetInterfaces() {
    return targetInterfaces;
  }
}
