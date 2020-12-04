package org.leandro.catalogue.service;

import io.reactivex.SingleSource;
import org.leandro.api.v1.model.Product;
import org.leandro.catalogue.integrated.controller.entity.CatalogueEntity;
import org.reactivestreams.Publisher;

import javax.validation.Valid;

public interface CatalogueService<T extends Product> {

    Publisher<T> findAll();

    Publisher<T> findByVendorName(String vendorName);

    Publisher<T> findByType(String name);

    Publisher<T> find(String title);

    SingleSource<T> save(@Valid CatalogueEntity catalogue);
}
