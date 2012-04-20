/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.testloader;

import org.webtop.component.WApplication;
import org.webtop.wsl.script.WSLNode;

import java.awt.Component;

public class TestLoader extends WApplication {
    public TestLoader(String title, String world) {
        super(title, world);
    }

    protected String getModuleName() {
        return "";
    }

    protected int getMajorVersion() {
        return 0;
    }

    protected int getMinorVersion() {
        return 0;
    }

    protected int getRevision() {
        return 0;
    }

    protected String getDate() {
        return "";
    }

    protected String getAuthor() {
        return "Paul A. Cleveland";
    }

    protected Component getFirstFocus() {
        return null;
    }

    //********** MODULE-SPECIFIC CODE **********//

    protected void setupGUI() {
    }

    protected void setupX3D() {
    }

    protected void setupMenubar() {
    }

    protected void setDefaults() {
    }

    public void invalidEvent(String node, String event) {
    }

    public static void main(String[] args) {
        TestLoader test = new TestLoader("WebTOP Test Loader", "michelson.x3dv");
    }

	@Override
	protected void toWSLNode(WSLNode node) {
		// TODO Auto-generated method stub
		
	}

	public String getWSLModuleName() {
		// TODO Auto-generated method stub
		return null;
	}
    
    
}
