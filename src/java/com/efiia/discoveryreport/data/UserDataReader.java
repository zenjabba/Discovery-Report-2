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

/**
 *
 * @author larry
 */
public class UserDataReader {

	int UserID;
	String BoxUserID;
	String Name;
	String Login;

	private static final DataBase db;

	static {
		try {
		db = new DataBase( "select UserID, BoxUserID, Name, Login from Users where BoxUserID=?" );
		} catch (DRException ex) {
			throw new ExceptionInInitializerError( ex );
		}
	}

	public UserDataReader( String pBoxUserID ) throws DRException {

		try {
			PreparedStatement s = db.getSelectStatement();
			s.setString( 1, pBoxUserID );

			ResultSet rec = s.executeQuery();
			if ( rec.next() ) {
				UserID = rec.getInt( "UserID" );
				BoxUserID = rec.getString( "BoxUserID" );
				Name = rec.getString( "Name" );
				Login = rec.getString( "Login" );
			}

		} catch ( SQLException ex ) {
			throw new DRException( 2400, "UserDataReader:new", "UserID=" + pBoxUserID, ex);
		}

	}

	@Override
	public String toString() {
		return String.format( "BoxUserID: %s; Name: %s; Login: %s", BoxUserID, Name, Login );
	}

	public int getUserID() { return UserID; }
	public String getBoxUserID() { return BoxUserID; }
	public String getName() { return Name; }
	public String getLogin() { return Login; }

}
