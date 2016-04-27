/* added debugging info item and removed automatic reporting in debug mode
 * Feb 2016 - LEN
 */
package com.efiia.discoveryreport;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 *
 * @author larry
 */
public final class DRException extends Exception {

	int ErrorCode;
	int SubErrorCode;
	String Module;
	String Info;
	String Extra[];
	String Debug[];
	String OriginalErrorClassName;

	String ResponseMessage;

	private boolean addStandardSuffix;			// autmatically add extra bits about contact whomever to displayed error message

	private String formatForSplunk = null;

	public DRException( int pErrorCode, String pModule, Exception pEx ) {
		this( pErrorCode, pModule, 0, pEx.getMessage(), null );
		initCause( pEx );
		// if ( DEBUG ) print();
	}

	public DRException( int pErrorCode, String pModule, String pMessage, Exception pEx ) {
		this( pErrorCode, pModule, 0, pMessage, pEx.getMessage() );
		initCause( pEx );
		if ( pEx instanceof SQLException ) {
			// add special interesting stuff from the SQL Exception
			SQLException sPX = (SQLException) pEx;
			Extra = new String[] { "SQL Error Code=" + sPX.getErrorCode(), "SQL State=" + sPX.getSQLState() };
		}
		// if ( DEBUG ) print();
	}

	// added for error situations that are not exceptions
	public DRException( int pErrorCode, String pModule, String pMessage, Throwable pErr ) {
		this( pErrorCode, pModule, 0, pMessage, pErr.getMessage() );
		initCause( pErr );
	}

	public DRException( int pErrorCode, String pModule, String pMessage ) {
		this( pErrorCode, pModule, 0, pMessage, null );
		// if ( DEBUG ) print();
	}

	public DRException( int pErrorCode, String pModule, String pMessage, String pInfo ) {
		this( pErrorCode, pModule, 0, pMessage, pInfo );
		// if ( DEBUG ) print();
	}

	public DRException( int pErrorCode, String pModule, int pSubErrorCode, String pMessage, String pInfo ) {
		super( pMessage );
		ErrorCode = pErrorCode;
		SubErrorCode = pSubErrorCode;
		Module = pModule;
		Info = pInfo;
		ResponseMessage = "Unable to Create Discovery Report.";
		Extra = null;
		OriginalErrorClassName = null;
		addStandardSuffix = true;
	}

	public void setResponseMessage( String pResponse ) {
		ResponseMessage = pResponse;
	}

	public String getResponseMessage() {
		return ( ResponseMessage );
	}

	public void setExtra( String[] pExtra ) {
		Extra = pExtra;
	}

	public String[] getExtra() {
		return ( Extra );
	}

	public void setDebug( String[] pDebug ) {
		Debug = pDebug;
	}

	public void disableStandardSuffix() {
		addStandardSuffix = false;
	}

	public void setSourceClassName( String pName ) {
		OriginalErrorClassName = pName;
	}

	public void sysPrint() {

		System.out.print( "--- Discovery Report Error in Module: " );
		System.out.print( Module );
		System.out.print( ", Error ID: ");
		System.out.println( ErrorCode );

		if ( SubErrorCode > 0 ) {
			System.out.print( " - Response Error: ");
			System.out.println(  SubErrorCode );
		}

		System.out.print( " - Message: " );
		System.out.println( getMessage() );

		if ( Info != null ) {
			System.out.print( "     Info: " );
			System.out.println( Info );
		}

		if ( Extra != null ) {
			String xh = "    Extra: ";
			for ( String x : Extra ) {
				System.out.print( xh );
				System.out.println( x );
				xh = "         : ";
			}
		}

		if ( Debug != null ) {
			String dbg = "     Debug: ";
			for ( String x : Debug ) {
				System.out.print( dbg );
				System.out.println( x );
				dbg = "         : ";
			}
		}

		if ( ResponseMessage != null ) {
			System.out.print( " - Response Msg: ");
			System.out.println( ResponseMessage );
		}

		Throwable cause = getCause();
		if ( cause != null ) {
			String msg = null;
			Throwable subcause = cause;
			do {
				String xmsg = subcause.getMessage();
				if ( xmsg != null ) {
					msg = ( OriginalErrorClassName != null ? OriginalErrorClassName : subcause.toString() ) + ": " + xmsg;
					break;
				}
				subcause = subcause.getCause();
			} while ( subcause != null );

			if ( msg != null )
				System.out.println( " - Original Cause: " + msg );

			boolean SeenFlag = false;
			for( StackTraceElement ste : cause.getStackTrace() ) {

				if ( ste.getClassName().startsWith( "com.efiia.discoveryreport" ))
					SeenFlag = true;
				else if ( SeenFlag )
					break;
				System.out.println( ste.toString() );
			}
		}

		ErrorWriter.writeException( LocalDateTime.now(), Module, "-", "-", "-", getSplunkText() );

	}

