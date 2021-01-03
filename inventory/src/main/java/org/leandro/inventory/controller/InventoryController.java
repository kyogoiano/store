package org.leandro.inventory.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.validation.Validated;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import org.leandro.inventory.service.InventoryService;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;

@Controller("/${catalogue.api.version}/products")
@Validated
public class InventoryController {

    @Inject
    private InventoryService inventoryService;

    @Produces(MediaType.TEXT_PLAIN)
    @Get("/stock/{barCode}")
    public Maybe<Boolean> stock(@NotBlank String barCode){
        return
                Flowable.fromPublisher(
                inventoryService.findByBarCode(barCode)).map(bi -> bi.getStock() > 0).firstElement();
    }
}
