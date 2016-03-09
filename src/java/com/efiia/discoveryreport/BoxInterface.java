/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/* Revised with enhanced debugging
 * Feb 2016 - LEN
 */
package com.efiia.discoveryreport;

//import static com.efiia.discoveryreport.DiscoveryReport.DEBUG;

import com.efiia.discoveryreport.enumBoxReportFolderSuffix;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxFolder.Permission;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxUser;
import java.io.InputStream;
import java.util.EnumSet;
// import com.eclipsesource.json.JsonObject;
// import com.eclipsesource.json.JsonValue;

/**
 *
 * @author larry
 */
public class BoxInterface {

	private final BoxAPIConnection api;

	private BoxUser curUser = null;

	public BoxInterface( String pDevToken ) throws DRException {

		// create new connection to box
		try {
			api = new BoxAPIConnection( pDevToken );

		} catch ( BoxAPIException e ) {
			throw new DRException( 720, "BoxAPI:DevConnection", e.getResponseCode(), e.getMessage(), e.getResponse() );
		}

	}

	public BoxInterface( String pClientID, String pClientSecret, String pAuthCode ) throws DRException {

		// create new connection to box
		try {
			api = new BoxAPIConnection( pClientID, pClientSecret, pAuthCode );

		} catch ( BoxAPIException e ) {
			DRException drx = new DRException( 721, "BoxAPI:Connection", e.getResponseCode(), e.getMessage(), e.getResponse() );
			// if ( DEBUG )
			drx.setDebug( new String[] { "ClientID=" + pClientID, "ClientSecret=" + pClientSecret, "AuthCode=" + pAuthCode } );
			throw ( drx );
		}
	}

	/**
	 *
	 * @param pBoxUserID User ID for error report only
	 * @param pBoxUserName User Name for error report only
	 * @throws DRException
	 */
	public void GetCurrentUser( String pBoxUserID, String pBoxUserName ) throws DRException {
		try {
			curUser = BoxUser.getCurrentUser( api );
		} catch ( BoxAPIException e ) {
			DRException drx = new DRException( 730, "BoxAPI:getCurrentUser", e.getResponseCode(), e.getMessage(), e.getResponse() );
			// now always in logs, not needed
			//if ( DEBUG )
			// drx.setDebug( new String[] { "UserID=" + pBoxUserID, "UserName=\"" + pBoxUserName + "\"" } );
			throw ( drx );
		}
	}

	public String getCurrentUserName() {
		return ( curUser != null ? curUser.getInfo().getName() : null );
	}

	public String getCurrentUserLogin() {
		return ( curUser != null ? curUser.getInfo().getLogin() : null );
	}

