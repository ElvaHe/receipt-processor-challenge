package com.example.receiptprocessor.controller;

import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.service.ReceiptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/receipts")
public class ReceiptController {

    private Map<String, Receipt> receiptStore = new ConcurrentHashMap<>();
    private Map<String, Integer> pointsStore = new ConcurrentHashMap<>();

    /**
     * POST /receipts/process
     * Accepts a JSON receipt, calculates points and returns an ID.
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, String>> processReceipt(@RequestBody Receipt receipt) {
        String id = UUID.randomUUID().toString();
        receiptStore.put(id, receipt);

        int points = ReceiptService.calculatePoints(receipt);
        pointsStore.put(id, points);

        Map<String, String> response = new HashMap<>();
        response.put("id", id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * GET /receipts/{id}/points
     * Returns the points awarded for a given receipt ID.
     */
    @GetMapping("/{id}/points")
    public ResponseEntity<Map<String, Integer>> getPoints(@PathVariable String id) {
        if (!pointsStore.containsKey(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Map<String, Integer> response = new HashMap<>();
        response.put("points", pointsStore.get(id));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
