package org.leandro.catalogue.service;


import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Singleton;

@Getter
@Setter
@ConfigurationProperties("catalogue")
@Singleton
public class CatalogueConfiguration {

    private String databaseName = "catalogue_db";
    private String collectionName = "catalogue";
}
