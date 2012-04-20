/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.wsl.client;

import java.awt.*;
//import java.awt.event.*;
import java.io.*;

// this only affects compilation
//import com.ms.security.PolicyEngine;
//import com.ms.security.PermissionID;

/**
 * This class provides local file access to the rest of WSLAPI.  This class is
 * meant to be installed on the user's computer in order to enable the
 * save/load scripts feature provided by WSLAPI.  The CLASSPATH environment
 * variable on the user's computer has to be set to point to
 * "WSLIOManager.class", or the .jar file the class is contained in.  This
 * makes <code>WSLIOManager</code> a system trusted class, and thus allows it
 * to access local files.
 *
 * <p>Essentially, <code>WSLIOManager</code> provides methods to obtain file
 * input and output stream objects. When invoked, these methods pop up a system
 * file dialog box in order to let the user to choose which file is to be used.
 *
 * <p>To prevent important local files from being overwritten or modified,
 * <code>WSLIOManager</code> will only open files with names that end with
 * ".wsl".  If the filename entered by the user does not end with ".wsl",
 * <code>WSLIOManager</code> appends the ".wsl" extension to the filename.
 *
 * @author Yong Tze Chi
 * @author Davis Herring
 */
public class WSLIOManager implements FilenameFilter {
	private String lastFilename;

	/**
	 * Determines whether <code>WSLIOManager</code> is trusted to read/write
	 * local files.	 This tests whether this class was loaded by the bootstrap
	 * class loader.	If it was, it must have been loaded locally and it is
	 * assumed that it will have permission to access local files.
	 *
	 * @return	true if <code>WSLIOManager</code> would be allowed to load and
	 *					save WSL scripts; false otherwise.
	 * @since WSL2
	 */
	public static boolean isTrusted() {
		//return WSLIOManager.class.getClassLoader()==Object.class.getClassLoader();
		return true; //TODO: for now, we are always trusted
	}

	/**
	 * Returns the version number of WSLIOManager.	This will not necessarily
	 * increase with each new version of WSL; however, this number will not
	 * increase without WSL's version also changing.	0 represents the WSL1
	 * version, and 1 the version through WSL v3.1; this method will never
	 * return those values because it did not yet exist then.
	 *
	 * @return the integer number of releases of WSLIOManager
	 * @since 3.11
	 */
	public static int getVersion() {return 3;}

	/**
	 * Obtains a <code>FileInputStream</code> to read a WSL script.
	 * <code>WSLIOManager</code> prompts the user for the file to open by using
	 * a system file dialog box. A ".wsl" extension is appended to the filename
	 * if the user enters one that does not end with ".wsl".
	 *
	 * @param	 parent	 the <code>Frame</code> (window) object where the WebTOP
	 *								 module resides in.
	 * @return	a <code>FileInputStream</code> object that can be used to read in
	 *					the file content; <code>null</code> if denied access to the file
	 *					or if no file was selected.
	 * @exception IOException if there is an I/O error opening the file.
	 */
	public FileInputStream getWSLScriptInputStream(Frame parent) throws IOException {
		try {
			//PolicyEngine.assertPermission(PermissionID.FILEIO);
			//PolicyEngine.assertPermission(PermissionID.UI);
			//PolicyEngine.assertPermission(PermissionID.USERFILEIO);
		} catch(NoClassDefFoundError e) {}

		try {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension dialogSize;
			FileDialog fileDialog = new FileDialog(parent, "Open WSL Script",
																						 FileDialog.LOAD);
			fileDialog.setFile("*.wsl");
			fileDialog.setFilenameFilter(this);
			dialogSize = fileDialog.getMinimumSize();
			fileDialog.setBounds((screenSize.width-dialogSize.width)/2,
													 (screenSize.height-dialogSize.height)/2,
													 dialogSize.width,dialogSize.height);
			fileDialog.show();

			if(fileDialog.getFile()==null) {
				lastFilename = null;
				return null;
			}

			String filename = fileDialog.getFile().trim();
			if(!filename.toLowerCase().endsWith(".wsl")) {
				/*int i;
				if((i=filename.lastIndexOf('.'))>0) {
					filename = filename.substring(0, i);
				}
				filename = filename + ".wsl";*/	//Clobbering people's filenames is bad
				filename+=".wsl";
			}

			lastFilename = filename;		//assign even if exception

			return new FileInputStream(fileDialog.getDirectory() + filename);
		} catch(SecurityException e) {
			System.err.println("WSLIOManager::getWSLScriptInputStream: permission denied");
			return null;
		}
	}

