/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.feature;

import com.intel.dcsg.cpg.io.file.DirectoryFilter;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.feature.fault.BadFeatureDescriptor;
import com.intel.mtwilson.feature.xml.FeatureType;
import com.intel.mtwilson.jaxrs2.Link;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/features")
public class FeatureDirectory {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FeatureDirectory.class);

    /**
     * 
     * @param filters currently supports only ONE "extends" parameter here;  in the future should accept multiple because a feature can extend more than one other features
     * @param request
     * @param response
     * @return 
     */
    @Produces({MediaType.APPLICATION_JSON})
    @GET
    public FeatureListing getFeatureListing(@BeanParam FeatureFilterCriteria filters, @Context HttpServletRequest request, @Context HttpServletResponse response) {
        if( filters == null ) { filters = new FeatureFilterCriteria(); }
        FeatureListing results = new FeatureListing();

        // start with a list of all the features
        File featureDirectory = new File(Folders.features());
        File[] featureSubdirectories = featureDirectory.listFiles(new DirectoryFilter());
        for (File featureSubdirectory : featureSubdirectories) {
            File featureXmlFile = featureSubdirectory.toPath().resolve("feature.xml").toFile();
            log.debug("feature.xml: {}", featureXmlFile.getAbsolutePath());
            if (featureXmlFile.exists()) {
                if( filters.featureId == null || filters.featureId.isEmpty() || featureSubdirectory.getName().equals(filters.featureId) ) {
                if (filters.featureExtends == null || filters.featureExtends.isEmpty() || featureSubdirectory.toPath().resolve(filters.featureExtends).toFile().isDirectory()) {
                    // read feature.xml and add to list
                    try (FileInputStream in = new FileInputStream(featureXmlFile)) {
                        String featureXml = IOUtils.toString(in, Charset.forName("UTF-8"));
                        JAXB jaxb = new JAXB();
                        FeatureType feature = jaxb.read(featureXml, FeatureType.class);
                        results.features.add(feature);
                    } catch (IOException | JAXBException | XMLStreamException e) {
                        log.error("Cannot open feature.xml in {}", featureSubdirectory.getAbsolutePath(), e);
                        results.faults.add(new BadFeatureDescriptor(e, featureSubdirectory.getName()));
                    }

                }
                }
            } else {
                // skip subdirectories not representing "packaged features"
                log.debug("Feature subdirectory {} does not have feature.xml", featureSubdirectory.getName());
            }
        }

        return results;
    }

    public static class FeatureListing {

        public ArrayList<FeatureType> features = new ArrayList<>();
        public ArrayList<Fault> faults = new ArrayList<>();
        public ArrayList<Link> links = new ArrayList<>();
    }
}
