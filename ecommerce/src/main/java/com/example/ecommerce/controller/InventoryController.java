package com.example.ecommerce.controller;


import com.example.ecommerce.dto.InventoryRequest;
import com.example.ecommerce.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @PostMapping("/supply")
    public ResponseEntity<String> supply(@RequestBody InventoryRequest request) {
        service.createSupply(request.getItemId(), request.getQuantity());
        return ResponseEntity.ok("Supply created");
    }

    @PostMapping("/reserve")
    public ResponseEntity<String> reserve(@RequestBody InventoryRequest request) {
        boolean reserved = service.reserveItem(request.getItemId(), request.getQuantity());
        return reserved ? ResponseEntity.ok("Reserved") :
                ResponseEntity.status(HttpStatus.CONFLICT).body("Not enough quantity");
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancel(@RequestBody InventoryRequest request) {
        service.cancelReservation(request.getItemId(), request.getQuantity());
        return ResponseEntity.ok("Reservation cancelled");
    }

    @GetMapping("/{itemId}/availability")
    public ResponseEntity<Integer> getAvailability(@PathVariable String itemId) {
        int available = service.getAvailability(itemId);
        return ResponseEntity.ok(available);
    }
}
