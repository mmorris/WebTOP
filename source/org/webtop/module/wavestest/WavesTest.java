package org.webtop.module.wavestest;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.webtop.component.WApplication;
import org.webtop.wsl.script.*;
import org.web3d.x3d.sai.*;
import javax.swing.*; 
import java.awt.*;
import java.util.*;
import org.webtop.x3d.*;

public class WavesTest extends WApplication implements ActionListener {

	//testing out stuff here
	private LinearWidget shape, shape1, shape2, shape3; 
	private SFVec3f translation;
	protected SFVec3f set_position;
	private float[] transVal = new float[3];
	private X3DObject x;
	
	//look at Photoelectric's Engine.java and Photoelectron.java
	//can make a finite amount of objects and add them to a vector for reuse....
	//try this.  if it works, then you are ready to put them into waves. 
	
	//this may be why the translation field for the SimpleTrans seems to not work.
	private Vector linearVector; 
	
	private JButton create; 
	
	
	
	/*
	 * Files that were open and why: 
	 * X3DObject.java - contains all the SAI interface stuff
	 * PoolWidget.java(Waves) - where the setx3dposition call came from 
	 * LinearSource.java(Waves)
	 * Engine.java(Photoelectric and Waves)
	 * SAI.java - all of the SAI calls and such
	 * HAVE TO figure out how to name each widget that is created....until I do this they 
	 * will all be created in the same location in the Widget-TRANSFORM and it will only 
	 * look like there is one widget...look at some of the waves files and see how they gave
	 * the widgets names (LinearSource.java, etc...)
	 * 
	 * Although, judging from LinearSource, we may end up having to create nodes in the 
	 * java code anyway
	 */
	public WavesTest(String title, String world){
		super(title, world, true, true); 
		linearVector = new Vector<LinearWidget>();
		
	}	
	
	protected String getAuthor() {
		return "Lamar Barnett, Jeremy Davis";
	}

	
	protected String getDate() {
		return null;
	}

	
	protected Component getFirstFocus() {
		return null;
	}

	
	protected int getMajorVersion() {
		return 0;
	}

	
	protected int getMinorVersion() {
		
		return 0;
	}

	
	protected String getModuleName() {
		
		return "WavesTest";
	}

	
	protected int getRevision() {
		return 0;
	}

	
	protected void setDefaults() {
	}

	
	protected void setupGUI() {
		//setLayout(new FlowLayout());
		
		JPanel panel = new JPanel();
		create = new JButton("Create Linear Widget");
		create.addActionListener(this);
		panel.add(create); 
		
		controlPanel.add(panel);
		
	}

	
	protected void setupMenubar() {
	}

	
	protected void setupX3D() {
		//translation = (SFVec3f)getSAI().getInputField("Linear1", "translation");
		translation = (SFVec3f)getSAI().getInputField("Widget-TRANSFORM", "translation");
		//trying something here
		//set_position = (SFVec3f)getSAI().getInputField("One", "set_position");
		//need some way to set the translation based on how many linear widgets there are
		//but for now, just move it around to make new nodes.
	}

	
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource().equals(create)){
			//System.out.println("Creating Shape: " ); 
			createShape();
		}
	}
	
	
	//Testing methods
	public void setTransVal(float x, float y, float z){
		System.out.println("xyz = " + x + " " + y + " " + z );
		translation.setValue(new float[]{x,y,z});
		translation.getValue(transVal);
		System.out.println("Setting transval to: " + transVal[0] + 
				" " + transVal[1] + " " + transVal[2]);
	}
	
	private void setX3DPosition(float x, float y, float z){
		System.out.println("Setting position to: " + x + " " + y + " " + z);
		getSAI().set3(set_position, x, y, z);
	}
	public void createShape(){
		//will dynamically create 2 of them...now need to do it based on number 
		//that are already there and place new ones in a vector.
		/*shape = new SimpleShape(getSAI(), "Linear0"); 
		setTransVal(0,0,10);
		shape1 = new SimpleShape(getSAI(), "Linear1");*/
		/*if(LinearWidget.count == 0){
			//setX3DPosition(0f, 50f, 0f);
			LinearWidget.count++;
		}
		else if(LinearWidget.count < 4 && LinearWidget.count >0){
			//shape = new LinearWidget(getSAI(), "Linear"+LinearWidget.count);
			shape = new LinearWidget(getSAI(), "Widget-TRANSFORM");
			set_position =(SFVec3f)getSAI().getInputField(shape.getNode(), "set_position");
			z+=10f;
			setX3DPosition(0f, z, 0f);
		}
		else{
			System.out.println("Cannot create anymore");
		}*/
		//if(LinearWidget.count < 4){
			if(LinearWidget.count == 0){
				shape =  new LinearWidget(getSAI(), "Widget-TRANSFORM"); 
				//not working...i think this is because "Widget-Transform does not have
				//a set position field.  Need to figure out how to name a linear widget
				//once it is created and then use the set_position field to change its
				//location...see top of page for files I had open and what to look for
				set_position = (SFVec3f)getSAI().getInputField(shape.getNode(), "set_position");
				setX3DPosition(0f, 10f, 0f);
				
				//let's try this and see if it works
				//works, but still moves all of the widgets instead of just one...and 
				//the widgets are still created in the same location so it looks like
				//there is just one widget..see above comment
				setTransVal(0f,10f,0f);
				LinearWidget.count++;
				
				
			}
			else if(LinearWidget.count >= 1){
				shape1 = new LinearWidget(getSAI(), "Widget-TRANSFORM");
				//ditto
				set_position=(SFVec3f)getSAI().getInputField(shape1.getNode(),"set_position");
				setX3DPosition(0f, 15f, 0f);
				//ditto
				setTransVal(0f, 20f, 0f);
				LinearWidget.count++;
			}
		//}
	}

	
	public void invalidEvent(String node, String event) {
	}
	/* 						WSL METHODS  				*/
	//Make sure that you have these two methods in here or the module won't work...
	public String getWSLModuleName() {
		//Must not be null...and must be only one word
		return "WavesTestSimulation";
	}
	public void toWSLNode(WSLNode node){
		super.toWSLNode(node); 
	}
	/**
	 * @param args - Should have access to the string for the title and the 
	 * string for the world
	 */
	public static void main(String[] args) {
		WavesTest test = new WavesTest("WavesTest" , "/org/webtop/x3dscene/wavestest.x3dv");
	}

}
