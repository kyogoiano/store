package org.leandro.api.v1.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Introspected
@ToString
public class Vendor {
    String name;
    List<Product> products = Collections.emptyList();

    @JsonCreator
    public Vendor(@JsonProperty("name") final String name) {
        this.name = name;
    }
}
