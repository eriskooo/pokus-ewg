package sk.lorman.controller;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import sk.lorman.domain.Kamion;
import sk.lorman.service.KamionService;

import java.net.URI;
import java.util.List;

@Path("/kamiony")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class KamionResource {

    @Inject
    KamionService service;

    @GET
    public List<Kamion> list() {
        log.debug("Listing all kamiony");
        return service.listAll();
    }

    @GET
    @Path("/{id}")
    public Kamion get(@PathParam("id") Long id) {
        log.debug("Fetching kamion with id={}", id);
        Kamion k = service.getById(id);
        if (k == null) {
            log.info("Kamion with id={} not found", id);
            throw new NotFoundException();
        }
        return k;
    }

    @POST
    @Transactional
    public Response create(Kamion input) {
        try {
            log.info("Creating new kamion with spz={}", input != null ? input.spz : null);
            Kamion created = service.create(input);
            return Response.created(URI.create("/kamiony/" + created.id)).entity(created).build();
        } catch (IllegalArgumentException e) {
            log.warn("Validation error when creating kamion: {}", e.getMessage());
            throw new BadRequestException(e.getMessage());
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Kamion update(@PathParam("id") Long id, Kamion input) {
        log.info("Updating kamion id={}", id);
        Kamion updated = service.update(id, input);
        if (updated == null) {
            log.info("Kamion with id={} not found for update", id);
            throw new NotFoundException();
        }
        return updated;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        log.info("Deleting kamion id={}", id);
        boolean deleted = service.delete(id);
        if (!deleted) {
            log.info("Kamion with id={} not found for delete", id);
            throw new NotFoundException();
        }
        return Response.noContent().build();
    }
}
