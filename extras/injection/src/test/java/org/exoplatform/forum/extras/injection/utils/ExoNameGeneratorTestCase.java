/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.forum.extras.injection.utils;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 * @author Ly Minh Phuong - http://phuonglm.net
 * @version $Revision$
 */

public class ExoNameGeneratorTestCase extends TestCase {

  public void testNameGenerator() throws Exception {
    ExoNameGenerator exoNameGenerator = new ExoNameGenerator();
    assertTrue(exoNameGenerator.compose(4).length() > 4);
    assertTrue(exoNameGenerator.compose(3).length() > 3);
    assertTrue(exoNameGenerator.compose(2).length() > 2);
    assertTrue(exoNameGenerator.compose(1).length() >= 1);
    // first
   // System.out.println(exoNameGenerator.compose(0, "demo"));
    // case 2
    System.out.println(exoNameGenerator.compose(2, "demo", "all"));
    //case 3
    System.out.println(exoNameGenerator.compose(2, "demo", ""));

    System.out.println(exoNameGenerator.compose(2, "demo", "v") + "\n\n");

    System.out.println(exoNameGenerator.compose(3, "", "all"));
    System.out.println(exoNameGenerator.compose(3, "", "v"));
    System.out.println(exoNameGenerator.compose(3, "", ""));
    
    System.out.println(exoNameGenerator.compose(0, "demo"));

    System.out.println(exoNameGenerator.compose(1, "demo", "f"));

    System.out.println(exoNameGenerator.compose(1, "demo", "all"));
    
    LoremIpsum4J  ipsum = new LoremIpsum4J();
    List<String>bbcodeSupports = Arrays.asList("b", "i", "u", "url");
    System.out.println(ipsum.getCharacters(3, bbcodeSupports));
  }
}
