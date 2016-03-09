/*
 * ReportWriter.java
 * December 2014 - LEN
 */
package com.efiia.discoveryreport;
import com.efiia.discoveryreport.data.EnumStatus;
import com.efiia.discoveryreport.data.ReportData;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Stack;

/**
 * HTMLReportWriter
 * @author larry
 */
public class HTMLReportWriter {

	private PrintWriter bwOut = null;
	private String StyleSheet = null;
	private String MediaHome = null;

	SimpleDateFormat sdf = new SimpleDateFormat( "MM-dd-yyyy" );
	SimpleDateFormat stf = new SimpleDateFormat( "hh:mm a" );

	/* future allow for inline or web mode */

	public HTMLReportWriter( PrintWriter pWriter, String pStyle, String pMediaHome ) /* throws IOException */ {
		bwOut = pWriter;
		StyleSheet = pStyle;
		MediaHome = pMediaHome;
	}

	public void open() /* throws IOException */ {

		// start document
		bwOut.write( "<!DOCTYPE html>" ); bwOut.println();
		bwOut.write( "<html>"); bwOut.println();

	}


	public void close() /* throws IOException */ {

		if ( bwOut == null )
			return;

		// end document
		bwOut.write( "</html>");
		bwOut.println();

		bwOut.close();
		bwOut = null;
	}


	public void WriteHeader( String pTitle ) /* throws IOException */ {
		bwOut.write( "<head>" ); bwOut.println();
		bwOut.write( "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"); bwOut.println();
		bwOut.write( "<title>" ); bwOut.write( pTitle ); bwOut.write( "</title>" ); bwOut.println();
		bwOut.write( "<link href=\"" ); bwOut.write( StyleSheet ); bwOut.write( "\" rel=\"stylesheet\" type=\"text/css\" />" ); bwOut.println();
		bwOut.write( "</head>" ); bwOut.println();
	}


	public void WriteBodyStart() /* throws IOException */ {
		bwOut.write( "<body>"); bwOut.println();
		bwOut.write( "<div class=\"container\">"); bwOut.println();
		bwOut.write( "<div class=\"content\">"); bwOut.println();
	}


	public void WriteBodyEnd() /* throws IOException */ {
		bwOut.write( "</div>" ); bwOut.println();
		bwOut.write( "</div>" ); bwOut.println();
		bwOut.write( "</body>" ); bwOut.println();
	}

	private void writeTR( String pCol1, String pCol2 ) /* throws IOException */ {
		bwOut.write( "<tr>" ); bwOut.println();
		bwOut.write( "<td>" ); bwOut.write( pCol1 ); bwOut.write( "</td>" );
		bwOut.write( "<td>" ); bwOut.write( pCol2 ); bwOut.write( "</td>" );
		bwOut.write( "</tr>"); bwOut.println();
	}

	public void WriteReportHeader(String pCaseName, String pDateRange, String pUserName, String pUserEmail ) /* throws IOException */ {
		bwOut.write( "<div class=\"container\" id=\"header\">" ); bwOut.println();
		bwOut.write( "<table width=\"100%\" border=\"0\">" ); bwOut.println();

		//bwOut.write( "<tr><td>1</td><td>2</td><td>3</td></tr>" ); bwOut.println();

		bwOut.write( "<tr>" ); bwOut.println();
		bwOut.write( "<td width=\"150\" rowspan=\"6\"><img src=\"" );
		bwOut.write( MediaHome );
		bwOut.write( "seal-US-Department-Of-Justice-Seal_HiRes.png\" width=\"126\" height=\"126\" alt=\"US Department of Justice Seal\" longdesc=\"http://www.placeholderurlhere.com\" class=\"logo\" /></td>" ); bwOut.println();
//		bwOut.write( "<td colspan=\"2\"></td>" ); bwOut.println();
//		bwOut.write( "</tr>" ); bwOut.println();
//
//		bwOut.write( "<tr>" ); bwOut.println();
		bwOut.write( "<td colspan=\"2\"><span class=\"hdr2\">Discovery Report</span></td>" ); bwOut.println();
		bwOut.write( "</tr>" ); bwOut.println();

		writeTR( "Case Name:", pCaseName );
		writeTR( "Date Range:", pDateRange );
		writeTR( "User:", "<span class=\"nobreak\">" + pUserName + "</span>" + ( pUserEmail != null ? " <span class=\"nobreak\">&lt;" + pUserEmail + "&gt;</span>" : "" ));
		writeTR( "&nbsp;", "&nbsp;");

		bwOut.write( "</table>" ); bwOut.println();
		bwOut.write( "</div>" ); bwOut.println();
	}

