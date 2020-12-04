package org.leandro.recommendations.feature;


import io.lettuce.core.KeyValue;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.validation.Validated;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.leandro.api.v1.model.Offer;
import org.leandro.api.v1.model.Product;
import org.leandro.recommendations.v1.ProductClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Singleton
@Validated
public class RecommendationRepository implements RecommendationsOperations {


    @Inject
    private ProductClient productClient;

    @Inject
    private StatefulRedisConnection<String, String> redisConnection;

    private final RedisReactiveCommands<String, String> commands;

    {
        this.commands = redisConnection.reactive();
    }

    /**
     * @return Returns all current offers
     */
    public Mono<List<Offer>> all() {
        return commands.keys("*").flatMap(keyToOffer(commands)).collectList();
    }

    /**
     * @return Obtain a random offer or return {@link Mono#empty()} if there is none
     */
    public Mono<Offer> random() {
        return commands.randomkey().flatMap(keyToOffer(commands));
    }


    @Override
    public Mono<Offer> save(@NotNull final Product product,
                            @Digits(integer = 10, fraction = 2) final BigDecimal price,
                            @NotBlank final String description,
                            @NotNull final Duration duration) {
        return Mono.from(productClient.find(
                product.getTitle()
        ).toFlowable())
                .flatMap(productInstance -> {
                    final ZonedDateTime expiryDate = ZonedDateTime.now().plus(duration);
                    final Offer offer = new Offer(
                            productInstance,
                            description,
                            price
                    );
                    final Map<String, String> data = dataOf(price, description, offer.getCurrency());

                    final String key = productInstance.getTitle();
                    return commands.hmset(key,data)
                            .flatMap(success-> commands.expireat(key, expiryDate.toEpochSecond() ))
                            .map(ok -> offer) ;
                });
    }

    private Map<String, String> dataOf(final BigDecimal price, final String description, final Currency currency) {
        final Map<String, String> data = new LinkedHashMap<>(3);
        data.put("currency", currency.getCurrencyCode());
        data.put("price", price.toString());
        data.put("description" ,description);
        return data;
    }

    @Override
    public void createInitialRecommendations() {
        try {
            redisConnection.sync().flushall();
        } catch (Exception e) {
            log.error("Error flushing Redis data: " +e.getMessage(), e);
        }

        if(log.isInfoEnabled()) {
            log.info("Creating Initial Offers for Pets: {}", productClient.list().blockingGet());

        }
        productClient.find("harry potter")
                .doOnError(noProductFoundThrowableConsumer())
                .onErrorComplete()
                .subscribe(product -> {
                            final Mono<Offer> savedOffer = save(
                                    product,
                                    new BigDecimal("49.99"),
                                    "Book",
                                    Duration.of(2, ChronoUnit.HOURS)
                                    );
                            savedOffer.subscribe((offer) -> {
                            }, errorSavingThrowableConsumer());
                        }
                );

        productClient.find("porter")
                .doOnError(noProductFoundThrowableConsumer())
                .onErrorComplete()
                .subscribe(product -> {
                            final Mono<Offer> savedOffer = save(
                                    product,
                                    new BigDecimal("29.99"),
                                    "Special Porter! Offer ends soon!",
                                    Duration.of(2, ChronoUnit.HOURS));
                            savedOffer.subscribe((offer) -> {
                            }, errorSavingThrowableConsumer());
                        }
                );

        productClient.find("Bettle")
                .doOnError(noProductFoundThrowableConsumer())
                .onErrorComplete()
                .subscribe(product -> {
                            final Mono<Offer> savedOffer = save(
                                    product,
                                    new BigDecimal("3999.99"),
                                    "Carefree Car! Low Maintenance! Looking for a Home!",
                                    Duration.of(1, ChronoUnit.DAYS));
                            savedOffer.subscribe((offer) -> {
                            }, errorSavingThrowableConsumer());
                        }
                );
    }

    private java.util.function.Consumer<Throwable> errorSavingThrowableConsumer() {
        return throwable -> {
            if (log.isErrorEnabled()) {
                log.error("Error occurred saving offer: " + throwable.getMessage(), throwable);
            }
        };
    }

    private Consumer<Throwable> noProductFoundThrowableConsumer() {
        return throwable -> {
            if (log.isErrorEnabled()) {
                log.error("No product found: " + throwable.getMessage(), throwable);
            }
        };
    }

    private Function<String, Mono<? extends Offer>> keyToOffer(final RedisReactiveCommands<String, String> commands) {
        return key -> {
            final Flux<KeyValue<String, String>> values = commands.hmget(key, "price", "description");
            final Map<String, String> map = new HashMap<>(3);
            return values.reduce(map, (all, keyValue) -> {
                all.put(keyValue.getKey(), keyValue.getValue());
                return all;
            })
                    .map(ConvertibleValues::of)
                    .flatMap(entries -> {
                        final String description = entries.get("description", String.class).
                                orElseThrow(() -> new IllegalStateException("No description"));
                        final BigDecimal price = entries.get("price", BigDecimal.class).
                                orElseThrow(() -> new IllegalStateException("No price"));
                        final Flowable<Product> findProductFlowable = productClient.find(key).toFlowable();
                        return Mono.from(findProductFlowable).map(product -> new Offer(product, description, price));
                    });
        };
    }
}
