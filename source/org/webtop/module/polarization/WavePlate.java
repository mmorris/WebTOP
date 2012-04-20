package org.webtop.module.polarization;



import org.webtop.util.*;
import org.webtop.x3d.*;
import org.web3d.x3d.sai.*;


public class WavePlate extends Filter {
	private float thickness;

	private SFFloat ThicknessEIn;
	private SFBool	 ThicknessHighlightedEIn;

	public WavePlate(SAI sai, float z_, float angle_, float thickness_) {
		super(sai, z_, angle_);
		thickness = thickness_;
		updateMatrix();
	}

	public WavePlate(SAI sai, float z_, float angle_, float thickness_, Filter prev_, Filter next_) {
		super(sai, z_, angle_, prev_, next_);
		thickness = thickness_;
		updateMatrix();
	}

	public void updateMatrix() {
		double delta2;
		double A, B, C, D, E;
		double cd, sd;
		double cp, sp;

		delta2 = Math.PI * thickness;
		cd = Math.cos(delta2);
		sd = Math.sin(delta2);
		cp = Math.cos(2.0 * -angle);
		sp = Math.sin(2.0 * -angle);

		A = Math.sqrt(cd*cd + sd*cp*sd*cp);
		B = Math.atan2(-sd*cp, cd);
		C = Math.atan2(sd*cp, cd);
		D = -sd*sp;
		E = (D>=0) ? -Math.PI/2.0 : Math.PI/2.0;
		D = Math.abs(D);

		Ma[0] = (float) A;
		Mp[0] = (float) B;
		Ma[1] = Ma[2] = (float) D;
		Mp[1] = Mp[2] = (float) E;
		Ma[3] = (float) A;
		Mp[3] = (float) C;
	}

	public void setThickness(float t, boolean setVRML) {
		thickness = t;
		updateMatrix();
		if(setVRML) {
			ThicknessEIn.setValue(t);
		}
	}

	public void setAngle(float angle_, boolean setVRML) {
		super.setAngle(angle_, setVRML);
		updateMatrix();
	}

	public float getThickness() {return thickness;}

	public void setThicknessHighlighted(boolean highlighted) {
		ThicknessHighlightedEIn.setValue(highlighted);
	}

	public String getType() {return "Wave Plate";}

/*	public void createVRMLNode(SAI eai, EventOutObserver obs) {
		final String vrml = VRML + "WavePlate { z " + z + " angle " + angle + " state 0  thickness " + thickness + " }";
		nodes=NamedNode.namedArray(eai.world.createVrmlFromString(vrml),"<wave plate>");
		eai.getEO(getField(),"angle_changed",obs,new VRMLEvent("ANGLE_CHANGED",this));
		eai.getEO(getField(),"z_changed",obs, new VRMLEvent("Z_CHANGED", this));
		eai.getEO(getField(),"thickness_changed",obs, new VRMLEvent("THICKNESS_CHANGED", this));
		eai.getEO(getField(),"angle_isActive",obs, new VRMLEvent("ANGLE_ACTIVE", this));
		eai.getEO(getField(),"z_isActive",obs, new VRMLEvent("Z_ACTIVE", this));
		eai.getEO(getField(),"thickness_isActive",obs, new VRMLEvent("THICKNESS_ACTIVE", this));
		eai.getEO(getField(),"isOver",obs, new VRMLEvent("MOUSE_OVER", this));
		eai.getEO(getField(),"z_isOver",obs, new VRMLEvent("Z_ISOVER", this));
		eai.getEO(getField(),"angle_isOver",obs, new VRMLEvent("ANGLE_ISOVER", this));
		eai.getEO(getField(),"thickness_isOver",obs, new VRMLEvent("THICKNESS_ISOVER", this));
		StateEIn = (EventInSFInt32) eai.getEI(getField(),"set_state");
		AngleEIn = (EventInSFFloat) eai.getEI(getField(),"set_angle");
		ZEIn = (EventInSFFloat) eai.getEI(getField(),"set_z");
		ThicknessEIn = (EventInSFFloat) eai.getEI(getField(),"set_thickness");

		EnabledEIn = (SFBool) eai.getEI(getField(),"set_enabled");
		AngleHighlightedEIn = (SFBool) eai.getEI(getField(),"set_angleHighlighted");
		ZHighlightedEIn = (SFBool) eai.getEI(getField(),"set_zHighlighted");
		ThicknessHighlightedEIn = (SFBool) eai.getEI(getField(),"set_thicknessHighlighted");
	}*/


}
