package com.example.ecommerce;

import com.example.ecommerce.service.InventoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InventoryServiceTest {

    @Autowired
    private InventoryService service;

    @Test
    void testSupplyAndReserve() {
        service.createSupply("item-1", 10);
        boolean reserved = service.reserveItem("item-1", 3);
        Assertions.assertTrue(reserved);
        int availability = service.getAvailability("item-1");
        Assertions.assertEquals(7, availability);
    }
}
