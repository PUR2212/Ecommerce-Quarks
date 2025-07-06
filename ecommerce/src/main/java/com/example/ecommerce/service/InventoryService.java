package com.example.ecommerce.service;

import com.example.ecommerce.entity.Inventory;
import com.example.ecommerce.repository.InventoryRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryService {

    private final InventoryRepository repository;
    private final RedisTemplate<String, Integer> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "inventory:";

    public InventoryService(InventoryRepository repository, RedisTemplate<String, Integer> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public void createSupply(String itemId, int quantity) {
        Inventory inventory = repository.findByItemId(itemId)
                .orElse(new Inventory());
        inventory.setItemId(itemId);
        inventory.setTotalQuantity(inventory.getTotalQuantity() + quantity);
        inventory.setUpdatedAt(LocalDateTime.now());
        repository.save(inventory);
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + itemId, inventory.getTotalQuantity() - inventory.getReservedQuantity());
    }

    @Transactional
    public boolean reserveItem(String itemId, int quantity) {
        Inventory inventory = repository.lockInventoryByItemId(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        int available = inventory.getTotalQuantity() - inventory.getReservedQuantity();
        if (available < quantity) return false;
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        repository.save(inventory);
        redisTemplate.opsForValue().decrement(REDIS_KEY_PREFIX + itemId, quantity);
        return true;
    }

    @Transactional
    public void cancelReservation(String itemId, int quantity) {
        Inventory inventory = repository.lockInventoryByItemId(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        repository.save(inventory);
        redisTemplate.opsForValue().increment(REDIS_KEY_PREFIX + itemId, quantity);
    }

    public int getAvailability(String itemId) {
        String redisKey = REDIS_KEY_PREFIX + itemId;
        Integer cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null) return cached;

        Inventory inventory = repository.findByItemId(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        int available = inventory.getTotalQuantity() - inventory.getReservedQuantity();
        redisTemplate.opsForValue().set(redisKey, available);
        return available;
    }
}

