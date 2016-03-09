/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport.data;

import com.efiia.discoveryreport.DRException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author larry
 */
public class EventDataReader {

	private final static boolean DEBUG = false;

	int EventID;			// primary key
	String CreatedAt;
	String BoxEventID;
	String EventType;
	String IPAddress;
	String BoxItemType;
	String BoxItemID;
	String BoxItemVersionID;
	String BoxUserID;
	String Note;

	private static final DataBase db;

	static {
		try {
			db = new DataBase( "select EventID, BoxEventID, Type, IPAddress, DateTime, BoxUserID, BoxItemType, BoxItemID, BoxItemVersionID, Note from Events where BoxItemID=? and BoxItemVersionID=?" );
		} catch( DRException drx ) {
			throw new ExceptionInInitializerError( drx.getCause() );
		}
	}

//	public static ArrayList<EventDataReader> getEventsForFile( String pBoxFileID ) {
//
//		ArrayList<EventDataReader> events = new ArrayList<>();
//
//		try {
//			PreparedStatement s = db.getSelectStatement();
//			s.setString( 1, pBoxFileID );
//
//			ResultSet rec = s.executeQuery();
//			while ( rec.next() )
//				events.add(  new EventDataReader( rec ));
//
//		} catch ( SQLException ex ) {
//			Logger.getLogger( EventDataReader.class.getName() ).log( Level.SEVERE, null, ex );
//		}
//
//		return ( events );
//
//	}

	public static ArrayList<EventDataReader> getEventsForFolder( String pBoxFolderID ) throws DRException {
		return getEventsForFile( pBoxFolderID, "0" );
	}

	public static ArrayList<EventDataReader> getEventsForFile( String pBoxFileID, String pBoxVersionID ) throws DRException {

		ArrayList<EventDataReader> events = new ArrayList<>();

		try {
			PreparedStatement s = db.getSelectStatement();
			s.setString( 1, pBoxFileID );
			s.setString( 2, pBoxVersionID);

			ResultSet rec = s.executeQuery();
			while ( rec.next() )
				events.add(  new EventDataReader( rec ));

		} catch ( SQLException ex ) {
			throw new DRException( 2100, "EventDataReader:getEventsForFile", ex );
		}

		return ( events );

	}

	public EventDataReader( ResultSet pRec ) throws DRException {

		try {
			EventID = pRec.getInt( "EventID" );
			BoxEventID = pRec.getString( "BoxEventID" );
			EventType = pRec.getString( "Type" );
			IPAddress = pRec.getString( "IPAddress" );
			CreatedAt = pRec.getString( "DateTime" );
			BoxUserID = pRec.getString( "BoxUserID" );
			BoxItemType = pRec.getString( "BoxItemType" );
			BoxItemID = pRec.getString( "BoxItemID" );
			BoxItemVersionID = pRec.getString( "BoxItemVersionID" );
			Note = pRec.getString( "Note" );
		} catch ( SQLException ex ) {
			throw new DRException( 2101, "EventDataReader:new", ex);
		}
	}

	@Override
	public String toString() {
		return String.format( "[%s] %s by %s for %s[%s]", BoxEventID, EventType, BoxUserID, BoxItemID, BoxItemType );
	}


}

