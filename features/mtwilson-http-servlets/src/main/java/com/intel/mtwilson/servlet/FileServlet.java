/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.servlet;

//import com.intel.mtwilson.My;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.http.Query;
import com.intel.dcsg.cpg.validation.ValidationUtil;
import java.io.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletException;
import org.apache.commons.io.IOUtils;

/**
 * @author jbuhacoff
 */
public class FileServlet extends HttpServlet {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileServlet.class);
    public static final String ALLOWED_PATH = "(?:[a-zA-Z0-9_/.]+)"; // just a character set right now, but should be expanded to include structure to exclude .. so we can eliminate the separate check for .. below
    public static final String ALLOWED_QUERY = "(?:[a-zA-Z0-9_/?#%.]+)"; // just a character set right now, but should be expanded to include structure to exclude .. so we can eliminate the separate check for .. below
    private String directory = null;
    private String prefixTarget = null;
	private Configuration configuration = null;
    
    /**
     * Only files inside this directory and sub-directories will be served.
     * The servlet will not access files outside the directory.
     * @param directory 
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }
    
@Override
public void doGet(HttpServletRequest request, HttpServletResponse response) 
                               throws ServletException, IOException {

    if( directory == null ) {
        //setDirectory(My.configuration().getPortalHtml5Dir());
		if( configuration == null ) { configuration = ConfigurationFactory.getConfiguration(); }
		setDirectory(configuration.get("mtwilson.portal.html5.dir"));
        log.info("Static content directory: {}", directory);
    }
    if( prefixTarget == null ) {
        File prefixFile = new File(directory);
        prefixTarget = prefixFile.getCanonicalPath();
    }
    
    // Get the file to view
    String path = request.getPathInfo();
    if (path == null) { path = ""; }
    
    // look for .. early and just return file not found if the pattern is found;  we simply don't support relative paths here to avoid a security vulnerability
    if( path.contains("..") ) {
        log.debug("Rejecting path with relative component: {}", path);
        response.setStatus(400);
        return;
    }
    if( !ValidationUtil.isValidWithRegex(path, ALLOWED_PATH)) {
        log.debug("Rejecting invalid path: {}", path);
        response.setStatus(400);
        return;
    }
    
    File file = new File(directory, path);
    
    // prevent client from using .. to get out of our content folder and to arbitrary files
    String target = file.getCanonicalPath();
    if( !target.startsWith(prefixTarget) ) {
        response.setStatus(404);
        return;
    }
    
    // redirect to use trailing slash on directories in order for relative filenames in links to work
    file = new File(target);
    if( file.isDirectory() && !path.endsWith("/") ) {
        String queryString = "";
        if(request.getQueryString() != null){            
            Query queryParameters = new Query(request.getParameterMap());
            queryString = "?" + queryParameters.toString();
        }
    if( !ValidationUtil.isValidWithRegex(queryString, ALLOWED_QUERY)) {
        log.debug("Rejecting invalid query string: {}", queryString);
        response.setStatus(400);
        return;
    }
        response.sendRedirect(request.getRequestURI() + "/" + queryString);
        return;
    }
    
    // automatic index file for directories; we don't support listing contents so if the index file is not there the client will get a 404 for the directory
    if( file.isDirectory() ) {
        file = new File(target, "index.html"); 
    }
    
    if (!file.exists()) {
        file = new File(target, "index.html5");
    }

    // Get and set the type of the file; relies on the mime types defined in web.xml
    String contentType = getServletContext().getMimeType(file.getName());
    response.setContentType(contentType);
    
    // we set the content length only if it's less than max int, to avoid
    // sending a bogus content length for a huge file (when it matters most!)
    if( file.length() < Integer.MAX_VALUE ) {
        response.setContentLength((int)file.length());
    }

    // read the file and send to the client
    try(FileInputStream in = new FileInputStream(file);
        ServletOutputStream out = response.getOutputStream()) {
        IOUtils.copy(in, out);
    }
    catch (FileNotFoundException e) {
        log.info("File not found: {}", file.getAbsolutePath());
        response.setStatus(404);
    }
    catch (IOException e) { 
        log.error("Cannot retrieve file", e);
        response.setStatus(500);
    }
    catch (IllegalArgumentException iae) {
        log.error("Illegal arguments specified.", iae);
        response.setStatus(500);
    }
  }    
}
