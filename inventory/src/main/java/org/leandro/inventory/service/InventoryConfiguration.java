package org.leandro.inventory.service;


import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Singleton;

@Getter
@Setter
@ConfigurationProperties("inventory")
@Singleton
public class InventoryConfiguration {

    private String databaseName = "store";
    private String collectionName = "inventory";
}
