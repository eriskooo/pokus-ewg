package sk.lorman.pokus.ewg.controller;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import sk.lorman.pokus.ewg.domain.Kamion;
import sk.lorman.pokus.ewg.dto.KamionDto;
import sk.lorman.pokus.ewg.mapper.KamionMapper;
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

    @Inject
    KamionMapper mapper;

    @GET
    public Uni<List<KamionDto>> list() {
        log.debug("Listing all kamiony");
        return Uni.createFrom().item(() -> mapper.toDtoList(service.listAll()))
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    @GET
    @Path("/{id}")
    public Uni<KamionDto> get(@PathParam("id") @NotNull @Min(1) Long id) {
        log.debug("Fetching kamion with id={}", id);
        return Uni.createFrom().item(() -> {
                    Kamion k = service.getById(id);
                    if (k == null) {
                        log.info("Kamion with id={} not found", id);
                        throw new NotFoundException();
                    }
                    return mapper.toDto(k);
                })
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    @POST
    public Uni<Response> create(@Valid @NotNull KamionDto input) {
        log.info("Creating new kamion with spz={}", input != null ? input.spz : null);
        return Uni.createFrom().item(() -> {
                    Kamion entity = mapper.toEntity(input);
                    Kamion created = service.create(entity);
                    KamionDto dto = mapper.toDto(created);
                    return Response.created(URI.create("/kamiony/" + created.id)).entity(dto).build();
                })
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());
    }

    @PUT
    @Path("/{id}")
    public Uni<KamionDto> update(@PathParam("id") @NotNull @Min(1) Long id, KamionDto input) {
        log.info("Updating kamion id={}", id);
        return Uni.createFrom().item(() -> {
                    Kamion patch = input != null ? mapper.toEntity(input) : null;
                    Kamion updated = service.update(id, patch);
                    if (updated == null) {
                        log.info("Kamion with id={} not found for update", id);
                        throw new NotFoundException();
                    }
                    return mapper.toDto(updated);
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
