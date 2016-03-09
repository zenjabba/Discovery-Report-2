/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport;

import com.efiia.discoveryreport.data.ReportData;
import com.efiia.discoveryreport.DRException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author larry
 */
public class CSVWriter {

	/* CSV Line Item Format
	 *	Full Path – colon or slash delimited
	 *	Full Path ID(s) - colon or slash delimited
	 *	Full File Name
	 *	File ID
	 *	File Size – in bytes
	 *	File Type
	 *	File Version
	 *	File SHA
	 *	Action Type
	 *	Action Date + Time
	 *	Action User Name
	 *	Action User ID
	 *	Action User IP
	 */

	private FileWriter csvOut = null;

	public CSVWriter( FileWriter pWriter ) {
		csvOut = pWriter;
	}

	public void ExportData( ReportData pData ) throws DRException {
		printHeader();
		printFolderInfo( pData.RootFolder, "", "" );
	}

	private void printHeader() throws DRException {
		// column titles
		//csvOut.println( "Path, PathID, FileName, FileID, FileSize, FileType, FileVersionID, FileSHA, EventType, EventDateTime, EventUser, EventUserID, EventIP" );
		try {
			csvOut.write( "\"File Path\",\"File Path ID\",\"File Name\",\"File ID\",\"File Size (Bytes)\",\"File Version ID\",\"SHA-1 Hash\",\"Event Type\",\"Event Date\",\"User Name\",\"User Login\",\"User ID\"\n" );
		} catch ( IOException ex ) {
			throw new DRException( 771, "CSVWriter:PrintHeader", ex );
		}
	}

	public void close() throws DRException {
		try {
			csvOut.flush();
			csvOut.close();
		} catch ( IOException ex ) {
			throw new DRException( 774, "CSVWriter:close", ex );
		}
	}

	// routines to snake through the report data
	private void printFolderInfo( FolderInfo f, String pParentFolder, String pParentID ) throws DRException {

		String Path = pParentFolder + "/" + f.getName();
		String PathID = pParentID + ( pParentID.length() > 0 ? ":" : "" ) + f.getID();

		// added 25-May-2015 - folder events
		int fldEventsCnt = f.getEvents().length;
		if ( fldEventsCnt > 0 ) {
			try {
				// get folder info into strings for convience
				String FolderName = "-";	// f.getName();
				String FolderID = "-";		// f.getID();
				String FolderSize = "-";
				String FolderVersionID = "-";
				String FolderSHA = "-";

				for ( FileEvent e : f.getEvents() ) {
					String EventType = e.getType().name();
					Date EventDate = e.getDateTime();
					String EventUser = e.getActor();
					String EventUserLogin = e.getActorLogin();
					String EventUserID = e.getActorID();
					//String EventIP = e.getIP();

					csvOut.write( String.format( "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%TF %<TT,\"%s\",\"%s\",\"%s\"%n",
								   Path, PathID, FolderName, FolderID, FolderSize, FolderVersionID, FolderSHA, EventType, EventDate, EventUser, EventUserID, EventUserLogin ));
				}
			} catch ( IOException ex ) {
				throw new DRException( 772, "CSVWriter:PrintFolderInfo", ex );
			}
		}

		for ( FileInfo i : f.getFiles() )
			writeFileInfo( i, Path, PathID );

		// recurse down
		for ( FolderInfo j : f.getFolders() )
			printFolderInfo( j, Path, PathID );
	}

	private void writeFileInfo( FileInfo g, String pPath, String pPathID ) throws DRException {

		// File Info
		String FileName = g.getName();
		String FileID = g.getBoxFileID();
		String FileSize = g.getSize();
		//String FileType = g.getType().name();
		String FileVersionID = g.getBoxFileID();			// version ID pending
		String FileSHA = g.getSHA();

		try {
			for ( FileEvent e : g.getEvents() ) {

				String EventType = e.getType().name();
				Date EventDate = e.getDateTime();
				String EventUser = e.getActor();
				String EventUserLogin = e.getActorLogin();
				String EventUserID = e.getActorID();
				//String EventIP = e.getIP();

				csvOut.write( String.format( "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%TF %<TT,\"%s\",\"%s\",\"%s\"%n",
							   pPath, pPathID, FileName, FileID, FileSize, FileVersionID, FileSHA, EventType, EventDate, EventUser, EventUserID, EventUserLogin ));
			}
		} catch ( IOException ex ) {
			throw new DRException( 773, "CSVWriter:WriteFileInfo", ex );
		}
	}
}
