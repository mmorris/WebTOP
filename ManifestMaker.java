/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

/**
 * This stand-alone program takes a module name as an argument, or "all"
 * to make all modules' manifests.
 *
 *
 * THIS PROGRAM IS NOT YET IN WORKING ORDER!!!!!!!!!!!!!
 * The comments are accurate (I think), but the argument parsing seems out of whack.
 * I recommend fixing this to save time making manifests.
 * Or come up with another method.     
 *
 *    
 * @author Paul Cleveland
 *
 */

import java.io.*;

public class ManifestMaker {
	
	public static String DEF_DIR_WTCORE = "jars";
	public static String DEF_DIR_SDL = "jars";
	public static String DEF_DIR_XJ3D = "lib";
	
	public static String DEF_DIR_MANIFEST = "manifests";
	public static String DEF_MANIFEST_SUFFIX = "_MANIFEST.MF";
	
	public static String DEF_MODULE_PACKAGE = "org.webtop.module";
	
	public static String CONFIG_FILE = "manifestmaker.conf";
	
	public static String[] DEF_MODULE_LIST = {};
	
	//I got lazy and copied this from below.
	static final String HELP_TEXT = 
	 "Usage: # java ManifestMaker [-m MODULE_NAME] [-d MANIF_DIR] [-p PACKAGE.NAME] libdir1 libdir2 ..." +
	 "OPTIONS:\n" +
	 "  -d : Specify the directory relative to execution where the manifest file\n" +  
	 "  	 is to be stored.  MANIF_DIR must contain no spaces.  If omitted this\n" +
	 "  	 option defaults to 'manifests'.  Remember that whatever you put after '-d'" +
	 "       WILL be considered the directory to store the manifest in and WILL NO be\n" + 
	 "  	 considered for anything else.\n" +
	 "\n" +  
	 " 	-m : Specifiy a module by giving it's MODULE_NAME.  A manifest for that\n" + 
	 " 		 module will be created with the name MODULE_NAME_MANIFEST.MF in the\n" + 
	 " 		 directory MANIF_DIR.  MODULE_NAME must contain no spaces.\n" +
	 "\n" +       
	 "       If this option is omitted, manifest files will be made with the\n" + 
	 "       described naming convention for all the names listed in manifestmaker.conf.\n" +
	 "\n" +       
	 "  -p : Specify the package that the module class belongs to, or default to org.webtop.module" +
	 "\n" +
	 "  You must include at least one directory for libraries (libdir).  There are\n" + 
	 "  no defaults for these options.\n" +  
	 "\n" +           
	 " Example:  #java ManifestMaker -m Geometrical -d manifests libs jars\n" +
	 "     This would run ManifestMaker to create the manifest GEOMETRICAL_MANIFEST.MF,\n" + 
	 "     including all the jar files in subdirectories 'libs' and 'jars' and store\n" + 
	 "     the created manifest in subdirectory 'manifests'.\n" +
	 "WARNING:  THIS PROGRAM IS NOT VERY FAULT-TOLERANT";

	/**
	 * See the HELP_TEXT member for a usage description, or simply run the program with
	 * no arguments.
	 */
	public static void main(String[] args) {
		
		if(args.length < 2) {
			System.err.println(HELP_TEXT);
			System.exit(-1);
		}
		String moduleName = null;
		int moduleNameIndex = 0;
		
		String manifestDir = DEF_DIR_MANIFEST;
		int manifestDirIndex = 0;
		String manifestName = null;
		
		String modulePackage = DEF_MODULE_PACKAGE;
		int modulePackageIndex = 0;
		
		int totalOptions = 0;
		
		//Get option-value pairs.
		for(int i=0; i<args.length && i<7; i++) {
			//Module name given?
			if(args[i]=="-m") {
				i++;
				moduleName = args[i];
				moduleNameIndex = i;
				i++;
				totalOptions++;
			}
			else if(args[i]=="-d") {
				i++;
				manifestDir = args[i];
				manifestDirIndex = i;
				i++;
				totalOptions++;
			}
			else if(args[i]=="-p") {
				i++;
				modulePackage = args[i];
				modulePackageIndex = i;
				i++;
				totalOptions++;
			}
		}
		
		System.out.println("module = " + moduleName + ", dir = " + manifestDir + ", package = " + modulePackage);
		
		//Now we figure out where to start parsing library directories
		int firstLibIndex = totalOptions*2 + 1;  //*2 for -option <option_value>, +1 for next argument
		System.out.println("first lib at index " + firstLibIndex);
		
		//Get a list of all the libdirs
		String[] libDirs = new String[args.length - firstLibIndex];
		for(int i=firstLibIndex, j=0; i<args.length; i++)
			libDirs[j] = args[i];
		
		for(int i=0; i<libDirs.length; i++)
			System.out.println("libDirs[i] = " + libDirs[i]);
		
		
		//Once you have the names and directories, get a list of the jars.
		String[][] jars = new String[libDirs.length][];
		for(int i=0; i<jars.length; i++)
			jars[i] = getJars(libDirs[i]);
		
		//Note that by this time, 'moduleName' should either contain a name or be null.
		if(moduleName==null) {
			//Not so sure if this will work.  I designed the makeManifest() method as the 'else' case below.
			//Thorough testing suggested.
			for(int i=0; i<DEF_MODULE_LIST.length; i++)
				makeManifest(DEF_MODULE_LIST[i], modulePackage, manifestDir, jars);
		}
		//If module name specified, make that module's manifest
		else {			
			makeManifest(moduleName, modulePackage, manifestDir, jars);
		} // end else (moduleName specified)
	} //end main()
	
