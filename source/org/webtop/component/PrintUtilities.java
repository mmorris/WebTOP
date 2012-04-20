/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.component;

import java.awt.*;
import javax.swing.*;
import java.awt.print.*;
import java.awt.image.BufferedImage;
import java.awt.image.*;

public class PrintUtilities implements Pageable, Printable {
	private Component componentToBePrinted;
	PageFormat format;
	
	public static void printComponent(Component c) {
	    new PrintUtilities(c).print();
	}
		  
	public PrintUtilities(Component componentToBePrinted) {
		this.componentToBePrinted = componentToBePrinted;
	}
	
	public int getNumberOfPages() {
		return 1;
	}

	public PageFormat getPageFormat(int arg0) throws IndexOutOfBoundsException {
		return format;
	}

	public Printable getPrintable(int arg0) throws IndexOutOfBoundsException {
		return this;
	}




  
  public void print() {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    //format = printJob.pageDialog(printJob.defaultPage());
    format = new PageFormat();
    System.out.println("START HERE");
    printJob.setPrintable(this);
    System.out.println("END HERE");
    try {
    if (printJob.printDialog()) {
      try {
    	 
        printJob.print();
      } catch(PrinterException pe) {
        System.out.println("Error printing!!!!!!!!!!: " + pe);
      }}
      else { System.out.println("error!!!!!!");}
    }catch(Exception e) {System.out.println("PRINT EXCEPTION");}
    System.out.println("Done printing");
  }

  public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		/*int xo = (int) pageFormat.getImageableX ();
		int yo = (int) pageFormat.getImageableY ();

		if (pageIndex >= 1) {
		    return Printable.NO_SUCH_PAGE;
		}
		System.out.println("Trying to print...");
		g.setFont(new Font("Helvetica-Bold", Font.PLAIN, 10));
		g.drawString("Hello, World!", xo+20, yo+10);
		return Printable.PAGE_EXISTS;*/
	  
	  
	  
	  int height = componentToBePrinted.getHeight();
	  int width  = componentToBePrinted.getWidth();
	  
	  //Dimension dim = printJob.
	  int printHeight = (int)pageFormat.getHeight()-2*72;
	  int printWidth  = (int)pageFormat.getWidth()-2*72;
	  //pageFormat.
	  
	  System.out.println("height: "+pageFormat.getImageableY());
	  
	  BufferedImage test = new BufferedImage(width, height,BufferedImage.TYPE_INT_ARGB);
	  Graphics2D g3d = test.createGraphics();
	  
	  System.out.println("HERE!!!!!! Page index: "+pageIndex);
	  if (pageIndex > 0) {
      return(NO_SUCH_PAGE);
    } else {
      Graphics2D g2d = (Graphics2D)g;
      
      g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
      disableDoubleBuffering(componentToBePrinted);
      componentToBePrinted.paint(g3d);
      enableDoubleBuffering(componentToBePrinted);
      
      g2d.drawImage(test, 0,0, printWidth, printHeight,null);
      
      
      return(PAGE_EXISTS);
    }
  }

  public static void disableDoubleBuffering(Component c) {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(false);
  }

  public static void enableDoubleBuffering(Component c) {
    RepaintManager currentManager = RepaintManager.currentManager(c);
    currentManager.setDoubleBufferingEnabled(true);
  }
}
