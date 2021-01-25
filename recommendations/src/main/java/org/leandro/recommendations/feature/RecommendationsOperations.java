package org.leandro.recommendations.feature;

import org.leandro.api.v1.model.Offer;
import org.leandro.api.v1.model.Product;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

public interface RecommendationsOperations {
       /**
         * Save an offer for the given pet, vendor etc.
         *
         * @param product the product object
         * @param price The price
         * @param description The description of the offer
         * @param duration The duration of the offer
         * @return The offer if it was possible to save it as a {@link Mono} or a empty {@link Mono} if no product exists to create the offer for
         */
        Mono<Offer> save(
                Product product,
                BigDecimal price,
                String description,
                Duration duration);

        void createInitialRecommendations();
}
