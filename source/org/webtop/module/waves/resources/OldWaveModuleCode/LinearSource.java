package webtop.wave;

import vrml.external.field.*;

import webtop.component.*;
import webtop.util.WTMath;
import webtop.wsl.client.*;
import webtop.wsl.script.WSLNode;
import webtop.wsl.event.*;

public class LinearSource extends WaveSource {
	private float angle;
	private EventInSFFloat set_angle;

	/*public LinearSource(Engine e, WidgetsPanel widgetPanel, StatusBar bar) {
		super(e, widgetPanel, bar);
		angle = 0.0f;
		createVRMLNode();
	}*/

	public LinearSource(Engine e, WidgetsPanel widgetPanel, float A, float L, float E, float x, float y, float T) {
		super(e, widgetPanel, A, L, E, x, y);
		angle = T;
		createVRMLNode();
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float a, boolean setVRML) {
		angle = a;
		if(setVRML) set_angle.setValue(angle);
	}

	public float getValue(float x, float y, float t) {
		return (float) (amplitude * Math.cos(k * (x * Math.cos(angle) + y * Math.sin(angle) - WAVE_SPEED * t) + phase));
	}

	protected void createVRMLNode() {
		StringBuffer sb = new StringBuffer(VRMLString);
		sb.append("LinearWidget { amplitude ").append(amplitude);
		sb.append(" wavelength ").append(wavelength);
		sb.append(" phase ").append(phase);
		sb.append(" angle ").append(angle);
		sb.append(" x ").append(X);
		sb.append(" y ").append(Y);
		sb.append(" }\n");
		create(sb.toString());

		set_angle = (EventInSFFloat) engine.getEAI().getEI(getNode(),"set_angle");

		engine.getEAI().getEO(getNode(),"angle_changed",this, "angle_changed");
		engine.getEAI().getEO(getNode(),"mouseOverAngle",this, "mouseOverAngle");
	}

	protected String getNodeName() {return "<LinearWidget>";}

	public String toString() {
		return new String("LinearSource(Amplitude=" + amplitude + ", Wavelength=" + wavelength
			+ ", Phase=" + phase + ", Angle=" + angle + ", X=" + X + ", Y=" + Y + ")");
	}

	public void callback(EventOut e, double timestamp, Object data) {
		String arg = (String) data;
		WSLPlayer wslPlayer = engine.getWSLPlayer();
		if(arg.equals("angle_changed")) {
			setAngle(((EventOutSFFloat) e).getValue(), false);
			((LinearPanel)widgetsPanel.getSwitcher().getActivePanel()).setAngle(angle);
			if(wslPlayer!=null)
				wslPlayer.recordMouseDragged(getID(), "angle", String.valueOf(WTMath.toDegs(angle)));
			engine.update();
		} else if(arg.equals("mouseOverAngle")) {
			if(((EventOutSFBool) e).getValue())
				engine.getStatusBar().setText("Use this widget to change the angle.  Rotate 360 degrees.");
			else if(!dragging)
				engine.getStatusBar().reset();
		} else super.callback(e,timestamp,data);
	}

	public WSLNode toWSLNode() {
		WSLNode node = super.toWSLNode();
		node.getAttributes().add("angle", String.valueOf(WTMath.toDegs(angle)));

		return node;
	}

