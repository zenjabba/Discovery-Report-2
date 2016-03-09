/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.*;

/**
 *
 * @author larry
 */
public class LogWriter {

	private static String NL = "\n";
	private static byte[] NEWLINE;

	static {
		NEWLINE = NL.getBytes();
	}

	Path logPath;

	public LogWriter( File pFile ) {
		// check if path exists
		pFile.getParentFile().mkdirs();
		logPath = Paths.get( pFile.toURI() );
	}

	public void Write( String pText ) {
		byte data[] = pText.getBytes();
		synchronized( logPath ) {
			try ( OutputStream out = new BufferedOutputStream( Files.newOutputStream( logPath, CREATE, APPEND) )) {
				out.write( data, 0, data.length );
				out.write( NEWLINE, 0, NEWLINE.length );
				out.flush();

			} catch (IOException x) {
				System.err.println(x);
			}
		}
	}

	public void Write( String pText[] ) {
		byte data[];
		synchronized( logPath ) {
			try ( OutputStream out = new BufferedOutputStream( Files.newOutputStream( logPath, CREATE, APPEND) )) {
				for( String line : pText ) {
					data = line.getBytes();
					out.write( data, 0, data.length );
					out.write( NEWLINE, 0, NEWLINE.length );
				}
				out.flush();
			} catch (IOException x) {
				System.err.println(x);
			}
		}
	}
}
