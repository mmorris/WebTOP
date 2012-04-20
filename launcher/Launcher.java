/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//  Launcher.java
//  Defines the code for the Desktop Launcher
//
//  Created by Shane Fry
//  Updated September 2, 2008

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.Runnable;
import java.util.LinkedHashMap;

public class Launcher implements ActionListener{

	JFrame frame;
	JFrame consoleFrame;
	JScrollPane allPanel;
	JPanel wavesPanel;
	JPanel fraunPanel;
	JPanel fresnelPanel;
	JPanel interPanel;
	JPanel photonPanel;
	JPanel polarPanel;
	JPanel geoPanel;
	
	PrintStream out; // replacement for System.out
	
	// This class will redirect all stdout and stderr output
	// to a JTextArea
	// This is needed so that we can get the output from the Modules
	// and redirect that output to the user, and should there be issues
	// to us.  This is unnecessary when the launcher is run from the console
	// because then System.out.println works properly, but when the launcher
	// is run from a file-explorer or shortcut the console isn't there
	// so System.out.println doesn't work.
	public class TextAreaOutputHandler extends OutputStream{
		private JTextArea text;
		public TextAreaOutputHandler(JTextArea t){
			text = t;
		}
		
		public void write(int b) throws IOException{
			text.append( String.valueOf((char)b));
		}
	}
	
	// Class needed so we can print exceptions and output from the
	// executed module.
	public class StreamHandler implements Runnable{
		InputStream stream;
		
