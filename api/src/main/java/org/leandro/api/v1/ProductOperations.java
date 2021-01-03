package org.leandro.api.v1;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.validation.Validated;
import io.reactivex.rxjava3.core.Single;
import org.leandro.api.v1.model.Product;

import javax.validation.Valid;
import java.util.List;

@Validated
public interface ProductOperations<T extends Product> {

    @Get()
    Single<List<T>> list();

    @Get("/vendor/{vendor}")
    Single<List<T>> byVendor(String vendor);

    @Get("/title/{title}")
    Single<List<T>> findByTitle(String title);

    @Post()
    Single<T> save(@Valid @Body T product);
}
