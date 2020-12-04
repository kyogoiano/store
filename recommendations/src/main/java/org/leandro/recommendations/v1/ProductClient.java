package org.leandro.recommendations.v1;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.leandro.api.v1.model.Product;

import java.util.List;

@Client(id = "products")
public interface ProductClient {
    @Get("/{title}")
    Maybe<Product> find(String title);

    @Get("/")
    Single<List<Product>> list();
}
