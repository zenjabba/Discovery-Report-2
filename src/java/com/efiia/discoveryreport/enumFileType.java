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
public enum enumFileType {

	UNKNOWN( "generic.png", "generic_g.png" ),
	AIFF( "aiff.png", "aiff_g.png" ),
	DOC( "doc.png", "doc_g.png" ),
	DOCX( "docx.png", "docx_g.png" ),
	HTM( "htm.png", "htm_g.png" ),
	HTML( "html.png", "html_g.png" ),
	KEY( "key.png", "key_g.png" ),
	M4A( "m4a.png", "m4a_g.png" ),
	MP3( "mp3.png", "mp3_g.png" ),
	PDF( "pdf.png", "pdf_g.png" ),
	PPT( "ppt.png", "ppt_g.png" ),
	PPTX( "pptx.png", "pptx_g.png" ),
	TXT( "txt.png", "txt_g.png" ),
	WAV( "wav.png", "wav_g.png" ),
	WMA( "wma.png", "wma_g.png" ),
	XLS( "xls.png", "xls_g.png" ),
	XLSX( "xlsx.png", "xlsx_g.png" ),
	BOXNOTE( "generic.png", "generic_g.png");

	private final String IconFile;
	private final String RIconFile;

	enumFileType( String pIcon, String pRIcon ) { this.IconFile = pIcon; this.RIconFile = pRIcon; }

	public String getIconFile( EnumStatus pStatus ) { return pStatus == EnumStatus.Active ? this.IconFile : this.RIconFile;	}

	public static enumFileType getValueOf( String pType ) {

		enumFileType retval;

		try {
			retval = enumFileType.valueOf( pType.toUpperCase() );
		} catch ( IllegalArgumentException | NullPointerException ignored ) {
			retval = UNKNOWN;
		}

		return retval;
	}
}
