/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport;

import com.efiia.discoveryreport.data.ReportData;
import com.efiia.discoveryreport.data.DataBase;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author larry
 */
public class DiscoveryReport extends HttpServlet {

	private static final boolean DELETEFILES = true;

	private final static int APICONFIGFILECANTREAD = 2001;
	private final static int APICONFIGFILEREADERROR = 2003;
	private final static int APICONFIGMISSINGCLIENTID = 2005;
	private final static int APICONFIGMISSINGCLIENTSECRET = 2006;

	private final static int CONFIGFILECANTREAD = 2001;
	private final static int CONFIGFILEREADERROR = 2003;
	private final static int CONFIGFILECAPNOTANUMBER = 2008;

	private final static int CANTINITMAIL = 2009;

	private final static String REPORTFOLDERBASENAME = "Reports";
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException {
		//processRequest( request, response );
		this.createDiscoveryReport( request, response );
	}

	public final static String APPNAME = "Discovery Report Creator";
	//private final static String APPDATE = "28-Jan-2014";
	//private final static String APPVER = "1.0.2";			// new error reporting
	//private final static String APPVER = "1.0.3";			// added extra start up debugging info, Java 1.7 support
	//private final static String APPVER = "1.0.4";			// recompiled Splunk lib for TLS v 1.2
//	private final static String APPVER = "1.99.1";			// first try with database
//	private final static String APPDATE = "24-Mar-2015";
	// private final static String APPVER = "1.99.1";			// first try with database
//	private final static String APPVER = "1.99.5";
//	private final static String APPDATE = "25-Mar-2015";
//	private final static String APPVER = "1.99.14";
//	private final static String APPVER = "2.0.0";
//	private final static String APPDATE = "26-Mar-2015";
//	private final static String APPVER = "2.1.0";
//	private final static String APPDATE = "10-Apr-2015";
//	private final static String APPVER = "2.1.1";				// bug fixes
//	private final static String APPDATE = "13-Apr-2015";
//	public final static String APPVER = "2.1.2";				// bug fixes
//	public final static String APPDATE = "15-Apr-2015";		// remove IP from output
//	public final static String APPVER = "2.1.3";			// add fix for copy
//	public final static String APPDATE = "16-Apr-2015";
//	public final static String APPVER = "2.1.4";			// add case name to parmaters for output message, email & box special message
//	public final static String APPDATE = "24-Apr-2015";
//	public final static String APPVER = "2.1.5";			// add folder events & notes
//	public final static String APPDATE = "22-May-2015";
//	public final static String APPVER = "2.1.6 ('Someone' Kludge)";			// add folder events & notes
//	public final static String APPDATE = "29-Sep-2015";
//	public final static String APPVER = "2.2.0";			// improved Error reporting
//	public final static String APPDATE = "8-Feb-2016";
//	public final static String APPVER = "2.2.1";			// logs and folder name
//	public final static String APPDATE = "11-Feb-2016";
//	public final static String APPVER = "2.2.2";			// Root, File Count, Log changes
//	public final static String APPDATE = "18-Feb-2016";
//	public final static String APPVER = "2.2.2a";			// Fix progress log errors with box user info
//	public final static String APPDATE = "18-Feb-2016";
//	public final static String APPVER = "2.2.2c";			// Fix unmanaged error message
//	public final static String APPDATE = "19-Feb-2016";
//	public final static String APPVER = "2.2.3";			// Root Folder Lockdown option
//	public final static String APPDATE = "10-Mar-2016";
//	public final static String APPVER = "2.2.4";			// Root Folder Pre-Check
//	public final static String APPDATE = "15-Mar-2016";
//	public final static String APPVER = "2.2.5";			// Error clean up
//	public final static String APPDATE = "21-Mar-2016";
//	public final static String APPVER = "2.2.6";			// Fix for emdash display in Box Pop-up
//	public final static String APPDATE = "11-Apr-2016";
//	public final static String APPVER = "2.2.7";			// Fix for get folder events, User 1 as "SYSTEM"
//	public final static String APPDATE = "27-Apr-2016";
															// Fix for mail to noreply@esfs.us, other minor cleanup
	public final static String APPVER = "2.2.8";			// Fix for get folder events, User 1 as "SYSTEM"
	public final static String APPDATE = "18-Jul-2016";		// Enhancement for emailoverride

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return String.format( "%s (%s %s)", APPNAME, APPDATE, APPVER );
	}

	// simple authorization
	private String[] AllowedAddressSuffixes = null;

	// collateral
	private String OutStyleSheet = null;
	private String OutMediaHome = null;

	// format string for file name
	private String FileNameFormat = null;
	private String ReportDateFormat = null;
	private String ReportDateFormatExpanded = null;

	// conversion options
	HTML2PDF Converter = null;

	// connect to box
	private String BoxClientID = null;
	private String BoxClientSecret = null;
	private enumBoxReportDest BoxReportDest;

	// added Feb 2016
	private enumBoxReportFolderSuffix BoxReportFolderNameStyle;

	// added Mar 2016
	private boolean RootFolderLockdown;

	private boolean CSVFlag;

	// email options
	MailUtility Mailer = null;

	private enum ReportingMode {
		HTMLinMemory,
		HTMLtoBrowser,
		HTMLFile,
		PDFFile
	};

	private ReportingMode ReportMode = ReportingMode.HTMLinMemory;

	// defaults strings to something usable
	private String EmailTemplate = null;

	// Config File Information Messages
	private String MsgNothingToReport = "Nothing to report";
	private String MsgBoxUpload = "0:%0$s; 1:%1$s; 2:%2$s Folder";
	private String MsgEmailSent =  "%s eMailed to %s/%s";
	private String MsgPending = "Files Pending - Try Later";
	private String MsgBoxAndEmailSuccess = null;

	// Config File Error Messages
	private String MsgUnauthorized = "Unauthorized User";
	private String MsgTooManyFiles = "Too Many Files in Report Folders";
	private String MsgNotRoot = "Not Root Folder";

	// new feb 2016
	// logging modules
	private File LogProgress = null;
	private File LogSuccessSplunk = null;
	private File LogErrorsSplunk = null;
	private File LogProgressSplunk = null;

	// generical global error that prevents reports from running at all
	int GlobalError = 0;
	String GlobalErrorMessage = null;

	private boolean loadMail;

	// new july 2016
	String eMailOverride = null;

	private void readBoxConfig( File pConfigDir ) throws DRException {
		// read box api file;
		File confFile = new File( pConfigDir, "reporter.boxapi.config" );
		if ( !confFile.canRead() )
			throw new DRException( APICONFIGFILECANTREAD, "DiscoverReportGenerator:init(1)", String.format( "Config File %s Not Found/Readable", confFile.toString()) );

		// connect to local database
		Properties propsAPI = new Properties();
		try (FileInputStream in = new FileInputStream(confFile )) {
			propsAPI.load( in );
		} catch ( IOException ex ) {
			throw new DRException( APICONFIGFILEREADERROR, "DGR:init(2)", "Read Property File", ex );
		}

		BoxClientID = propsAPI.getProperty( "ClientID" );
		BoxClientSecret = propsAPI.getProperty( "ClientSecret" );

		if ( BoxClientID == null )
			throw new DRException( APICONFIGMISSINGCLIENTID, "DiscoverReportGenerator:init(2)", String.format( "Config File %s Missing Box API ClientID", confFile.toString() ));
		if ( BoxClientSecret == null )
			throw new DRException( APICONFIGMISSINGCLIENTSECRET, "DiscoverReportGenerator:init(4)", String.format( "Config File %s Missing Box API Client Secret", confFile.toString() ));
	}

	private void readReportConfig( File pConfigDir ) throws DRException {

		// now read the Report's config
		File confFile = new File( pConfigDir, "reporter.config" );
		if ( !confFile.canRead() )
			throw new DRException( CONFIGFILECANTREAD, "DiscoverReportGenerator:init(5)", String.format( "Config File %s Not Found/Readable", confFile.toString()) );

		// connect to
		Properties propsRPT = new Properties();
		try (FileInputStream in = new FileInputStream(confFile )) {
			propsRPT.load( in );
		} catch ( IOException ex ) {
			throw new DRException( CONFIGFILEREADERROR, "DiscoverReportGenerator:init(6)", "Read Property File", ex );
		}

		String x;

		// new logging modules - do first in case there are any issues
		x = propsRPT.getProperty( "ErrorLog" );
		if ( x == null )
			x = propsRPT.getProperty( "ErrorLogSplunk" );
		if ( x != null ) {
			LogErrorsSplunk = new File( x );
			ErrorWriter.setLogWriter( LogErrorsSplunk );
		}
		x = propsRPT.getProperty( "SuccessLog" );
		if ( x == null )
			x = propsRPT.getProperty( "SuccessLogSplunk" );
		if ( x != null ) {
			LogSuccessSplunk = new File( x );
			SuccessWriter.setLogWriter( LogSuccessSplunk );
		}
		x = propsRPT.getProperty( "ProgressLogSplunk" );
		if ( x != null )
			LogProgressSplunk = new File( x );

		x = propsRPT.getProperty( "ProgressLog" );
		if ( x != null )
			LogProgress = new File( x );


		x = propsRPT.getProperty( "AllowedAddressSuffixes" );
		AllowedAddressSuffixes = x.split( "," );
		for ( int i = 0; i < AllowedAddressSuffixes.length; i++ )
			AllowedAddressSuffixes[i] = AllowedAddressSuffixes[i].trim();		// get rid of extra white space in the xml list

		OutStyleSheet = propsRPT.getProperty( "WebStyleSheetURL" );
		OutMediaHome = propsRPT.getProperty( "WebMediaHomeURL" );

		FileNameFormat = "USAfx Discovery Report: %1$s - %2$s";
		ReportDateFormat = "yyyy-MM-dd hh:mm aa zz";
		ReportDateFormatExpanded = "%1$tB %1$te%2$s, %1$tY at %1$tI:%1$tM %1$Tp %1$tZ";

		x = propsRPT.getProperty( "HTML2PDFUtility" );
		if ( x != null )
			Converter = new HTML2PDF( x, propsRPT.getProperty( "HTML2PDFUtilOptions" ));

		BoxReportDest = enumBoxReportDest.getValueOf( propsRPT.getProperty( "BoxReportDest" ));		// Root or Case Folder
		BoxReportFolderNameStyle = enumBoxReportFolderSuffix.getValueOf( propsRPT.getProperty( "BoxReportFolderSuffix" ));

		// added Mar 2016
		// "AllowSubFolderReports" Y or N - invert the state to RootFolderLockdown
		x = propsRPT.getProperty( "AllowSubFolderReports" );
		if ( x != null && Character.toLowerCase( x.charAt( 0 )) != 'y' )
			RootFolderLockdown = true;
		else
			RootFolderLockdown = false;

		x = propsRPT.getProperty( "CSVFlag" );
		if ( x != null && Character.toLowerCase( x.charAt( 0 )) == 'y')
			CSVFlag = true;
		else
			CSVFlag = false;

		x = propsRPT.getProperty( "EmailReportFlag" );
		loadMail = ( x != null && Character.toLowerCase( x.charAt( 0 )) == 'y' ? true : false );

		x = propsRPT.getProperty( "EmailOverride" );
		if ( x != null )
			eMailOverride = x;

		x = propsRPT.getProperty( "FileCap" );
		if ( x != null ) {
			try {
				int fc = Integer.parseInt( x );
				ReportData.setCapCount( fc );
			} catch ( NumberFormatException nfe ) {
				throw new DRException( CONFIGFILECAPNOTANUMBER, "DiscoveryReportGenerator:init(7)", nfe );
			}
		}

		// strings
		x = propsRPT.getProperty( "EmailMessageText" );
		if ( x != null )
			EmailTemplate = x + "\n";
		x = propsRPT.getProperty( "MsgUnauthorizedRequestor");
		if ( x != null )
			MsgUnauthorized = x;
		x = propsRPT.getProperty( "MsgBoxUpload" );
		if ( x != null )
			MsgBoxUpload = x;
		x = propsRPT.getProperty( "MsgNothingToReport" );
		if ( x != null )
			MsgNothingToReport = x;
		x = propsRPT.getProperty( "MsgEmailSent" );
		if ( x != null )
			MsgEmailSent = x;
		x = propsRPT.getProperty( "MsgBoxAndEmailSuccess" );
		if ( x != null )
			MsgBoxAndEmailSuccess = x;
		x = propsRPT.getProperty( "MsgTryLater" );
		if ( x != null )
			MsgPending = x;
		x = propsRPT.getProperty( "MsgTooManyFiles" );
		if ( x != null )
			MsgTooManyFiles = x;
		x = propsRPT.getProperty( "MsgNotRoot" );
		if ( x != null )
			MsgNotRoot = x;

		ReportMode = ReportingMode.HTMLinMemory;
		if ( BoxReportDest == enumBoxReportDest.BOXNONE && Mailer == null )
			ReportMode = ReportingMode.HTMLtoBrowser;
		else if ( Converter != null || Mailer != null )
			ReportMode = ReportingMode.PDFFile;

		// override manually for testing
		// ReportMode = ReportingMode.HTMLtoBrowser;

	}

	@Override
	public void init( ServletConfig pxConfig ) throws ServletException {
		super.init(pxConfig);	// this is the only class that throws a Servlet Exception

		Logger myLogger = Logger.getLogger( "org.apache.catalina.logger.SystemOutLogger");
		myLogger.logp(Level.INFO, APPNAME, APPVER, "Started" );

		String osName = System.getProperty( "os.name" );
		String osVersion = System.getProperty( "os.version" );
		String osArch = System.getProperty( "os.arch" );
		myLogger.logp( Level.INFO, APPNAME, APPVER, String.format( "OS Info: %s (%s) / %s", osName, osVersion, osArch ));

		String jVersion = System.getProperty( "java.version" );
		String jVendor = System.getProperty( "java.vendor" );
		String jHome = System.getProperty( "java.home" );
		myLogger.logp( Level.INFO, APPNAME, APPVER, String.format( "Java Version Info: %s: %s, Home: %s", jVendor, jVersion, jHome ));

		int localErrorNo = 0;
		String localErrorMsg = "init";

		try {
			/* get config location from web.xml */
			File pConfigDir = new File( "/etc/discoveryreport" );
			myLogger.logp( Level.INFO, APPNAME, APPVER, String.format( "Reading Configuration Files from: %s", pConfigDir.toString() ));

			// moved so that log files can capture start up errors

			readReportConfig( pConfigDir );
			readBoxConfig( pConfigDir );

			if ( loadMail ) {
				localErrorNo = CANTINITMAIL;
				localErrorMsg = "Initalize Mail Utility";
				Mailer = new MailUtility();		// needs a newline afterwards to be pretty
				localErrorNo = 0;
				localErrorMsg = "init";
			}

			// finally read the database config
			DataBase.Config( pConfigDir );
			DataBase.Connect();

		} catch ( Error err ) {
			DRException drx = new DRException( localErrorNo, "init", localErrorMsg, err );
			drx.setSourceClassName( err.toString() );
			drx.disableStandardSuffix();
			GlobalError = drx.ErrorCode;
			GlobalErrorMessage = drx.getResponseMessage();
			drx.setResponseMessage( null );
			drx.sysPrint();

		} catch ( DRException dre ) {
			GlobalError = dre.ErrorCode;
			dre.sysPrint();

		}
	}

	private void createDiscoveryReport( HttpServletRequest pRequest, HttpServletResponse pResponse ) /* throws IOException */ {

		/* get from Box:
		 * User ID
		 * User Name (DOJ stipulates that is their email address
		 * Folder ID for report root
		 * Folder Name for Report name
		 * Auth Code for OAuth Login
		 */

		PrintWriter respWriter = null;
		File tmpRpt = null;
		File tmpCSV = null;

		// default as n/a to make it nicer for Splunk
		String BoxUserID = "n/a";
		String BoxUserName = "n/a";
		String BoxUserLogin = "n/a";

		ProcessWriter procStatus = new ProcessWriter( LogProgress, LogProgressSplunk );
		procStatus.updateStatus( 100, "Report Started" );

		try {

			try {
				respWriter = pResponse.getWriter();
			} catch ( IOException ex ) {
				throw new DRException( 700, "CreateDiscoveryReport:getResponseWriter", ex);
			}

			if ( GlobalError != 0 ) {
				//quick exit, system can't run
				throw new DRException( GlobalError, "CreateDiscoveryReport", GlobalErrorMessage );
			}


			//System.out.println( "*** Discovery Report Called at " + new Date() );
			procStatus.updateStatus( 101, "URL Query=" + pRequest.getQueryString() );
			//System.out.println( "  URL Query: " + pRequest.getQueryString() );

			String BoxFolderID = pRequest.getParameter( "folderid" );
			String BoxFolderName = pRequest.getParameter( "foldername" );
			BoxUserID = pRequest.getParameter( "userid" );
			BoxUserName = pRequest.getParameter( "username" );			// should be the DOJ email address
			procStatus.setUserID( BoxUserID );
			procStatus.setUserName( BoxUserName );
			// moved to default as n/a
			// BoxUserEmail = null;
			String BoxAuthCode = pRequest.getParameter( "authcode" );
			String BoxDevToken = pRequest.getParameter( "devtoken" );

			String FileExt = "html";

			// other parameters already found in init routine

			/* Step 1 - check if the requestor is authorized */
			procStatus.updateStatus( 110, "Check Box Authorization" );
			BoxInterface bi = ( BoxAuthCode != null ? new BoxInterface( BoxClientID, BoxClientSecret, BoxAuthCode ) : new BoxInterface( BoxDevToken ));
			bi.GetCurrentUser( BoxUserID, BoxUserName );

			String UserLogin = bi.getCurrentUserLogin();
			BoxUserName = bi.getCurrentUserName();
			BoxUserLogin = bi.getCurrentUserLogin();
			procStatus.setUserName( BoxUserName );		// should be the same, but just in case
			procStatus.setUserLogin( BoxUserLogin );
			if ( !IsAuthorizedToReport( UserLogin )) {
				DRException drxunauth = new DRException( 701, "CreateDiscoveryReport:Auth", "Unauthorized User" );
				drxunauth.setResponseMessage( MsgUnauthorized );
				drxunauth.disableStandardSuffix();
				drxunauth.setDebug( new String[] { "UserID=" + BoxUserID, "UserName=\"" + BoxUserName, "UserLogin=" + UserLogin } );
				throw drxunauth;
			}

			/* future Step 2 - fetch file from the folder for interesting report information
			 * build a place to hold the report data
			 */

			/*
			 * Mar 2016 - added flag to make this optional
			 */
			if ( RootFolderLockdown ) {
				procStatus.updateStatus( 111, "Check Case Folder Parent" );
				if ( !bi.CheckForRootFolder( BoxFolderID )) {
					DRException drxnotroot = new DRException ( 707, "CreateDiscoveryReport:CheckRoot", "Report Folder Not In Root" );
					drxnotroot.setResponseMessage( MsgNotRoot );
					drxnotroot.setDebug( new String[] { "FolderID=" + BoxFolderID, "FolderName=\"" + BoxFolderName + "\"" });
					drxnotroot.disableStandardSuffix();
					throw drxnotroot;
				}
			}

			/* later Mar 2016
			 * Pre-Check the Report Folder for Writeability
			 */
			String parentFolderID = ( BoxReportDest == enumBoxReportDest.BOXROOT ? "0" : BoxFolderID );
			String parentFolderName = ( BoxReportDest == enumBoxReportDest.BOXROOT ? "Root" : BoxFolderName );
			String reportFolderName = GetReportFolderName( BoxUserName, BoxUserLogin );

			// this will create the report folder if it does not exist
			// the folder is kept in the BoxInterface class so we don't have
			// to manage low-level stuff here
			procStatus.updateStatus( 112, "Get Report Folder" );
			bi.GetReportFolder( parentFolderID, reportFolderName );
			/* end of March 2016 updates */

			/* get case info as folder name */
			//String CaseName = BoxFolderName.replace( '+', ' ' );	// simple clean up of spaces in folder name
			Date CaseStart = null;
			Date CaseEnd = null;
			procStatus.updateStatus( 120, "Report Data Initialized" );
			ReportData rptData = new ReportData( procStatus, new UserInfo( BoxUserID, BoxUserName, BoxUserLogin), null, CaseStart, CaseEnd );

			/* Step 3 - Fetch Report Details */
			procStatus.updateStatus( 121, "Collecting Report Data" );
			rptData.fetchData( BoxFolderID, rptData );

			if ( rptData.hasTooManyFiles() ) {
				// throw new too many files error
				DRException drxtoomany =  new DRException( 708, "CreateDiscoveryReport:CheckSize", "Too Many Files", String.format( "%d Files Found, Reporting Cap is %d", rptData.getFileCount(), rptData.getCapSize() ));
				drxtoomany.setResponseMessage( MsgTooManyFiles );
				drxtoomany.disableStandardSuffix();
				throw drxtoomany;
			}

			/* Step 3a - prepare the data prior to reporting */
			procStatus.updateStatus( 123, "Preparing Report Data" );
			if ( !rptData.hasData() ) {
				// check with Box for Files in Folder
				if ( bi.CheckReportFolderHasFiles( BoxFolderID )) {
					// message is now try later
					respWriter.printf( MsgPending );
					procStatus.updateStatus( 129, "Waiting on Report Loader Update - Try Later" );
					return;
				}
				// nothing to report, just return a quick message to the Box User
				respWriter.printf( MsgNothingToReport );
				procStatus.updateStatus( 128, "No Files found to Report" );
				//-System.out.println( " No Files found to report" );
				return;
			}

			/* Step 4 - output the report to a known stream */
			procStatus.updateStatus( 130, "Preparing Output" );
			ByteArrayOutputStream baos = null;
			PrintWriter bw = null;
			switch( ReportMode ) {
				case HTMLtoBrowser:
					procStatus.updateStatus( 131, "Preparing Output to Browser" );
					bw = respWriter;
					break;

				case HTMLinMemory:
					// use a byte steam and output directly
					procStatus.updateStatus( 132, "Preparing Output to Memory" );
					baos = new ByteArrayOutputStream();
					bw = new PrintWriter( baos );
					break;

				case HTMLFile:
				case PDFFile:
					procStatus.updateStatus( 133, "Preparing Output to File" );
					try {
						tmpRpt = File.createTempFile( "DiscRpt", ".html" );
						bw = new PrintWriter( tmpRpt );
					} catch ( IOException ex ) {
						throw new DRException( 703, "CreateDiscoveryReport:CreateOpenTempFile", ex );
					}
					break;
			}

			procStatus.updateStatus( 140, "Creating HTML Report Object" );
			HTMLReportWriter rw = new HTMLReportWriter( bw, this.OutStyleSheet, this.OutMediaHome );
			procStatus.updateStatus( 141, "Writing HTML Report to Output" );
			rw.WriteReport( rptData );
			bw.flush();		// commit output to the browser
			procStatus.updateStatus( 142, "HTML Report Finished" );
			// don't close something you didn't open
			// bw.close();

			/* New Step 4a - convert the HTML to PDF */
			if ( ReportMode == ReportingMode.PDFFile ) {
				procStatus.updateStatus( 150, "Converting HTML to PDF" );
				bw.close();
				String pdfFileName = Converter.run( tmpRpt );
				procStatus.updateStatus( 159, "PDF Created" );
				if ( DELETEFILES )
					tmpRpt.delete();
				tmpRpt = new File( pdfFileName );
				FileExt = "pdf";
			}

			/* new Mar 2015
			 * create CSV from the same data
			 */
			if ( CSVFlag ) {
				procStatus.updateStatus( 160, "Writing CSV Report to Output" );
				FileWriter fwx;
				try {
					tmpCSV = File.createTempFile( "DiscRpt", ".csv" );
					fwx = new FileWriter(tmpCSV );
				} catch ( IOException ex ) {
					throw new DRException( 704, "CreateDiscoveryReport:CreateOpenTempCSV", ex );
				}
				CSVWriter cw = new CSVWriter( fwx );
				cw.ExportData( rptData );
				cw.close();
			}

			/* Step 5 - do something with the report
			 * for now return it as HTML
			 * future, upload it to box
			 */
//			String FileNameFormat = "USAfx Discovery Report: %1$s - %2$s";
			procStatus.updateStatus( 170, "Preparing Final Output" );

			SimpleDateFormat sdfFileName = new SimpleDateFormat( ReportDateFormat );
			Date now = new Date();
			String FileNameDate = sdfFileName.format( now );
			String ReportName = String.format( FileNameFormat, rptData.CaseName, FileNameDate );
			String ReportDateExpanded = String.format( ReportDateFormatExpanded, now, getDayOfMonthSuffix( now ) );

			String destFileName = ReportName + "." + FileExt;
			String destCSVName = ReportName + ".csv";

			String upFileMessage = "'" + destFileName + "'";
			if ( tmpCSV != null )
				upFileMessage += " and '" + destCSVName + "'";

			boolean BoxSuccess = false;
			boolean EmailSuccess = false;

			if ( BoxReportDest != enumBoxReportDest.BOXNONE ) {
				procStatus.updateStatus( 171, "Preparing Upload to Box" );
				InputStream rptSource = null;
				InputStream csvSource = null;
				switch ( ReportMode ) {
					case HTMLFile:
					case PDFFile:
						// use the new HTML File
						procStatus.updateStatus( 172, "Creating Final Output Files" );
						try {
							rptSource = new FileInputStream( tmpRpt );
						} catch ( FileNotFoundException ex ) {
							throw new DRException( 705, "DiscoveryReportGenerator:OpenUploadReportFile", ex );
						}
						if ( tmpCSV != null ) {
							try {
								csvSource = new FileInputStream( tmpCSV );
							} catch ( FileNotFoundException ex ) {
								throw new DRException( 706, "DiscoveryReportGenerator:OpenUploadCSVFile", ex );
							}
						}
						break;
					case HTMLinMemory:
						procStatus.updateStatus( 173, "Creating HTML Output Buffer" );
						ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );
						rptSource = bais;
						break;
				}

				procStatus.updateStatus( 174, String.format( "Uploading Report File [%s] to Box Folder [%s/%s](Parent ID:%s)", destFileName, parentFolderName, reportFolderName, parentFolderID ));
				// bi.UploadReport( parentFolderID, reportFolderName, destFileName, rptSource );
				bi.UploadReport2( destFileName, rptSource );
				if ( csvSource != null ) {
					procStatus.updateStatus( 175, String.format( "Uploading CSV File [%s] to Box Folder [%s/%s](Parent ID:%s)", destCSVName, parentFolderName, reportFolderName, parentFolderID ));
					//bi.UploadReport( parentFolderID, reportFolderName, destCSVName, csvSource );
					bi.UploadReport2( destCSVName, csvSource );
				}
				procStatus.updateStatus( 179, "Upload Successful" );
				//- System.out.println( " Upload Status: Success" );
				BoxSuccess = true;
			}

			/* added email override statuses, etc
			 * LEN - July 2016
			 */
			if ( Mailer != null ) {
				String eMailDestAddr = ( eMailOverride != null ? eMailOverride : BoxUserLogin );		// override the email address
				String eMailDestName = ( eMailOverride != null ? "(eMail Override)" : BoxUserName );
				String eMailSubject = ( eMailOverride != null ? "Diverted Discovery Report: " + ReportName : ReportName );
				String msgText = String.format( EmailTemplate, rptData.CaseName, ReportDateExpanded );
				procStatus.updateStatus( 180, String.format( "Emailing Report to [%s <%s>]", eMailDestName, eMailDestAddr ));
				Mailer.SendMessage( eMailDestAddr, BoxUserName, BoxUserLogin, eMailSubject, msgText, tmpRpt, destFileName, tmpCSV, destCSVName );
				if ( eMailOverride != null )
					procStatus.updateStatus( 188, String.format( "eMail Diverted from [%s/<%s>] to [%s] with Attachments [%s]", BoxUserName, BoxUserLogin, eMailDestAddr, destFileName ));
				else
					procStatus.updateStatus( 189, String.format( "eMail Sent to [%s/<%s>] with Attachments [%s]", eMailDestName, eMailDestAddr, destFileName ));
				EmailSuccess = true;
			}
			procStatus.updateStatus( 190, "Report Completed" );

			String successMsg = "";
			if ( MsgBoxAndEmailSuccess == null ) {
				// individual messages
				if ( BoxSuccess && MsgBoxUpload != null )
					successMsg += String.format( MsgBoxUpload, upFileMessage, parentFolderName, reportFolderName, rptData.CaseName );
				if ( BoxSuccess && EmailSuccess && MsgBoxUpload != null && MsgEmailSent != null )
					successMsg += "; ";
				if ( EmailSuccess && MsgEmailSent != null )
					successMsg += String.format( MsgEmailSent, upFileMessage, BoxUserName, BoxUserLogin, rptData.CaseName );
			} else {
				if ( BoxSuccess && EmailSuccess ) {
					// group messages
					successMsg = String.format( MsgBoxAndEmailSuccess, rptData.CaseName, upFileMessage, parentFolderName, reportFolderName, BoxUserName, BoxUserLogin );
				} else {
					if ( BoxSuccess )
						successMsg += String.format( MsgBoxUpload, upFileMessage, parentFolderName, reportFolderName, rptData.CaseName );
					if ( EmailSuccess )
						successMsg += String.format( MsgEmailSent, upFileMessage, BoxUserName, BoxUserLogin, rptData.CaseName );
				}
			}

			// 2016-Apr-11 -- added filter for emdash
			respWriter.write( filterForBoxDisplay( successMsg ));

			if ( ReportMode == ReportingMode.HTMLtoBrowser ) {
				procStatus.updateStatus( 191, "Report Returned in Browser" );
			}

			// write success log
			StringBuilder bldr = new StringBuilder();
			bldr.append( "CaseFolder=\"" ).append( rptData.CaseName ).append( "\" ");
			if ( BoxSuccess ) {
				bldr.append( "ReportFolder=\"" ).append(parentFolderName).append("\\").append( reportFolderName ).append( "\" " );
				bldr.append( "ReportFileName=\"").append( destFileName ).append(  "\" " );
				if ( tmpCSV != null )
					bldr.append( "CSVFileName=\"").append( destCSVName ).append(  "\" " );
			}
			if ( EmailSuccess )
				bldr.append( "EmailedTo=\"" ).append( BoxUserLogin ).append( "\"" );
			procStatus.updateStatus( 198, "Report Success:" + successMsg );

			if ( LogSuccessSplunk != null )
				SuccessWriter.writeSuccess( procStatus.getLastUpdateTime(), procStatus.getProcessID(), BoxUserID, BoxUserName, BoxUserLogin, bldr.toString() );

		} catch ( DRException dre ) {
			dre.Log( procStatus, ( LogErrorsSplunk != null ? true : false ), respWriter, BoxUserID, BoxUserName, BoxUserLogin );

		} catch ( ExceptionInInitializerError eiie ) {
			// this is a wrapper around the DR Exception
			DRException dre = (DRException) eiie.getCause();
			dre.Log( procStatus, ( LogErrorsSplunk != null ? true : false ), respWriter, BoxUserID, BoxUserName, BoxUserLogin );

		// catch-all
		} catch ( Throwable catchall ) {

			DRException drx = new DRException( 500, "DiscoveryReportGenerator", "Unmanaged Exception" );
			drx.initCause( catchall );
			drx.setResponseMessage( "Unable to Create Discovery Report. Please contact your Systems Manager and provide error code 500 for more information." );

			drx.Log( procStatus, ( LogErrorsSplunk != null ? true : false ), respWriter, BoxUserID, BoxUserName, BoxUserLogin );

		} finally {
			procStatus.updateStatus( 199, "Report Ended" );

			//- System.out.println( "*** End Run");
			respWriter.flush();
			if ( DELETEFILES ) {
				if ( tmpRpt != null )
					tmpRpt.delete();
				if ( tmpCSV != null )
					tmpCSV.delete();
			}
		}

	}

	private String GetReportFolderName( String BoxUserName, String BoxUserLogin ) {

		if ( BoxReportFolderNameStyle == enumBoxReportFolderSuffix.NONE )
			return ( REPORTFOLDERBASENAME );

		String reportFolderName = REPORTFOLDERBASENAME;

		int lastoffset = BoxUserName.lastIndexOf( ' ' );
		String lastName = BoxUserName;
		String firstName = " ";
		String x = "";
		if ( lastoffset > 0 ) {
			lastName = BoxUserName.substring( lastoffset ).trim();
			firstName = BoxUserName.substring( 0, lastoffset-1).trim();
			if ( firstName.length() == 0 )
				firstName = " ";
		}
		switch ( BoxReportFolderNameStyle ) {
			case LAST:
				x = lastName;
				break;
			case LASTF:
				x = lastName + " " + firstName.substring( 0, 1 );
				break;
			case LASTFIRST:
				x = lastName + ", " + firstName;
				break;
			case LOGIN:
				x = BoxUserLogin;
				int offset = x.indexOf( '@' );
				if ( offset >= 0 )
					x = x.substring( 0, offset );
				break;
		}
		x = x.trim();
		if ( x.length() > 0 )
			reportFolderName += " - " + x;

		return reportFolderName;
	}

	private boolean IsAuthorizedToReport( String pRequestorEmail ) {
		boolean OKtoReport = false;

		for ( String test : AllowedAddressSuffixes ) {
			if ( pRequestorEmail.endsWith(test) ) {
				OKtoReport = true;
				break;
			}
		}
		return ( OKtoReport );
	}

	String getDayOfMonthSuffix( Date pDate ) {

		// get day of month
		Calendar cal = Calendar.getInstance();
		cal.setTime( pDate );
		int n =  cal.get(Calendar.DAY_OF_MONTH);

		// special case
		if (n >= 11 && n <= 13)
		    return "th";

		switch (n % 10) {
			case 1:
				return "st";
			case 2:
				return "nd";
			case 3:
				return "rd";
		}

		return "th";

	}

	private String filterForBoxDisplay( String pMsg ) {
		String s = pMsg.replaceAll( "\\p{Pd}", "-" );		// special regex for dashes "Unicode Dash punctuation" property
		// reserved for more filters later...
		return ( s );
	}



	/*
	private String ExtractAndFormatName( String pIn ) {

		String wrk1 = pIn.replace( '.', ' ' );			// get rid of the dot in the email address
		if ( wrk1.equals(  pIn )) {
			// no change, can't be a property formatted email
			return null;
		}

		String[] wrk2 = wrk1.split( " " );
		StringBuilder wrk3 = new StringBuilder();
		for ( String w : wrk2 ) {
			wrk3.append( Character.toUpperCase( w.charAt( 0 )));
			wrk3.append( w.substring( 1 ));
			wrk3.append( ' ' );
		}

		String wrk4 = wrk3.toString().trim();

		return wrk4;
	}
	*/
}
