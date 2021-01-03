package org.leandro.recommendations.v1;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.leandro.api.v1.model.Product;

import java.util.List;

@Client(id = "products")
public interface ProductClient {
    @Get("/{title}")
    Maybe<Product> find(String title);

    @Get()
    Single<List<Product>> list();
}