		public void run(){
			try{
				InputStreamReader isr = new InputStreamReader(stream);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ( (line = br.readLine()) != null)
					System.out.println(line);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
		
		public StreamHandler(InputStream s){
			stream = s;
		}
	}
	
	public class Module
	{
		String moduleName;
		String picName;
		String className;
		boolean enabled;
		
		public Module(String modName, String pic, String classN, boolean en)
		{
			moduleName = modName;
			picName = pic;
			enabled = en;
			className = classN;
		}
	}
	
	LinkedHashMap modules;
	 
	public void setupModules()
	{
		String imageDir = new String("./images/"); // image directory
		
		modules = new LinkedHashMap();
		
		// Setup modules and add them to the Hashtable
		// Keep these in alphabetical order
		Module temp;
		
		
		temp = new Module("Wavefronts",imageDir + "wavefront.jpg", "WaveFront", true);
		modules.put(temp.moduleName,temp);
		
		temp = new Module("Waves Two Media",imageDir + "twomedia.jpg", "TwoMedia", true);
		modules.put(temp.moduleName,temp);
		
		temp = new Module("Waves Three Media",imageDir + "threemedia.jpg", "ThreeMedia", true);
		modules.put(temp.moduleName,temp);
		
		temp = new Module("Fraunhofer N-Slit",imageDir + "nslit.jpg", "NSlit", true);
		modules.put(temp.moduleName,temp);
		
		temp = new Module("Rayleigh Resolution",imageDir + "rayleigh.jpg", "Rayleigh", true);
		modules.put(temp.moduleName,temp);

		temp = new Module("Diffraction Grating",imageDir + "grating.jpg", "Grating", true);
		modules.put(temp.moduleName,temp);
		
		temp = new Module("Rectangular Aperture",imageDir + "rectangularslit.jpg", "RectangularSlit", true);
		modules.put(temp.moduleName,temp);
		
		temp = new Module("Fresnel Single Slit",imageDir + "singleslit.jpg", "SingleSlit", true);
		modules.put(temp.moduleName,temp);
		
		temp = new Module("Fresnel Circular",imageDir + "circular.jpg", "Circular", true);
		modules.put(temp.moduleName,temp);
		
		temp = new Module("Michelson Interferometer",imageDir + "michelson.jpg", "Michelson", true);
		modules.put(temp.moduleName,temp);
		
		temp = new Module("Fabry-Perot Etalon",imageDir + "fabryperot.jpg", "FabryPerot", true);
		modules.put(temp.moduleName,temp);
		
		temp = new Module("Photoelectric Effect",imageDir + "photoelectric.jpg", "Photoelectric", true);
		modules.put(temp.moduleName,temp);
		
		temp = new Module("Lasers",imageDir + "laser.jpg", "Laser", true);
		modules.put(temp.moduleName,temp);
		
		temp = new Module("Two Slit Photon",imageDir + "twoslitphoton.jpg", "TwoSlitPhoton", false);
		modules.put(temp.moduleName,temp);
		
		temp = new Module("Waves",imageDir + "waves.jpg", "Waves", false);
		modules.put(temp.moduleName,temp);
		
		temp = new Module("Polarization",imageDir + "polarization.jpg", "Polarization", false);
		modules.put(temp.moduleName,temp);
		
/*		these modules aren't done, so dr. foley doesn't want to show them -SF

		temp = new Module("Geometrical",imageDir + "geometrical.jpg", "Geometrical", false);
		modules.put(temp.moduleName,temp);
		temp = new Module("Scattering",imageDir + "scattering.jpg", "Scattering", false);
		modules.put(temp.moduleName,temp);
		
*/		
		
		
		
		
		
		
		
	}
	
	public void createAllPanel()
	{
		/*String[] modules = 
		{
			"Circular",
			"FabryPerot",
			"Geometrical",
			"Grating",
			"Laser",
			"Michelson",
			"NSlit",
			"Photoelectric",
			"Polarization",
			"Rayleigh",
			"RectangularSlit",
			"SingleSlit",
			"TwoMedia",
			"ThreeMedia",
			"WaveFront",
			"Waves",
			"TwoSlitPhoton",
			"Scattering"
		};*/
		
		//Arrays.sort(modules);
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(modules.size()/3+1, 3));
		
		Iterator iter = modules.entrySet().iterator();
		
		while(iter.hasNext())
		{
			Map.Entry entry = (Map.Entry) iter.next();
			
			//String modName = (String)entry.getKey();
			Module module = (Module)entry.getValue();
			
			JButton button = new JButton(module.moduleName, new ImageIcon(module.picName));
			button.addActionListener(this);
			button.setHorizontalAlignment(SwingConstants.LEFT);
			//button.setHorizontalTextPosition(SwingConstants.LEFT);
			//button.setContentAreaFilled(false);
			//button.setBorderPainted(false);
			button.setEnabled(module.enabled);
			panel.add(button);
		}
		
		allPanel = new JScrollPane(panel);
	}
	
	public void createWavesPanel()
	{
		wavesPanel = new JPanel();
		wavesPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.weighty = 0;	// if more than one row, on the last row set this to a 1
		
		Module module;
		
		module = (Module)modules.get("Waves");
		JButton button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		button.setEnabled(module.enabled);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		wavesPanel.add(button, constraints);
		
		module = (Module)modules.get("Waves Two Media");
		button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		button.setEnabled(module.enabled);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		constraints.gridx++;
		wavesPanel.add(button, constraints);
		
		module = (Module)modules.get("Waves Three Media");
		button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		button.setEnabled(module.enabled);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		constraints.gridx++;
		wavesPanel.add(button, constraints);
		
		module = (Module)modules.get("Wavefronts");
		button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		button.setEnabled(module.enabled);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.weighty = 1;
		wavesPanel.add(button, constraints);
	}
	
	public void createFraunPanel()
	{
		fraunPanel = new JPanel();
		fraunPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.weighty = 0;	// if more than one row, on the last row set this to a 1
		
		
		Module module = (Module)modules.get("Diffraction Grating");
		JButton button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.setEnabled(module.enabled);
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		fraunPanel.add(button, constraints);
		
		
		module = (Module)modules.get("Fraunhofer N-Slit");
		button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.setEnabled(module.enabled);
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		constraints.gridx++;
		fraunPanel.add(button, constraints);
		
		module = (Module)modules.get("Rayleigh Resolution");
		button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.setEnabled(module.enabled);
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		constraints.gridx++;
		fraunPanel.add(button, constraints);
		
		module = (Module)modules.get("Rectangular Aperture");
		button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.setEnabled(module.enabled);
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.weighty = 1;
		fraunPanel.add(button, constraints);
	}
	
