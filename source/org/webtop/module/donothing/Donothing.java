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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.webtop.component.WApplication;
import org.webtop.wsl.script.WSLNode;

import javax.swing.*;
import org.web3d.x3d.sai.*;
import org.sdl.gui.numberbox.*;
import org.webtop.x3d.widget.*;
import org.webtop.module.wavefront.Engine;
import org.webtop.module.wavefront.WaveFront.Data;
import org.webtop.util.Animation;
import org.webtop.util.script.*;
import org.webtop.util.AnimationEngine;

public class Donothing extends WApplication implements NumberBox.Listener {

	/*
	 * Declares the number boxes that appear on the screen
	 */
	private FloatBox scaleField;
	private FloatBox speedField;

	/*
	 * Widget declarations used from the x3d scene
	 */
	private XDragWidget scaleWidget;
	private XDragWidget speedWidget;

	/*
	 * Declare couplers 
	 * scaleCoupler couples the scaleWidget to the scaleField numberbox
	 * speedCoupler couples the speedWidget to the speedField numberbox
	 */
	private ScalarCoupler scaleCoupler;
	private ScalarCoupler speedCoupler;

	/*
	 * Instance of the Animation class and AnimationEngine class used for animations
	 */
	private Animation animation;
	private AnimationEngine engine;

	/*
	 * Declare scripters to attach to the scale and speed numberboxes
	 */
	private NumberBoxScripter scaleScripter;
	private NumberBoxScripter speedScripter;

	/*
	 * Constants 
	 */
	static public float INITIAL_SPEED = (float) 0.0;
	
	//
	public static final int ANIMATION_PERIOD = 30;
	
	
	/*
	 * Instance of the Data class used with the animations
	 */
	Data data;

	/*
	 * X3D Field declaration
	 */
	SFVec3f sphereScale;

	//Animation Data class
	public static final class Data implements Animation.Data, Cloneable {
		public float speed;
		//must implement this method
		public Animation.Data copy() {
			try {
				return (Data) clone(); // all data is primitive; clone() is
										// fine
			} catch (CloneNotSupportedException e) {
				return null;
			} // can't happen
		}
	}

	@Override
	protected String getAuthor() {
		
		return "Your Name Here:";
	}

	@Override
	protected String getDate() {
		
		return "Date:";
	}

	@Override
	protected Component getFirstFocus() {
		
		return null;
	}

	@Override
	protected int getMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int getMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected String getModuleName() {
		return "DoNothing";
	}

	@Override
	protected int getRevision() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	/**
	 * This method sets the default values for the initial state of the module
	 */
	protected void setDefaults() {
		/*
		 * float array to pass to the scale field of the sphere
		 */
		float[] val = new float[3];
		val[0] = (float) 1 / 10;
		val[1] = val[0];
		val[2] = val[1];
		sphereScale.setValue(val);
		
		//Create an instance of the data class
		data = new Data();
		data.speed = INITIAL_SPEED;
		
		//copies the new data to the animation engine 
		animation.setData(data);
		animation.setPlaying(true);

	}

	@Override
	/**
	 * Setup the GUI and all GUI elements
	 */
	protected void setupGUI() {
		/*
		 * Create the scale and speed number boxes, add them to the console, 
		 * and add a number listener to detect any changes
		 */
		scaleField = new FloatBox((float) 0, (float) 10, (float) 0, 5);
		addToConsole(scaleField);
		scaleField.addNumberListener(this);
		
		speedField = new FloatBox((float) -10, (float) 10, (float) 0, 5);
		addToConsole(speedField);
		speedField.addNumberListener(this);
		
		
		/*
		 * Set up the scale and speed couplers.  These couplers connect the scale and speed
		 * widget to the scale and speed numberboxes respectively
		 */
		scaleCoupler = new ScalarCoupler(scaleWidget, scaleField, 3);
		speedCoupler = new ScalarCoupler(speedWidget, speedField, 3);

		/*
		 * Set up the scale and speed scripters to allow playback of any recorded 
		 * changes to fields
		 */
		scaleScripter = new NumberBoxScripter(scaleField, getWSLPlayer(), null,
				"myScripter", (float) 0);
		speedScripter = new NumberBoxScripter(speedField, getWSLPlayer(), null,
				"speedScripter", (float) 0);

	}

	@Override
	protected void setupMenubar() {

	}

	@Override
	/**
	 * This method gets all of the necessary fields (widgets, scale from the sphere, etc.) 
	 * from the x3d scene
	 */
	protected void setupX3D() {
		/*
		 * Connect to the scale widget and the speed widget in the x3d scene.
		 */
		scaleWidget = new XDragWidget(getSAI(), getSAI().getNode("TestWidget"),
				(short) 2, "Use to scale sphere");
		speedWidget = new XDragWidget(getSAI(), getSAI().getNode("SpeedWidget"),
				(short) 2, "Use to change speed");
		
		/*
		 * Connect to the scale field of the sphere
		 */
		sphereScale = (SFVec3f) getSAI().getInputField(
				getSAI().getNode("HelloTransform"), "scale");
		
		/*
		 * Call the constructor to create new instances of the NothingEngine and 
		 * Animation classes
		 */
		engine = new NothingEngine(getSAI());
		
		/*
		 * Constructs a self-running Animation with the given period
		 * and data object.  This constructor is for the use of classes that
		 * subclass Animation to provide functionality.
		 *
		 * p the number of milliseconds between animation frames.
		 * d the Data object containing whatever parameters are
		 *         needed by the calculations.  May be null without error;
		 *         however, if it is not null, then getData() will
		 *         never return null (which is useful).
		 */
		animation = new Animation(engine, data, ANIMATION_PERIOD);
		

	}

	@Override
	/*
	 * This method connects any scripted objects to one WSLNode.  By doing this, the recorded
	 * actions, events, etc... can be played back
	 */
	protected void toWSLNode(WSLNode node) {
		scaleScripter.addTo(node);
		speedScripter.addTo(node);

	}

	public void invalidEvent(String node, String event) {
		// TODO Auto-generated method stub

	}
	
	//This must return a string (not null) for the module to work properly
	public String getWSLModuleName() {
		return ("DoNothing");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Donothing donothing = new Donothing("Donothing",
				"/org/webtop/x3dscene/donothing.x3dv");
	}

	public Donothing(String title, String world) {
		super(title, world, true, true);
	}

	public void boundsForcedChange(NumberBox source, Number oldVal) {

	}

	public void invalidEntry(NumberBox source, Number badVal) {
		// TODO Auto-generated method stub
		System.out.println("bad val: " + badVal.floatValue());

	}
	
	/*
	 * This method is the action listener method for the number boxes created above. 
	 * Anytime a change occurs, find out what generated the event, and perform appropriate 
	 * actions. 
	 */

	public void numChanged(NumberBox source, Number newVal) {
		float[] val;
		//If the source was the scaleField, then scale the sphere
		if(source == scaleField) {
			val = new float[3];
			val[0] = (newVal.floatValue() + 1) / 10;
			val[1] = val[0];
			val[2] = val[1];
			sphereScale.setValue(val);
		}
		//Else if the source was the speedField, then adjust the speed of rotation of the 
		//Cylinder
		else if(source == speedField) {
			data = new Data();
			data.speed = newVal.floatValue();
			animation.setData(data);
		}
		
		
		
		
	}

}
