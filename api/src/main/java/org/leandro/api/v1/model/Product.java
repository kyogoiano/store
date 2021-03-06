package org.leandro.api.v1.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.validation.Validated;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;

@Validated
@EqualsAndHashCode
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Introspected
@ToString
public class Product {

    @JsonCreator
    public Product(@JsonProperty("vendor") final String vendor, @JsonProperty("title") final String title,
                   @JsonProperty("description") final String description) {
        this.vendor = vendor;
        this.title = title;
        this.description = description;
    }

    @NotBlank
    String title;
    String description;
    String vendor;
    ProductType type = ProductType.UNDEFINED;

    public Product type(final ProductType type){
        if(type != null) {
            this.type = type;
        }
        return this;
    }
    public Product title(String title) {
        if(title != null) {
            this.title = title;
        }
        return this;
    }


}
