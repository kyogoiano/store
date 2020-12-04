package org.leandro.inventory.integrated.controller;

import io.micronaut.http.client.annotation.Client;
import io.micronaut.validation.Validated;
import io.reactivex.Single;
import org.leandro.api.v1.ProductOperations;
import org.leandro.catalogue.integrated.controller.entity.CatalogueEntity;

import java.util.List;

@Client("/${catalogue.api.version}/catalogue")
@Validated
public interface InventoryControllerTestClient extends ProductOperations<CatalogueEntity> {

    @Override
    Single<List<CatalogueEntity>> byVendor(String name);
}
