package org.axonframework.cdi.example.javase.rest;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("rest")
@OpenAPIDefinition(
        info = @Info(
                title = "Sample REST application using Axon CDI extension",
                version = "1.0.0"
        ),
        servers = {
                @Server (url = "http://localhost:8080/rest")
        }
)
public class RestApplication extends Application {}
