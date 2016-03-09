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
public enum enumBoxReportDest {

	BOXNONE,
	BOXROOT,
	BOXCASE;

	public static enumBoxReportDest getValueOf( String pAction ) {

		enumBoxReportDest retval = BOXROOT;

		try {
			retval = enumBoxReportDest.valueOf( pAction.toUpperCase() );

		} catch ( IllegalArgumentException | NullPointerException e ) {
			retval = BOXROOT;
		}

		return retval;

	}
}
