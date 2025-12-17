package sk.lorman.pokus.ewg.controller;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import sk.lorman.pokus.ewg.domain.Kamion;
import sk.lorman.pokus.ewg.service.KamionService;

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
    public Uni<List<Kamion>> list() {
        log.debug("Listing all kamiony");
        return Uni.createFrom().item(service::listAll)
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    @GET
    @Path("/{id}")
    public Uni<Kamion> get(@PathParam("id") Long id) {
        log.debug("Fetching kamion with id={}", id);
        return Uni.createFrom().item(() -> {
                    Kamion k = service.getById(id);
                    if (k == null) {
                        log.info("Kamion with id={} not found", id);
                        throw new NotFoundException();
                    }
                    return k;
                })
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    @POST
    public Uni<Response> create(Kamion input) {
        log.info("Creating new kamion with spz={}", input != null ? input.spz : null);
        return Uni.createFrom().item(() -> {
                    Kamion created = service.create(input);
                    return Response.created(URI.create("/kamiony/" + created.id)).entity(created).build();
                })
                .onFailure(IllegalArgumentException.class)
                .transform(e -> new BadRequestException(e.getMessage()))
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    @PUT
    @Path("/{id}")
    public Uni<Kamion> update(@PathParam("id") Long id, Kamion input) {
        log.info("Updating kamion id={}", id);
        return Uni.createFrom().item(() -> {
                    Kamion updated = service.update(id, input);
                    if (updated == null) {
                        log.info("Kamion with id={} not found for update", id);
                        throw new NotFoundException();
                    }
                    return updated;
                })
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        log.info("Deleting kamion id={}", id);
        return Uni.createFrom().item(() -> {
                    boolean deleted = service.delete(id);
                    if (!deleted) {
                        log.info("Kamion with id={} not found for delete", id);
                        throw new NotFoundException();
                    }
                    return Response.noContent().build();
                })
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }
}
