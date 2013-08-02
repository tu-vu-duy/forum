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
package org.exoplatform.forum.extras.injection.utils;


public class ExoNameGenerator {

  static final String[] prefix = {"a","al","an","au","ba","be","bi","br","da","di","do","du","e","eu","fa","jo", "ng", "kh"};
  static final String[] middle = {"au","be","bi","bo","bu","da","fri","gu","gus","nul","hn", "uy", "a"};
  static final String[] surfix =   {"cio","cus","es","ius","lius","lus","nus","tin","tor","tus", "en", "ng"};

  static final String[] prefix_ = {"n","a","b","c","h","m","d", "n", "l", "ng", "kh", "k", "s", "t", "nh", "qu", "tr", "v"};
  static final String[] surfix_ =   {"a","ao","am","an","anh","ang", "y", "iem","im","in","ing","ien","inh","on","om", "ong", "oc", 
                                       "oi", "u", "uyen", "uong", "uoc", "e", "em", "en"};

  static final String[] middle_ = {"nguyen","vu","do","le","lai","ho","ly","tran","pham","kieu","ha", "mai", "luu", "dao"};
  
  public String upper(String string) {
    return string.substring(0, 1).toUpperCase().concat(string.substring(1));
  }


  /**
   * Compose a new name.
   *
   * @param syls The number of syllables used in name.
   * @return Returns composed name as a String
   * @throws RuntimeException when logical mistakes are detected inside chosen file, and program is unable to complete
   *                          the name.
   */
  public String compose(int syls) {
    if (syls < 1) {
      throw new RuntimeException("compose(int syls) can't have less than 1 syllable");
    }
    StringBuffer name = new StringBuffer();
    name.append(prefix[(int) (Math.random() * prefix.length)]);

    for (int i = 0; i < syls-2; i++){
      name.append(middle[(int) (Math.random() * middle.length)]);
    }

    if( syls > 1 ){
      name.append(surfix[(int) (Math.random() * surfix.length)]);
    }
    return upper(name.toString());
  }

  public String compose(int syls, String first) {
    if (syls < 1) {
      return upper(first);
    }
    StringBuffer name = new StringBuffer(first);
    name.append((first.length() > 0) ? "_" : "").append(compose(syls).toLowerCase());
    return upper(name.toString());
  }
  
  public String compose(int syls, String first, String type) {
    if (type == null || type.length() == 0) {
      return compose(syls, first);
    }
    if (syls < 1) {
      return upper(first);
    }
    StringBuffer name = new StringBuffer();
    if (type != null && type.length() > 0) {
      // case first name
      
      if(first.length() > 0) {
        if(type.equals("all")) {
          name.append(compose(3));
        } else if(type.equals("v")) {
          name.append(getVWord());
        } else {
          name.append(first);
        }
      } else {
        if(type.equals("v")) {
          name.append(upper(middle_[(int) (Math.random() * middle_.length)]));
          name.append(" ").append(getVWord());
        } else {
          name.append(compose(syls));
        }
      }
    }
    return upper(name.toString());
  }
  
  private String getVWord() {
    StringBuffer name = new StringBuffer();
    name.append(prefix_[(int) (Math.random() * prefix_.length)]);
    String l = surfix_[(int) (Math.random() * surfix_.length)];
    if (name.equals("ng") && (l.indexOf("i") == 0 || l.indexOf("e") == 0)) {
      name.append("h");
    }
    name.append(l);
    return upper(name.toString());
  }
}
