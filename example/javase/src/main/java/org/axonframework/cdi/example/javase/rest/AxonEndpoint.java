package org.axonframework.cdi.example.javase.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.axonframework.cdi.example.javase.command.CreateAccountCommand;
import org.axonframework.cdi.example.javase.query.GetAllAccountsQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Api(value = "/axon")
@Path("axon")
@Produces(MediaType.APPLICATION_JSON)
public class AxonEndpoint {

    @Inject
    private Logger logger;

    @Inject
    private CommandGateway commandGateway;

    @Inject
    private QueryGateway queryGateway;


    @ApiOperation(value = "Create account")
    @POST
    @Path("{id}")
    public Response create(@PathParam("id") String account) {
        Object result = commandGateway.sendAndWait(new CreateAccountCommand(account, 1000D));
        return Response.ok(result.toString(), MediaType.TEXT_PLAIN).build();
    }


    @ApiOperation(value = "Get all accounts")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String all() {

        try {
            List<String> list = queryGateway.query(new GetAllAccountsQuery(), ResponseTypes.multipleInstancesOf(String.class)).get();
            return list.stream().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

}
