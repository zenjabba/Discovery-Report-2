/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport.data;

import java.io.IOException;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 * @author larry
 */
public class xFetchData {


/*
    void process( String boxFolderID, ReportData rptData ) throws DRException {

        // from Splunk Java API Examples
        // http://dev.splunk.com/view/java-sdk/SP-CAAAEHQ

		/* original query from Jonathan/qmolus
        final String splunkQuery = "search index=box event_type=DOWNLOAD OR event_type=PREVIEW OR event_type=UPLOAD OR event_type=ITEM_CREATE sha1=* size=* path=* pathname=* folder_size=* | " +
                                    "dedup event_id | eval path=split(path, \":\") | eval pathname=split(pathname, \":\") | " +
                                    "table user, source.item_name, source.item_id, sha1, size, pathname, path, folder_size, event_type, _time | " +
                                    "sort path, source.item_id | search path=" + boxFolderID;
		*//*

		// revised query per Brian & Steve that works not quite as well
		// removed split of paths
		// added wildcards around folder
		String boxQueryFolderID = ( !boxFolderID.equals( "0" ) ? "*:" : "" ) + boxFolderID + ":*";
		final String splunkQuery = "search index=box event_type=DOWNLOAD OR event_type=PREVIEW OR event_type=UPLOAD OR event_type=ITEM_CREATE sha1=* size=* path=* pathname=* folder_size=* | " +
									"dedup event_id | " +
									"table user, source.item_name, source.item_id, sha1, size, pathname, path, folder_size, event_type, _time | " +
									"sort path, source.item_id | search path=" + boxQueryFolderID;
		InputStream splunkResults = splunkService.oneshotSearch( splunkQuery );

        // places to hold folders for quick access
        HashMap<String, FolderInfo> allFolders = new HashMap<String, FolderInfo>();
        HashMap<String, FileInfo> allFiles = new HashMap<String, FileInfo>();

        // use a single formatter to convert dates here to avoid performance issues
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" );

        //FolderInfo topFolder = new FolderInfo( boxFolderID );
        //allFolders.put( boxFolderID, topFolder );

        try {
            ResultsReaderXml resultReader = new ResultsReaderXml( splunkResults );

            HashMap<String, String> lineItem;
            /* int ctr = 0; *//*

            while (( lineItem = resultReader.getNextEvent() ) != null ) {

                /*
                System.out.println( "Result #" + String.valueOf(  ++ctr ));
                int ctr2 = 0;
                for( Map.Entry<String, String> me : lineItem.entrySet() )
                    System.out.println( String.valueOf( ++ctr2 ) + ") " + me.getKey() + " => " + me.getValue() );
                System.out.println( "   Total Items: " + String.valueOf( ctr2 ));
                */

				/* kludge for Splunk shitty query that returns too much data
				 * sha, size, path, pathname all might return too many records, just
				 * use the first one
				 *//*
                String xEventTime = lineItem.get( "_time" );
                String xEventType = lineItem.get( "event_type" );
                String xEventUser = lineItem.get( "user" );

                String[] xPathID = lineItem.get( "path" ).split(",")[0].split( ":" );
				/* because path names can have commas in the name, we have to do much more work here with the
				 * file name
				 *//*
                // String[] xPathName = lineItem.get( "pathname" ).split(",")[0].split( ":" );
				String[] xPath = lineItem.get( "pathname" ).split( ":" );
				String[] xPathName = Arrays.copyOfRange( xPath, 0, xPathID.length );


                String xItemName = lineItem.get( "source.item_name" );
                String xItemID = lineItem.get(  "source.item_id" );
                String xItemSize = lineItem.get( "size" ).split( ",")[0];
                String xItemSHA = lineItem.get( "sha1" ).split( "," )[0];

                String xFolderSize = lineItem.get( "folder_size" );

                // does this folder exist
                String curFolderID = xPathID[ xPathID.length-1 ];
                FolderInfo curFolderInfo = allFolders.get( curFolderID );
                if ( curFolderInfo == null ) {

                    // create the folder tree
					boolean skipfolder = true;
                    for( int i = 0; i < xPathID.length; i++ ) {
                        String xID = xPathID[i];

//if ( xPathID.length != xPathName.length ) {
//	System.out.println( "Data Error");
//}
						// skip folders above the home folder in the hierarchy
						if ( xID.equals( boxFolderID ))
							skipfolder = false;
						if ( skipfolder )
							continue;

                        FolderInfo walker = allFolders.get( xID );
                        if ( walker == null ) {
                            walker = new FolderInfo( xID, xPathName[i], curFolderInfo );
                            allFolders.put( xID, walker );
                            rptData.Folders.add( walker );
                        }
                        curFolderInfo = walker;
                    }
                }

                // does this file exist in this folder
                FileInfo curFileInfo = allFiles.get( xItemID );
				enumFileType yFileType;
                if ( curFileInfo == null ) {
                    // calulate type from ext
                    int off = xItemName.lastIndexOf( "." );
					yFileType = enumFileType.getValueOf( xItemName.substring( off+1 ));
                    curFileInfo = new FileInfo( xItemID, xItemName, yFileType, xItemSize, xItemSHA );
                    curFolderInfo.addFile( curFileInfo );
                    allFiles.put(  xItemID, curFileInfo );
                }

                // add event to the current file
                enumFileEvent yEventType = enumFileEvent.getValueOf( xEventType );
                Date yEventDateTime = sdf.parse( xEventTime );
                String yUnknownSize = xItemSize;

                FileEvent e = new FileEvent( yEventType, xEventUser, yEventDateTime, yUnknownSize );
                curFileInfo.addEvent( e );

                rptData.DateRange( yEventDateTime );
            }

            resultReader.close();

            /* System.out.println( "*** Total Result Items: " + String.valueOf( ctr )); *//*

			// now trim from the folder heirarchy the folders about the home path

        } catch ( IOException | ParseException ex ) {
			throw new DRException( 725, "Process Data", ex.getMessage(), null );

        } catch ( Exception e ) {
			throw new DRException( 726, "Process Data", e );
		}


    }
*/

    public void close() {
        // no documented way to close a Service
    }

}
