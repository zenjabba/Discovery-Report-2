package com.efiia.discoveryreport.data;

import com.efiia.discoveryreport.DRException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class FolderDataReader {

	int FolderID;
	String BoxFolderID;
	String FolderName;
	String BoxParentFolderID;
	String Note;
	EnumStatus Status;

	/* database stuff */
	private final static DataBase dbFolder;
	static {
		try {
			dbFolder = new DataBase( null,
								new String[] {
									"select FolderID, BoxFolderID, FolderName, BoxParentFolderID, Note, Status from Folders where BoxFolderID=?",
									"select FolderID, BoxFolderID, FolderName, BoxParentFolderID, Note, Status from Folders where BoxParentFolderID=? order by FolderName"
								},
								null );
		} catch ( DRException ex ) {
			throw new ExceptionInInitializerError( ex );
		}
	}

	public static FolderDataReader getFolderByID( String pBoxFolderID ) throws DRException {

		FolderDataReader retFDR = null;

		try {
			PreparedStatement s = dbFolder.getSelectStatement(0);
			s.setString( 1, pBoxFolderID );
			ResultSet rec = s.executeQuery();

			if ( rec.next() ) {
				retFDR = new FolderDataReader( rec );
			}

		} catch ( SQLException ex ) {
			throw new DRException( 2300, "FolderDataReader:getFolderByID", "FolderID=" + pBoxFolderID, ex );
		}

		return ( retFDR );
	}

	public static ArrayList<FolderDataReader> getFoldersForParent( String pBoxFolderID ) throws DRException {

		ArrayList<FolderDataReader> folders = new ArrayList<>();

		try {

			PreparedStatement s = dbFolder.getSelectStatement(1);
			s.setString( 1, pBoxFolderID );

			ResultSet rec = s.executeQuery();
			while ( rec.next() )
				folders.add( new FolderDataReader( rec ));

		} catch ( SQLException ex ) {
			throw new DRException( 2301, "FolderDataReader:getFoldersForParent", "FolderID=" + pBoxFolderID, ex);
		}

		return ( folders );

	}

	private FolderDataReader( ResultSet pRec ) throws DRException  {
		try {
			FolderID = pRec.getInt( "FolderID" );
			BoxFolderID = pRec.getString( "BoxFolderID" );
			FolderName = pRec.getString( "FolderName" );
			BoxParentFolderID = pRec.getString( "BoxParentFolderID" );
			Note = pRec.getString( "Note" );
			Status = EnumStatus.valueOf( pRec.getString( "Status" ));
		} catch ( SQLException ex ) {
			throw new DRException( 2302, "FolderDataReader:new", ex );
		}
	}

	public String getFolderID() { return BoxFolderID; }
	public String getFolderName() { return FolderName; }
	public String getNote() { return Note; }
	public EnumStatus getStatus() { return Status; }

/*
	@Override
	public String toString() {
		return String.format("  FolderData::Folder Name: %s [%s]\n", FolderName, BoxFolderID );
	}
*/
}
