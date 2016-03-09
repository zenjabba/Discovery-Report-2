/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport;

import com.efiia.discoveryreport.data.EnumStatus;
import com.efiia.discoveryreport.data.FileDataReader;
import java.util.ArrayList;

/**
 *
 * @author larry
 */
public class FileInfo implements Comparable<FileInfo> {

	private final String ID;
	private final String VersionID;
	private final String Name;
	private final enumFileType Type;
	private final String Size;
	private final String SHA;
	private final EnumStatus Status;
	private final String Note;

	// have to get what these are supposed to be from Steve/Belinda
	private final String IconUnknown1;
	private int NumberOfAccesses;

	private final ArrayList<FileEvent> Events;

	public FileInfo( FileDataReader pFDR ) {
		ID = pFDR.getBoxFileID();
		VersionID = pFDR.getBoxFileVersionID();
		Name = pFDR.getName();
		Type = pFDR.getFileType();
		Size = pFDR.getSize();
		SHA = pFDR.getSHA();
		Status = pFDR.getFileStatus();
		Note = pFDR.getNote();

		IconUnknown1 = "ic-graphbw.gif";
		NumberOfAccesses = 0;
        Events = new ArrayList<FileEvent>();
	}

    public void addEvent( FileEvent pEvent ) {
        Events.add( pEvent );
		NumberOfAccesses++;
    }

	public String getBoxFileID() { return ID; }
	public String getName() { return Name; }
	public enumFileType getType() { return Type; }
	public String getSize() { return Size; }
	public String getSHA() { return SHA; }
	public String getIconAccesses() { return IconUnknown1; }
	public EnumStatus getStatus() { return Status; }
	public String getNote() { return Note; };

	public int getNumberOfAccesses() {	return NumberOfAccesses; }
	public FileEvent[] getEvents() { return Events.toArray( new FileEvent[ Events.size() ]); }

    @Override
    public int compareTo( FileInfo o ) {
        return ( Name.compareToIgnoreCase( o.Name) );
    }

}