	public void createFresnelPanel()
	{
		fresnelPanel = new JPanel();
		fresnelPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.weighty = 1;	// if more than one row, on the last row set this to a 1
		
		
		Module module = (Module)modules.get("Fresnel Circular");
		JButton button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.setEnabled(module.enabled);
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		fresnelPanel.add(button, constraints);
		
		
		module = (Module)modules.get("Fresnel Single Slit");
		button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.setEnabled(module.enabled);
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		constraints.gridx++;
		fresnelPanel.add(button, constraints);
		
	}
	
	public void createInterPanel()
	{
		interPanel = new JPanel();
		interPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.weighty = 1;	// if more than one row, on the last row set this to a 1
		
		
		Module module = (Module)modules.get("Fabry-Perot Etalon");
		JButton button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.setEnabled(module.enabled);
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		interPanel.add(button, constraints);
		
		
		module = (Module)modules.get("Michelson Interferometer");
		button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.setEnabled(module.enabled);
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		constraints.gridx++;
		interPanel.add(button, constraints);
	}
	
	public void createPhotonPanel()
	{
		photonPanel = new JPanel();
		photonPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.weighty = 1;	// if more than one row, on the last row set this to a 1
		
		
		Module module = (Module)modules.get("Lasers");
		JButton button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.setEnabled(module.enabled);
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		photonPanel.add(button, constraints);
		
		
		module = (Module)modules.get("Photoelectric Effect");
		button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.setEnabled(module.enabled);
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		constraints.gridx++;
		photonPanel.add(button, constraints);
		
		module = (Module)modules.get("Two Slit Photon");
		button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.setEnabled(module.enabled);
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		constraints.gridx++;
		photonPanel.add(button, constraints);
	}
	
	public void createPolarPanel()
	{
		polarPanel = new JPanel();
		polarPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.weighty = 1;	// if more than one row, on the last row set this to a 1
		
		
		Module module = (Module)modules.get("Polarization");
		JButton button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.setEnabled(module.enabled);
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		polarPanel.add(button, constraints);
		
		
		module = (Module)modules.get("Scattering");
		button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.setEnabled(module.enabled);
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		constraints.gridx++;
		polarPanel.add(button, constraints);
	}
	
	public void createGeoPanel()
	{
		geoPanel = new JPanel();
		geoPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.weighty = 1;	// if more than one row, on the last row set this to a 1
		
		
		Module module = (Module)modules.get("Geometrical");
		JButton button = new JButton(module.moduleName, new ImageIcon(module.picName));
		button.setEnabled(module.enabled);
		button.addActionListener(this);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setVerticalAlignment(SwingConstants.TOP);
		//button.setHorizontalTextPosition(SwingConstants.LEFT);
		//button.setContentAreaFilled(false);
		//button.setBorderPainted(false);
		geoPanel.add(button, constraints);
	}
	
	
	public Launcher()
	{		
		frame = new JFrame();
		//BoxLayout grid = new BoxLayout(this, BoxLayout.LINE_AXIS);
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800,600);
		frame.setResizable(false);
		frame.setTitle("The Optics Project on the Web (WebTop)");
		//setLayout(grid);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		setupModules();
		
		createAllPanel();
		createWavesPanel();
		createFraunPanel();
		createFresnelPanel();
		createInterPanel();
		createPhotonPanel();
		
		
		// -SF These modules don't exist, so we comment this out
		//createPolarPanel();
		//createGeoPanel();
		
		
		tabbedPane.addTab("All", allPanel);
		tabbedPane.addTab("Waves", wavesPanel);
		tabbedPane.addTab("Fraunhofer Diffraction", fraunPanel);
		tabbedPane.addTab("Fresnel Diffraction", fresnelPanel);
		tabbedPane.addTab("Interference", interPanel);
		tabbedPane.addTab("Photons", photonPanel);
		tabbedPane.addTab("Polarization", polarPanel);
		tabbedPane.setEnabledAt(6, false);
		//tabbedPane.addTab("Geometrical", geoPanel);
		
