/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport;

import java.io.File;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Session;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
/**
 *
 * @author larry
 */
public class MailUtility {

	Session MailSession;

	public MailUtility() throws DRException {

		// get mail resource
		Context initCtx;
		Context envCtx;
		try {
			initCtx = new InitialContext();
			envCtx = (Context) initCtx.lookup( "java:comp/env" );
			MailSession = (Session) envCtx.lookup( "mail/Session" );

			//MailSession.setDebug( true );

		} catch ( NoClassDefFoundError ncd ) {
			DRException drx = new DRException( 712, "MailUtility:Initalize", "Unable to Find Mail Supporting Classes", ncd );
			drx.setResponseMessage( drx.getMessage() );
			throw (drx );

		} catch ( NamingException ex ) {
			DRException drx = new DRException( 710, "MailUtility:Initialize", "Unable to Initalize Email Interface", ex );
			drx.setResponseMessage( drx.getMessage() );
			throw ( drx );

		} catch ( Throwable anyothererror ) {
			DRException drx = new DRException( 713, "MailUtility:Initalize", "General Error Loading Email Interface", anyothererror );
			drx.setResponseMessage( drx.getMessage() );
			throw (drx );
		}

	}

	public boolean SendMessage( String pDestEmail, String pFromName, String pFromEmail, String pSubject, String pBody, File pReportFile, String pReportFileName, File pCSVFile, String pCSVFileName ) throws DRException {

		try {
			MimeMessage msg = new MimeMessage( MailSession );

			// 2016-May - change to match what Brian did manuall
			// msg.setFrom( String.format( "\"%s\" <%s>", pFromName, pFromEmail) );
			msg.setFrom( String.format( "\"%s\" <%s>", pFromName, "noreply@esfs.us" ));
			msg.setSubject( pSubject );
			msg.setRecipients( Message.RecipientType.TO, pDestEmail );
			//msg.setRecipients( Message.RecipientType.TO, "larry@smoke-mirrors.com.au" );

			if ( pBody != null )
				msg.setText( pBody );

			Multipart multiPart = new MimeMultipart();
			if ( pBody != null ) {
				MimeBodyPart msgTextPart = new MimeBodyPart();
				msgTextPart.setText( pBody, "utf-8" );
				multiPart.addBodyPart( msgTextPart );
			}

			MimeBodyPart msgFilePart = new MimeBodyPart();
			DataSource ds = new FileDataSource( pReportFile );
			msgFilePart.setDataHandler( new DataHandler( ds ));
			msgFilePart.setFileName( pReportFileName );
			multiPart.addBodyPart( msgFilePart );

			if ( pCSVFile != null ) {
				MimeBodyPart msgFilePart2 = new MimeBodyPart();
				DataSource ds2 = new FileDataSource( pCSVFile );
				msgFilePart2.setDataHandler( new DataHandler( ds2 ));
				msgFilePart2.setFileName( pCSVFileName );
				multiPart.addBodyPart( msgFilePart2 );
			}

			msg.setContent( multiPart );

			Transport.send( msg );

		} catch ( MessagingException ex ) {
			DRException drx = new DRException( 711, "MailUtility:SendMessage", "Unable to Email Discovery Report.", ex );
			drx.setResponseMessage( drx.getMessage() );
			throw ( drx );
		}

		return ( true );
	}

}