	/**
	 * Obtains a <code>FileOutputStream</code> object to save a WSL script.
	 * <code>WSLIOManager</code> prompts the user for the file to open by using
	 * a system file dialog box. A ".wsl" extension is appended to the filename
	 * if the user enters one that does not end with ".wsl".
	 *
	 * @param	 parent	 the <code>Frame</code> (window) object wherein the WebTOP
	 *								 module resides.
	 * @return	a <code>FileOutputStream</code> object that can be used to write
	 *					out a WSL script; <code>null</code> if denied access to the file
	 *					or if user opted not to save the script.
	 * @exception IOException if there is an I/O error opening the file.
	 */
	public FileOutputStream getWSLScriptOutputStream(Frame parent) throws IOException {
		try {
			//PolicyEngine.assertPermission(PermissionID.FILEIO);
			//PolicyEngine.assertPermission(PermissionID.UI);
			//PolicyEngine.assertPermission(PermissionID.USERFILEIO);
		} catch(NoClassDefFoundError e) {}

		try {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension dialogSize;
			FileDialog fileDialog = new FileDialog(parent, "Save WSL Script",
																						 FileDialog.SAVE);
			fileDialog.setModal(true);
			fileDialog.setFile("*.wsl");
			fileDialog.setFilenameFilter(this);
			dialogSize = fileDialog.getMinimumSize();
			fileDialog.setBounds((screenSize.width-dialogSize.width)/2,
													 (screenSize.height-dialogSize.height)/2,
													 dialogSize.width,dialogSize.height);
			fileDialog.show();

			if(fileDialog.getFile()==null) {
				lastFilename = null;
				return null;
			}

			String filename = fileDialog.getFile().trim();
			if(!filename.toLowerCase().endsWith(".wsl")) {
				/*int i;
				if((i=filename.lastIndexOf('.'))>0) {
					filename = filename.substring(0, i);
				}
				filename = filename + ".wsl";*/	//Clobbering people's filenames is bad
				filename+=".wsl";
			}

			lastFilename = filename;		//assign even if exception

			return new FileOutputStream(fileDialog.getDirectory() + filename);
		}
		catch(SecurityException e) {
			System.err.println("WSLIOManager::getWSLScriptOutStream: permission denied");
			return null;
		}
	}

	/**
	 * Returns the filename associated with the
	 * <code>getWSLScriptInputStream()</code> or
	 * <code>getWSLScriptOutputStream()</code> method call.	 Because the process
	 * of specifying the filename is invisible to the caller, this method is
	 * needed for the caller to obtain the filename.
	 *
	 * @return filename entered or selected by the user on the last save/load
	 *				 operation, or <code>null</code> if there have been no operations
	 *				 or if the last operation was canceled by the user.
	 */
	public String getLastFilename() {return lastFilename;}

	/**
	 * Returns whether the specified file is to be displayed in the file dialog
	 * box. This method is called by the AWT framework to determine which files
	 * to display in the file dialog box.	 Only files with names ending in
	 * ".wsl" are displayed.
	 *
	 * @param	 dir			 the folder information associated with the file.
	 * @param	 filename	 name of the file concerned.
	 * @return <code>true</code> if the file is to be displayed (i.e., it ends
	 *				 in ".wsl"); <code>false</code> otherwise.
	 */
	public boolean accept(File dir, String filename) {
		return filename.toLowerCase().endsWith(".wsl");
	}
}
