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
public class ErrorWriter {

	static LogWriter myLogger;

	public static void setLogWriter( File pLogFile ) {
		myLogger = ( pLogFile == null ? null : new LogWriter( pLogFile ));
	}

	/* retired
	public static void writeException( LocalDateTime pNow, String pProcessID, String pInfo ) {
		myLogger.Write( String.format("%s TaskID=%s Status=\"Error\" %s", pNow, pProcessID, pInfo ));
	}
	*/
	static void writeException( LocalDateTime pNow, String pProcessID, String pBoxUserID, String pBoxUserName, String pBoxLogin, String pInfo ) {
		myLogger.Write( String.format("%s TaskID=%s BoxUserID=%s BoxUserName=\"%s\" BoxUserLogin=\"%s\" Status=\"Error\" %s", pNow, pProcessID, pBoxUserID, pBoxUserName, pBoxLogin, pInfo ));
	}

	/* moved to consolidated processwriter
	public static void writeException( LocalDateTime pNow, String pProcessID, DRException pDRE ) {

		StringBuilder bldr = new StringBuilder();

		bldr.append( "ErrorCode=").append( pDRE.ErrorCode );

		if ( pDRE.SubErrorCode > 0 ) {
			bldr.append( " SubError=").append( pDRE.SubErrorCode);
		}
		bldr.append( " Module=" ).append( pDRE.Module );
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
				bldr.append( " ErrorCause=\"" + cause.getMessage() + "\"");
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

		myLogger.Write( String.format("%s TaskID=%s Status=\"Error\" %s", pNow, pProcessID, bldr ));

	}
	*/

}
