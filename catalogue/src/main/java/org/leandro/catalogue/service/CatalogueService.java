package org.leandro.catalogue.service;

import io.reactivex.SingleSource;
import org.leandro.api.v1.model.Product;
import org.leandro.catalogue.integrated.controller.entity.CatalogueEntity;
import org.reactivestreams.Publisher;

import javax.validation.Valid;
import java.util.List;

public interface CatalogueService<T extends Product> {

    Publisher<List<T>> findAll();

    Publisher<List<T>> findByVendorName(String vendorName);

    Publisher<T> find(String title);

    SingleSource<T> save(@Valid CatalogueEntity catalogue);
}