	public void Log( ProcessWriter pStatus, boolean pSplunkErrors, PrintWriter pRespWriter, String pUserID, String pUserName, String pUserLogin ) {

		pStatus.writeException( this );
		//-dre.print();

		if ( pSplunkErrors )
			ErrorWriter.writeException( pStatus.getLastUpdateTime(), pStatus.getProcessID(), pUserID, pUserName, pUserLogin, getSplunkText() );

		String xMsg = this.getResponseMessage();
		if ( xMsg == null ) {
			xMsg = "Unable to Create Discovery Report: " + this.getMessage();
			if ( this.Info != null )
				xMsg += " [" + this.Info +"]";
		}

		// add new error txt to end of message for Diane
		// March 2016
		if ( addStandardSuffix )
			xMsg += " Please contact your Systems Manager and provide error number " + this.ErrorCode + " for more information.";

		pRespWriter.print( xMsg );

	}

	public synchronized String getSplunkText() {
		if ( formatForSplunk == null )
			formatForSplunk = formatExcecptionForSlplunk();
		return ( formatForSplunk );
	}

	private String formatExcecptionForSlplunk() {

		StringBuilder bldr = new StringBuilder();

		bldr.append( "ErrorCode=").append( ErrorCode );

		if ( SubErrorCode > 0 ) {
			bldr.append( " SubError=").append( SubErrorCode);
		}
		bldr.append( " Module=\"" ).append( Module ).append( "\"");
		bldr.append( " Message=\"").append( getMessage() ).append(  "\"" );
		if ( Info != null ) {
			bldr.append( " Info=\"" ).append(  Info ).append( "\"" );
		}

		if ( Extra != null ) {
			for ( String x : Extra ) {
				bldr.append( " " ).append( x );
			}
		}

		if ( Debug != null ) {
			for ( String x : Debug ) {
				bldr.append( " " ).append( x );
			}
		}

		Throwable cause = getCause();
		if ( cause != null ) {

			String cmsg = null;
			Throwable subcause = cause;
			do {
				String xmsg = subcause.getMessage();
				if ( xmsg != null ) {
					cmsg = ( OriginalErrorClassName != null ? OriginalErrorClassName : subcause.toString() ) + ": " + xmsg;
					break;
				}
				subcause = subcause.getCause();
			} while ( subcause != null );

			if ( cmsg == null )
				bldr.append( " ErrorCause=\"" + cause.getClass().getName() + "\"");
			else if ( !cmsg.equals(  getMessage() ))		// some exceptions don't have a message
				bldr.append( " ErrorCause=\"" + cause.getClass().getName() + ": " + cmsg + "\"");

			// convert to strings
			StringBuilder stbldr = new StringBuilder();
			String xClass;
			Boolean xTest;
			int ctr = 0;
			final String Me = "com.efiia.discoveryreport";
			for ( StackTraceElement ste : cause.getStackTrace() ) {
				// only go as deep as the third call to com.efiia.discoveryreport or once that appears, and goes elsewhere
				xClass = ste.getClassName();
				xTest = xClass.startsWith( Me );
				if ( xTest )
					ctr++;
				stbldr.append( ",\"" ).append( ste.toString() ).append( "\"");
				//if ( ctr >= 3 )
				//	break;
				//else
				if ( ctr > 0 && !xTest )
					break;
			}
			stbldr.deleteCharAt( 0 );
			bldr.append( " StackTrace=[").append( stbldr ).append( "]");
		}

		return ( bldr.toString() );
	}
}
