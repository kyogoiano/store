package org.leandro.inventory.entity;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Introspected
@AllArgsConstructor
@Data
public class ProductInventory {

    @NonNull
    @NotBlank
    String barCode;

    @NonNull
    @NotNull
    Integer stock;

}
