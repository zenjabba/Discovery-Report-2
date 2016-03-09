/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author larry
 */
public class HTML2PDF {

	private String Command;
	private String[] Options;

	public HTML2PDF( String pCmd, String pOpts ) {
		Command = pCmd;
		Options = (pOpts.trim().isEmpty() ? null : pOpts.split( " " ) );
	}

	public String run( File pHTMLFile ) throws DRException {

		String retval = null;

		if ( pHTMLFile == null )
			throw new DRException( 752, "ConvertToPDF2", "Null File Name", "No HTML File Named for Conversion - check web.xml configuration" );

		try {

			String inFileName = pHTMLFile.getAbsolutePath();
			String outFileName = inFileName.replace( ".html", ".pdf" );

			ArrayList<String> a = new ArrayList<>();
			a.add( Command );
			if ( Options != null ) {
				for ( int i = 0; i < Options.length; i++ )
					a.add( Options[i] );
			}
			a.add( inFileName );
			a.add( outFileName );

			ProcessBuilder pb = new ProcessBuilder( a );
			Process p = pb.start();
			p.waitFor();

			if ( p.exitValue() == 0 ) {
				retval = outFileName;
			} else {
				DRException drx = new DRException( 750, "ConvertToPDF", "Error Running Converter", "Exit Code=" + p.exitValue() );
				drx.setDebug( pb.command().toArray(new String[] {} ) );
				throw drx;
			}

		} catch ( IOException | InterruptedException ex ) {
			throw new DRException( 751, "ConvertToPDF2", ex.getClass().getSimpleName(), ex.toString() );

		}

		return ( retval );
	}
}