	public void WriteFolderInfo(FolderInfo f, boolean isLastFolder ) /* throws IOException */ {

		bwOut.write( "<table width=\"90%\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" class=\"red\">" );
		//bwOut.write( "<table width=\"90%\" border=\"1\" align=\"center\" cellpadding=\"1\" cellspacing=\"1\" class=\"red\">" );

		bwOut.write( "<tr>" );bwOut.println();
		bwOut.write( "<th height=\"30\" colspan=\"5\" align=\"left\" class=\"tblhdr\" scope=\"col\">&nbsp;&gt; <strong>" );
        // rewind to top parent

        Stack<FolderInfo> rewinder = new Stack<>();
		FolderInfo p = f.getParent();
        while ( p != null ) {
            rewinder.push( p );
            p = p.getParent();
        }
        while ( !rewinder.empty() ) {
            p = rewinder.pop();
            bwOut.write( p.getName() );
            bwOut.write( " [Folder ID: " );
            bwOut.write( p.getID() );
            bwOut.write( "] &gt; ");
        }
        // now the current folder
		bwOut.write( f.getName() );
		bwOut.write( " [Folder ID: " );
		bwOut.write( f.getID() );
		bwOut.write( "]</strong>" );
		// added 22 May 2015 - folder notes
		// don't add - redundant?
//		if ( f.getNote() != null ) {
//			bwOut.write( "<br/>" );
//			bwOut.write( "<span class=\"foldernote\">Folder Notes:");
//			bwOut.write( f.getNote() );
//			bwOut.write( "</span>");
//			bwOut.println();
//		}
        bwOut.write( "</th>"); bwOut.println();
        bwOut.write( "</tr>"); bwOut.println();

		// added 22 May 2015 - folder events and notes

		// spacer
		int ceiling = f.getEvents().length;
		if ( ceiling > 0 ) {
			bwOut.write( "<tr><td height=\"30\" colspan=\"5\">Folder Events:</td></tr>" ); bwOut.println();

			int ctr = 0;
			for( FileEvent e : f.getEvents() ) {
				boolean tblbtm = ( ++ctr == ceiling );
				writeFolderEvent( e, f.getStatus(), tblbtm, "tblbtm" );
			}
		}
		// end add 22 May 2015

        FileInfo[] files = f.getFiles();
		switch( files.length ) {
			case 0:
	            WriteNoFileInfo();
				break;

			case 1:
                WriteFileInfo( files[0], false, isLastFolder );
				break;

			default:
				int fCtr = 0;
				FileInfo fiCur = files[fCtr++];
				FileInfo fiNext = files[fCtr++];
				while ( fiCur != null ) {
					boolean hasNewVersion = ( fiNext != null && fiCur.getBoxFileID().equals( fiNext.getBoxFileID() ) );
	                WriteFileInfo( fiCur, hasNewVersion, isLastFolder || (fCtr == files.length ? false : true ) );
					fiCur = fiNext;
					fiNext = fCtr < files.length ? files[fCtr++] : null;
				}

		}
		//bwOut.write( "<tr><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td></tr>");

		bwOut.write( "</table>" ); bwOut.println();
		bwOut.write( "<p>&nbsp;</p>" );

		int lCtr = 0;
		FolderInfo[] fldrs = f.getFolders();
		for ( FolderInfo l : fldrs ) {
			WriteFolderInfo( l, isLastFolder || (++lCtr == fldrs.length ? false : true ) );
		}

	}

    public void WriteNoFileInfo() /* throws IOException */ {

        bwOut.write( "<tr>" ); bwOut.println();

		bwOut.write( "<td width=\"49\" rowspan=\"2\">&nbsp;</td>" ); bwOut.println();

		bwOut.write( "<td height=\"19\" colspan=\"3\" valign=\"bottom\" class=\"h3\">" );
		bwOut.write( "[No Files in Folder]" );
		bwOut.write( "</td>" ); bwOut.println();

		bwOut.write( "<td width=\"342\" height=\"19\" align=\"right\" valign=\"bottom\">" );
		bwOut.write( "</td>" ); bwOut.println();

		bwOut.write( "</tr>"); bwOut.println();

    }