	private static String VRMLString =
		"PROTO RotationWidget [\n" +
		"  field        SFFloat     minAngle            0\n" +
		"  eventIn      SFFloat     set_minAngle\n" +
		"  eventOut     SFFloat     minAngle_changed\n" +
		"  field        SFFloat     maxAngle            -1\n" +
		"  eventIn      SFFloat     set_maxAngle\n" +
		"  eventOut     SFFloat     maxAngle_changed\n" +
		"  field        SFFloat     offset              0\n" +
		"  eventIn      SFFloat     set_offset\n" +
		"  eventOut     SFFloat     offset_changed\n" +
		"  exposedField SFBool      enabled             TRUE\n" +
		"  eventOut     SFBool      isActive\n" +
		"  eventOut     SFBool      isOver\n" +
		"  eventIn      SFBool      set_isOver\n" +
		"  eventIn      SFBool      set_isActive\n" +
		"  eventOut     SFRotation  rotation_changed\n" +
		"  eventOut     SFVec3f     trackPoint_changed\n" +
		"  field        MFNode      normalGeometry      []\n" +
		"  field        MFNode      overGeometry        []\n" +
		"  field        MFNode      clickedGeometry     []\n" +
		"]\n" +
		"{\n" +
		"  Group {\n" +
		"    children [\n" +
		"      DEF Touch-SENSOR TouchSensor {\n" +
		"        enabled IS enabled\n" +
	"        isOver IS isOver\n" +
		"      }\n" +
		"      DEF Rotational-SENSOR PlaneSensor {\n" +
		"        isActive IS isActive\n" +
		"        enabled IS enabled\n" +
		"        maxPosition 2000 2000\n" +
		"        minPosition -2000 -2000\n" +
		"        offset 2000 0 0\n" +
		"      }\n" +
		"      DEF Rotational-TRANSFORM Transform {\n" +
		"        children DEF Rotational-SWITCH Switch {\n" +
		"          whichChoice 0\n" +
		"          choice [\n" +
		"            Group { children IS normalGeometry }\n" +
		"            Group { children IS overGeometry }\n" +
		"            Group { children IS clickedGeometry }\n" +
		"          ]\n" +
		"        }\n" +
		"      }\n" +
		"    ]\n" +
		"  }\n" +
		"  DEF Rotational-SCRIPT Script {\n" +
		"    field    SFFloat minAngle IS minAngle\n" +
		"    eventIn  SFFloat set_minAngle IS set_minAngle\n" +
		"    eventOut SFFloat minAngle_changed IS minAngle_changed\n" +
		"    field    SFFloat maxAngle IS maxAngle\n" +
		"    eventIn  SFFloat set_maxAngle IS set_maxAngle\n" +
		"    eventOut SFFloat maxAngle_changed IS maxAngle_changed\n" +
		"    field    SFFloat trackOffset 0\n" +
		"    field    SFFloat offset IS offset\n" +
		"    eventIn  SFFloat set_offset IS set_offset\n" +
		"    eventOut SFFloat offset_changed IS offset_changed\n" +
		"    eventIn  SFVec3f set_translation\n" +
		"    eventIn  SFVec3f set_hitPoint\n" +
		"    eventIn  SFBool  set_touchSensorIsActive\n" +
		"    eventIn  SFBool  set_touchSensorIsOver\n" +
		"    eventIn  SFBool  set_planeSensorIsActive\n" +
		"    eventIn  SFBool  set_isActive IS set_isActive\n" +
		"    eventIn  SFBool  set_isOver IS set_isOver\n" +
		"    eventOut SFRotation rotation_changed IS rotation_changed\n" +
		"    eventOut SFVec3f    trackPoint_changed IS trackPoint_changed\n" +
		"    eventOut SFVec3f    internalOffset_changed\n" +
		"    eventOut SFInt32    whichChoice_changed\n" +
		"    field    SFBool  setOffset FALSE\n" +
		"    field    SFFloat lastOffset 0\n" +
		"    field    SFFloat lastAngle  0\n" +
		"    field    SFBool  isActive FALSE\n" +
		"    field    SFBool  isOver   FALSE\n" +
		"    field    MFNode  normalGeometry IS normalGeometry\n" +
		"    field    MFNode  overGeometry IS overGeometry\n" +
		"    field    MFNode  clickedGeometry IS clickedGeometry\n" +
		"    field    SFNode  Rotational-SWITCH USE Rotational-SWITCH\n" +
		"    #field    SFNode  Touch-SENSOR USE Touch-SENSOR\n" +
		"    url \"javascript:\n" +
		"      function initialize() {\n" +
		"        rotation_changed = new SFRotation(0, 0, 1, offset);\n" +
		"        internalOffset_changed = new SFVec3f(10, 0, 0);\n" +
		"        trackOffset = offset;\n" +
		"        lastOffset = offset;\n" +
		"        lastAngle = offset;\n" +
		"        whichChoice_changed = 0;\n" +
		"      }\n" +
		"      function set_translation(value, time) {\n" +
		"        length = Math.sqrt(value[0]*value[0] + value[1]*value[1]);\n" +
		"        angle = Math.atan2(value[1], value[0]);\n" +
		"        if(angle<0) angle = 2*Math.PI + angle;\n" +
		"        if(angle>=0 && angle<Math.PI/2.0 && lastAngle>=Math.PI*3.0/2.0)\n" +
		"          trackOffset += angle + Math.PI*2.0 - lastAngle;\n" +
		"        else if(angle>=Math.PI*3.0/2.0 && lastAngle>=0 && lastAngle<Math.PI/2.0)\n" +
		"          trackOffset += angle - lastAngle - Math.PI*2.0;\n" +
		"        else\n" +
		"          trackOffset += angle - lastAngle;\n" +
		"        offset = trackOffset;\n" +
		"        if(minAngle<maxAngle) {\n" +
		"          if(offset<minAngle) offset = minAngle;\n" +
		"          if(trackOffset<minAngle-Math.PI*2.0) trackOffset += Math.PI*2.0;\n" +
		"        }\n" +
		"        if(maxAngle>minAngle) {\n" +
		"          if(offset>maxAngle) offset = maxAngle;\n" +
		"          if(trackOffset>maxAngle+Math.PI*2.0) trackOffset -= Math.PI*2.0;\n" +
		"        }\n" +
		"        lastOffset = offset;\n" +
		"        lastAngle = angle;\n" +
		"        rotation_changed[3] = offset;\n" +
		"        offset_changed = offset;\n" +
		"      }\n" +
		"      function set_hitPoint(value, time) {\n" +
		"        if(setOffset) {\n" +
		"          setOffset = FALSE;\n" +
		"          internalOffset_changed[0] = value[0];\n" +
		"          internalOffset_changed[1] = value[1];\n" +
		"          length = Math.sqrt(value[0]*value[0] + value[1]*value[1]);\n" +
		"          lastAngle = Math.atan2(value[1], value[0]);\n" +
		"          if(lastAngle<0) lastAngle = Math.PI*2.0 + lastAngle;\n" +
		"        }\n" +
		"      }\n" +
		"      function set_offset(value, time) {\n" +
		"        offset = value;\n" +
		"        trackOffset = offset;\n" +
		"        rotation_changed[3] = offset;\n" +
		"        internalOffset_changed[0] = 2000 * Math.cos(offset);\n" +
		"        internalOffset_changed[1] = 2000 * Math.sin(offset);\n" +
		"      }\n" +
		"      function set_touchSensorIsActive(value, time) {\n" +
		"        isActive = value;\n" +
		"        if(value) setOffset = TRUE;\n" +
		"      }\n" +
		"      function set_touchSensorIsOver(value, time) {\n" +
		"        isOver = value;\n" +
		"        if(value && !isActive && overGeometry.length > 0) {\n" +
		"          whichChoice_changed = 1;\n" +
		"        }\n" +
		"        else if(!value && !isActive) {\n" +
		"          whichChoice_changed = 0;\n" +
		"        }\n" +
		"      }\n" +
		"      function set_planeSensorIsActive(value, time) {\n" +
		"        if(!value) {\n" +
		"          trackOffset = offset;\n" +
		"          lastOffset = offset;\n" +
		"          whichChoice_changed = 0;\n" +
		"       }\n" +
		"        else {\n" +
		"          if(clickedGeometry.length >0) {\n" +
		"            whichChoice_changed = 2;\n" +
		"          }\n" +
		"        }\n" +
		"      }\n" +
		"      function set_isActive(value, time) {\n" +
		"        isActive = value;\n" +
		"        if(value && clickedGeometry.length > 0) whichChoice_changed = 2;\n" +
		"        else if(!value) {\n" +
		"          if(isOver && overGeometry.length > 0) whichChocie_changed = 1;\n" +
		"          else whichChoice_changed = 0;\n" +
		"        }\n" +
		"      }\n" +
		"      function set_isOver(value, time) {\n" +
		"        isOver = value;\n" +
		"        if(value && !isActive && overGeometry.length > 0) whichChoice_changed = 1;\n" +
		"        else if(!value) {\n" +
		"          if(isActive && activeGeometry.length > 0) whichChoice_changed = 2;\n" +
		"          else whichChoice_changed = 0;\n" +
		"        }\n" +
		"      }\n" +
		"      function set_minAngle(value, time) {\n" +
		"        minAngle = value;\n" +
		"      }\n" +
		"      function set_maxAngle(value, time) {\n" +
		"        maxAngle = value;\n" +
		"      }\n" +
		"    \"\n" +
		"  }\n" +
		"  ROUTE Touch-SENSOR.hitPoint_changed TO Rotational-SCRIPT.set_hitPoint\n" +
		"  ROUTE Touch-SENSOR.isActive TO Rotational-SCRIPT.set_touchSensorIsActive\n" +
		"  ROUTE Touch-SENSOR.isOver TO Rotational-SCRIPT.set_touchSensorIsOver\n" +
		"  ROUTE Rotational-SENSOR.translation_changed TO Rotational-SCRIPT.set_translation\n" +
		"  ROUTE Rotational-SENSOR.isActive TO Rotational-SCRIPT.set_planeSensorIsActive\n" +
		"  ROUTE Rotational-SCRIPT.rotation_changed TO Rotational-TRANSFORM.set_rotation\n" +
		"  ROUTE Rotational-SCRIPT.internalOffset_changed TO Rotational-SENSOR.set_offset\n" +
		"  ROUTE Rotational-SCRIPT.whichChoice_changed TO Rotational-SWITCH.set_whichChoice\n" +
		"}\n" +
		"PROTO LinearWidget [ field    SFFloat amplitude  0.0\n" +
		"                     field    SFFloat angle      0.0\n" +
		"                     field    SFFloat phase      0.0\n" +
		"                     field    SFFloat wavelength 1.0\n" +
		"                     field    SFFloat x          0.0\n" +
		"                     field    SFFloat y          0.0\n" +
		"                     exposedField SFBool enabled TRUE\n" +
		"                     eventIn  SFFloat set_amplitude\n" +
		"                     eventIn  SFFloat set_angle\n" +
		"                     eventIn  SFFloat set_phase\n" +
		"                     eventIn  SFFloat set_wavelength\n" +
		"                     eventIn  SFVec3f set_position\n" +
		"                     eventIn  SFBool  set_widgetVisible\n" +
		"                     eventOut SFFloat amplitude_changed\n" +
		"                     eventOut SFFloat angle_changed\n" +
		"                     eventOut SFFloat phase_changed\n" +
		"                     eventOut SFFloat wavelength_changed\n" +
		"                     eventOut SFVec3f position_changed\n" +
		"                     eventOut SFBool  mouseClicked\n" +
		"                     eventOut SFBool  mouseOver\n" +
		"                     eventOut SFBool  mouseOverAmplitude\n" +
		"                     eventOut SFBool  mouseOverWavelength\n" +
		"                     eventOut SFBool  mouseOverPhase\n" +
		"                     eventOut SFBool  mouseOverAngle      ]\n" +
		"{\n" +
		"  DEF PoolWidget-SWITCH Switch {\n" +
		"    whichChoice 0\n" +
		"    choice [\n" +
		"      Transform { children [\n" +
		"        DEF Icon0-SENSOR TouchSensor {\n" +
		"          isOver IS mouseOver\n" +
		"          enabled IS enabled\n" +
		"        }\n" +
		"        DEF Icon0-TRANSFORM3 Transform {\n" +
		"          rotation 1 0 0 -1.57\n" +
		"          children DEF Icon0-TRANSFORM2 Transform {\n" +
		"            children Transform {\n" +
		"              rotation 1 0 0 1.57\n" +
		"              children Transform {\n" +
		"                rotation 0 1 0 -1.57\n" +
		"                children DEF Icon0-TRANSFORM Transform {\n" +
		"                  children DEF Icon-SHAPE Group { children [\n" +
		"                    Shape {\n" +
		"                      appearance DEF PoolWidget-APPEARANCE2 Appearance {\n" +
		"                        material Material {\n" +
		"                          diffuseColor 1.0 0.2 0.2\n" +
		"                        }\n" +
		"                      }\n" +
		"                      geometry Sphere {\n" +
		"                        radius 1.0\n" +
		"                      }\n" +
		"                    }\n" +
		"                    DEF Linear-SHAPE Shape {\n" +
		"                      appearance DEF PoolWidget-APPEARANCE3 Appearance {\n" +
		"                        material Material {\n" +
		"                          diffuseColor 0.2 0.2 0.2\n" +
		"                          transparency 0.25\n" +
		"                        }\n" +
		"                      }\n" +
		"                      geometry Box {\n" +
		"                        size 4.0 1.0 0.04\n" +
		"                      }\n" +
		"                    }\n" +
		"                    Transform {\n" +
		"                      translation 0 0 -0.5\n" +
		"                      children USE Linear-SHAPE\n" +
		"                    }\n" +
		"                    Transform {\n" +
		"                      translation 0 0 -1.0\n" +
		"                      children USE Linear-SHAPE\n" +
		"                    }\n" +
		"                    Transform {\n" +
		"                      translation 0 0 0.5\n" +
		"                      children USE Linear-SHAPE\n" +
		"                    }\n" +
		"                    Transform {\n" +
		"                      translation 0 0 1.0\n" +
		"                      children USE Linear-SHAPE\n" +
		"                    }\n" +
		"                  ] }\n" +
		"                }\n" +
		"              }\n" +
		"            }\n" +
		"          }\n" +
		"        }\n" +
		"      ] }\n" +
		"      Group { children [\n" +
		"        USE Icon0-TRANSFORM3\n" +
		"        Transform { rotation 1 0 0 -1.57 children [\n" +
		"          DEF PoolWidget-TRANSFORM Transform { children [\n" +
		"            DEF Angle-TRANSFORM Transform { children [\n" +
		"              DEF Angle-ROTATIONWIDGET RotationWidget {\n" +
		"                minAngle 0\n" +
		"                maxAngle 6.28318530\n" +
		"                set_offset IS set_angle\n" +
		"                offset_changed IS angle_changed\n" +
		"                offset IS angle\n" +
		"                isOver IS mouseOverAngle\n" +
		"        enabled IS enabled\n" +
		"                normalGeometry [\n" +
		"                  Transform { rotation 1 0 0 1.57 children [\n" +
		"                    Transform {\n" +
		"                      rotation 0 1 0 -1.57\n" +
		"                      children [\n" +
		"                        DEF Arrow-TRANSFORM Transform { children [\n" +
		"                          Transform {\n" +
		"                            translation 20 0 0\n" +
		"                            scale 4 4 4\n" +
		"                            children [\n" +
		"                              DEF Arrow Transform {\n" +
		"                                translation -0.141421 -0.125 0.353553\n" +
		"                                rotation 0 1 0 0.785398\n" +
		"                                children [\n" +
		"                                  Shape {\n" +
		"                                    appearance DEF PoolWidget-APPEARANCE Appearance {\n" +
		"                                      material Material {\n" +
		"                                        diffuseColor 0.4 0.4 0.8\n" +
		"                                      }\n" +
		"                                    }\n" +
		"                                    geometry DEF Arrow-FACES IndexedFaceSet {\n" +
		"                                      ccw TRUE\n" +
		"                                      solid TRUE\n" +
		"                                      coord DEF Arrow-COORD Coordinate { point [\n" +
		"                                        0 0.25 0, 0.5 0.25 0, 0.5 0.25 -0.5, 0 0.25 -0.1, 0.4 0.25 -0.1, \n" +
		"                                        0.4 0.25 -0.5, 0 0.25 0, 0.5 0.25 0, 0.5 0.25 -0.5, \n" +
		"                                        0.4 0.25 -0.5, 0.4 0.25 -0.1, 0 0.25 -0.1, 0 0 0, 0.5 0 0, \n" +
		"                                        0.5 0 -0.5, 0.4 0 -0.5, 0.4 0 -0.1, 0 0 -0.1, 0 0 0, \n" +
		"                                        0.5 0 0, 0.5 0 -0.5, 0 0 -0.1, 0.4 0 -0.1, 0.4 0 -0.5]\n" +
		"                                      }\n" +
		"                                      coordIndex [\n" +
		"                                        3, 0, 1, -1, 3, 1, 4, -1, 4, 1, 2, -1, 4, 2, 5, -1, 6, 12, 13, -1, \n" +
		"                                        6, 13, 7, -1, 7, 13, 14, -1, 7, 14, 8, -1, 8, 14, 15, -1, \n" +
		"                                        8, 15, 9, -1, 9, 15, 16, -1, 9, 16, 10, -1, 10, 16, 17, -1, \n" +
		"                                        10, 17, 11, -1, 11, 17, 12, -1, 11, 12, 6, -1, 18, 21, 22, -1, \n" +
		"                                        18, 22, 19, -1, 19, 22, 23, -1, 19, 23, 20, -1]\n" +
		"                                      }\n" +
		"                                  }\n" +
		"                                ]\n" +
		"                              }\n" +
		"                            ]\n" +
		"                          }\n" +
		"                          Transform {\n" +
		"                            rotation 0 1 0 3.142\n" +
		"                            scale 4 4 4\n" +
		"                            translation -20 0 0\n" +
		"                            children USE Arrow\n" +
		"                          }\n" +
		"                          Transform {\n" +
		"                            rotation 0 0 1 1.57\n" +
		"                            children [\n" +
		"                              Shape {\n" +
		"                                appearance USE PoolWidget-APPEARANCE\n" +
		"                                geometry Cylinder {\n" +
		"                                  radius 0.1\n" +
		"                                  height 41\n" +
		"                                }\n" +
		"                              }\n" +
		"                            ]\n" +
		"                          }\n" +
		"                        ] }\n" +
		"                      ]\n" +
		"                    }\n" +
		"                  ] }\n" +
		"                ]\n" +
		"              }\n" +
		"            ] }\n" +
		"            DEF WidgetGroup-TRANSFORM Transform {\n" +
		"              children [\n" +
		"                Transform { rotation 1 0 0 1.57 children [\n" +
		"                  Transform {\n" +
		"                    rotation 0 0 1 -1.57\n" +
		"                    children [\n" +
		"                      DEF Wavelength-TRANSFORM2 Transform { children\n" +
		"                        Transform {\n" +
		"                          translation 0 0.5 0\n" +
		"                          children Shape {\n" +
		"                            appearance USE PoolWidget-APPEARANCE\n" +
		"                            geometry Cylinder {\n" +
		"                              radius 0.1\n" +
		"                              height 1\n" +
		"                            }\n" +
		"                          }\n" +
		"                        }\n" +
		"                      }\n" +
		"                      Transform { children [\n" +
		"                        TouchSensor {\n" +
		"                          isOver IS mouseOverWavelength\n" +
		"                          enabled IS enabled\n" +
		"                        }\n" +
		"                        DEF Wavelength-SENSOR PlaneSensor {\n" +
		"                          minPosition 0 2.01\n" +
		"                          maxPosition 0 52.0\n" +
		"                          offset      0 2.01 0\n" +
		"                          isActive IS mouseClicked\n" +
		"                          enabled IS enabled\n" +
		"                        }\n" +
		"                        DEF Wavelength-TRANSFORM Transform {\n" +
		"                          children Shape {\n" +
		"                            appearance USE PoolWidget-APPEARANCE\n" +
		"                            geometry Cone {\n" +
		"                              height 2.0\n" +
		"                              bottomRadius 0.8\n" +
		"                            }\n" +
		"                          }\n" +
		"                        }\n" +
		"                      ] }\n" +
		"                    ]\n" +
		"                  }\n" +
		"                ] }\n" +
		"                DEF Phase-TRANSFORM3 Transform { children\n" +
		"                  Transform {\n" +
		"                    rotation 0 0 1 -1.57\n" +
		"                    children [\n" +
		"                      DEF Phase-TRANSFORM2 Transform { children\n" +
		"                        Transform {\n" +
		"                          translation 0 -0.5 0\n" +
		"                          children Shape {\n" +
		"                            appearance USE PoolWidget-APPEARANCE\n" +
		"                            geometry Cylinder {\n" +
		"                              radius 0.1\n" +
		"                              height 1\n" +
		"                            }\n" +
		"                          }\n" +
		"                        }\n" +
		"                      }\n" +
		"                      Transform { children [\n" +
		"                        TouchSensor {\n" +
		"                          isOver IS mouseOverPhase\n" +
		"                          enabled IS enabled\n" +
		"                        }\n" +
		"                        DEF Phase-SENSOR PlaneSensor {\n" +
		"                          minPosition 0 -8.283195\n" +
		"                          maxPosition 0 -2.0\n" +
		"                          offset      0 -2.0 0\n" +
		"                          isActive IS mouseClicked\n" +
		"                          enabled IS enabled\n" +
		"                        }\n" +
		"                        DEF Phase-TRANSFORM Transform { translation 0 -0.5 0 children [\n" +
		"                          Shape {\n" +
		"                            appearance USE PoolWidget-APPEARANCE2\n" +
		"                            geometry Sphere {\n" +
		"                              radius 0.8\n" +
		"                            }\n" +
		"                          }\n" +
		"                        ] }\n" +
		"                      ] }\n" +
		"                    ]\n" +
		"                  }\n" +
		"                }\n" +
		"              ]\n" +
		"            }\n" +
		"            Transform {\n" +
		"              rotation 1 0 0 1.57\n" +
		"              children [\n" +
		"                DEF Amplitude-TRANSFORM2 Transform { children\n" +
		"                  Transform {\n" +
		"                    translation 0 0.5 0\n" +
		"                    children [\n" +
		"                      Shape {\n" +
		"                        appearance USE PoolWidget-APPEARANCE\n" +
		"                        geometry Cylinder {\n" +
		"                          radius 0.1\n" +
		"                          height 1\n" +
		"                        }\n" +
		"                      }\n" +
		"                    ]\n" +
		"                  }\n" +
		"                }\n" +
		"                Transform { children [\n" +
		"                  TouchSensor {\n" +
		"                    isOver IS mouseOverPhase\n" +
		"                    enabled IS enabled\n" +
		"                  }\n" +
		"                  DEF Amplitude-SENSOR PlaneSensor {\n" +
		"                    minPosition 0 2.0\n" +
		"                    maxPosition 0 12.0\n" +
		"                    offset 0 2.0 0\n" +
		"                    isActive IS mouseClicked\n" +
		"                    enabled IS enabled\n" +
		"                  }\n" +
		"                  DEF Amplitude-TRANSFORM Transform {\n" +
		"                    translation 0 3 0\n" +
		"                    children [\n" +
		"                      Shape {\n" +
		"                        appearance USE PoolWidget-APPEARANCE\n" +
		"                        geometry Cone {\n" +
		"                          height 2.0\n" +
		"                          bottomRadius 0.8\n" +
		"                        }\n" +
		"                      }\n" +
		"                    ]\n" +
		"                  }\n" +
		"                ] }\n" +
		"              ]\n" +
		"            }\n" +
		"          ] }\n" +
		"        ] }\n" +
		"      ] }\n" +
		"    ]\n" +
		"  }\n" +
		"  DEF Linear-SCRIPT Script {\n" +
		"    field    SFFloat amplitude  IS amplitude\n" +
		"    field    SFFloat angle      IS angle\n" +
		"    field    SFFloat wavelength IS wavelength\n" +
		"    field    SFFloat phase      IS phase\n" +
		"    field    SFFloat x          IS x\n" +
		"    field    SFFloat y          IS y\n" +
		"    eventIn  SFVec3f    set_translation1\n" +
		"    eventIn  SFVec3f    set_translation2\n" +
		"    eventIn  SFVec3f    set_translation3\n" +
		"    eventIn  SFVec3f    set_translation4\n" +
		"    eventIn  SFRotation set_rotation\n" +
		"    eventIn  SFFloat    set_amplitude  IS set_amplitude\n" +
		"    eventIn  SFFloat    set_angle      IS set_angle\n" +
		"    eventIn  SFFloat    set_phase      IS set_phase\n" +
		"    eventIn  SFFloat    set_wavelength IS set_wavelength\n" +
		"    eventIn  SFVec3f    set_position   IS set_position\n" +
		"    eventIn  SFBool     set_widgetVisible IS set_widgetVisible\n" +
		"    eventIn  SFBool     set_widgetVisibleInternal\n" +
		"    eventOut SFVec3f    scale1_changed\n" +
		"    eventOut SFVec3f    translation1_changed\n" +
		"    eventOut SFRotation rotation_changed\n" +
		"    eventOut SFVec3f    scale2_changed\n" +
		"    eventOut SFVec3f    translation2_changed\n" +
		"    eventOut SFVec3f    scale3_changed\n" +
		"    eventOut SFVec3f    translation3_changed\n" +
		"    eventOut SFVec3f    translation4_changed\n" +
		"    eventOut SFVec3f    offset1_changed\n" +
		"    eventOut SFVec3f    offset2_changed\n" +
		"    eventOut SFVec3f    offset3_changed\n" +
		"    eventOut SFFloat    angle_init\n" +
		"    eventOut SFInt32    whichChoice\n" +
		"    eventOut SFFloat amplitude_changed  IS amplitude_changed\n" +
		"    eventOut SFFloat angle_changed      IS angle_changed\n" +
		"    eventOut SFFloat phase_changed      IS phase_changed\n" +
		"    eventOut SFFloat wavelength_changed IS wavelength_changed\n" +
		"    url \"vrmlscript:\n" +
		"      function initialize() {\n" +
		"        offset1_changed = new SFVec3f(0, amplitude+2.0, 0);\n" +
		"        scale1_changed = new SFVec3f(1, amplitude+2.0, 1);\n" +
		"        translation1_changed = new SFVec3f(0, amplitude+2.0, 0);\n" +
		"        angle_init = angle;\n" +
		"        rotation_changed = new SFRotation(0, 1, 0, angle_init);\n" +
		"        if(wavelength<=0) wavelength = 0.01;\n" +
		"        offset2_changed = new SFVec3f(0, wavelength+2.0, 0);\n" +
		"        scale2_changed = new SFVec3f(1, wavelength+2.0, 1);\n" +
		"        translation2_changed = new SFVec3f(0, wavelength+2.0, 0);\n" +
		"        offset3_changed = new SFVec3f(0, -2.0-phase, 0);\n" +
		"        scale3_changed = new SFVec3f(1, 2.0+phase, 1);\n" +
		"        translation3_changed = new SFVec3f(0, -2.0-phase, 0);\n" +
		"        translation4_changed = new SFVec3f(x, y, 10);\n" +
		"        //position_changed = new SFVec3f(x, y, 0);\n" +
		"      }\n" +
		"      function set_translation1(value, time) {\n" +
		"        scale1_changed[1] = value[1];\n" +
		"        amplitude = value[1]-2.0;\n" +
		"        amplitude_changed = amplitude;\n" +
		"      }\n" +
		"      function set_amplitude(value, time) {\n" +
		"        amplitude = value;\n" +
		"        translation1_changed[1] = value+2.0;\n" +
		"        scale1_changed[1] = value+2.0;\n" +
		"        offset1_changed[1] = value+2.0;\n" +
		"      }\n" +
		"      function set_rotation(value, time) {\n" +
		"        angle = value[3];\n" +
		"        angle_changed = angle;\n" +
		"      }\n" +
		"      function set_angle(value, time) {\n" +
		"        angle = value;\n" +
		"        rotation_changed[3] = value;\n" +
		"        angle_init = value;\n" +
		"      }\n" +
		"      function set_translation2(value, time) {\n" +
		"        scale2_changed[1] = value[1];\n" +
		"        wavelength = value[1]-2.0;\n" +
		"        wavelength_changed = wavelength;\n" +
		"      }\n" +
		"      function set_wavelength(value, time) {\n" +
		"        scale2_changed[1] = value+2.0;\n" +
		"        translation2_changed[1] = value+2.0;\n" +
		"        offset2_changed[1] = value+2.0;\n" +
		"        wavelength = value;\n" +
		"      }\n" +
		"      function set_translation3(value, time) {\n" +
		"        scale3_changed[1] = -value[1];\n" +
		"        phase = -2.0-value[1];\n" +
		"        phase_changed = phase;\n" +
		"      }\n" +
		"      function set_phase(value, time) {\n" +
		"        scale3_changed[1] = value+2.0;\n" +
		"        translation3_changed[1] = -2.0-value;\n" +
		"        offset3_changed[1] = -2.0-value;\n" +
		"        phase = value;\n" +
		"      }\n" +
		"      function set_translation4(value, time) {\n" +
		"        x = value[0];\n" +
		"        y = value[1];\n" +
		"        position_changed[0] = x;\n" +
		"        position_changed[1] = y;\n" +
		"      }\n" +
		"      function set_position(value, time) {\n" +
		"        x = value[0];\n" +
		"        y = value[1];\n" +
		"        translation4_changed[0] = x;\n" +
		"        translation4_changed[1] = y;\n" +
		"      }\n" +
		"      function set_widgetVisible(value, time) {\n" +
		"        if(value) whichChoice = 1;\n" +
		"        else whichChoice = 0;\n" +
		"      }\n" +
		"      function set_widgetVisibleInternal(value, time) {\n" +
		"        if(value) whichChoice = 1;\n" +
		"      }\n" +
		"    \"\n" +
		"  }\n" +
		"  ROUTE Icon0-SENSOR.isOver TO Linear-SCRIPT.set_widgetVisibleInternal\n" +
		"  ROUTE Angle-ROTATIONWIDGET.rotation_changed TO Icon0-TRANSFORM2.rotation\n" +
		"  ROUTE Angle-ROTATIONWIDGET.rotation_changed TO WidgetGroup-TRANSFORM.rotation\n" +
		"  ROUTE Amplitude-SENSOR.translation_changed TO Amplitude-TRANSFORM.translation\n" +
		"  ROUTE Amplitude-SENSOR.translation_changed TO Linear-SCRIPT.set_translation1\n" +
		"  ROUTE Wavelength-SENSOR.translation_changed TO Wavelength-TRANSFORM.set_translation\n" +
		"  ROUTE Wavelength-SENSOR.translation_changed TO Linear-SCRIPT.set_translation2\n" +
		"  ROUTE Phase-SENSOR.translation_changed TO Phase-TRANSFORM.set_translation\n" +
		"  ROUTE Phase-SENSOR.translation_changed TO Linear-SCRIPT.set_translation3\n" +
		"  ROUTE Linear-SCRIPT.rotation_changed TO Icon0-TRANSFORM2.rotation\n" +
		"  ROUTE Linear-SCRIPT.rotation_changed TO WidgetGroup-TRANSFORM.rotation\n" +
		"  ROUTE Linear-SCRIPT.offset1_changed TO Amplitude-SENSOR.offset\n" +
		"  ROUTE Linear-SCRIPT.scale1_changed TO Amplitude-TRANSFORM2.scale\n" +
		"  ROUTE Linear-SCRIPT.translation1_changed TO Amplitude-TRANSFORM.translation\n" +
		"  ROUTE Linear-SCRIPT.offset2_changed TO Wavelength-SENSOR.offset\n" +
		"  ROUTE Linear-SCRIPT.scale2_changed TO Wavelength-TRANSFORM2.scale\n" +
		"  ROUTE Linear-SCRIPT.translation2_changed TO Wavelength-TRANSFORM.translation\n" +
		"  ROUTE Linear-SCRIPT.offset3_changed TO Phase-SENSOR.offset\n" +
		"  ROUTE Linear-SCRIPT.scale3_changed TO Phase-TRANSFORM2.scale\n" +
		"  ROUTE Linear-SCRIPT.translation3_changed TO Phase-TRANSFORM.translation\n" +
		"  ROUTE Linear-SCRIPT.translation4_changed TO Icon0-TRANSFORM2.set_translation\n" +
		"  ROUTE Linear-SCRIPT.translation4_changed TO PoolWidget-TRANSFORM.set_translation\n" +
		"  ROUTE Linear-SCRIPT.whichChoice TO PoolWidget-SWITCH.whichChoice\n" +
		"}\n";
}
