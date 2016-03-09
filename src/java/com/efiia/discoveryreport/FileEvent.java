/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author larry
 */
public class FileEvent {

	private final enumFileEvent Type;
	private final String Actor;
	private final String ActorID;
	private final String ActorLogin;
    private final Date DateTime;
	private final String IP;
	private final String Note;

	public FileEvent( enumFileEvent pType, Date pDate, String pActor, String pActorID, String pActorLogin, String pIP, String pNote ) {
		Type = pType;
        DateTime = pDate;
		Actor = pActor;
		ActorID = pActorID;
		ActorLogin = pActorLogin;
		IP = pIP;
		Note = pNote;

        /*
        System.out.println( "Added File Event " + DateTime.toString() );
        */
	}

	public enumFileEvent getType() { return Type; }
	public String getActor() {
		/* kludge for "Someone" */
		if ( Actor.toLowerCase().equals(  "someone" ))
			return "USAFx";
		return Actor;
	}
	public String getActorID() { return ActorID; }
	public String getActorLogin() { return ActorLogin; }
	public Date getDateTime() { return DateTime; }
	public String getIP() { return IP; }
	public String getNote() { return Note; }
}
