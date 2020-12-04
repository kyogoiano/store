package org.leandro.api.v1;


import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.validation.Validated;
import io.reactivex.Single;
import org.leandro.api.v1.model.Vendor;

import java.util.List;

@Validated
public interface VendorOperations {

    @Get("/")
    Single<List<Vendor>> list();

    @Get("/names")
    Single<List<String>> names();

    @Post("/")
    Single<Vendor> save(String name);
}
