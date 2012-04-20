//Pool.java
//Creates the X3D pool PROTO used in this module 
//Updated June 06 2008 by Jeremy Davis

package org.webtop.module.waves;

import org.webtop.x3d.*;
import org.webtop.x3d.output.Switch;

import org.web3d.*;
import org.web3d.x3d.sai.*;


public class Pool extends X3DObject {
	
	private Waves applet; 
	
	public static final int FULL =1, SPARSE = 2;
	
	private final float size;  //length of side of square
	
	private int resolution; 
	private float spacing; 
	
	private int sparseResolution; 
	private float sparseSpacing; 
	
	private int renderingMode = FULL; 
	
	private boolean gridVisible = true; 
	private boolean normalPerVertex = true;
	
	
	private MFFloat SetHeightEI, SetSparseHeightEI;
	private SFBool SetSparseEI; 
	private final Switch gridSwitch;
	
	//Create new instance of Pool 
	public Pool(SAI sai, float s, Waves wave){
		super(sai, sai.getNode("World"));
		gridSwitch = new Switch(sai, sai.getNode("Grid-SWITCH"),2);
		size = s; 
		applet = wave;
		System.out.println("Pool consctructor is calling reset");
		reset();
	}
	
	public void reset(){
		normalPerVertex = true; 
		System.out.println("reset called setResolutions");
		setResolutions(200, 50);
		gridSwitch.setChoice(0);
	}
	
	public void setOptions(int res, boolean isPerVertex){
		normalPerVertex = isPerVertex;
		setResolution(res);
	}
	
	public void setNormalPerVertex(boolean isPerVertex){
		destroy();
		normalPerVertex = isPerVertex; 
		System.out.println("setNormalPerVertex called me");
		createX3DNode();
	}
	
	public boolean getNormalPerVertex() { return normalPerVertex; }
	
	public void setResolutions(int res1, int res2){
		destroy(); 
		resolution = res1; 
		spacing = size/(res1 -1);
		sparseResolution = res2;
		sparseSpacing = size/(res2 -1);
		System.out.println("setResolutions called me:");
		createX3DNode();
	}
	
	public void setResolution(int res) { setResolutions(resolution, res); }
	
	public int getResolution() { return resolution; }
	
	public float getSpacing() { return spacing; }
	
	public void setSparseResolution(int res) { setResolutions(resolution, res); }
	
	public int getSparseResolution() { return sparseResolution; }
	
	public float getSparseSpacing() { return sparseSpacing; }
	
	public void setHeight(float height[]){
		try{
			if(renderingMode == FULL)
				SetHeightEI.setValue(height.length, height); //this 0 here may throw exceptions [JD]
			else
				SetSparseHeightEI.setValue(height.length, height);
		}catch (OutOfMemoryError fake) {}
		catch(ClassCastException fake) {}
	}
	
	//Note: applyRenderingMode() does the actual work 
	public void setRenderingMode(int mode) { 
		renderingMode = mode; 
	}
	
	public void applyRenderingMode(){
		SetSparseEI.setValue(renderingMode == SPARSE);
	}
	
	public int getRenderingMode(){ return renderingMode; }
	
	public void setGridVisible(boolean visible){
		gridSwitch.setVisible(gridVisible=visible);
	}
	
	public boolean getGridVisible() { return gridVisible; }
	
	public void callback(SFVec3f e, double timestamp, Object data){
		if(data.equals("mouse_clicked")){
			float xyz[] = new float[3];
			((SFVec3f)e).getValue(xyz); //eh...may not work if this method is necessary [JD]
			System.out.println("(x, y, z)=(" + xyz[0] + ", " + xyz[1] + ", " + xyz[2] + ")");
		}
	}
	
