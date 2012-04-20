package webtop.wave;

import java.awt.*;
import java.awt.event.*;

import webtop.util.WTMath;
import sdl.math.FPRound;
import webtop.wsl.client.*;
import sdl.gui.numberbox.*;

// This panel is used for both Plucked and Struck sources and so needs to be
// renamed
public class PluckedPanel extends WidgetPanel implements NumberBox.Listener{
	private FloatBox ampField,widthField,xField,yField;

	private SingleWaveSource source;

	public PluckedPanel(WSLPlayer player) {
		super(player);

		add(new Label("Amplitude:",Label.RIGHT));
		ampField = new FloatBox(-20,100,10,3);
		ampField.addNumberListener(this);
		ampField.setEnabled(true);
		add(ampField);

		add(new Label("Width:",Label.RIGHT));
		widthField= new FloatBox(0.1f,500,5,3);
		widthField.addNumberListener(this);
		ampField.setEnabled(true);
		add(widthField);

		add(new Label("X0:",Label.RIGHT));
		xField = new FloatBox(-50,50,0,3);
		xField.addNumberListener(this);
		xField.setEnabled(true);
		add(xField);

		add(new Label("Y0:",Label.RIGHT));
		yField = new FloatBox(-50,50,0,3);
		yField.addNumberListener(this);
		yField.setEnabled(true);
		add(yField);
	}

	public void show(PluckedSource ws) {
		source = ws;
		if(ws==null) {
			ampField.setEnabled(false);
			widthField.setEnabled(false);
			xField.setEnabled(false);
			yField.setEnabled(false);
		} else {
			ampField.setEnabled(true);
			widthField.setEnabled(true);
			xField.setEnabled(true);
			yField.setEnabled(true);
			setAmplitude(ws.getAmplitude());
			setWidth(ws.getWidth());
			setX(ws.getX());
			setY(ws.getY());
		}
	}

	public void show(StruckSource ss) {
		source = ss;
		if(ss==null) {
			ampField.setEnabled(false);
			widthField.setEnabled(false);
			xField.setEnabled(false);
			yField.setEnabled(false);
		} else {
			ampField.setEnabled(true);
			widthField.setEnabled(true);
			xField.setEnabled(true);
			yField.setEnabled(true);
			setAmplitude(ss.getAmplitude());
			setWidth(ss.getWidth());
			setX(ss.getX());
			setY(ss.getY());
			//setAmplitude(ws.getAmplitude());
			//setWavelength(ws.getWavelength());
			//setPhase(ws.getPhase());
		}
	}


	public void setAmplitude(float a) {
		System.out.println("PluckedPanel::setAmplitude");
		ampField.silence();
		ampField.setValue(FPRound.toSigVal(a,5));
	}

	public void setWidth(float w) {
		widthField.silence();
		widthField.setValue(FPRound.toSigVal(w,5));
	}

	public void setX(float x) {
		xField.silence();
		xField.setValue(FPRound.toSigVal(x,5));
	}

	public void setY(float y) {
		yField.silence();
		yField.setValue(FPRound.toSigVal(y,5));
	}

	public void setXY(float X,float Y) {setX(X);setY(Y);}

	public void numChanged(NumberBox eventSource, Number newVal){
		if(wslPlayer.isPlaying()) return;

		float f = newVal.floatValue();

		if(eventSource==ampField) {
			source.setAmplitude(f,true);
			if(wslPlayer!=null)
				wslPlayer.recordActionPerformed(source.getID(), "amplitude", String.valueOf(source.getAmplitude()));
		} else if(eventSource==widthField) {
			source.setWidth(f,true);
			if(wslPlayer!=null)
				wslPlayer.recordActionPerformed(source.getID(), "width",
																				String.valueOf(source.getWidth()));
		} else if(eventSource==xField) {
			source.setX(f,true);
			if(wslPlayer!=null)
				wslPlayer.recordActionPerformed(source.getID(), "X0",
																				String.valueOf(WTMath.toDegs(source.getX())));
		} else if(eventSource==yField) {
			source.setY(f,true);
			if(wslPlayer!=null)
				wslPlayer.recordActionPerformed(source.getID(), "Y0",
																				String.valueOf(WTMath.toDegs(source.getY())));
		}

		if(!engine.isPlaying()) engine.update();
	}
	public void invalidEntry(NumberBox src, Number badVal) {
		/*		if(src == amplitude)
			engine.getStatusBar().setText("The amplitude must be between 0 and 100.");
		else if(src == wavelength)
			engine.getStatusBar().setText("The wavelength must be between 0 and 100.");
		else if(src == phase)
			engine.getStatusBar().setText("The phase must be between 0 and 180.");
		*/
		engine.getStatusBar().setText("Invalid Entry");
	}

	public void boundsForcedChange(NumberBox src, Number oldVal){}
}
