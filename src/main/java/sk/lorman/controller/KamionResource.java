package sk.lorman.controller;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.lorman.domain.Kamion;
import sk.lorman.service.KamionService;

import java.net.URI;
import java.util.List;

@Path("/kamiony")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KamionResource {

    @Inject
    KamionService service;

    @GET
    public List<Kamion> list() {
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    public Kamion get(@PathParam("id") Long id) {
        Kamion k = service.getById(id);
        if (k == null) {
            throw new NotFoundException();
        }
        return k;
    }

    @POST
    @Transactional
    public Response create(Kamion input) {
        try {
            Kamion created = service.create(input);
            return Response.created(URI.create("/kamiony/" + created.id)).entity(created).build();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Kamion update(@PathParam("id") Long id, Kamion input) {
        Kamion updated = service.update(id, input);
        if (updated == null) {
            throw new NotFoundException();
        }
        return updated;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = service.delete(id);
        if (!deleted) {
            throw new NotFoundException();
        }
        return Response.noContent().build();
    }
}