	private void createX3DNode(){
		/*final StringBuffer sb = new StringBuffer(resolution*resolution*5);
		sb.append(POOL_PROTO);
		sb.append("Pool {\n");
		sb.append(" normalPerVertex ").append(normalPerVertex?"TRUE":"FALSE").append('\n');
		sb.append(" resolution ").append(resolution).append('\n');
		sb.append(" spacing ").append(spacing).append('\n');
		sb.append(" height[ ");
		
		for(int i=0; i<resolution*resolution; i++){
			if(i%20 == 0){
				sb.append("\n    ");
			}
			sb.append("0 ");
		}
		sb.append("  ]\n");
		sb.append("  sparse_resolution ").append(sparseResolution).append('\n');
		sb.append ("  sparse_spacing ").append(sparseSpacing).append('\n');
		sb.append(" sparse_height [");
		
		for(int i=0; i<sparseResolution*sparseResolution; i++){
			if(i%20 == 0) sb.append("\n    ");
			sb.append("0 ");
		}
		
		sb.append("  ]\n");
		sb.append(" translation -").append(size/2).append(" 0 -").append(size/2).append('\n');
		sb.append("}\n");*/
		
		//applet.getSAI().getScene().createNode(sb.toString()); //create the pool in the X3D file [JD]
		//How to place the node in the scene? [JD]
		//place(); //guess that way :D //Create the pool in the X3D file [JD]
		/*SetHeightEI = (MFFloat) sai.getField(getNode(), "set_height");
		SetSparseHeightEI = (MFFloat) sai.getField(getNode(), "set_sparse_height");
		SetSparseEI = (SFBool) sai.getField(getNode(), "set_sparse");*/
		
		
		/*Trying something from 2Media here */
		System.out.println("Am I even getting called?");
		createProto("Pool");
		//Get access to the initializeOnly fields
		SFInt32 res = (SFInt32)getNode().node.getField("resolution");
		SFFloat spac = (SFFloat)getNode().node.getField("spacing");
		MFFloat hght = (MFFloat)getNode().node.getField("height");
		SFInt32 sparseRes = (SFInt32)getNode().node.getField("sparse_resolution");
		SFFloat sparseSpace = (SFFloat)getNode().node.getField("sparse_spacing");
		MFFloat sparseHeight = (MFFloat)getNode().node.getField("sparse_height");
		SFBool norm = (SFBool)getNode().node.getField("normalPerVertex");
		SFVec3f trans = (SFVec3f)getNode().node.getField("translation");
		
		//Initialize large grid
		res.setValue(resolution);
		spac.setValue(spacing);


		//Initialize sparse grid
		sparseRes.setValue(sparseResolution);
		sparseSpace.setValue(sparseSpacing);

		
		//Move pool
		float values[] = {-size/2,0f,-size/2};		
		trans.setValue(values);
		
		//Set normal per vertex calculations
		norm.setValue(normalPerVertex);

		//Attach pool to scene
		place();

		//Bind input fields
		SetHeightEI = (MFFloat) sai.getInputField(getNode(),"set_height");
		SetSparseHeightEI = (MFFloat) sai.getInputField(getNode(),"set_sparse_height");
		SetSparseEI = (SFBool) sai.getInputField(getNode(),"set_sparse");
	}
	
	protected String getNodeName() { return "<Pool>"; }
	
