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
public class UserInfo {

	private String ID;
	private String FullName;
	private String eMail;

	public UserInfo( String pID ) {
		ID = pID;
		FullName = null;
		eMail = null;

	}

    public UserInfo( String pID, String pFullName, String pEMail ) {
        this(pID);
        FullName = pFullName;
        eMail = pEMail;
    }

//	public void setFullName(String pFullName) { FullName = pFullName; }
//	public void seteMail(String pEMail) { eMail = pEMail; }

	public String getID() {	return ID; }
	public String getFullName() {
		/* kludge for "Someone" */
		if ( FullName.toLowerCase().equals(  "someone" ))
			return "USAFx";
		return FullName;
	}
	public String geteMail() { return eMail; }

}
