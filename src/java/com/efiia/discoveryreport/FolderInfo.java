/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport;

import com.efiia.discoveryreport.data.EnumStatus;
import com.efiia.discoveryreport.data.FolderDataReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author larry
 */
public class FolderInfo {

	private final String ID;
	private final String Name;
	private final String Note;
	private final EnumStatus Status;
	private final FolderInfo Parent;

	private final ArrayList<FolderInfo> Folders;
	private final ArrayList<FileInfo> Files;

	// added 22-May-2015
	private final ArrayList<FileEvent> Events;

	// add new constructor to accommate notes
	public FolderInfo( FolderDataReader pFDR, FolderInfo pParent ) {
		ID = pFDR.getFolderID();
		Name = pFDR.getFolderName();
		Note = pFDR.getNote();
		Status = pFDR.getStatus();
		Parent = pParent;

        Folders = new ArrayList<>();
        Files = new ArrayList<>();
		Events = new ArrayList<>();
	}

	/* don't use anymore
    public FolderInfo( String pFolderID, String pFolderName, FolderInfo pParentFolder ) {
        ID = pFolderID;
        Name = pFolderName;
        Parent = pParentFolder;
		Note = null;
		Status = EnumStatus.Active;
        Folders = new ArrayList<>();
        Files = new ArrayList<>();
		Events = new ArrayList<>();

		/*
        System.out.println( "Created New Folder: " + Name + " [" + ID + "]" );
        FolderInfo xparent = Parent;
        while ( xparent != null ) {
            System.out.println( " Child of " + xparent.Name + " [" + xparent.ID + "]");
            xparent = xparent.Parent;
        }
		*//*
    }
	*/

    public void addFolder( FolderInfo pFolder ) {
        Folders.add( pFolder );
    }

    public void addFile( FileInfo pFile ) {
        Files.add( pFile );
    }

	public void addEvent( FileEvent pEvent ) {
		Events.add( pEvent );

	}
	public int getFileCount() {
		return Files.size();
	}

    public void sortFiles() {
        Collections.sort( Files, new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo  f1, FileInfo  f2) { return f1.getName().compareToIgnoreCase( f2.getName() ); }
        });
    }

	public String getID() { return ID; }
	public String getParentID() { return Parent.ID; }
	public FolderInfo getParent() { return Parent; }
	public String getName() { return Name; }
	public String getNote() { return Note; }
	public EnumStatus getStatus() { return Status; }
	public FileInfo[] getFiles() { return Files.toArray( new FileInfo[ Files.size() ]); }
	public FolderInfo[] getFolders() { return Folders.toArray( new FolderInfo[ Folders.size() ]); }

	public FileEvent[] getEvents() { return Events.toArray( new FileEvent[ Events.size() ]); }
}
