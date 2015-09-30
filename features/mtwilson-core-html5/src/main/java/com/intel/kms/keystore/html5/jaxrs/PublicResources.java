/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.kms.keystore.html5.jaxrs;

import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.IOUtils;

/**
 * Serves static content from classpath resources. The resources must be under a
 * "publicResources" directory in order to be found. Example jar structure:
 * <pre>
 * META-INF/services
 * publicResources/*
 * com/example/*.class
 * </pre>
 *
 * Example Maven project structure:
 * <pre>
 * src/main/resources/publicResources
 * </pre>
 *
 * A limitation of this API is discovery or directory listing is not possible,
 * so the caller must know the file path under publicResources in advance.
 *
 * To serve files from the filesystem, use the default servlet that comes with
 * Jetty or any other web server.
 *
 * @author jbuhacoff
 */
@V2
@Path("/resources")
public class PublicResources {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PublicResources.class);
    /**
     * The resource path is relative to the classpath inside the jar. No
     * trailing slash because getPathInfo() always has a leading slash so
     * requests look like "/index.html" so after concatenating the path would be
     * "/publicResources/index.html"
     */
    private final String resourcePath = "/publicResources";

    @Path("{path:.+}")
    @GET
    @Produces(MediaType.WILDCARD)
    public byte[] getFile(@PathParam("path") String path, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        log.debug("JAX-RS path: {}", path);
        log.debug("RequestURI: " + request.getRequestURI());  //  RequestURI: /v1/resources/index.html
        log.debug("ContextPath: " + request.getContextPath()); // ContextPath:  (empty)
        log.debug("PathInfo: " + request.getPathInfo()); // PathInfo: /resources/index.html     (because this servlet is rooted at /file so this is relative to servlet)
        log.debug("PathTranslated: " + request.getPathTranslated());  // PathTranslated: null unless jetty has a default servlet with a directory configured (look for jetty.hypertext variable) and if it's configured then it would be configuredPath/resources/index.html
        log.debug("RemoteAddr: " + request.getRemoteAddr()); // RemoteAddr: 127.0.0.1
        log.debug("Scheme: " + request.getScheme()); // Scheme: https
        log.debug("ServletPath: " + request.getServletPath()); // ServletPath: /v1  (the Jersey servlet path)
        log.debug("ServerName: " + request.getServerName()); // ServerName: 127.0.0.1

        String relativePath = request.getPathInfo().replaceFirst("^/resources", "");
        log.debug("Relative path: {}", relativePath);

        // no path separator because getPathInfo() always has leading slash
        try (InputStream in = getClass().getResourceAsStream(resourcePath + relativePath)) {
            if (in == null) {
                throw new WebApplicationException(Status.NOT_FOUND); // resp.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            }
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            log.error("Cannot retrieve file", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }
}
