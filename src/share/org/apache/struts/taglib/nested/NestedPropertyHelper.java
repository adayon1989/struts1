/*
 * $Header: /home/cvs/jakarta-struts/src/share/org/apache/struts/taglib/nested/NestedPropertyHelper.java,v 1.14 2003/04/22 02:28:52 dgraham Exp $
 * $Revision: 1.14 $
 * $Date: 2003/04/22 02:28:52 $
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.struts.taglib.nested;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.Tag;

import org.apache.struts.taglib.html.Constants;
import org.apache.struts.taglib.html.FormTag;

/**
 * <p>A simple helper class that does everything that needs to be done to get
 * the nested tag extension to work. The tags will pass in their relative
 * properties and this class will leverage the accessibility of the request
 * object to calculate the nested references and manage them from a central
 * place.</p>
 *
 * <p>The helper method {@link #setNestedProperties} takes a reference to the
 * tag itself so all the simpler tags can have their references managed from a
 * central location. From here, the reference to a provided name is also
 * preserved for use.</p>
 *
 * <p>With all tags keeping track of themselves, we only have to seek to the
 * next level, or parent tag, were a tag will append a dot and it's own
 * property.</p>
 *
 * @author Arron Bates
 * @since Struts 1.1
 * @version $Revision: 1.14 $ $Date: 2003/04/22 02:28:52 $
 */ 
public class NestedPropertyHelper {

  /* key that the tags can rely on to set the details against */
  public static final String NESTED_INCLUDES_KEY = "<nested-includes-key/>";


  /**
   * Returns the current nesting property from the request object.
   * @param request object to fetch the property reference from
   * @return String of the bean name to nest against
   */
  public static final String getCurrentProperty(HttpServletRequest request) {
    // get the old one if any
    NestedReference nr = (NestedReference) request.getAttribute(NESTED_INCLUDES_KEY);
    // return null or the property
    return (nr == null) ? null : nr.getNestedProperty();
  }


  /**
   * <p>Returns the bean name from the request object that the properties are
   * nesting against.</p>
   *
   * <p>The requirement of the tag itself could be removed in the future, but is
   * required if support for the <html:form> tag is maintained.</p>
   * @param request object to fetch the bean reference from
   * @param nested tag from which to start the search from
   * @return the string of the bean name to be nesting against
   */
  public static final String getCurrentName(HttpServletRequest request,
                                            NestedNameSupport nested) {
    // get the old one if any
    NestedReference nr = (NestedReference) request.getAttribute(NESTED_INCLUDES_KEY);
    // return null or the property
    if (nr != null) {
      return nr.getBeanName();

    } else {
      // need to look for a form tag...
      Tag tag = (Tag) nested;
      Tag formTag = null;

      // loop all parent tags until we get one that can be nested against
      do {
        tag = tag.getParent();
        if (tag != null && tag instanceof FormTag) {
          formTag = tag;
        }
      } while (formTag == null && tag != null);

      if (formTag == null) {
        return "";
      }
      // return the form's name
      return ((FormTag) formTag).getBeanName();
    }
  }

  /**
   * Get the adjusted property. 
   * Apply the provided property, to the property already stored 
   * in the request object.
   * @param request to pull the reference from
   * @param property to retrieve the evaluated nested property with
   * @return String of the final nested property reference.
   */
  public static final String getAdjustedProperty(HttpServletRequest request,
                                                 String property) {
    // get the old one if any
    String parent = getCurrentProperty(request);
    return calculateRelativeProperty(property, parent);
  }

  /**
   * Sets the provided property into the request object for reference by the
   * other nested tags.
   * @param request object to set the new property into
   * @param property String to set the property to
   */
  public static final void setProperty(HttpServletRequest request,
                                       String property) {
    // get the old one if any
    NestedReference nr = referenceInstance(request);
    nr.setNestedProperty(property);
  }

