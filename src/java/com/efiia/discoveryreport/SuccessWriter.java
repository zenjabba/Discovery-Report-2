/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport;

// import static com.efiia.discoveryreport.DiscoveryReport.DEBUG;
import java.io.File;
import java.time.LocalDateTime;

/**
 *
 * @author larry
 */
public class SuccessWriter {

	static LogWriter myLogger;

	static public void setLogWriter( File pLogFile ) {
		myLogger = ( pLogFile == null ? null : new LogWriter( pLogFile ));
	}

	/* retired
	static public void writeSuccess( LocalDateTime pNow, String pProcessID, String pMsg ) {
		myLogger.Write( String.format( "%s TaskID=%s Status=\"Success\" %s", pNow, pProcessID, pMsg ));
	}
	*/
	
	static void writeSuccess( LocalDateTime pNow, String pProcessID, String pBoxUserID, String pBoxUserName, String pBoxUserLogin, String pMsg ) {
		myLogger.Write( String.format( "%s TaskID=%s BoxUserID=%s BoxUserName=\"%s\" BoxUserLogin=\"%s\" Status=\"Success\" %s", pNow, pProcessID, pBoxUserID, pBoxUserName, pBoxUserLogin, pMsg ));
	}

}
