/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport;

// import static com.efiia.discoveryreport.DiscoveryReport.DEBUG;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 *
 * @author larry
 */
public class ProcessWriter {

	String myProcessID;
	LogWriter myLogger;
	LogWriter splunkLogger;
	LocalDateTime myLastUpdate;
	int myLastStatus;

	String myUserID = "n/a";
	String myUserName = "n/a";
	String myUserLogin = "n/a";

	public ProcessWriter( File pLogFile, File pSplunkFile ) {
		myProcessID = UUID.randomUUID().toString();
		myLogger = ( pLogFile == null ? null : new LogWriter( pLogFile ));
		splunkLogger = ( pSplunkFile == null ? null : new LogWriter( pSplunkFile ));
	}

	public void setUserID( String pID ) {
		myUserID = pID;
	}

	public void setUserName( String pName ) {
		myUserName = pName;
	}

	public void setUserLogin( String pLogin ) {
		myUserLogin = pLogin;
	}

	public String getProcessID() {
		return myProcessID;
	}

	public LocalDateTime getLastUpdateTime() {
		return myLastUpdate;
	}

	public int getLastStatus() {
		return myLastStatus;
	}

	public void updateStatus( int pStatusCode, String pStatusMessage ) {

		myLastUpdate = LocalDateTime.now();
		myLastStatus = pStatusCode;

		// update database

		// write to standard log
		if ( myLogger != null ) {
			String line = String.format( "%s %s %4d %s", myLastUpdate, myProcessID, pStatusCode, pStatusMessage );
			myLogger.Write( line );
		}

		if ( splunkLogger != null ) {
			String line = String.format("%s TaskID=%s BoxUserID=%s BoxUserName=\"%s\" BoxUserLogin=\"%s\" TaskCode=%d TaskAction=\"%s\"", myLastUpdate, myProcessID, myUserID, myUserName, myUserLogin, pStatusCode, pStatusMessage );
			splunkLogger.Write( line );
		}
	}

	public void writeException( DRException pDRE ) {

		myLastUpdate = LocalDateTime.now();

		if ( myLogger != null ) {

			ArrayList<String> lines = new ArrayList<String>();

			lines.add( String.format("%s %s %4d %s", myLastUpdate, myProcessID, pDRE.ErrorCode, "Error Code=" + pDRE.ErrorCode ));

			if ( pDRE.SubErrorCode > 0 )
				lines.add( String.format( "%s %s    | %s", myLastUpdate, myProcessID, " Sub Error=" + pDRE.SubErrorCode ));

			lines.add( String.format( "%s %s    | User: ID=%s, Name=%s, Login=%s", myLastUpdate, myProcessID, myUserID, myUserName, myUserLogin ));
			lines.add( String.format( "%s %s    | Module=%s", myLastUpdate, myProcessID, pDRE.Module ));
			lines.add( String.format( "%s %s    | Message=%s", myLastUpdate, myProcessID, pDRE.getMessage() ));
			if ( pDRE.Info != null )
				lines.add( String.format( "%s %s    | Info=%s", myLastUpdate, myProcessID, pDRE.Info ));

			if ( pDRE.Extra != null ) {
				String xh = "Extra=";
				for ( String x : pDRE.Extra ) {
					lines.add( String.format("%s %s    | %s%s", myLastUpdate, myProcessID, xh,x ));
					xh = "     ";
				}
			}

			if ( pDRE.Debug != null ) {
				String dbg = "Debug: ";
				for ( String x : pDRE.Debug ) {
					lines.add( String.format("%s %s    | %s%s", myLastUpdate, myProcessID, dbg,x ));
					dbg = "     ";
				}
			}

			Throwable cause = pDRE.getCause();
			if ( cause != null ) {
				String cmsg = cause.getMessage();
				if ( cmsg == null )
					lines.add( String.format("%s %s    | Exception=%s", myLastUpdate, myProcessID, cause.getClass().getName() ));
				else if ( !cmsg.equals( pDRE.getMessage()))
					lines.add( String.format("%s %s    | Exception=%s: %s", myLastUpdate, myProcessID, cause.getClass().getName(), cmsg ));
				// convert to strings
				boolean SeenFlag = false;
				//lines.add( String.format("%s %s    | %s", myLastUpdate, myProcessID, "StackTrace:" ));
				for( StackTraceElement ste : cause.getStackTrace() ) {
					if ( ste.getClassName().startsWith( "com.efiia.discoveryreport" ))
						SeenFlag = true;
					else if ( SeenFlag )
						break;
					lines.add( String.format("%s %s    @ %s", myLastUpdate, myProcessID, ste.toString() ));
				}
			}

			myLogger.Write( lines.toArray( new String[ lines.size() ] ));
		}

		// new format for splunk
		if ( splunkLogger != null )
			splunkLogger.Write( String.format("%s TaskID=%s BoxUserID=%s BoxUserName=\"%s\" BoxUserLogin=\"%s\" TaskCode=%d TaskStatus=\"Error\" %s", myLastUpdate, myProcessID, myUserID, myUserName, myUserLogin, myLastStatus, pDRE.getSplunkText() ));

	}
}