	/**
	 * 
	 * @param moduleName Name of the module
	 * @param modulePackage Name of the module's package parent package (e.g. org.webtop.module) 
	 * @param manifestDir Name of the directory to store the manifest in
	 * @param jars String[][] of jar filenames.
	 */
	private static void makeManifest(String moduleName,	String modulePackage, String manifestDir, String[][] jars) {
		/* Process:
		 * 1. Write generic manifest headers to contents
		 * 2. Write Main-class attribute to contents
		 * 3. Write Class-path attribute to contents
		 * 4. Write contents to fout
		 */
		
		FileWriter fout = null;
		StringWriter contents = new StringWriter();
		
		//Before getting started, let's make sure we can open the output file.
		String manifestName = manifestDir + File.pathSeparator + moduleName.toUpperCase() + DEF_MANIFEST_SUFFIX;
		System.out.println("Creating manifest " + manifestName);
		
		try {
			
			fout = new FileWriter(manifestName);
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
			System.out.println("\n\nError: Could not open file " + manifestName);
			System.exit(-1);
		}
		
		contents.write("MANIFEST-VERSION: 1.0\n");
		contents.write("Main-Class: " + modulePackage + "." + moduleName + "\n");
		
		//Add jars
		/********************* NEW WAY ****************************/
		contents.write("Class-path: ");
		for(int i=0; i<jars.length; i++)
			for(int j=0; j<jars[i].length; j++)
				if(jars[i][j]!=null && jars[i][j]!="")
					contents.write(jars[i][j]);  //Should not put in a \n.  MUST NOT!
		
		/******************* END NEW WAY **************************/
		//End the Class-path attribute with a newline...
		contents.write("\n");
		
		//Finally, print contents to the file fout.
		try {
			fout.write(contents.getBuffer().toString());
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
			System.out.println("\n\nError: Could not write file " + manifestName);
			System.exit(-1);
		}
		
		try {
			fout.close();
		}
		catch(Exception e) {
			System.out.println("Error closing manifest file!");
		}
		
		//Notifiy user that program is finished
		System.out.println("Wrote manifest to " + manifestName);
		System.out.println("Finished!");
	} //end ManifestMaker.makeManifest()
	
	/**
	 * Takes a directory as an argument and returns a String array with all 
	 * the jar filenames in the given directory.
	 * @param dir  Directory to be searched.
	 * @return Array of jar filenames in dir.
	 */
	
	public static String[] getJars(String dirname) {
		return getResources(dirname, ".jar");
	}
	/**
	 * A generic method for getting resouces, just in case having a generic 
	 * method proves handy one day.
	 * 
	 * Generally, if this encounters a problem, it returns {""} so that the program 
	 * will complete but no invalid data will be written to the manifest.  This directory 
	 * will just not have any files listed.
	 * 
	 * @param dirname Directory to search
	 * @param _ext File extension to filter on
	 * @return Array of resource files ending in _ext
	 */
	public static String[] getResources(String dirname, String _ext) {		
		String[] resources = {""};
		if(dirname==null) {
			System.out.println("Warning, dirname null. Returning {\"\"}.");
			return resources;
		}

		final String ext = _ext;
		
		//Create a File object from 'dir'
		File dir = null;
		try {
			dir = new File(dirname);
		}
		catch(NullPointerException npe) {
			System.out.println("Error opening directory " + dirname + "!");
			npe.printStackTrace();
		}
		
		//Check that 'dir' is a dir
		if(!dir.isDirectory()) {
			System.out.println(dirname + " is not a directory!");
			System.out.println("Returning {\"\"}.");
			return resources;
		}
		
		//So we have a handle to a valid directory.  List the appropriate files.
		resources = dir.list(
					//This anonymous interface implementation will look for files ending in 'ext'
					new FilenameFilter() {
						public boolean accept(File dir, String filename) {
							return  !filename.equals(ext) &&
									!filename.equals("") &&
									filename.endsWith(ext);
						}
					}
				);

		return resources;
	}//end ManifestMaker.getResources()
} //end class ManifestMaker
