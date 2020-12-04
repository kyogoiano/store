package org.leandro.api.v1.model;


import io.micronaut.core.annotation.Introspected;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Introspected
@ToString
public class Offer {
    Product product;
    String description;
    BigDecimal price;

    public Offer(final Product product, final String description, final BigDecimal price) {
        this.product = product;
        this.description = description;
        this.price = price;
    }

    Currency currency = Currency.getInstance(Locale.getDefault());
}
