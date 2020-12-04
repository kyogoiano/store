package org.leandro.api.v1;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.validation.Validated;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.leandro.api.v1.model.Product;

import javax.validation.Valid;
import java.util.List;

@Validated
public interface ProductOperations<T extends Product> {

    @Get("/")
    Single<List<T>> list();

    @Get("/vendor/{name}")
    Single<List<T>> byVendor(String name);

    @Get("/type/{name}")
    Single<List<T>> byType(String name);

    @Get("/{title}")
    Maybe<T> find(String title);

    @Post("/")
    Single<T> save(@Valid @Body T product);
}
