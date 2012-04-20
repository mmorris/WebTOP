/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.x3d.output;

import org.webtop.x3d.AbstractNode;
import org.webtop.x3d.SAI;
import org.webtop.x3d.NamedNode;
import org.web3d.x3d.sai.*;

/**
 * <p>Title: X3DWebTOP</p>
 *
 * <p>Description: The X3D version of The Optics Project for the Web
 * (WebTOP)</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: MSU Department of Physics and Astronomy</p>
 *
 * @author Paul Cleveland, Peter Gilbert
 * @version 0.0
 */
public class Switch extends AbstractNode implements AbstractSwitch {
        //VRML event names for a Switch's input
        public static final String WHICHCHOICE_IN="set_whichChoice";

        private final NamedNode node;
        private final SFInt32 set_whichChoice;

        //Clients can change this value dynamically; it can be 0 to indicate
        //that there should be no upper limit set on a value for setChoice().
        private int choiceCount;
        private int lastChoice = 0;

        //public Switch(SAI sai,String switchNode,int choices)
        //{this(sai,switchNode,choices,WHICHCHOICE_IN);}
        //public Switch(SAI sai,String switchNode,int choices, String choice_in) {
        //	setChoiceCount(choices);
        //	set_whichChoice=(EventInSFInt32)sai.getEI(switchNode,choice_in);
        //}
        public Switch(SAI sai, NamedNode switchNode, int choices) {
            this(sai, switchNode, choices, WHICHCHOICE_IN);
        }

        public Switch(SAI sai, NamedNode switchNode, int choices, String choice_in) {
            setChoiceCount(choices);
            node = switchNode;
            set_whichChoice = (SFInt32) sai.getInputField(node, choice_in);
        }

        public NamedNode getNode() {
            return node;
        }

        public void setChoice(int choice) {
            if (choice < -1 || choiceCount > 0 && choice >= choiceCount)
                throw new IndexOutOfBoundsException("Choice " + choice +
                                                    " out of range [-1," +
                                                    choiceCount + ").");
            if (choice != -1) lastChoice = choice;
            set_whichChoice.setValue(choice);
        }

        public void setVisible(boolean yea) {
            setChoice(yea ? lastChoice : -1);
        }

        public void setChoiceCount(int choices) {
            if (choices <= 0)throw new IllegalArgumentException("Number of choices must be positive.");
            choiceCount = choices;
        }

        public String toString() {
            return getClass().getName() + '[' + lastChoice + '/' + choiceCount + ']';
        }
}
