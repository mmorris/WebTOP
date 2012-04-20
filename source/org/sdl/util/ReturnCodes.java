/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//ReturnCodes.java
//Defines commonly-used system return codes.
//Davis Herring
//Created October 6 2003
//Updated October 6 2003

package org.sdl.util;

public final class ReturnCodes {
	public static final int
		OK=0,												//0, of course
		BAD_INPUT=1,								//from stdin
		BAD_ARGS=2,									//from argv[]
		BAD_INFILE=3,								//formatting/parsing error
		FILE_ERROR=4,								//for errors accessing files
		IO_ERROR=5,									//for errors on an I/O channel
		NETWORK_ERROR=6,						//for those that are sockets
		NOT_IMPLEMENTED=7,					//no code to fulfill request
		PROC_ERROR=8;								//for errors dealing with process setup

	private ReturnCodes() {}			//instantiateth not
}
