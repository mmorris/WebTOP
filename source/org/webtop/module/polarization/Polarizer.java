package org.webtop.module.polarization;



import org.webtop.util.*;
import org.webtop.x3d.SAI;
import org.web3d.x3d.*;
import org.web3d.x3d.sai.*;

public class Polarizer extends Filter {
	public Polarizer(SAI sai, float z_, float angle_) {super(sai, z_, angle_); clearMp();}

	public Polarizer(SAI sai, float z_, float angle_, Filter prev_, Filter next_) {
		super(sai, z_, angle_, prev_, next_);
		clearMp();
	}

	private void clearMp() {
		Mp[0] = Mp[1] = Mp[2] = Mp[3] = 0;
	}

	public String getType() {return "Polarizer";}

	public void setAngle(float angle_, boolean setVRML) {
		super.setAngle(angle_, setVRML);
		final double c=Math.cos(angle), s=Math.sin(angle);

		Ma[0] = (float) (c * c);
		Ma[1] = Ma[2] = (float) (s * c);
		Ma[3] = (float) (s * s);
	}

	public void transform(final Engine.EVector E, boolean polarized) {
		//If the light is polarized, the EVector has phase information to be
		//considered; otherwise we can (for efficiency) just transform the
		//electric field itself.
		if(polarized) super.transform(E,polarized);
		else {
			final float oldEx=E.x;		// we'll need it after overwrite
			E.x = Ma[0] * E.x + Ma[1] * E.y;
			E.y = Ma[2] * oldEx + Ma[3] * E.y;
		}
	}

/*	public void createVRMLNode(EAI eai, EventOutObserver obs) {
		final String vrml = VRML + "Polarizer { z " + z + " angle " + angle + " state 0 }";
		nodes=NamedNode.namedArray(eai.world.createVrmlFromString(vrml),"<polarizer>");
		eai.getEO(getNode(),"angle_changed",obs,new VRMLEvent("ANGLE_CHANGED",this));
		eai.getEO(getNode(),"z_changed",obs, new VRMLEvent("Z_CHANGED", this));
		eai.getEO(getNode(),"angle_isActive",obs, new VRMLEvent("ANGLE_ACTIVE", this));
		eai.getEO(getNode(),"z_isActive",obs, new VRMLEvent("Z_ACTIVE", this));
		eai.getEO(getNode(),"isOver",obs, new VRMLEvent("MOUSE_OVER", this));
		eai.getEO(getNode(),"z_isOver",obs, new VRMLEvent("Z_ISOVER", null));
		eai.getEO(getNode(),"angle_isOver",obs, new VRMLEvent("ANGLE_ISOVER", null));
		StateEIn = (EventInSFInt32) eai.getEI(getNode(),"set_state");
		AngleEIn = (EventInSFFloat) eai.getEI(getNode(),"set_angle");
		ZEIn = (EventInSFFloat) eai.getEI(getNode(),"set_z");
		EnabledEIn = (EventInSFBool) eai.getEI(getNode(),"enabled");
		AngleHighlightedEIn = (EventInSFBool) eai.getEI(getNode(),"set_angleHighlighted");
		ZHighlightedEIn = (EventInSFBool) eai.getEI(getNode(),"set_zHighlighted");
	}*/

	
}
