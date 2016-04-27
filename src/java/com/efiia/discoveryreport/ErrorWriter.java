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
		if ( myLogger != null )
			myLogger.Write( String.format("%s TaskID=%s BoxUserID=%s BoxUserName=\"%s\" BoxUserLogin=\"%s\" Status=\"Error\" %s", pNow, pProcessID, pBoxUserID, pBoxUserName, pBoxLogin, pInfo ));
	}

}
