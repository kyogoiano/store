package org.leandro.inventory.service;

import com.mongodb.reactivestreams.client.FindPublisher;
import org.leandro.inventory.entity.ProductInventory;

public interface InventoryService {

    FindPublisher<ProductInventory> findByBarCode(String barCode);
}
