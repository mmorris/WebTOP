/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * Currently there is some sort of feedback issue that causes      !
 * the widgets to constantly be set in a loop. I will remove this  !
 * comment block when I have resolved the issue.                   !
 * [3.28.2006, PC]                                                 !
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */


package org.webtop.module.nslit;

/**
 * Defines a class that takes two XDrag widgets and controls them
 * to conform to the boundaries of the NSlit aperture screen.
 * Based on work by Kiril Vidimce and others.  See NSlit.java for
 * all authors of NSlit material.
 * @author Paul Cleveland
 */

import org.webtop.x3d.widget.*;
import org.webtop.util.WTInt;
import org.webtop.util.DebugPrinter;

public class NSlitDragger implements ScalarWidget.Listener {

    //***** Widgets to control. *****//
    XDragWidget distanceWidget, widthWidget;
    
    //***** Shared Wrapper for number of slits *****//
    WTInt N;
    
    //***** Other calculation variables *****//
    private boolean updating = false;  //Used to avoid feedback.  Hopefully.  [PC]
    
    float 			APERTURE_WIDTH = 1000f,
    				//width = 40.0f,
    				width = 62.5f,
    				//distance = 220.0f,
    				distance = 250f,
    				//distanceWidgetPosition=220.0f;
    				distanceWidgetPosition = 125f;
    
    
    //***** Book keeping values for other classes *****//
    private float   min_width = 0.0f,
    				max_width = 110.0f, 
    				min_distance = 40.0f, 
    				max_distance = 480.0f;
    private boolean widthSetInternally = false, distanceSetInternally=false;
    	
    public NSlitDragger(XDragWidget widthDragger, XDragWidget distDragger, WTInt n) {
    	//Get references to the Widgets
        widthWidget    = widthDragger;
        distanceWidget = distDragger;
                
        //Add this as a Listener to the Widgets
        widthWidget.addListener(this);
        distanceWidget.addListener(this);
        
        //Get a reference to N
        N = n;
    }
    
    //***** ScalarWidget.Listener Interface *****//
    //valueChanged() is called as soon as a Widget is activated (I think) [PC]
    public void valueChanged(ScalarWidget src, float value) {
    	/* I'm not sure if there need to be two cases for each widget:
		 * (1) the dragger is being manipulated
		 * (2) the dragger's value was set directly via a Coupled NumberBox
		 * In the case of (1), the [WIDTH/DISTANCE]_TRAVERSAL case of the original callback
		 *  switch should probably be used.
		 * In the case of (2), the [WIDTH/DISTANCE] case may should be used.
		 * I believe these two situations must be considered, so to check this, I've included
		 *  a selection structure that relies on Widget.isActive() to tell whether the Widget
		 *  is being dragged or not.  [PC]
		 */
    	
    	if(!updating) {  
    		
    		//** Debug: Finding value sent by a widget **//
        	String widget = (src==distanceWidget) ? "distanceWidget" : ( (src==widthWidget) ? "widthWidget" : "Unknown Widget");
        	DebugPrinter.println("NSlitDragger ScalarWidget.Listener: " + widget + "'s value changed to " + value);
      
    		
        	updating = true;
	    	if(src==widthWidget) {
	    		/* Updates needed:
	    		 * (1) update distance constraints
	    		 */
	    		
	    		//GRANT - ADDED THIS
	    		if(widthSetInternally)
	    		{
	    			widthSetInternally = false;
	    			updating = false;
	    			return;
	    		}
	    		
	        		width = value;
	        		//System.out.println("WidthChanged to: " + value);
	        		//updateDistanceDraggerConstraints(); //why distance?
	        		//Changed this call...seems that you should update the width instead of the distance if 
	        		//the width widget is generating the event...seems to be working now [JD]
	        		updateWidthDraggerConstraints();

	    	}
	    	
	    	else if(src==distanceWidget) {
	    		/* Updates needed:
	    		 * (1) update width constraints
	    		 * (2) move width Widgets
	    		 */
	    		
	    		if(distanceSetInternally)
	    		{
	    			distanceSetInternally=false;
	    			updating = false;
	    			return;
	    		}
	    	
		    		
	    			distance = 2*value/(N.getValue() - 1);
	    			
	    			if(N.getValue()==1)
	    				distance=0;
	    			
		    		distanceWidgetPosition = value;
		    		System.out.println("Distance changed to: " + distance);
		    		System.out.println("Distance Widget Position changed to: " + distanceWidgetPosition);
		    		//updateWidthDraggerConstraints(); //why width?
		    		//changed the call to update the distance...seems that you should update the distance if 
		    		//the distance widget generated the event..seems to be working now [JD]
		    		updateDistanceDraggerConstraints();
	    	}
	    	else {
	    		System.out.println("NSlitDragger.valueChanged(): ERROR: Invalid widget source: " + src);
	    	}
	    	
	    	//Once done updating, allow further updates to take place.
	    	updating = false;
    	}//End actions to perform if updating
    }
    
    public void updateWidthDraggerConstraints() {
    	float max,
    	max1=APERTURE_WIDTH/2.0f - distanceWidgetPosition,	//The slit touches the wall
    	max2=distance/2.0f;									//The two slits touch
    	
    	DebugPrinter.println("distWidgetPosition-> " + distanceWidgetPosition + " distance->" + distance);
    		
    	if(N.getValue()==1) {
    		max = APERTURE_WIDTH/2.0f;
    	}
    	else{
    		max = (max1 < max2) ? max1 : max2; 
    	}
    	min_width = 0;
    	max_width = max;
    	
    	widthWidget.setMin(0);
    	widthWidget.setMax(max);
    }
    
    
 
    
    public void updateDistanceDraggerConstraints() {
    	float min = width * (N.getValue() - 1),
			  max = APERTURE_WIDTH / 2.0f - width; 

    	//** Manage book keeping variables for other classes **//
    	min_distance = min;
    	max_distance = max;
    	
    	distanceWidget.setMin(min);
    	distanceWidget.setMax(max);
    	
    	distanceSetInternally=true;   	   	
    }
    
    public void evaluateDistance() {
    		distanceSetInternally = true;
			distanceWidgetPosition=distance*(N.getValue()-1)/2.0f;
			distanceWidget.setValue(distanceWidgetPosition);		
    }
    
    //If was single slit, evaluate differently
    public float evaluateSingle() {
    	distanceSetInternally = true;
    	distance=2*width;
    	distanceWidgetPosition=distance*(N.getValue()-1)/2.0f;
    	distanceWidget.setValue(distanceWidgetPosition);
    	return distance/500.0f;
    }
    
    //Checks to see if the new n value is possible
    boolean validN(int n)
    {
    	return ((distance*(n-1)/2.0f + width) < 500);
    }   
    
    //***** Reporting methods for other classes *****//
    //Returns values for use by the float boxes
    public float getMinWidth(){
    	return min_width/500.0f;  
    }
    public float getMaxWidth() {
    	return max_width/500.0f;
    }
    public float getMinDistance() {
    	return 2*min_distance/(N.getValue() - 1)/500.0f;
    }
    public float getMaxDistance() {
    	return 2*max_distance/(N.getValue() - 1)/500.0f;
    }
}