		frame.add(tabbedPane);
				
		// setup our console output text area
		consoleFrame = new JFrame();
		consoleFrame.setTitle("WebTop Console");
		//JPanel consolePanel = new JPanel();
		//consolePanel.setLayout(new FlowLayout());
		
		JTextArea consoleText = new JTextArea(20,40);
		consoleText.setEditable(false);
		//consoleText.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(consoleText);
		scrollPane.createHorizontalScrollBar();
		scrollPane.createVerticalScrollBar();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		out = new PrintStream( new TextAreaOutputHandler(consoleText),true);
		
		//consolePanel.add(scrollPane);
		consoleFrame.add(scrollPane);
		//consoleFrame.setSize(400,400);
		consoleFrame.setSize(500, 400);
		//consoleFrame.setSize(scrollPane.getSize());
		consoleFrame.setVisible(true);
		
		System.setErr(out);
		System.setOut(out);
		
		frame.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e)
	{	
		String classPath = new String();
		String classesDir = new String();
		String libnativeDir = new String();
		String jarsDir = new String();
		String libsDir = new String();
		
		char classPathSeparator = ';'; // ; is for windows
		
		if(!System.getProperty("os.name").contains("Windows"))
			classPathSeparator = ':'; // : for linux and Mac
		
		String buttonPressed = e.getActionCommand();
		Module module = (Module)modules.get(buttonPressed);
		//module = module.toLowerCase();
		
		File f = new File("." + "/");
		try {
			f = f.getCanonicalFile();
			String path = f.getCanonicalPath();
			String[] pathParts = path.split(" ");
			
			classPath = pathParts[0];
			
			if(pathParts.length > 1)
			{
				for(int i = 1; i < pathParts.length; i++)
				{
					classPath += "\\" + " " + pathParts[i];
				}
			}
			
			libnativeDir = "./libnative";
			classesDir = "./classes";
			jarsDir = "./jars";
			libsDir = "./lib";
			System.out.println("jarsDir: " + jarsDir);
		}
		catch(java.io.IOException except)
		{
			System.out.println(except);
			System.exit(0);
		}
		
		String command ="java -Xmx768m -Dsun.java2d.noddraw=true -Djava.library.path=" + libnativeDir;
		command += " -classpath " + classesDir;
		File file = new File(jarsDir);
		String[] fileList = file.list();
		
		for(int i = 0; i < fileList.length; i++)
		{
			if(fileList[i].endsWith(".jar"))
			{
				command += classPathSeparator;
				command += jarsDir+"/"+fileList[i];	
			}
		}
		
		
		file = new File(libsDir);
		fileList = file.list();
		for(int i = 0; i < fileList.length; i++)
		{
			if(fileList[i].endsWith(".jar"))
			{
				command += classPathSeparator;
				command += libsDir+"/"+fileList[i];	
			}
		}	
		command += " org.webtop.module.";
		command += module.className.toLowerCase();
		command += "." + module.className;
		System.out.println(command);
		
		try
		{
			frame.setVisible(false);
			consoleFrame.setVisible(true);
			System.out.println("------------------------");
			System.out.println("Running Module: " + module.className);
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(command);
			
			// Start the threads that will listen for 
			// exceptions and console output
			new Thread(new StreamHandler(proc.getErrorStream())).start();
			new Thread(new StreamHandler(proc.getInputStream())).start();
			
			proc.waitFor();
			System.out.println("Module ended");
			System.out.println("------------------------");
			frame.setVisible(true);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	

	public static void main(String args[])
	{
		Launcher launcher = new Launcher();
	}
}
