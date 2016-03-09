/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport;

import com.efiia.discoveryreport.data.EnumStatus;

/**
 *
 * @author larry
 */
public enum enumFileEvent {

	UNKNOWN( "(Unknown)", "icon-createdbw.gif", "icon-createdbw.gif" ),
	ITEM_CREATE( "Created", "icon-createdbw.gif", "icon-createdbw.gif" ),
	UPLOAD( "Uploaded", "icon-uploadedbw.gif", "icon-uploadedbw.gif" ),
	COPY( "Copied", "icon-copiedbw.png", "icon-copiedbw.png" ),
	EDIT( "Edit Uploaded", "icon-editedbw.png", "icon-editedbw.png"),
	DOWNLOAD( "Downloaded", "icon-downloadedbw.gif", "icon-downloadedbw.gif" ),
	PREVIEW( "Previewed", "icon-previewedbw.gif", "icon-previewedbw.gif" ),
	RENAME( "Renamed", "icon-previewedbw.gif", "icon-previewedbw.gif" ),
	DELETE( "Deleted", "icon-deletedbw.png", "icon-deletedbw.png" ),
	UNDELETE( "Undeleted", "icon-undeletedbw.png", "icon-undeletedbw.png" ),
	MOVE( "Moved", "icon-previewedbw.gif", "icon-previewedbw.gif" );

	private final String Label;
	private final String IconFile;
	private final String ReplaceIconFile;

	enumFileEvent( String pLabel, String pIconFile, String pRIconFile ) {  this.Label = pLabel; this.IconFile = pIconFile; this.ReplaceIconFile = pRIconFile; }
	public String getLabel() { return this.Label; }
	public String getIconFile( EnumStatus pStatus ) { return ( pStatus == EnumStatus.Active ? this.IconFile : this.ReplaceIconFile ); }

	public static enumFileEvent getValueOf( String pAction ) {

		enumFileEvent retval = UNKNOWN;

		try {
			retval = enumFileEvent.valueOf( pAction.toUpperCase() );

		} catch ( IllegalArgumentException | NullPointerException e ) {
			retval = UNKNOWN;
		}

		return retval;

	}
}
