package org.leandro.catalogue.integrated.controller;


import io.micronaut.http.annotation.Controller;
import io.micronaut.validation.Validated;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.leandro.api.v1.ProductOperations;
import org.leandro.catalogue.integrated.controller.entity.CatalogueEntity;
import org.leandro.catalogue.service.CatalogueService;
import org.leandro.catalogue.util.FriendlyUrl;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@Controller("/${catalogue.api.version}/catalogue")
@Validated
public class CatalogueController implements ProductOperations<CatalogueEntity> {


    @Inject
    private CatalogueService<CatalogueEntity> service;


    @Override
    public Single<List<CatalogueEntity>> list() {
        return Flowable.fromPublisher(
                service.findAll()
        ).singleOrError();
    }

    @Override
    public Single<List<CatalogueEntity>> byVendor(String vendor) {
        return Flowable.fromPublisher(
                service.findByVendorName(vendor)
        ).singleOrError();
    }


    @Override
    public Single<List<CatalogueEntity>> findByTitle(String title) {
        return Flowable.fromPublisher(
                service.findByTitle(FriendlyUrl.sanitizeWithDashes(title))
        ).singleOrError();
    }

    @Override
    public Single<CatalogueEntity> save(@Valid CatalogueEntity catalogue) {
        final String title = FriendlyUrl.sanitizeWithDashes(catalogue.getTitle());
        catalogue.title(title);
        catalogue.key(UUID.randomUUID());
        return service.save(catalogue);
    }


}
