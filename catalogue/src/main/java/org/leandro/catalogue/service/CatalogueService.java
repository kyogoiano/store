package org.leandro.catalogue.service;

import io.reactivex.rxjava3.core.Single;
import org.leandro.api.v1.model.Product;
import org.reactivestreams.Publisher;

import javax.validation.Valid;
import java.util.List;

public interface CatalogueService<T extends Product> {

    Publisher<List<T>> findAll();

    Publisher<List<T>> findByVendorName(String vendorName);

    Publisher<List<T>> findByTitle(String title);

    Single<T> save(@Valid T product);
}
