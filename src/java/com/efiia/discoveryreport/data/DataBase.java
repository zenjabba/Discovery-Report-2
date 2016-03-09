/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport.data;

import com.efiia.discoveryreport.DRException;
import com.efiia.discoveryreport.DiscoveryReport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


// TODO
// 25-Mar-2015 08:54:12.564 WARNING [http-nio-8443-exec-20] org.apache.catalina.loader.WebappClassLoaderBase.clearReferencesJdbc The web application [dr2] registered the JDBC driver [org.postgresql.Driver] but failed to unregister it when the web application was stopped. To prevent a memory leak, the JDBC Driver has been forcibly unregistered.

/**
 *
 * @author larry
 */
public class DataBase {

    public static String url;		// "jdbc:postgresql://localhost:5432/discoveryreport";
	public static String host;		// localhost
	public static String port;		// 5432
	public static String database;	// discoveryreport
    public static String username;	// "postgres";
    public static String password;	// "postgres";

	public static String[] getConfigInfo() {
		return new String[] {
			"URL=" + url,
			"Host=" + host,
			"Port=" + port,
			"Database=" + database,
			"UserName=" + username,
			"Password=" + password
		};
	}


	private static Connection conn = null;

	public static void Config( File pConfigDir ) throws DRException {

		File confFile = new File( pConfigDir, "postgresql.config" );

		if ( !confFile.canRead() )
			throw new DRException( 1001, "Database:Config", String.format( "Config File %s Not Found/Readable", confFile.toString()) );

		// connect to local database
		Properties propsSQL = new Properties();
		try (FileInputStream in = new FileInputStream(confFile )) {
			propsSQL.load( in );

		} catch ( IOException ex ) {
			throw new DRException( 1002, "Database:Config", "Read Property File", ex );
		}

		//url = propsSQL.getProperty( "jdbcpath" );
		host = propsSQL.getProperty( "host" );
		port = propsSQL.getProperty( "port" );
		database = propsSQL.getProperty( "database" );
		username = propsSQL.getProperty( "username" );
		password = propsSQL.getProperty( "password" );

		url = String.format( "jdbc:postgresql://%s:%s/%s", host, port, database );

	}

	public static synchronized void Connect() throws SQLException {
		Reconnect();

		Logger myLogger = Logger.getLogger( "org.apache.catalina.logger.SystemOutLogger");
		myLogger.logp( Level.INFO, DiscoveryReport.APPNAME, DiscoveryReport.APPVER, String.format( "Connected to Database at: %s:%s as user %s", host, database, username ));
	}

	public static synchronized void Reconnect() throws SQLException {

		// test connection
		if ( conn != null )
			disconnect();

        try {
			org.postgresql.Driver driver = new org.postgresql.Driver();
		    DriverManager.registerDriver(driver);
			conn = DriverManager.getConnection(url, username, password);

			if ( conn == null )
				throw new SQLException( "No SQL Connection Made" );

        } catch ( SQLException e) {
            e.printStackTrace();
			throw e;
	    }
    }

	private final static String UNIQUE_CONSTRAINT_VIOLATION = "23505";

	protected static synchronized void disconnect() throws SQLException {

		try {
			if ( conn != null && !conn.isClosed() )
				conn.close();

		} catch ( SQLException e ) {
			e.printStackTrace();
			throw ( e );
		}
	}

	private PreparedStatement stmtInsert;
	private ArrayList<PreparedStatement> stmtSelect;
	private ArrayList<PreparedStatement> stmtUpdate;

	public DataBase( String pSelect ) throws DRException {
		this( null, new String[] { pSelect }, null );
	}

	public DataBase( String pInsert, String[] pSelect ) throws DRException {
		this(pInsert, pSelect, null );
	}

	public DataBase( String pInsert, String[] pSelect, String[] pUpdate ) throws DRException {

		if ( conn == null )
			throw new DRException( 2008, "Database:new", "Connection Missing" );

		try {
			stmtInsert = ( pInsert != null ? conn.prepareStatement( pInsert, Statement.RETURN_GENERATED_KEYS ) : null );
			if ( pSelect == null ) {
				stmtSelect = null;
			} else {
				stmtSelect = new ArrayList<>();
				for ( String s : pSelect ) {
					stmtSelect.add( conn.prepareStatement( s ));
				}
			}
			if ( pUpdate == null ) {
				stmtUpdate = null;
			} else {
				stmtUpdate = new ArrayList<>();
				for ( String s : pUpdate ) {
					stmtUpdate.add( conn.prepareStatement( s ));
				}
			}

		} catch ( SQLException ex ) {
			throw new DRException( 2009, "Database:new", ex );
			//throw new ExceptionInInitializerError( ex );
		}
	}


	public PreparedStatement getInsertStatement() {
		return stmtInsert;
	}

	public PreparedStatement getSelectStatement() {
		return stmtSelect.get(0);
	}

	public PreparedStatement getSelectStatement( int pItem ) {
		return stmtSelect.get( pItem );
	}

	public PreparedStatement getUpdateStatement( int pItem ) {
		return stmtUpdate.get( pItem );
	}

	public boolean isDuplicateKeyException( SQLException pSQX ) {
		return ( pSQX.getSQLState().equals( UNIQUE_CONSTRAINT_VIOLATION ) ? true : false );
	}
}
