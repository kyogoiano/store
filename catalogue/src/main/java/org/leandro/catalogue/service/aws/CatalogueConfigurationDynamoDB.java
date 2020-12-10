package org.leandro.catalogue.service.aws;


import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.inject.Singleton;

@Getter
@Setter
@ConfigurationProperties("catalogue")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Singleton
public class CatalogueConfigurationDynamoDB {

    String databaseName = "catalogue_db";
    String tableName = "catalogue";
    String catalogueVendor = "vendor";
    String catalogueTitle = "title";
    String catalogueDescription = "description";

}
