/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.core.data.bundle.jaxrs;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.core.data.bundle.Contributor;
import com.intel.mtwilson.core.data.bundle.TarGzipBundle;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/databundle")
public class DataBundleFileUpload {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataBundleFileUpload.class);

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void uploadDataBundle(@FormDataParam("file") InputStream file, @FormDataParam("file") FormDataContentDisposition fileInfo) {
        try {
            TarGzipBundle bundle = new TarGzipBundle();
            bundle.read(file);
            List<Contributor> contributors = Extensions.findAll(Contributor.class);
            for (Contributor contributor : contributors) {
                log.debug("Data bundle contributor: {}", contributor.getClass().getName());
                try {
                    contributor.receive(bundle);
                } catch (Exception e) {
                    log.error("Failed to invoke contributor {}", contributor.getClass().getName(), e);
                }
            }
        } catch (IOException e) {
            log.error("Failed to read uploaded bundle", e);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
    }
}
