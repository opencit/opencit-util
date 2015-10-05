/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.keystore.html5.jaxrs;

import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.feature.FeatureDirectory;
import com.intel.mtwilson.feature.FeatureDirectory.FeatureListing;
import com.intel.mtwilson.feature.FeatureFilterCriteria;
import com.intel.mtwilson.feature.xml.FeatureType;
import com.intel.mtwilson.jaxrs2.Link;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * Serves a directory listing of static content under any feature's html5
 * directory. For example a feature with id "xyz", the path might be:
 * /opt/mtwilson/features/xyz/html5
 *
 * GET /directory/{feature} Will produce a directory listing of the html5 folder
 * of that feature.
 *
 *
 * Example Maven project structure:
 * <pre>
 * src/main/html5
 * </pre>
 *
 *
 * @author jbuhacoff
 */
@V2
@Path("/html5")
public class Directory {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Directory.class);
    private FeatureDirectory featureDirectory = new FeatureDirectory();

    /**
     *
     * @param feature identifier to define the scope of the directory listing;
     * corresponds to a subdirectory of /opt/mtwilson/features
     * @param path of the directory to list within the html5 resources of the
     * specified feature, relative to /opt/mtwilson/features/{feature}/html5;
     * leading slash "/" is optional; can leave the path empty or set to "/" to
     * get the top-level directory listing
     * @param request
     * @param response
     * @return
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/directory")
    public DirectoryListing getDirectoryListing(@BeanParam DirectoryFilterCriteria filters, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        if( filters == null ) { filters = new DirectoryFilterCriteria(); }
        // if a feature was specified, we look only there - otherwise we look in all features that extend html5
        FeatureFilterCriteria featureFilters = new FeatureFilterCriteria();
        featureFilters.featureExtends = "html5";
        if (filters.featureId != null && !filters.featureId.isEmpty()) {
            featureFilters.featureId = filters.featureId;
        }

        ArrayList<String> features = new ArrayList<>();
        // list all features that match the criteria (must extend html5, optional specific feature id)
        FeatureListing featureListing = featureDirectory.getFeatureListing(featureFilters, request, response);
        for (FeatureType featureType : featureListing.features) {
            features.add(featureType.getId());
        }

        DirectoryListing listing = new DirectoryListing();
        for (String feature : features) {
            String featureHtml5Path = Folders.features(feature) + File.separator + "html5";

            String platformRelativePath = "";
            if (filters.relativePath != null) {
                platformRelativePath = filters.relativePath.replace("/", File.separator);
            }

            File file = new File(featureHtml5Path + File.separator + platformRelativePath);
            log.debug("Absolute path: {}", file.getAbsolutePath());

            // protect against tricks like .. to escape the html5 directory
            if (!file.getAbsolutePath().startsWith(featureHtml5Path)) {
                continue; //throw new WebApplicationException(Status.NOT_FOUND); // resp.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            }

            // check if target file exists
            if (!file.exists()) {
                continue; //throw new WebApplicationException(Status.NOT_FOUND); // resp.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            }

            // protect hidden files and dot-files by convention
            if (file.isHidden() || file.getName().startsWith(".")) {
                continue; //throw new WebApplicationException(Status.NOT_FOUND); // resp.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            }


            // provide a directory listing in JSON format
            if (file.isDirectory()) {
                String directoryRelativePath = file.getAbsolutePath().replace(featureHtml5Path, "");
                if( directoryRelativePath.isEmpty() ) {
                    directoryRelativePath = "/";
                }
                String directoryRelativePathUri = directoryRelativePath.replace(File.separator, "/");

                File[] files = file.listFiles();
                if( files != null ) {
                for (File directoryFile : files) {
                    DirectoryEntry entry = new DirectoryEntry();
                    entry.name = directoryFile.getName();
                    String fileRelativePath = directoryFile.getAbsolutePath().replace(featureHtml5Path, "").replace(File.separator, "/");
                    String rel = directoryFile.isDirectory() ? "directory" : "file";
                    try {
                        BasicFileAttributes attributes = Files.readAttributes(directoryFile.toPath(), BasicFileAttributes.class);
                        entry.feature = feature;
                        entry.directory = directoryRelativePathUri;
                        entry.size = attributes.size();
                        entry.links.add(Link.build().href(getDirectoryLink(feature, fileRelativePath)).rel(rel));
                        if( directoryFile.isFile() ) { entry.links.add(Link.build().href(getContentLink(feature, fileRelativePath)).rel("download")); }
                        listing.entries.add(entry);
                    } catch (IOException e) {
                        log.error("Cannot get file attributes: {}", file.getAbsolutePath(), e);
                        listing.faults.add(new FileAttributeReadFault(fileRelativePath));
                    }
                }
                }
            } else if (file.isFile()) {
                String fileRelativePath = file.getAbsolutePath().replace(featureHtml5Path, "").replace(File.separator, "/");
                String parentRelativePath = file.getParentFile().getAbsolutePath();
                if (parentRelativePath.startsWith(featureHtml5Path)) {
                    parentRelativePath = parentRelativePath.replace(featureHtml5Path, "");
                } else {
                    // if the parent is outside the html5 directory, just reset its relative path to "/" to indicate the html5 "root" for the feature
                    parentRelativePath = "/";
                }
                String parentRelativePathUri = parentRelativePath.replace(File.separator, "/");
                
                
                File directoryFile = file;
                try {
                    BasicFileAttributes attributes = Files.readAttributes(directoryFile.toPath(), BasicFileAttributes.class);
                    DirectoryEntry entry = new DirectoryEntry();
                    entry.feature = feature;
                    entry.directory = parentRelativePathUri;
                    entry.size = attributes.size();
                    entry.links.add(Link.build().href(getDirectoryLink(feature, parentRelativePathUri)).rel("parent"));
                    if( directoryFile.isFile() ) { entry.links.add(Link.build().href(getContentLink(feature, fileRelativePath)).rel("download")); }
                   listing.entries.add(entry);
                } catch (IOException e) {
                    log.error("Cannot get file attributes: {}", file.getAbsolutePath(), e);
                    listing.faults.add(new FileAttributeReadFault(fileRelativePath));
                }
            }
        }
        return listing;
    }
    
    private String getDirectoryLink(String feature, String path) {
        return String.format("/html5/directory?feature=%s&path=%s", feature, path);
    }
    
    private String getContentLink(String feature, String path) {
        return String.format("/html5/features/%s/%s", feature, path);
    }

    /**
     * Downside of having this API here is that it repeats (with less quality)
     * the same logic as standard static file download servlets... the only
     * difference is we have the specific requirement to provide a feature id
     * in which the file will be found and set the local directory path 
     * accordingly. It would be better to handle the path translation and then
     * delegate to existing code for handling the request including setting
     * response headers, checking accept headers, etc.  Or at least ensure
     * that this API is being accessed behind  org.glassfish.jersey.server.filter.UriConnegFilter
     * and let that filter set the headers.
     * @param featureId
     * @param path
     * @param request
     * @param response
     * @return 
     */
    @Path("/features/{featureId}/{path:.+}")
    @GET
    @Produces(MediaType.WILDCARD)
    public byte[] getFile(@PathParam("featureId") String featureId, @PathParam("path") String path, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        log.debug("JAX-RS path: {}", path);
        log.debug("RequestURI: " + request.getRequestURI());  //  RequestURI: /v1/resources/index.html
        log.debug("ContextPath: " + request.getContextPath()); // ContextPath:  (empty)
        log.debug("PathInfo: " + request.getPathInfo()); // PathInfo: /resources/index.html     (because this servlet is rooted at /file so this is relative to servlet)
        log.debug("PathTranslated: " + request.getPathTranslated());  // PathTranslated: null unless jetty has a default servlet with a directory configured (look for jetty.hypertext variable) and if it's configured then it would be configuredPath/resources/index.html
        log.debug("RemoteAddr: " + request.getRemoteAddr()); // RemoteAddr: 127.0.0.1
        log.debug("Scheme: " + request.getScheme()); // Scheme: https
        log.debug("ServletPath: " + request.getServletPath()); // ServletPath: /v1  (the Jersey servlet path)
        log.debug("ServerName: " + request.getServerName()); // ServerName: 127.0.0.1

        String relativePath = FilenameUtils.normalize(request.getPathInfo(), true).replaceFirst(String.format("^/html5/features/%s/", featureId), "");
        log.debug("Relative path: {}", relativePath);

        String featureHtml5Path = Folders.features(featureId) + File.separator + "html5";

        log.debug("Combined path: {}{}", featureHtml5Path, relativePath);
        File file = new File(featureHtml5Path + File.separator + relativePath);
        log.debug("Absolute path: {}", file.getAbsolutePath());

        // protect against tricks like .. to escape the html5 directory
        if (!file.getAbsolutePath().startsWith(featureHtml5Path)) {
            throw new WebApplicationException(Status.NOT_FOUND); // resp.setStatus(Response.Status.NOT_FOUND.getStatusCode());
        }

        // check if target file exists
        if (!file.exists()) {
            throw new WebApplicationException(Status.NOT_FOUND); // resp.setStatus(Response.Status.NOT_FOUND.getStatusCode());
        }

        // we don't provide directory listings from this feature
        if (file.isDirectory()) {
            throw new WebApplicationException(Status.NOT_FOUND); // resp.setStatus(Response.Status.NOT_FOUND.getStatusCode());
        }
        
        try (FileInputStream in = new FileInputStream(file)) {
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            log.error("Cannot retrieve file", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static class DirectoryListing {

        public ArrayList<DirectoryEntry> entries = new ArrayList<>();
        public ArrayList<Fault> faults = new ArrayList<>();
    }

    public static class DirectoryEntry {

        public String feature;
        public String directory;
        public String name;
        public Long size;
        public ArrayList<Link> links = new ArrayList<>();
//        public BasicFileAttributes attributes;
    }

    /*
     BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);     * 
     */
    public static class FileAttributeReadFault extends Fault {

        public FileAttributeReadFault(String path) {
            super(path);
        }
    }
}
