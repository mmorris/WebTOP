/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

import org.web3d.x3d.sai.*;
import javax.swing.*;
import java.util.HashMap;
import java.awt.*;


public class saiTutorial extends JFrame implements X3DFieldEventListener {
	
	//global declarations of Nodes, Fields, etc..
	X3DNode boxMaterial;
	X3DNode boxTouchSensor;
	X3DNode sphereMaterial;
	X3DNode sphereTouchSensor;
	X3DNode boxPlaneSensor;
	X3DNode boxTransform;
	X3DNode spherePlaneSensor;
	X3DNode sphereTransform;
	X3DNode sphere2Transform;
	SFBool boxIsOver;
	SFColor boxColor;
	SFBool sphereIsOver;
	SFColor sphereColor;
	SFVec3f boxTranslationChanged;
	SFBool boxIsActive;
	SFVec3f boxTranslation;
	SFBool sphereIsActive;
	SFVec3f sphereTranslationChanged;
	SFVec3f sphereTranslation;
	SFVec3f sphere2Scale;
	
	//constructor
	public saiTutorial(){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container contentPane = this.getContentPane();
		
		//Setup the Browser Parameters
		HashMap requestedParameters = new HashMap();
		//Create a Scene Access Interface(SAI) component
		X3DComponent x3dComp = BrowserFactory.createX3DComponent(requestedParameters);
		
		//Add the component to the user interface
		JComponent x3dPanel = (JComponent)x3dComp.getImplementation();
		contentPane.add(x3dPanel, BorderLayout.CENTER);
		
		//Get an external browser
		ExternalBrowser x3dBrowser = x3dComp.getBrowser();
		setSize(600,500);
		setVisible(true);
		
		//Create an X3D scene by loading a file
		X3DScene mainScene = x3dBrowser.createX3DFromURL(new String[] {"saiTutorial.x3dv"});
		//Replace the current world with the new one 
		x3dBrowser.replaceWorld(mainScene);
		
		//Get the material node of the box in the scene
		boxMaterial = mainScene.getNamedNode("boxColor");
		if(boxMaterial == null){
			return;
		}
		//Get the touchSensor for the box
		boxTouchSensor = mainScene.getNamedNode("myPlane");
		if(boxTouchSensor == null)
			return;
		
		//Get the diffuseColor field from material
		boxColor = (SFColor) boxMaterial.getField("diffuseColor");
		
		//get the isOver field of the box touchSensor
		boxIsOver = (SFBool)boxTouchSensor.getField("isOver");
		boxIsOver.addX3DEventListener(this); //add event listener to the touch sensor
		
		sphereMaterial = mainScene.getNamedNode("sphereColor");
		if(sphereMaterial == null)
			return;
		
		//Get the touch sensor for the sphere
		sphereTouchSensor = mainScene.getNamedNode("myPlane2");
		if(sphereTouchSensor == null)
			return;
		
		//Get the diffuseColor field from the sphere Material
		sphereColor = (SFColor) sphereMaterial.getField("diffuseColor");
		
		//Get the touch sensor for the sphere
		sphereIsOver = (SFBool)sphereTouchSensor.getField("isOver");
		sphereIsOver.addX3DEventListener(this);
		
		//Get the plane sensor for the box
		boxPlaneSensor = mainScene.getNamedNode("myPlane");
		if(boxPlaneSensor == null) 
			return;
		
		//Get the translation_changed field from the box plane sensor
		boxTranslationChanged = (SFVec3f) boxPlaneSensor.getField("translation_changed");
		//Get the isActive field from the box plane sensor
		boxIsActive = (SFBool) boxPlaneSensor.getField("isActive");
		boxIsActive.addX3DEventListener(this);
		
		//Get the transform for the box
		boxTransform = mainScene.getNamedNode("myTrans");
		if(boxTransform ==  null)
			return;
		//Get the translation field from the box
		boxTranslation = (SFVec3f)boxTransform.getField("translation");	
		
		//Get the plane sensor for the sphere
		spherePlaneSensor = mainScene.getNamedNode("myPlane2");
		if(spherePlaneSensor == null)
			return;
		
		//Get the translation_changed field from the sphere plane sensor
		sphereTranslationChanged = (SFVec3f)spherePlaneSensor.getField("translation_changed");
		//Get the isActive field from the sphere plane sensor
		sphereIsActive = (SFBool) spherePlaneSensor.getField("isActive");
		sphereIsActive.addX3DEventListener(this);
		
		//Get the transform for the sphere
		sphereTransform = mainScene.getNamedNode("myTrans2");
		if(sphereTransform == null)
			return;
		//Get the translation field from the box
		sphereTranslation = (SFVec3f) sphereTransform.getField("translation");
		
		//Get the transform for the 2nd sphere
		sphere2Transform = mainScene.getNamedNode("mySphere");
		if(sphere2Transform == null)
			return;
		//Get the scale field from the 2nd sphere
		sphere2Scale = (SFVec3f) sphere2Transform.getField("scale");
		
		}
	
	//event handling method
	/**
	 * Event handling method for X3DFields.  Make appropriate changes to the fields in this method
	 * as they are fired by the browser.
	 * @param e - The source of the fired event
	 */
	public void readableFieldChanged(X3DFieldEvent e){
		float[] blue = {0, 0, 1};
		float[] red = {1,0,0};
		/** Color Changes **/
		//for the Box color change
		if(boxIsOver.equals(e.getSource())){
			boxColor.setValue(blue);
		}
		if(boxIsOver.getValue()==false){
			boxColor.setValue(red);
		}
		
		//for the sphere color change
		if(sphereIsOver.equals(e.getSource())){
			sphereColor.setValue(blue);
		}
		if(sphereIsOver.getValue() == false){
			sphereColor.setValue(red);
		}
		
		/** Moving the objects**/
		//Move the box
		if(boxIsActive.equals(e.getSource())){
			float[] boxMoved = new float[]{0,0,0};//float array to hold the translation value
			float[] sphereMoved = new float[]{0,0,0};//to reflect the sphere translation move
			boxTranslationChanged.getValue(boxMoved);//get the value of the translation change
			boxTranslation.setValue(boxMoved);//set the translation for the box
			
			//move the sphere opposite of the box
			for(int i = 0; i<3; i++)
				sphereMoved[i] = -1*boxMoved[i];//move the sphere exactly opposite of the box
			sphereTranslation.setValue(sphereMoved);
			
			//NOTE: The initial change will make the cube or sphere jump to zero simply because it is being passed
			//an empty array at first.
			
			//Now use the box to scale the size of the green sphere
			float[] scale = new float[]{1,1,1};
			for(int i=0; i<3; i++)
				scale[i] = sphereMoved[0]/5;
			sphere2Scale.setValue(scale);//set the value of the scale of the sphere
		}
		//Move the sphere
		if(sphereIsActive.equals(e.getSource())){
			float[] sphereMoved = new float[]{5,0,0}; //float array to hold the translation value
			float[] boxMoved = new float[]{0,0,0};
			sphereTranslationChanged.getValue(sphereMoved);//get the value of the moved sphere
			sphereTranslation.setValue(sphereMoved);//set the translation for the sphere
			
			//move the box opposite of the sphere
			for (int i = 0; i<3; i++)
				boxMoved[i] = -1*sphereMoved[i];
			boxTranslation.setValue(boxMoved);//move the box exactly opposite of the sphere
			
			//NOTE: The initial change will make the cube or sphere jump to zero simply because it is being passed
			//an empty array at first.
		}
	}
	
	
	//Main 
	public static void main(String args[]){
		saiTutorial mySAI = new saiTutorial();
	}
}
