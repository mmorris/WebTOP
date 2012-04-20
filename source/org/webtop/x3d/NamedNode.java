/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.x3d;

import org.web3d.x3d.sai.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class NamedNode {

    /* Much of this class may become unnecessary through the use of the X3DMetaDataObject class.  [PC]
    */


    public final X3DNode node;
    public final String name;
    //Ideally, of course, the function n.getName() would preclude the need for
    //this class.  Unfortunately, Blaxxun (at least) provides a rather useless
    //getName() function, and so we have this. [by Davis [PC]]
    //So does this mean that if the X3D/Xj3D method X3DNode.getNodeName() is
    //good we don't need this class?  Hm....  [PC]
    public NamedNode(X3DNode n) {
        this(n, n.getNodeName());
    }

    public NamedNode(X3DNode n, String s) {
        node = n;
        name = s;
    }

    //For convenience in use of NamedNode arrays
    public static X3DNode[] rawArray(NamedNode[] nna) {
        final X3DNode[] raw = new X3DNode[nna.length];
        for (int i = 0; i < nna.length; ++i) raw[i] = nna[i].node;
        return raw;
    }

    public static NamedNode[] namedArray(X3DNode[] na, String name) {
        return namedArray(na, name, null);
    }

    public static NamedNode[] namedArray(X3DNode[] na, String[] names) {
        return namedArray(na, null, names);
    }

    public static NamedNode[] namedArray(X3DNode[] na, String name,
                                         String[] names) {
        if (name == null && names == null)
            throw new NullPointerException("No name given");
        if (names != null && names.length != na.length)
            throw new IllegalArgumentException(names.length +
                                               " names given for " +
                                               na.length + " nodes");
        final NamedNode[] named = new NamedNode[na.length];
        //And now, way too many ?: operators -- they just make a good name.
        for (int i = 0; i < na.length; ++i)
            named[i] = new NamedNode(na[i],
                                     name == null ? names[i] : names == null ?
                                     na.length > 1 ? name + '[' + i + ']' :
                                     name : name + names[i]);
        return named;
    }

    public String toString() {
        return getClass().getName() + '[' + node + ',' +
            org.webtop.util.WTString.quote(name) + ']';
    }

}
