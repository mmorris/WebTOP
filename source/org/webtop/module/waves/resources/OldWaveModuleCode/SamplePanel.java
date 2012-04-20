//SamplePanel.java
//The output panel class for the sampling stick.
//Davis Herring
//Created December 21 2002
//Updated May 2 2003
//Version 1.01

package webtop.wave;

import java.awt.*;

import webtop.wsl.client.WSLPlayer;

import sdl.math.FPRound;
import sdl.gui.numberbox.*;

public class SamplePanel extends WidgetPanel implements NumberBox.Listener
{
	private final Label value;
	private final FloatBox x=makePositionBox(),y=makePositionBox();
	private SamplingStick stick;

	public SamplePanel(WSLPlayer player) {
		super(player);

		//Use default FlowLayout
		add(new Label("x:",Label.RIGHT));
		add(x);
		x.addNumberListener(this);
		add(new Label("y:",Label.RIGHT));
		add(y);
		y.addNumberListener(this);
		add(new Label("value:",Label.RIGHT));
		add(value=new Label("-0.000e-4"));
	}

	public void show(SamplingStick s) {
		stick=s;
		if(s==null) {
			x.setEnabled(false);
			y.setEnabled(false);
			value.setText(null);
		} else {
			x.setEnabled(true);
			y.setEnabled(true);
			setXY(s.getX(),s.getY());
			s.query();
		}
	}

	public void setX(float X) {x.silence(); x.setSigValue(X,SIG_DIGITS);}
	public void setY(float Y) {y.silence(); y.setSigValue(Y,SIG_DIGITS);}

	public void setXY(float X,float Y) {setX(X);setY(Y);}

	public void setValue(float val) {
		value.setText(String.valueOf(FPRound.toSigVal(val,SIG_DIGITS)));
	}

	public void numChanged(NumberBox source, Number newVal) {
		float f=newVal.floatValue();
		String param;
		if(source==x) {
			stick.setX(f,true);
			param="x";
		} else if(source==y) {
			stick.setY(f,true);
			param="y";
		} else {
			System.err.println("SamplePanel: unexpected numChanged from "+source);
			return;
		}
		wslPlayer.recordActionPerformed(stick.getID(),param,String.valueOf(f));
	}

	public void boundsForcedChange(NumberBox source, Number oldVal) {}	//bounds are never changed

	//There should eventually be input error notification, but at the moment there's no convenient mechanism for that.
	public void invalidEntry(NumberBox source, Number badVal) {}
}
