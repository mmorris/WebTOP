package org.webtop.module.polarization;



import org.webtop.x3d.*;
import org.web3d.x3d.sai.*;

public abstract class Filter extends X3DObject {
	
	protected String id;

	// Jones-Matrix:
	// [ 0	1 ]
	// [ 2	3 ]
	//
	// Ma = amplitude
	// Mp = phase
	protected final float Ma[]={1,0,0,1},Mp[]={1,0,0,1};
	protected float z,angle;

	protected NamedNode nodes[];

	protected SFInt32 StateEIn;
	protected SFFloat AngleEIn,ZEIn;

	protected SFBool EnabledEIn;
	protected SFBool ZHighlightedEIn;
	protected SFBool AngleHighlightedEIn;

	protected static final float PI = (float) (Math.PI);
	protected static final float PI_2 = (float) (PI * 2.0f);
	protected static final float PI_1_2 = (float) (PI/2.0f);
	protected static final float PI_3_2 = (float) (PI*3.0f/2.0f);

	//private Engine engine;
	//private FilterPanel filterPanel;

	//public Filter() {
	//	super(sai, sai.getNode())
	//}

	public Filter(SAI sai, float z_, float angle_) {
		super(sai, sai.getNode("FilterHolder"));
		z = z_;
		setAngle(angle_, false);
	}

	public Filter(SAI sai, float z_, float angle_, Filter prev_, Filter next_) {
		super(sai, sai.getNode("FilerHolder"));
		z = z_;
		setAngle(angle_, false);
	}

	public void setID(String s) {id = s;}
	public String getID() {return id;}

	//public void setEngine(Engine e) {engine = e;}

	//public void setFilterPanel(FilterPanel f) {filterPanel = f;}

	public float getZ() {return z;}

	public void setZ(float z_, boolean setVRML) {
		z = z_;
		if(setVRML) ZEIn.setValue(z_);
	}

	public void setAngle(float angle_) {setAngle(angle_, false);}

	public void setAngle(float angle_, boolean setVRML) {
		angle = angle_;
		if(setVRML) AngleEIn.setValue(angle);
	}

	public float getAngle() {return angle;}

	public void setActive(boolean active) {
		if(active) StateEIn.setValue(1);
		else StateEIn.setValue(0);
	}

	public void setEnabled(boolean enabled) {
		EnabledEIn.setValue(enabled);
	}

	public void setAngleHighlighted(boolean highlighted) {
		AngleHighlightedEIn.setValue(highlighted);
	}

	public void setZHighlighted(boolean highlighted) {
		ZHighlightedEIn.setValue(highlighted);
	}

	protected float[] getMatrix() {return (float[])Ma.clone();}

	// Transform the electric field vector using the Jones-Matrix.
	// The argument 'polarized' may be useful to subclasses.
	public void transform(Engine.EVector E, boolean polarized) {
		double A1, A2, B1, B2;
		double r1, x1, r2, x2;
		double sr, sx;
		double Ex, Ey, Ax, Ay;			// we'll need the old values until the end

		// X Component
		A1 = E.x * Ma[0];
		B1 = E.xphase + Mp[0];
		A2 = E.y * Ma[1];
		B2 = E.yphase + Mp[1];
		r1 = A1 * Math.cos(B1);
		x1 = A1 * Math.sin(B1);
		r2 = A2 * Math.cos(B2);
		x2 = A2 * Math.sin(B2);
		sr = r1 + r2;
		sx = x1 + x2;
		Ex = Math.sqrt(sr*sr + sx*sx);
		Ax = Math.atan2(sx, sr);

		// Y Component
		A1 = E.x * Ma[2];
		B1 = E.xphase + Mp[2];
		A2 = E.y * Ma[3];
		B2 = E.yphase + Mp[3];
		r1 = A1 * Math.cos(B1);
		x1 = A1 * Math.sin(B1);
		r2 = A2 * Math.cos(B2);
		x2 = A2 * Math.sin(B2);
		sr = r1 + r2;
		sx = x1 + x2;
		Ey = Math.sqrt(sr*sr + sx*sx);
		Ay = Math.atan2(sx, sr);

		E.x = (float) Ex;
		E.y = (float) Ey;
		E.xphase = (float) Ax;
		E.yphase = (float) Ay;
	}


	public NamedNode[] getNodes() {return nodes;}

	public NamedNode getNode() {return nodes[0];}

	public abstract String getType();

}