	/* from http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java */
	private String humanReadableByteCount( String pSizeStr ) {
		if ( pSizeStr.equals( "-" ))
			return pSizeStr;
		long bytes = Long.valueOf( pSizeStr );
		int unit = 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		char pre = ( "KMGTPE" ).charAt(exp-1);
		return String.format("%.1f %cB", bytes / Math.pow(unit, exp), pre);
	}

	public void WriteFileInfo( FileInfo i, boolean hasMore, boolean isLastFile ) /* throws IOException */ {

		// set up for dotted line at bottom of replaced files, unless they are a boxnote
		String bottomClass = ( hasMore && i.getType() != enumFileType.BOXNOTE ? "tblbtmdash" : "tblbtm");

		// file header
		bwOut.write( "<tr>" ); bwOut.println();
		bwOut.write( "<td width=\"49\" rowspan=\"" );
		bwOut.write( i.getNote() == null ? "2" : "3" );
		bwOut.write( "\">" );
		bwOut.write( "<img src=\"" );
		bwOut.write( MediaHome );
		bwOut.write( i.getType().getIconFile( i.getStatus() ));
		bwOut.write( "\" width=\"44\" height=\"44\" class=\"iconDocType\" />" ); bwOut.println();
		bwOut.write( "</td>" ); bwOut.println();

		bwOut.write( "<td height=\"19\" colspan=\"3\" valign=\"bottom\" class=\"h3\">" );
		bwOut.write( i.getName() );
		if ( i.getStatus() != EnumStatus.Active ) {
			bwOut.write(  " [");
			bwOut.write( i.getStatus().name() );
			bwOut.write( "]");
		}
		bwOut.write( "</td>" ); bwOut.println();

		bwOut.write( "<td width=\"342\" height=\"19\" align=\"right\" valign=\"bottom\">" );
		bwOut.write( "File ID: " );
		bwOut.write( i.getBoxFileID() );
		bwOut.write( "</td>" ); bwOut.println();

		bwOut.write( "</tr>"); bwOut.println();

		bwOut.write( "<tr>" );
		bwOut.write( "<td colspan=\"2\" valign=\"bottom\">" );
		bwOut.write( humanReadableByteCount( i.getSize() ));
		bwOut.write( "&nbsp;&nbsp;&nbsp;<img src=\"");
		bwOut.write( MediaHome );
		bwOut.write( i.getIconAccesses() );
		bwOut.write( "\" width=\"17\" height=\"14\\\" class=\"icons\" />" );
		bwOut.write( String.valueOf( i.getNumberOfAccesses() ));
		bwOut.write( "</td>"); bwOut.println();

		//bwOut.write( "<td width=\"158\" valign=\"bottom\">&nbsp;</td>" );
        bwOut.write( "<td colspan=\"2\"><div align=\"right\">SHA1: " );
		bwOut.write( i.getSHA() );
		bwOut.write( "</div></td>" );
		bwOut.write( "</tr>" ); bwOut.println();

		if ( i.getNote() != null ) {
			bwOut.write( "<tr>" );
			bwOut.write( "<td class=\"filenote\" colspan=\"3\">" );
			bwOut.write( i.getNote() );
			bwOut.write( "</td>"); bwOut.println();
		}

		// spacer
		int ceiling = i.getEvents().length;
		if ( ceiling == 0 ) {
			// no events
			bwOut.write( "<tr><td>&nbsp;</td><td height=\"30\" colspan=\"4\" class=\"" );
			bwOut.write( bottomClass );
			bwOut.write( "\">&nbsp;</td></tr>" ); bwOut.println();
			return;
		}
		bwOut.write( "<tr><td height=\"30\" colspan=\"5\">&nbsp;</td></tr>" ); bwOut.println();

        //SimpleDateFormat sdf = new SimpleDateFormat( "MM-dd-yyyy" );
        //SimpleDateFormat stf = new SimpleDateFormat( "hh:mm a" );

		int ctr = 0;
		for( FileEvent e : i.getEvents() ) {
			ctr++;
			boolean tblbtm = ( isLastFile && ctr == ceiling );
			writeFileEvent( e, i.getStatus(), tblbtm, bottomClass );
		}

	}

