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

	public String writeException( DRException pDRE ) {

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
				if ( pDRE.getMessage() == null )
					lines.add( String.format("%s %s    | Exception=%s", myLastUpdate, myProcessID, cause.getClass().getName() ));
				else if ( !cause.getMessage().equals( pDRE.getMessage()))
					lines.add( String.format("%s %s    | Exception=%s: %s", myLastUpdate, myProcessID, cause.getClass().getName(), cause.getMessage() ));
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
		////
		StringBuilder bldr = new StringBuilder();

		bldr.append( "ErrorCode=").append( pDRE.ErrorCode );

		if ( pDRE.SubErrorCode > 0 ) {
			bldr.append( " SubError=").append( pDRE.SubErrorCode);
		}
		bldr.append( " Module=\"" ).append( pDRE.Module ).append( "\"");
		bldr.append( " Message=\"").append( pDRE.getMessage() ).append(  "\"" );
		if ( pDRE.Info != null ) {
			bldr.append( " Info=\"" ).append(  pDRE.Info ).append( "\"" );
		}

		if ( pDRE.Extra != null ) {
			for ( String x : pDRE.Extra ) {
				bldr.append( " " ).append( x );
			}
		}

		if ( pDRE.Debug != null ) {
			for ( String x : pDRE.Debug ) {
				bldr.append( " " ).append( x );
			}
		}

		Throwable cause = pDRE.getCause();
		if ( cause != null ) {
			if ( pDRE.getMessage() == null )
				bldr.append( " ErrorCause=\"" + cause.getClass().getName() + "\"");
			else if ( !cause.getMessage().equals(  pDRE.getMessage() ))		// some exceptions don't have a message
				bldr.append( " ErrorCause=\"" + cause.getClass().getName() + ": " + cause.getMessage() + "\"");
			// convert to strings
			StringBuilder stbldr = new StringBuilder();
			String xClass;
			Boolean xTest;
			int ctr = 0;
			final String Me = "com.efiia.discoveryreport";
			for ( StackTraceElement ste : cause.getStackTrace() ) {
				// only go as deep as the third call to com.efiia.discoveryreport or once that appears, and goes elsewhere
				xClass = ste.getClassName();
				xTest = xClass.startsWith( Me );
				if ( xTest )
					ctr++;
				stbldr.append( ",\"" ).append( ste.toString() ).append( "\"");
				//if ( ctr >= 3 )
				//	break;
				//else
				if ( ctr > 0 && !xTest )
					break;
			}
			stbldr.deleteCharAt( 0 );
			bldr.append( " StackTrace=[").append( stbldr ).append( "]");
		}

		if ( splunkLogger != null )
			splunkLogger.Write( String.format("%s TaskID=%s BoxUserID=%s BoxUserName=\"%s\" BoxUserLogin=\"%s\" TaskCode=%d TaskStatus=\"Error\" %s", myLastUpdate, myProcessID, myUserID, myUserName, myUserLogin, myLastStatus, bldr ));

		return ( bldr.toString() );

	}
}
