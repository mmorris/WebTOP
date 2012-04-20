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
public class NodeInputField {
    public final MFNode mf;
    public final SFNode sf;
    public NodeInputField(X3DField inputField) {
        if (inputField instanceof SFNode) {
            sf = (SFNode) inputField;
            mf = null;
        }
        else if (inputField == null || inputField instanceof MFNode) {
            //It's strange to openly admit that the event might be null, but it's
            //not our job to worry about such things.
            mf = (MFNode) inputField;
            sf = null;
        }
        else throw new IllegalArgumentException("Bad event type: " +
                                                inputField.getDefinition().getFieldTypeString());
    }

    public boolean multi() {
        return mf != null;
    }

    public boolean single() {
        return sf != null;
    }

    public void set(NamedNode nn) {
        sf.setValue(nn.node);
    }

    public void set(NamedNode[] nns) {
        mf.setValue(nns.length, NamedNode.rawArray(nns));
    }

}
