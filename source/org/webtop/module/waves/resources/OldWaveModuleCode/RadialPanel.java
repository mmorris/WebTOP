package webtop.wave;

import java.awt.*;
import java.awt.event.*;

import webtop.wsl.client.*;
import webtop.wsl.event.*;
import sdl.gui.numberbox.*;

public class RadialPanel extends SourcePanel implements NumberBox.Listener {
	private FloatBox x;
	private FloatBox y;

	private RadialSource rSource;

	public RadialPanel(WSLPlayer player) {
		super(player);

		add(new Label("X:", Label.RIGHT));
		x = makePositionBox();
		x.addNumberListener(this);
		add(x);

		add(new Label("Y:", Label.RIGHT));
		y = makePositionBox();
		y.addNumberListener(this);
		add(y);
	}

	public void show(RadialSource s) {
		super.show(s);
		if(s!=null) {
			setXY(s.getX(),s.getY());
			x.setEnabled(true);
			y.setEnabled(true);
		} else {
			x.setEnabled(false);
			y.setEnabled(false);
		}
		rSource=s;
	}

	public void setX(float X) {x.silence(); x.setSigValue(X,SIG_DIGITS);}
	public void setY(float Y) {y.silence(); y.setSigValue(Y,SIG_DIGITS);}
	public void setXY(float X,float Y) {setX(X);setY(Y);}

	public void numChanged(NumberBox eventSource, Number newVal){
		if(wslPlayer.isPlaying()) return;

		//Object eventSource = e.getSource();
		if(eventSource==x) {
			rSource.setX(new Float(x.getText()).floatValue(), true);
			if(wslPlayer!=null)
				wslPlayer.recordActionPerformed(rSource.getID(), "x", x.getText());
		} else if(eventSource==y) {
			rSource.setY(new Float(y.getText()).floatValue(), true);
			if(wslPlayer!=null)
				wslPlayer.recordActionPerformed(rSource.getID(), "y", y.getText());
		}

		//super.actionPerformed(new ActionEvent(eventSource,0,""));
		//super.changedInterface(eventSource);
		super.numChanged(eventSource, newVal);
	}
}