	public void writeFolderEvent( FileEvent pEvent, EnumStatus pStatus, boolean pTblBtm, String pBtmClassName ) {
		_writeEvent( pEvent, pStatus, ( pTblBtm ? pBtmClassName : null ), true);
	}

	public void writeFileEvent( FileEvent pEvent, EnumStatus pStatus, boolean pTblBtm, String pBtmClassName ) {
		_writeEvent( pEvent, pStatus, ( pTblBtm ? pBtmClassName : null ), false);
	}

	private void _writeEvent( FileEvent pEvent, EnumStatus pStatus, String pTblBtmClass, boolean pFirstCell ) {

			bwOut.write( "<tr valign=\"top\">" ); bwOut.println();
			bwOut.write( "<td" );
			if ( pFirstCell && pTblBtmClass != null ) {
				bwOut.write( " class=\"" );
				bwOut.write( pTblBtmClass );
				bwOut.write( "\"" );
			}
			bwOut.write( ">&nbsp;</td>" );
			bwOut.write( "<td class=\"eventicon" );
			if ( pTblBtmClass != null ) {
				bwOut.write( " " );
				bwOut.write( pTblBtmClass );
			}
			bwOut.write( "\">" );
			bwOut.write( "<img src=\"" );
			bwOut.write( MediaHome );
			bwOut.write( pEvent.getType().getIconFile( pStatus ) );
			bwOut.write( "\"/></td>" ); bwOut.println();
			bwOut.write( "<td class=\"eventname" );
			if ( pTblBtmClass != null ) {
				bwOut.write( " " );
				bwOut.write( pTblBtmClass );
			}
			bwOut.write( "\">" );
			bwOut.write( pEvent.getActor() );
			bwOut.write( "</td>" );
			bwOut.write( "<td class=\"eventtype" );
			if ( pTblBtmClass != null ) {
				bwOut.write( " " );
				bwOut.write( pTblBtmClass );
			}
			bwOut.write( "\">" );
			bwOut.write( pEvent.getType().getLabel() );
			if ( pEvent.getNote() != null ) {
				bwOut.write( "<br/>" );
				bwOut.write( "<span class=\"filenote\">");
				bwOut.write( pEvent.getNote() );
				bwOut.write(  "</span>");
			}
			bwOut.write( "</td>" );
			bwOut.write( "<td align=\"right\" class=\"eventmeta" );
			if ( pTblBtmClass != null ) {
				bwOut.write( " " );
				bwOut.write( pTblBtmClass );
			}
			bwOut.write( "\">" );
			/*
			 * byte count removed before final version
			 * 19-Jan-2014 LEN
			bwOut.write( humanReadableByteCount( e.getEventSize() ));
			bwOut.write( " &#8226; " );
			 */
			/* IP Removed
			bwOut.write( e.getIP() );
			bwOut.write( " &#8226; " );
			*/
			bwOut.write( sdf.format( pEvent.getDateTime() ));			// date
			bwOut.write( " &#8226; ");
			bwOut.write( stf.format( pEvent.getDateTime() ));			// time
			bwOut.write( "</td>" ); bwOut.println();
			bwOut.write( "</tr>" ); bwOut.println();

	}

	public void WriteReport(ReportData pData ) /* throws IOException */ {

		String rptDate = new SimpleDateFormat( "yyyy-mm-dd").format( pData.ReportDate );

		open();

		WriteHeader( rptDate + " Discovery Report: " + pData.CaseName );

		WriteBodyStart();

		WriteReportHeader( pData.CaseName, pData.getDateRange(), pData.Requestor.getFullName(), pData.Requestor.geteMail() );

		// loop through the folders
		int fCtr = 0;
//		int ceiling = pData.Folders.size();
//		for ( FolderInfo f : pData.Folders )
//			WriteFolderInfo( f, ( ++fCtr == ceiling ? true : false ) );
		WriteFolderInfo( pData.RootFolder, true );

		WriteBodyEnd();

		close();
	}

}
