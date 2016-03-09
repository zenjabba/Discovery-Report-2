/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.efiia.discoveryreport;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author larry
 */
public class DiscoveryReportURLFeedback extends HttpServlet {

	private final static String TITLE = "Discovery Report URL Feedback";

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {

		response.setContentType( "text/html;charset=UTF-8" );

		try ( PrintWriter out = response.getWriter() ) {

			out.println( "<!DOCTYPE html>" );
			out.println( "<html>" );

			out.println( "<head>" );
			out.println( "<title>" );
			out.println( TITLE );
			out.println( "</title>" );
			out.println( "</head>" );

			out.println( "<body>" );

			out.println( "<h1>" );
			out.println( TITLE );
			out.println( "</h1>" );

			out.println( "<p>URL: " );
			out.println( "<b>" );
			out.println( request.getRequestURL().append( request.getQueryString() ).toString() );
			out.println( "</b>" );
			out.println( "</p>" );

			out.println( "<p>URL Parameters" );

			// loop through parameters
			Map<String, String[]> params = request.getParameterMap();
			out.println( "<ul>" );
			for ( Map.Entry<String, String[]> me : params.entrySet() ) {
				out.println( "<li>" );
				out.println( me.getKey() );
				out.println( " =&gt; " );
				for ( String s : me.getValue() ) {
					out.println( s );
				}
				out.println( "</li>" );
			}
			out.println( "</ul>" );

			out.println( "</p>" );

			out.println( "</body>" );

			out.println( "</html>" );
		}
	}

	// no doPost on purpose
	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Discovery Report Generator";
	}
	// </editor-fold>

}
