package org.leandro.catalogue.integrated.controller.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.leandro.api.v1.model.Product;
import org.leandro.api.v1.model.ProductType;

@Setter
public class CatalogueEntity extends Product {
    @BsonCreator
    @JsonCreator
    public CatalogueEntity(@JsonProperty("vendor") @BsonProperty("vendor") final String vendor,
                           @JsonProperty("title") @BsonProperty("title") final String title,
                           @JsonProperty("description") @BsonProperty("description") final String description) {
        super(vendor, title, description);
    }

    @Override
    public CatalogueEntity type(ProductType type) {
        return (CatalogueEntity) super.type(type);
    }

}