	//stopped on POOL_PROTO [JD]
	/*private static final String POOL_PROTO =  //TO UNCOMMENT, REMOVE HERE  [JD]
		"PROTO Pool [ initializeOnly        SFInt32 resolution 50\n" +
		"             initializeOnly        SFFloat spacing 2\n" +
		"             initializeOnly        MFFloat height []\n" +
		"             initializeOnly        SFInt32 sparse_resolution 25\n" +
		"             initializeOnly        SFFloat sparse_spacing 4\n" +
		"             initializeOnly        MFFloat sparse_height []\n" +
		"             initializeOnly        SFBool  sparse FALSE\n" +
		"             initializeOnly        SFBool  normalPerVertex TRUE\n" +
		"             initializeOnly        SFVec3f translation 0 0 0\n" +
		"             inputOnly      MFFloat set_height\n" +
		"             inputOnly      MFFloat set_sparse_height\n" +
		"             inputOnly      SFBool  set_sparse\n" +
//		"             outputOnly     SFVec3f mouse_clicked\n" +
		"]\n" +
		"{\n" +
		"  DEF Pool-TRANSFORM Transform {\n" +
		"    translation IS translation\n" +
		"    children [\n" +
//		"      DEF Pool-SENSOR TouchSensor { }\n" +
		"      DEF Pool-SWITCH Switch {\n" +
		"        whichChoice 0\n" +
		"        choice [\n" +
		"          Shape {\n" +
		"            geometry ElevationGrid {\n" +
		"              normalPerVertex IS normalPerVertex\n" +
		"              solid TRUE\n" +
		"              creaseAngle 3.142\n" +
		"              height          IS height\n" +
		"              xDimension      IS resolution\n" +
		"              zDimension      IS resolution\n" +
		"              xSpacing        IS spacing\n" +
		"              zSpacing        IS spacing\n" +
		"              set_height      IS set_height\n" +
		"            }\n" +
		"            appearance DEF Pool-APPEARANCE Appearance {\n" +
		"              material Material {\n" +
		"                ambientIntensity 0.4\n" +
			 	"                diffuseColor     0.7 0.7 1.0\n" +
		"                diffuseColor     0.25 0.25 0.32\n" +
		"                specularColor    0.10 0.10 0.10\n" +
		"                emissiveColor    0.1 0.1 0.2\n" +
		"                transparency     0\n" +
		//"diffuseColor .122 .129 .604\n specularColor .1 .1 .1\n emissiveColor .1 .1 .2\n"+
		"              }\n" +
		"            }\n" +
		"          }\n" +
		"          Shape {\n" +
		"            geometry ElevationGrid {\n" +
		"              colorPerVertex  FALSE\n" +
		"              normalPerVertex IS normalPerVertex\n" +
		"              solid TRUE\n" +
		"              creaseAngle 3.142\n" +
		"              height          IS sparse_height\n" +
		"              xDimension      IS sparse_resolution\n" +
		"              zDimension      IS sparse_resolution\n" +
		"              xSpacing        IS sparse_spacing\n" +
		"              zSpacing        IS sparse_spacing\n" +
		"              set_height      IS set_sparse_height\n" +
		"            }\n" +
		"            appearance USE Pool-APPEARANCE\n" +
		"          }\n" +
		"        ]\n" +
		"      }\n" +
		"    ]\n" +
		"  }\n" +
		"  DEF Pool-SCRIPT Script {\n" +
		"    initializeOnly    SFBool  sparse IS sparse\n" +
//		"    initializeOnly    SFVec3f point  0 0 0\n" +
//		"    initializeOnly    SFBool  mouse_down FALSE\n" +
		"    inputOnly  SFBool  set_sparse IS set_sparse\n" +
//		"    inputOnly  SFBool  mouse_pressed\n" +
//		"    inputOnly  SFVec3f mouse_moved\n" +
//		"    outputOnly SFVec3f mouse_clicked IS mouse_clicked\n" +
		"    outputOnly SFInt32 whichChoice\n" +
		"    url \"vrmlscript:\n" +
		"      function initialize() {\n" +
		"        if(sparse) whichChoice = 1;\n" +
		"        whichChoice = 0;\n" +
		"        mouse_clicked = new SFVec3f(0, 0, 0);\n" +
		"      }\n" +
		"      function set_sparse(value, time) {\n" +
		"        if(value) whichChoice = 1;\n" +
		"        else whichChoice = 0;\n" +
		"      }\n" +
/*
		"      function mouse_pressed(value, time) {\n" +
		"        if(!value) {\n" +
		"          mouse_clicked = position;\n" +
		"          mouse_down = FALSE;\n" +
		"        }\n" +
		"        else {\n" +
		"          mouse_down = TRUE;\n" +
		"        }\n" +
		"      }\n" +
		"      function mouse_moved(value, time) {\n" +
		"        if(mouse_down) {\n" +
		"          position = value;\n" +
		"        }\n" +
		"      }\n" +
*/
		/*"    \"\n" +  //TO UNCOMMENT, REMOVE HERE [JD]
		"  }\n" +
		"  ROUTE Pool-SCRIPT.whichChoice TO Pool-SWITCH.whichChoice\n" +
		//"  ROUTE Pool-SENSOR.isActive TO Pool-SCRIPT.mouse_pressed\n" +
		//"  ROUTE Pool-SENSOR.hitPoint_changed TO Pool-SCRIPT.mouse_moved\n" +
		"}\n";

//		"          ambientIntensity 0.05\n" +
//		"          diffuseColor 0.20 0.08 0.02\n" +
//		"          specularColor  1.0 1.0 1.0\n" +
//		"          emissiveColor  0 0 0\n" +
//		"          shininess 1\n" +
//		"          transparency 0\n" +";  */
}
