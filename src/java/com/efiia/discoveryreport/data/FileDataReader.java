/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport.data;

import com.efiia.discoveryreport.DRException;
import com.efiia.discoveryreport.enumFileType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class FileDataReader {

	private final static boolean DEBUG = false;

	private int FileID;				// primary key (integer)
	private String Type;			// file
	private String BoxFileID;				// box file id
	private String BoxFileVersionID;
	private String Name;			// box file name
	private String SHA;
	private String Size;
	private String FolderID;
	private EnumStatus Status;
	private String Note;

	private enumFileType FileType;

	private static final DataBase dbFile;
	static {
		try {
			dbFile = new DataBase( "select FileID, BoxFileID, BoxFileVersionID, Name, SHA, Type, FileSize, BoxFolderID, Status, Note from Files where BoxFolderID=? order by Name, BoxFileID, BoxFileVersionID" );
		} catch ( DRException ex ) {
			throw new ExceptionInInitializerError( ex );
		}
	}

	public static ArrayList<FileDataReader> getFilesForParent( String pBoxFolderID ) throws DRException {

		ArrayList<FileDataReader> files = new ArrayList<>();

		try {

			PreparedStatement s = dbFile.getSelectStatement();
			s.setString( 1, pBoxFolderID );

			ResultSet rec = s.executeQuery();
			while ( rec.next() )
				files.add( new FileDataReader( rec ));

		} catch ( SQLException ex ) {
			throw new DRException( 2200, "FileDataReader:getFilesForParent", ex);
		}

		return ( files );

	}

	public FileDataReader( ResultSet pRec ) throws DRException {

		try {
			FileID = pRec.getInt( "FileID" );
			BoxFileID = pRec.getString( "BoxFileID" );
			BoxFileVersionID = pRec.getString( "BoxFileVersionID" );

			Name = pRec.getString( "Name" );
			int off = Name.lastIndexOf( "." );
			if ( off == -1 )
				FileType = enumFileType.UNKNOWN;
			else
				FileType = enumFileType.getValueOf( Name.substring( off+1 ));
			SHA = pRec.getString( "SHA" );
			Type = pRec.getString( "Type" );
			Size = pRec.getString( "FileSize" );
			FolderID = pRec.getString( "BoxFolderID" );
			Status = EnumStatus.valueOf( pRec.getString( "Status" ));
			Note = pRec.getString( "Note" );

		} catch ( SQLException ex ) {
			throw new DRException( 2201, "FileDataReader:new", ex);
		}

	}

	@Override
	public String toString() {
		return String.format( "Type: %s; Name: %s[%s]", Type, Name, BoxFileID );
	}

	public int getFileID() { return FileID; }
	public String getType() { return Type; }
	public String getBoxFileID() {  return BoxFileID; }
	public String getBoxFileVersionID() { return BoxFileVersionID; }
	public String getName() { return Name; }
	public enumFileType getFileType() { return FileType; }
	public String getSHA() { return SHA; }
	public String getSize() { return Size; }
	public String getFolderID() { return FolderID; }
	public EnumStatus getFileStatus() { return Status; }
	public String getNote() { return Note; }

}