  /**
   * Sets the provided name into the request object for reference by the
   * other nested tags.
   * @param request object to set the new name into
   * @param name String to set the name to
   */
  public static final void setName(HttpServletRequest request, String name) {
    // get the old one if any
    NestedReference nr = referenceInstance(request);
    nr.setBeanName(name);
  }

  /**
   * Deletes the nested reference from the request object.
   * @param request object to remove the reference from
   */
  public static final void deleteReference(HttpServletRequest request) {
    // delete the reference
    request.removeAttribute(NESTED_INCLUDES_KEY);
  }

  /**
   * Helper method that will set all the relevant nesting properties for the
   * provided tag reference depending on the implementation.
   * @param request object to pull references from
   * @param tag to set the nesting values into
   */
  public static void setNestedProperties(HttpServletRequest request,
                                         NestedPropertySupport tag) {
    boolean adjustProperty = true;
    /* if the tag implements NestedNameSupport, set the name for the tag also */
    if (tag instanceof NestedNameSupport) {
      NestedNameSupport nameTag = (NestedNameSupport)tag;
      if (nameTag.getName() == null|| Constants.BEAN_KEY.equals(nameTag.getName())) {
        nameTag.setName(getCurrentName(request, (NestedNameSupport) tag));
      } else {
        adjustProperty = false;
      }
    }

    /* get and set the relative property, adjust if required */
    String property = tag.getProperty();
    if (adjustProperty) {
      property = getAdjustedProperty(request, property);
    }
    tag.setProperty(property);
  }


  /**
   * Pulls the current nesting reference from the request object, and if there
   * isn't one there, then it will create one and set it.
   * @param request object to manipulate the reference into
   * @return current nesting reference as stored in the request object
   */
  private static final NestedReference referenceInstance(HttpServletRequest request) {
    /* get the old one if any */
    NestedReference nr = (NestedReference) request.getAttribute(NESTED_INCLUDES_KEY);
    // make a new one if required
    if (nr == null) {
      nr = new NestedReference();
      request.setAttribute(NESTED_INCLUDES_KEY, nr);
    }
    // return the reference
    return nr;
  }

  /* This property, providing the property to be appended, and the parent tag
   * to append the property to, will calculate the stepping of the property
   * and return the qualified nested property
   *
   * @param property the property which is to be appended nesting style
   * @param parent the "dot notated" string representing the structure
   * @return qualified nested property that the property param is to the parent
   */
  private static String calculateRelativeProperty(String property,
                                                  String parent) {
    if (parent == null) { parent = ""; }
    if (property == null) { property = ""; }

    /* Special case... reference my parent's nested property.
       Otherwise impossible for things like indexed properties */
    if ("./".equals(property) || "this/".equals(property)) {
      return parent;
    }

    /* remove the stepping from the property */
    String stepping;

    /* isolate a parent reference */
    if (property.endsWith("/")) {
      stepping = property;
      property = "";
    } else {
      stepping = property.substring(0, property.lastIndexOf('/') + 1);
      /* isolate the property */
      property = property.substring(property.lastIndexOf('/') + 1, property.length());
    }

    if (stepping.startsWith("/")) {
      /* return from root */
      return property;
    } else {
      /* tokenize the nested property */
      StringTokenizer proT = new StringTokenizer(parent, ".");
      int propCount = proT.countTokens();

      /* tokenize the stepping */
      StringTokenizer strT = new StringTokenizer(stepping, "/");
      int count = strT.countTokens();

      if (count >= propCount) {
        /* return from root */
        return property;

      } else {
        /* append the tokens up to the token difference */
        count = propCount - count;
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < count; i++) {
          result.append(proT.nextToken());
          result.append('.');
        }
        result.append(property);

        /* parent reference will have a dot on the end. Leave it off */
        if (result.charAt(result.length()-1) == '.') {
          return result.substring(0,result.length()-1);
        } else {
          return result.toString();
        }
      }
    }
  }
}
