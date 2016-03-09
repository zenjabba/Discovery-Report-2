/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport.data;

import com.efiia.discoveryreport.DRException;

import com.efiia.discoveryreport.FileEvent;
import com.efiia.discoveryreport.FileInfo;
import com.efiia.discoveryreport.FolderInfo;
import com.efiia.discoveryreport.ProcessWriter;
import com.efiia.discoveryreport.UserInfo;
import com.efiia.discoveryreport.enumFileEvent;
import com.efiia.discoveryreport.enumFileType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author larry
 */
public class ReportData {

	public Date ReportDate = null;

	public String CaseName= null;

	public Date StartDate = null;
	public Date EndDate = null;
    private boolean bTrackStart = false;
    private boolean bTrackEnd = false;

	public UserInfo Requestor = null;

	public FolderInfo RootFolder;

	private int FileCount = 0;
	private int FolderCount = 0;
	private static int CapCount = 50000;

	HashMap<String,UserDataReader> UserList;

	// use a single formatter to convert dates here to avoid performance issues
	SimpleDateFormat sdf;

	ProcessWriter procWtr;

	public static void setCapCount( int pSize ) {
		CapCount = pSize;
	}

    public ReportData( ProcessWriter pWtr, UserInfo pRequestor, String pCaseName, Date pCaseStart, Date pCaseEnd ) {

        Requestor = pRequestor;
        CaseName = pCaseName;

        ReportDate = new Date();

        StartDate = ( pCaseStart != null ? pCaseStart : new Date( Long.MAX_VALUE ));
        EndDate = ( pCaseEnd != null ? pCaseEnd : new Date( 0 ) );
        bTrackStart = ( pCaseStart == null ? true : false );
        bTrackEnd = ( pCaseEnd == null ? true : false );

		procWtr = pWtr;
    }
    public boolean hasData() {
		return ( RootFolder != null ? true : false);
    }

	// smart functions
	public String getDateRange() {
        SimpleDateFormat sdf1 = new SimpleDateFormat( "MMMM d, yyy hh:mm aa" );
        SimpleDateFormat sdf2 = new SimpleDateFormat( "MMMM d, yyy hh:mm aa zzz" );
        return String.format( "%s - %s", sdf1.format( StartDate ), sdf2.format( EndDate ));
    }

    private void updateReportDates( Date yEventDateTime ) {
        if ( bTrackStart && yEventDateTime.before( StartDate ))
            StartDate = yEventDateTime;
        if ( bTrackEnd && yEventDateTime.after(  EndDate ))
            EndDate = yEventDateTime;
    }

	public int getFileCount() {
		return FileCount;
	}

	public boolean hasTooManyFiles() {
		return FileCount >= CapCount;
	}

	public int getCapSize() {
		return CapCount;
	}
	
	public void fetchData( String BoxFolderID, ReportData rptData ) throws DRException {

		UserList = new HashMap<>();
		sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssXXX" );

		// set up for root folder
		FolderDataReader Root = FolderDataReader.getFolderByID( BoxFolderID );

		if ( Root == null ) {
			// folder not found
			return;
		}

		CaseName = Root.FolderName;
		RootFolder = fetchFolderData( Root, null );
	}

	private FolderInfo fetchFolderData( FolderDataReader pCurFolder, FolderInfo pParentFI ) throws DRException {

		procWtr.updateStatus( 122, String.format( "Fetching Folder Data for %s[%s]", pCurFolder.FolderName, pCurFolder.BoxFolderID ) );

		// convert current folder into FolderInfo
		//FolderInfo curFolder = new FolderInfo( pCurFolder.getFolderID(), pCurFolder.getFolderName(), pParentFI );
		FolderInfo curFolder = new FolderInfo( pCurFolder, pParentFI );
		FolderCount++;

		// added 22-May-2015
		// get events for Folder
		ArrayList<EventDataReader> folderEvents = EventDataReader.getEventsForFolder( curFolder.getID() );
		for ( EventDataReader edr : folderEvents ) {

			try {
				enumFileEvent yEventType = enumFileEvent.getValueOf( edr.EventType );
				Date yEventDateTime = sdf.parse( edr.CreatedAt );

				// get the user's name
				UserDataReader udr = UserList.get( edr.BoxUserID );
				if ( udr == null ) {
					udr = new UserDataReader( edr.BoxUserID );
					UserList.put(  edr.BoxUserID, udr );
				}

				FileEvent fe = new FileEvent( yEventType, yEventDateTime, udr.Name, udr.BoxUserID, udr.Login, edr.IPAddress, edr.Note );
				curFolder.addEvent( fe );

				updateReportDates( yEventDateTime );

			} catch ( ParseException ex ) {
				DRException drx = new DRException( 2500, "ReportData:fetchFolderData(1)", ex );
				drx.setDebug( new String[] { "FolderID=" + pCurFolder.BoxFolderID, "ParentID=" + pParentFI.getID() } );
				throw ( drx );
			}

		}

		// add files to current folder
		ArrayList<FileDataReader> files = FileDataReader.getFilesForParent( pCurFolder.getFolderID() );
		String lastNoteID = "*";

		for ( FileDataReader ldr : files ) {

			FileInfo fInfo = new FileInfo( ldr );
			FileCount++;

			if ( fInfo.getType() == enumFileType.BOXNOTE ) {
				// only add the current note once to the folder for reporting
				if ( fInfo.getBoxFileID().equals( lastNoteID ))
					continue;
				lastNoteID = fInfo.getBoxFileID();

			} else {
				// get events for the file unless its a BOXNOTE
				ArrayList<EventDataReader> fileEvents = EventDataReader.getEventsForFile( ldr.getBoxFileID(), ldr.getBoxFileVersionID() );
				for ( EventDataReader edr : fileEvents ) {

					try {
						enumFileEvent yEventType = enumFileEvent.getValueOf( edr.EventType );
						Date yEventDateTime = sdf.parse( edr.CreatedAt );

						// get the user's name
						UserDataReader udr = UserList.get( edr.BoxUserID );
						if ( udr == null ) {
							udr = new UserDataReader( edr.BoxUserID );
							UserList.put(  edr.BoxUserID, udr );
						}

						FileEvent fe = new FileEvent( yEventType, yEventDateTime, udr.Name, udr.BoxUserID, udr.Login, edr.IPAddress, edr.Note );
						fInfo.addEvent( fe );

						updateReportDates( yEventDateTime );

					} catch ( ParseException ex ) {
						DRException drx = new DRException( 2501, "ReportData:fetchFolderData(2)", ex );
						drx.setDebug( new String[] { "FileID=" + ldr.getBoxFileID(), "EventID=" + edr.BoxEventID } );
						throw ( drx );
					}

				}
			}
			curFolder.addFile( fInfo );
		}

		// now get Folders sub-Folders, and recurse through them
		ArrayList<FolderDataReader> subFolders = FolderDataReader.getFoldersForParent( pCurFolder.getFolderID() );

		for ( FolderDataReader fldr : subFolders ) {
			// add new folders
			//FolderInfo subFldr = new FolderInfo( fldr.getFolderID(), fldr.getFolderName(), pParentFI );
			curFolder.addFolder( fetchFolderData( fldr, curFolder ));
		}

		return ( curFolder );
	}

}
