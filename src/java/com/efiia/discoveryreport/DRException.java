/* added debugging info item and removed automatic reporting in debug mode
 * Feb 2016 - LEN
 */
package com.efiia.discoveryreport;

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

	String ResponseMessage;

	public DRException( int pErrorCode, String pModule, Exception pEx ) {
		this( pErrorCode, pModule, 0, pEx.getMessage(), null );
		initCause( pEx );
		// if ( DEBUG ) print();
	}

	public DRException( int pErrorCode, String pModule, String pMessage, Exception pEx ) {
		this( pErrorCode, pModule, 0, pMessage, pEx.getMessage() );
		initCause( pEx );
		// if ( DEBUG ) print();
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
		ResponseMessage = null;
		Extra = null;
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
			System.out.print( "Rspon Msg: ");
			System.out.println( ResponseMessage );
		}

		Throwable cause = getCause();
		if ( cause != null ) {
			System.out.println( " - Original Cause:" );
			cause.printStackTrace( System.out );
		}

	}
}
