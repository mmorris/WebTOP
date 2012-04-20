/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.donothing;

import org.webtop.util.Animation;
import org.webtop.util.AnimationEngine;
import org.webtop.util.Animation.Data;
import org.webtop.x3d.SAI;

import org.web3d.x3d.sai.*;

public class NothingEngine implements AnimationEngine {
	float scaledTime = 0;
	float speed = Donothing.INITIAL_SPEED;
	SAI sai;
	
	
	SFRotation rotation; 
	

	 public NothingEngine(SAI _sai) {
         sai=_sai;
         System.out.println("Engine created");
	 }

	 /*
	  *Performs the calculation for the animation. 
	  *The data object supplied is guaranteed to remain unchanged throughout the call 
	  *to this function; furthermore, successive calls to execute will use the same Data 
	  *object if and only if no new Data object has been given to the Animation.
	  */
	public void execute(Animation.Data data) {
		System.out.println("rendering frame");
		speed = ((Donothing.Data)data).speed;
		
		//float angle = (float)Math.cos(scaledTime);
		float angle = scaledTime;
		
		float[] newRotation = new float[4];
		newRotation[0] = 0;
		newRotation[1] = 0;
		newRotation[2] = (float)1;
		newRotation[3] = angle;
		rotation.setValue(newRotation);
	}

	public void init(Animation a) {
		System.out.println("Animation initialized");
		rotation = (SFRotation)sai.getInputField("RotationTransform", "set_rotation");
	}
	
	/*
	 * Called just before a calculation.	The argument indicates how many
	 * animation periods have elapsed since last call.	Ideally is always close
	 * to 1; may be greater if system is unable to keep up with animation.	A
	 * value of 0 indicates that an update has been explicitly requested.	 This
	 * function should return false if the engine does not wish to execute this
	 * iteration (as might be appropriate if system load is too high).
	 *
	 * periods the number of periods that have elapsed since the last
	 *								call to this function.
	 * return true if execute() should be called for this iteration; false
	 *				 otherwise.
	 */
	public boolean timeElapsed(float periods) {
		System.out.println("timeElapsed");
		scaledTime += periods*speed*0.1;
		return true;
	}

}
