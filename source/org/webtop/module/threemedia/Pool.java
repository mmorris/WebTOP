/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.threemedia;

//import vrml.external.field.*;

import org.webtop.x3d.*;
import org.webtop.x3d.output.Switch;	// eventually use Grid, yes?
import org.webtop.x3d.SAI;
import org.web3d.x3d.sai.*;

public class Pool extends X3DObject {
	public static final int FULL=1,SPARSE=2;

	private final float size;			// length of side of square

	private int resolution;
	private float spacing;

	private int sparseResolution;
	private float sparseSpacing;

	private int renderingMode=FULL;

	private boolean gridVisible=true;
	private boolean normalPerVertex=true;

	private MFFloat SetHeightEI,SetSparseHeightEI;
	private SFBool SetSparseEI;
	private final Switch gridSwitch;

	public Pool(SAI sai,float s) {
		super(sai,sai.getNode("World"));
		gridSwitch=new Switch(sai,sai.getNode("Grid-SWITCH"),2);
		size=s;
		reset();
	}

	public void reset() {
		normalPerVertex = true;
		//Make res1 200 in order to have the default Resolution be Very Smooth - JD
		setResolutions(200, 50);
		gridSwitch.setChoice(0);
	}

	public void setOptions(int res, boolean isPerVertex) {
		normalPerVertex = isPerVertex;
		setResolution(res);
	}

	public void setNormalPerVertex(boolean isPerVertex) {
		destroy();
		normalPerVertex = isPerVertex;
		createNode();
	}

	public boolean getNormalPerVertex() {return normalPerVertex;}

	public void setResolutions(int res1, int res2) {
		//System.out.println("Attempting to destroy pool!!!!!!!");
		destroy();
		resolution = res1;
		//spacing = size/(res1-1);
		spacing = size/ res1;
		sparseResolution = res2;
		//sparseSpacing = size/(res2-1);
		sparseSpacing = size/res2;
		createNode();
	}

	public void setResolution(int res) {setResolutions(res, sparseResolution);}

	public int getResolution() {return resolution;}

	public float getSpacing() {return spacing;}

	public void setSparseResolution(int res) {setResolutions(resolution, res);}

	public int getSparseResolution() {return sparseResolution;}

	public float getSparseSpacing() {return sparseSpacing;}

	public void setHeight(float height[]) {
		try {
			if(renderingMode == FULL) SetHeightEI.setValue(height.length, height);
			else SetSparseHeightEI.setValue(height.length, height);
		} catch(OutOfMemoryError fake) {}
		catch(ClassCastException fake) {}
	}

	//Note -- applyRenderingMode() does the actual work!
	public void setRenderingMode(int mode) {renderingMode=mode;}

	public void applyRenderingMode()
	{SetSparseEI.setValue(renderingMode==SPARSE);}

	public int getRenderingMode() {return renderingMode;}

	public void setGridVisible(boolean visible) {
		gridSwitch.setVisible(gridVisible=visible);
	}

	public boolean getGridVisible() {return gridVisible;}

	private void createNode() {
		
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

	protected String getNodeName() {return "<Pool>";}

	private static final String POOL_PROTO =
		"PROTO Pool [ field        SFInt32 resolution 50\n" +
		"             field        SFFloat spacing 2\n" +
		"             field        MFFloat height []\n" +
		"             field        SFInt32 sparse_resolution 25\n" +
		"             field        SFFloat sparse_spacing 4\n" +
		"             field        MFFloat sparse_height []\n" +
		"             field        SFBool  sparse FALSE\n" +
		"             field        SFBool  normalPerVertex TRUE\n" +
		"             field        SFVec3f translation 0 0 0\n" +
		"             eventIn      MFFloat set_height\n" +
		"             eventIn      MFFloat set_sparse_height\n" +
		"             eventIn      SFBool  set_sparse\n" +
//		"             eventOut     SFVec3f mouse_clicked\n" +
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
		"    field    SFBool  sparse IS sparse\n" +
//		"    field    SFVec3f point  0 0 0\n" +
//		"    field    SFBool  mouse_down FALSE\n" +
		"    eventIn  SFBool  set_sparse IS set_sparse\n" +
//		"    eventIn  SFBool  mouse_pressed\n" +
//		"    eventIn  SFVec3f mouse_moved\n" +
//		"    eventOut SFVec3f mouse_clicked IS mouse_clicked\n" +
		"    eventOut SFInt32 whichChoice\n" +
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
		"    \"\n" +
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
//		"          transparency 0\n" +
}