	public boolean UploadReport( String pFolderID, String pReportFolderName, String pReportName, InputStream pReportStream ) throws DRException {

		// todo - get location of error with step info...
		int errNumber = 740;		// generic
		String errModule = "GenericBOXAPI";
		String errDebug[] = null;

		try {
			// swap out pFolderID
			// check for existing 'report' subfolder in parent
			errNumber = 741;
			errModule = "BoxAPI:BoxFolder";
			//if ( DEBUG )
			errDebug = new String[] { "FolderID=" + pFolderID };
			BoxFolder baseFldr = new BoxFolder( api, pFolderID );
			BoxFolder reportFolder = null;

			// walk the folder to see if there is already a report folder
			errNumber = 742;
			errModule = "BoxAPI:BoxFolder.getChildren";
			// permissions on root appear to be null
			// EnumSet<Permission> bfip = bfi.getPermissions();
			// String bfips = bfip.toString();
			errDebug = new String[] { "FolderID=" + baseFldr.getID(), "FolderInfo=" + baseFldr.getInfo().getName() };
			for( BoxItem.Info item : baseFldr.getChildren() ) {
				if ( item.getName().equals( pReportFolderName ) && item instanceof BoxFolder.Info ) {
					BoxFolder.Info bfi = (BoxFolder.Info)item;
					errModule = "BoxAPI:BoxFolder.Info.getResource";
					errNumber = 743;
					// if ( DEBUG )
					errDebug = new String[] { "ItemName=\"" + bfi.getName() + "\"", "ItemID=" + bfi.getID() };
					reportFolder = bfi.getResource();
					break;
				}
			}

			if ( reportFolder == null ) {
				errNumber = 744;
				errModule = "BoxAPI:BoxFolder.createFolder";
				// if ( DEBUG )
				errDebug = new String[] { "FolderName=\"" + pReportFolderName + "\""};
				BoxFolder.Info rFldrI = baseFldr.createFolder( pReportFolderName );

				errNumber = 745;
				errModule = "BoxAPI:BoxFolder.getResource";
				errDebug = null;
				reportFolder = rFldrI.getResource();
			}

			// upload report file to box
			// find the old one first to overwrite
			boolean newFileFlag = true;
			errNumber = 746;
			errModule = "BoxAPI:BoxItem.Info.(Iterate)";
			//if ( DEBUG )
			errDebug = new String[] { "ReportFolderID=" + reportFolder.getID(), "ReportFolderName=\"" + reportFolder.getInfo().getName() + "\"" };
			for ( BoxItem.Info item2 : reportFolder ) {
				if ( item2.getName().equals( pReportName ) && item2 instanceof BoxFile.Info ) {
					BoxFile.Info rFile = (BoxFile.Info)item2;
					errNumber = 747;
					errModule = "BoxAPI:BoxFile.Info.getResource";
					// if ( DEBUG )
					errDebug = new String[] { "ResourceName=\"" + item2.getName() +"\"", "ResourceID=" + item2.getID() };
					BoxFile bFile = rFile.getResource();
					errNumber = 748;
					errModule = "BoxAPI:BoxFile.uploadVersion";
					// same debug info
					bFile.uploadVersion( pReportStream );
					newFileFlag = false;
					break;
				}
			}
			// no way to replace a file with the API?
			if ( newFileFlag ) {
				errNumber = 749;
				errModule = "BoxAPI:BoxFolder.uploadFile";
				//if ( DEBUG )
				errDebug = new String[] { "UploadName=\"" + pReportName + "\""};
				reportFolder.uploadFile( pReportStream, pReportName );
			}

		} catch ( BoxAPIException e ) {
			DRException drx = new DRException( errNumber, errModule, e.getResponseCode(), e.getMessage(), e.getResponse() );
			// if ( DEBUG && errDebug != null ) drx.setDebug( errDebug );
			if ( errDebug != null ) drx.setDebug( errDebug );
			throw ( drx );
		}
		catch ( Exception e ) {
			DRException drx = new DRException( errNumber, errModule, e );
			// if ( DEBUG && errDebug != null ) drx.setDebug( errDebug );
			if ( errDebug != null ) drx.setDebug( errDebug );
			throw ( drx );
		}
		catch ( Throwable t ) {
			DRException drx = new DRException( errNumber, errModule, t.getMessage() );
			// if ( DEBUG && errDebug != null ) drx.setDebug( errDebug );
			if ( errDebug != null ) drx.setDebug( errDebug );
			throw ( drx );
		}

		return ( true );
	}

//	public void UploadReport( String pFolderID, InputStream pReportStream ) {
//
//		// create new connection to box
//		BoxAPIConnection api = new BoxAPIConnection( "dcuKj88I4SWZOKSRiSl5cxomqSfdDxwG" );
//
//		// check for existing 'report' subfolder in parent
//		BoxFolder baseFldr = new BoxFolder( api, pFolderID );
//		BoxFolder reportFolder = null;
//		try {
//			BoxFolder.Info rFldrI = baseFldr.createFolder( "report2" );
//			reportFolder = rFldrI.getResource();
//
//		} catch ( BoxAPIException e ) {
//			// thrown if the folder exists?
//			/* {
//				"type":"error",
//				"status":409,
//				"code":"item_name_in_use",
//				"context_info": {
//					"conflicts":[{
//						"type":"folder",
//						"id":"2875414895",
//						"sequence_id":"0",
//						"etag":"0",
//						"name":"report"}]},
//				"help_url":"http:\/\/developers.box.com\/docs\/#errors",
//				"message":"Item with the same name already exists",
//				"request_id":"200608153854a0f588e96d6"
//				}
//			*/
//			if ( e.getResponseCode() == 409 ) {	// conflict - name already in use we hope
//				// need to extract the folder id from the JSON data
//				JsonObject errInfo = JsonObject.readFrom( e.getResponse() );
//				JsonValue errInfo2 = errInfo.get( "context_info" );
//				JsonValue errInfo3 = errInfo.get( "id" );
//			}
//		}
//
//		// upload report file to box
//		BoxFile.Info upBoxFile = reportFolder.uploadFile( pReportStream, "Discovery Report.html" );
//	}

	public boolean CheckForRootFolder( String pFolderID ) throws DRException {

		int errNumber = 750;
		String errModule = "CheckRootFolder";
		String errDebug[] = null;

		boolean isRoot = false;

		try {

			BoxFolder rptFolder = null;
			BoxFolder.Info rptFolderInfo = null;
			BoxFolder.Info rootFolderInfo = null;

			errNumber = 751;
			errModule = "CheckRootFolder:BoxFolder";
			errDebug = new String[] { "FolderID=" + pFolderID };
			rptFolder = new BoxFolder( api, pFolderID );

			errNumber = 752;
			errModule = "CheckRootFolder:getInfo";
			rptFolderInfo = rptFolder.getInfo();

			errNumber = 753;
			errModule = "CheckRootFolder:getParent";
			rootFolderInfo = rptFolderInfo.getParent();

			isRoot = rootFolderInfo.getID().equals( "0" );

		} catch ( BoxAPIException e ) {
			DRException drx = new DRException( errNumber, errModule, e.getResponseCode(), e.getMessage(), e.getResponse() );
			// if ( DEBUG && errDebug != null ) drx.setDebug( errDebug );
			if ( errDebug != null ) drx.setDebug( errDebug );
			throw ( drx );
		}

		return ( isRoot );
	}

	public boolean CheckReportFolderHasFiles( String pFolderID ) throws DRException {

		int errNumber = 760;
		String errModule = "CheckRptFolderSize";
		String errDebug[] = null;

		boolean hasFiles = false;

		try {

			BoxFolder rptFolder = null;
			BoxFolder.Info rptFolderInfo = null;

			errNumber = 761;
			errModule = "CheckRptFolderSize:BoxFolder";
			errDebug = new String[] { "FolderID=" + pFolderID };
			rptFolder = new BoxFolder( api, pFolderID );

			hasFiles = rptFolder.iterator().hasNext();

		} catch ( BoxAPIException e ) {
			DRException drx = new DRException( errNumber, errModule, e.getResponseCode(), e.getMessage(), e.getResponse() );
			// if ( DEBUG && errDebug != null ) drx.setDebug( errDebug );
			if ( errDebug != null ) drx.setDebug( errDebug );
			throw ( drx );
		}

		return ( hasFiles );

	}
}
