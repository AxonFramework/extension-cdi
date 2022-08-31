package org.axonframework.extensions.cdi.test;

import org.axonframework.extensions.cdi.AxonProducers;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ArchiveTemplates {

    private static Logger LOGGER = LoggerFactory.getLogger(ArchiveTemplates.class);

    public static WebArchive webArchiveFlat() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "axon-cdi4-test-flat.war")
                .addPackages(true, AxonProducers.class.getPackage())
                .deletePackages(true, ArchiveTemplates.class.getPackage())
                .addAsManifestResource("META-INF/services/jakarta.enterprise.inject.spi.Extension")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return archive;
    }

    public static WebArchive webArchiveWithLibs() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "axon-cdi4-test-libs.war")
                .addAsLibrary(javaArchive());

        return archive;
    }


    public static JavaArchive javaArchive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "axon-cdi4-test.jar")
                .addPackages(true, AxonProducers.class.getPackage())
                .deletePackages(true, ArchiveTemplates.class.getPackage())
                .addAsResource("META-INF/services/jakarta.enterprise.inject.spi.Extension")
                .addAsResource(EmptyAsset.INSTANCE, "META-INF/beans.xml");

        return archive;
    }

}