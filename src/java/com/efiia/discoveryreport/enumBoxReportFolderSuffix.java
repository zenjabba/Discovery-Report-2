/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport;

/**
 *
 * @author larry
 */
public enum enumBoxReportFolderSuffix {

	NONE,
	LAST,
	LASTF,
	LASTFIRST,
	LOGIN;

	public static enumBoxReportFolderSuffix getValueOf( String pAction ) {

		enumBoxReportFolderSuffix retval = LAST;

		try {
			retval = enumBoxReportFolderSuffix.valueOf( pAction.toUpperCase() );

		} catch ( IllegalArgumentException | NullPointerException e ) {
			retval = LAST;
		}

		return retval;

	}
}
