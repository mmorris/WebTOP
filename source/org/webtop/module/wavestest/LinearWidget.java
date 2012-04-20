package org.webtop.module.wavestest;

import org.webtop.x3d.*;

public class LinearWidget extends X3DObject {

	private float[] position = new float[3]; 
	public static int count = 0; 
	private SAI sai;
	
	
	public LinearWidget(SAI _sai, String nodeName){
		super(_sai, _sai.getNode(nodeName));
		
		sai = _sai;
		//create an instance of the linear widget...
		createProto("LinearWidget");
		//place the linear widget in the scene (hopefully)
		place(); 
		count++;
		
		//May have to use createNode to make a new transform for each linear widget...not
		//sure yet 
	}
	
	public String getNodeName(){
		return "<LinearWidget>";
	}
	
	
	public void createShape(){
		createProto("LinearWidget");
		place();
		count++;
	}
}
