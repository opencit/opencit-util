/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.keystore.html5.jaxrs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.feature.FeatureDirectory;
import com.intel.mtwilson.feature.FeatureDirectory.FeatureListing;
import com.intel.mtwilson.feature.FeatureFilterCriteria;
import com.intel.mtwilson.feature.xml.FeatureType;
import com.intel.mtwilson.jaxrs2.Link;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.util.validation.faults.Thrown;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
     * ONLY looks in the html5 directories of installed features, which means
     * it's possible to search for public resources via this API too (by starting
     * with the "public/" prefix) but that would be an exploitation of this
     * implementation, not the correct way to query for that. The correct way
     * would be to query for /html5/public/directory  which only looks in public
     * resources.
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
    public DirectoryListing getResourceDirectoryListing(@BeanParam DirectoryFilterCriteria filters, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        DirectoryFinderFactory directoryFinderFactory = new DirectoryFinderFactory("html5", "/", "/", false);
        return getDirectoryListing(directoryFinderFactory,filters, request, response);
    }

    /**
     * ONLY looks in the html5/public directories of installed features
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
    @Path("/public/directory")
    public DirectoryListing getPublicResourceDirectoryListing(@BeanParam DirectoryFilterCriteria filters, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        DirectoryFinderFactory directoryFinderFactory = new DirectoryFinderFactory("html5/public", "/", "/public", true);
//        if( filters.relativePath != null && filters.relativePath.startsWith("/public")) {
//            filters.relativePath = filters.relativePath.substring("/public".length());
//            log.debug("Adjusting relative path for public resource: {}", filters.relativePath);
//        }
        return getDirectoryListing(directoryFinderFactory,filters, request, response);
    }
    
    public static class DirectoryFinderFactory {
        private String featureRelativePath;
        private String featureRelativePathRoot;
        private String hrefRelativePath;
        private boolean isPublic;

        /**
         * 
         * @param featureRelativePath like html5  used to form all the paths under a feature directory by appending the relative path to this
         * @param featureRelativePathRoot like / or /public to be appended to featureRelativePath and only allow files under this, note this may overlap the relative path which is why it's a separate setting
         * @param hrefRelativePath like /html (which would translate to /v1/html5) to create urls bya ppending the relative path to this
         */
        public DirectoryFinderFactory(String featureRelativePath, String featureRelativePathRoot, String hrefRelativePath, boolean isPublic) {
            this.featureRelativePath = featureRelativePath;
            this.featureRelativePathRoot = featureRelativePathRoot;
            this.hrefRelativePath = hrefRelativePath;
            this.isPublic = isPublic;
        }
        
        public DirectoryFinder create(String feature) {
            String featurePath = Folders.features(feature);
            return new DirectoryFinder(featurePath + File.separator + featureRelativePath, featureRelativePathRoot, hrefRelativePath, isPublic);
        }
    }
    public static class DirectoryFinder {
        private String absoluteBasePath;
        private String relativeRootPath;
        private String hrefRelativePath;
        private boolean isPublic;
        
        /**
         * 
         * @param absoluteBasePath like /opt/mtwilson/features/featureABC/html5  used to form all the paths by appending the relative path to this
         * @param relativeRootPath like / or /public to be appended to absoluteBasePath and only allow files under this,  note this may overlap the relative path which is why it's a separate setting
         * @param hrefRelativePath like /html5 (which would translate to /v1/html5) to create urls by appending the relative path to this
         */
        public DirectoryFinder(String absoluteBasePath, String relativeRootPath, String hrefRelativePath, boolean isPublic) {
            this.absoluteBasePath = absoluteBasePath;
            this.relativeRootPath = relativeRootPath;
            this.hrefRelativePath = hrefRelativePath;
            this.isPublic = isPublic;
        }
        
        public boolean isAllowed(File file) {
            String rootPath = absoluteBasePath + File.separator + relativeRootPath;
            rootPath = rootPath.replace(File.separator, "/").replaceAll("/+", "/");
            String filePath = file.getAbsolutePath().replace(File.separator, "/").replaceAll("/+", "/");
            log.debug("Root path: {}", rootPath);
            if( filePath.startsWith(rootPath) && file.exists() && !file.isHidden() && !file.getName().startsWith(".") ) {
                return true;
            }
            
            return false;
        }
        
        public boolean isPublic(File file) {
            return isPublic;
        }
        
        public String getBasePath() { return absoluteBasePath; }
        public String getBaseHref() { return hrefRelativePath; }
    }
    
    private DirectoryListing getDirectoryListing(DirectoryFinderFactory directoryFinderFactory, @BeanParam DirectoryFilterCriteria filters, @Context HttpServletRequest request, @Context HttpServletResponse response) {
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
            DirectoryFinder finder = directoryFinderFactory.create(feature);
            String basePath = finder.getBasePath();
            String baseHref = finder.getBaseHref();

            String platformRelativePath = "";
            if (filters.relativePath != null) {
                platformRelativePath = filters.relativePath.replace("/", File.separator);
            }

            File file = new File(basePath + File.separator + platformRelativePath);
            log.debug("Absolute path: {}", file.getAbsolutePath());

            // protect against tricks like .. to escape the html5 directory
            if( !finder.isAllowed(file) ) {
                log.warn("Skipping file because not allowed: {}", file.getAbsolutePath());
                continue;
            }

            // provide a directory listing in JSON format
            if (file.isDirectory()) {
                String directoryRelativePath = file.getAbsolutePath().replace(basePath, "");
                if( directoryRelativePath.isEmpty() ) {
                    directoryRelativePath = "/";
                }
                String directoryRelativePathUri = directoryRelativePath.replace(File.separator, "/");

                File[] files = file.listFiles();
                if( files != null ) {
                for (File directoryFile : files) {
                    DirectoryEntry entry = new DirectoryEntry();
                    String fileRelativePath = directoryFile.getAbsolutePath().replace(basePath, "").replace(File.separator, "/");
                    String rel = directoryFile.isDirectory() ? "directory" : "file";
                    try {
                        BasicFileAttributes attributes = Files.readAttributes(directoryFile.toPath(), BasicFileAttributes.class);
                        entry.feature = feature;
                        entry.directory = directoryRelativePathUri;
                        entry.size = attributes.size();
                        entry.name = directoryFile.getName();
                        entry.isPublic = finder.isPublic(directoryFile);
                        entry.links.add(Link.build().href(getDirectoryLink(feature, normalize(baseHref+fileRelativePath))).rel(rel));
                        if( directoryFile.isFile() ) { entry.links.add(Link.build().href(getContentLink(feature, normalize(baseHref+fileRelativePath))).rel("download")); }
                        listing.entries.add(entry);
                    } catch (IOException e) {
                        log.error("Cannot get file attributes: {}", file.getAbsolutePath(), e);
                        listing.faults.add(new FileAttributeReadFault(fileRelativePath));
                    }
                }
                }
            } else if (file.isFile()) {
                String fileRelativePath = file.getAbsolutePath().replace(basePath, "").replace(File.separator, "/");
                String parentRelativePath = file.getParentFile().getAbsolutePath();
                if (parentRelativePath.startsWith(basePath)) {
                    parentRelativePath = parentRelativePath.replace(basePath, "");
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
                    entry.name = directoryFile.getName();
                    entry.isPublic = finder.isPublic(directoryFile);
                    entry.links.add(Link.build().href(getDirectoryLink(feature, normalize(baseHref+parentRelativePathUri))).rel("parent"));
                    if( directoryFile.isFile() ) { entry.links.add(Link.build().href(getContentLink(feature, normalize(baseHref+fileRelativePath))).rel("download")); }
                   listing.entries.add(entry);
                } catch (IOException e) {
                    log.error("Cannot get file attributes: {}", file.getAbsolutePath(), e);
                    listing.faults.add(new FileAttributeReadFault(fileRelativePath));
                }
            }
        }
        return listing;
    }
    
    private String normalize(String path) {
        String result = path.replaceAll("/+", "/");
        if( !result.startsWith("/") ) {
            result = "/" + result;
        }
        return result;
    }
    
    private String getDirectoryLink(String feature, String path) {
        if( path.startsWith("/public/") ) {
            return String.format("/html5/public/directory?feature=%s&path=%s", feature, path.substring("/public/".length()));
        }
        return String.format("/html5/directory?feature=%s&path=%s", feature, path);
    }
    
    private String getContentLink(String feature, String path) {
        log.debug("getContentLink feature:{}, path:{}", feature, path);
        if( path.startsWith("/public/") ) {
            return String.format("/html5/public/%s/%s", feature, path.substring("/public/".length()));
        }
        return String.format("/html5/resources/%s/%s", feature, path);
    }

    
    private void logRequest(HttpServletRequest request) {
        log.debug("RequestURI: " + request.getRequestURI());  //  RequestURI: /v1/resources/index.html
        log.debug("ContextPath: " + request.getContextPath()); // ContextPath:  (empty)
        log.debug("PathInfo: " + request.getPathInfo()); // PathInfo: /resources/index.html     (because this servlet is rooted at /file so this is relative to servlet)
        log.debug("PathTranslated: " + request.getPathTranslated());  // PathTranslated: null unless jetty has a default servlet with a directory configured (look for jetty.hypertext variable) and if it's configured then it would be configuredPath/resources/index.html
        log.debug("RemoteAddr: " + request.getRemoteAddr()); // RemoteAddr: 127.0.0.1
        log.debug("Scheme: " + request.getScheme()); // Scheme: https
        log.debug("ServletPath: " + request.getServletPath()); // ServletPath: /v1  (the Jersey servlet path)
        log.debug("ServerName: " + request.getServerName()); // ServerName: 127.0.0.1
    }
    
    private boolean isValidFileRequest(String rootPath, File file) {
        return 
                // check absolute path to protect against tricks like .. to escape the html5 directory
                file.getAbsolutePath().startsWith(rootPath+File.separator) 
                // check if target file exists
                && file.exists() 
                // we don't provide directory listings from this feature
                && !file.isDirectory();
    }

    
    public byte[] getBytes(String basedir, String relativePath) {
        log.debug("Combined path: {}{}", basedir, relativePath);
        File file = new File(basedir + File.separator + relativePath);
        log.debug("Absolute path: {}", file.getAbsolutePath());
        
        if( !isValidFileRequest(basedir, file) ) {
            throw new NotFoundException();
        }
        
        try (FileInputStream in = new FileInputStream(file)) {
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            log.error("Cannot retrieve file", e);
            throw new ServerErrorException(Response.serverError().build());
        }        
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
    @Path("/resources/{featureId}/{path:.+}")
    @GET
    @Produces(MediaType.WILDCARD)
    public byte[] getResource(@PathParam("featureId") String featureId, @PathParam("path") String path, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        log.debug("getResource JAX-RS path: {}", path);
        logRequest(request);

        String relativePath = FilenameUtils.normalize(request.getPathInfo(), true).replaceFirst(String.format("^/html5/resources/%s/", featureId), "");
        log.debug("Relative path: {}", relativePath);
        
        String featureHtml5Path = Folders.features(featureId) + File.separator + "html5";
        
        return getBytes(featureHtml5Path, relativePath);
    }
    
    
    /**
     * @deprecated api path /features/{featureId}/{path} will be removed in a later release, use /resources/{featureId}/{path} instead for protected resources or /public/{featureId}/{path} for public resources
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
        log.debug("getFile JAX-RS path: {}", path);
        logRequest(request);

        String relativePath = FilenameUtils.normalize(request.getPathInfo(), true).replaceFirst(String.format("^/html5/features/%s/", featureId), "");
        log.debug("Relative path: {}", relativePath);
        
        String featureHtml5Path = Folders.features(featureId) + File.separator + "html5";
        
        return getBytes(featureHtml5Path, relativePath);
    }

    /**
     * Provides a mechanism for making specific resources publicly
     * accessible (no authorization requird) under a single path prefix
     * for all features, which simplifies the security configuration.
     * 
     * For a resource with a URL like http://server/v1/public/featureId/resourceA
     * the file would be found under /opt/application/features/featureId/html5/public/resourceA
     * 
     * @param featureId
     * @param path
     * @param request
     * @param response
     * @return 
     */
    @Path("/public/{featureId}/{path:.+}")
    @GET
    @Produces(MediaType.WILDCARD)
    public byte[] getPublicResource(@PathParam("featureId") String featureId, @PathParam("path") String path, @Context HttpServletRequest request, @Context HttpServletResponse response) {

        
        log.debug("getPublicResource JAX-RS path: {}", path);
        logRequest(request);
        
        String relativePath = FilenameUtils.normalize(request.getPathInfo(), true).replaceFirst(String.format("^/html5/public/%s/", featureId), "");
        log.debug("Relative path: {}", relativePath);

        String featureHtml5Path = Folders.features(featureId) + File.separator + "html5" + File.separator + "public";

        return getBytes(featureHtml5Path, relativePath);
    }
    
    
    @Path("/public/merge")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode getMergedPublicResources(@QueryParam("path") String path, @Context HttpServletRequest request, @Context HttpServletResponse response) {

        DirectoryFilterCriteria directoryFilterCriteria = new DirectoryFilterCriteria();
        directoryFilterCriteria.relativePath = path;
        DirectoryListing matchingJsonFiles = getPublicResourceDirectoryListing(directoryFilterCriteria, request, response);
        ObjectNode result = new ObjectNode(JsonNodeFactory.instance);
        if( matchingJsonFiles.faults != null && !matchingJsonFiles.faults.isEmpty() ) {
            result.putPOJO("faults", matchingJsonFiles.faults);
            return result;
        }
        
        if( matchingJsonFiles.entries == null ) {
            return result;
        }
        
        ObjectMapper mapper = new ObjectMapper();
        for( DirectoryEntry entry : matchingJsonFiles.entries ) {
            String feature = entry.feature; 
            String directory = ( entry.isPublic != null && entry.isPublic.booleanValue() ? "/public" : "" ) + entry.directory; 
            String filename = entry.name;
            String entryPath = Folders.features(feature) + File.separator + "html5" + directory.replace("/", File.separator) + File.separator + filename;
            log.debug("/public/merge.json reading file: {}", entryPath);
            try {
                File entryFile = new File(entryPath);
                JsonNode json = mapper.readTree(entryFile);
                if( json != null && json.isObject() ) {
                    mergeJsonObjects(result, (ObjectNode)json);
                }
            }
            catch(IOException e) {
                ArrayList<Fault> faults = new ArrayList<>();
                faults.add(new Thrown(e));
                result.putPOJO("faults", faults);
                continue;
            }
        }
        
        return result;
    }
    
    // deep merge for objects , merge for arrays, and copy (but not replace) for primitive types
    private void mergeJsonObjects(ObjectNode to, ObjectNode from) {
        if( to == null || from == null ) { return; }
        Iterator<String> fieldNames = from.fieldNames();
        while(fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode source = from.get(fieldName);
            JsonNode target = to.get(fieldName);
            if( target == null ) {
                to.set(fieldName, source);
            }
            else if( target.isObject() && source.isObject() ) {
                mergeJsonObjects((ObjectNode)target, (ObjectNode)source);
            }
            else if( target.isArray() && source.isArray() ) {
                ((ArrayNode)target).addAll((ArrayNode)source);
            }
            else {
                log.error("Cannot merge data to {} from {}", to.getNodeType().name(), from.getNodeType().name());
            }
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
        public Boolean isPublic;
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
