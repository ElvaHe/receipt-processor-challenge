package com.example.receiptprocessor;

import com.example.receiptprocessor.model.*;
import com.example.receiptprocessor.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReceiptProcessorApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Positive Test: process a valid receipt and get points.
     */
    @Test
    void testProcessAndGetPoints() throws Exception {
        Receipt receipt = new Receipt();
        receipt.setRetailer("Target");
        receipt.setPurchaseDate("2022-01-01");
        receipt.setPurchaseTime("13:01");
        receipt.setTotal("35.35");

        List<ReceiptItem> items = Arrays.asList(
                new ReceiptItem("Mountain Dew 12PK", "6.49"),
                new ReceiptItem("Emils Cheese Pizza", "12.25"),
                new ReceiptItem("Knorr Creamy Chicken", "1.26"),
                new ReceiptItem("Doritos Nacho Cheese", "3.35"),
                new ReceiptItem("   Klarbrunn 12-PK 12 FL OZ  ", "12.00")
        );
        receipt.setItems(items);

        String receiptJson = objectMapper.writeValueAsString(receipt);

        String responseBody = mockMvc.perform(post("/receipts/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(receiptJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(responseBody).get("id").asText();
        assertThat(id).isNotBlank();

        mockMvc.perform(get("/receipts/" + id + "/points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.points").value(28)); // Based on the example breakdown
    }

    /**
     * Negative Test: process invalid ID return 404.
     */
    @Test
    void testGetPointsWithInvalidId() throws Exception {
        mockMvc.perform(get("/receipts/invalid-id/points"))
                .andExpect(status().isNotFound());
    }

    /**
     * Unit Test: test point calculation logic for example.
     */
    @Test
    void testCalculatePointsEdgeCase() {
        Receipt receipt = new Receipt();
        receipt.setRetailer("M&M Corner Market");
        receipt.setPurchaseDate("2022-03-20");
        receipt.setPurchaseTime("14:33");
        receipt.setTotal("9.00");

        List<ReceiptItem> items = Arrays.asList(
                new ReceiptItem("Gatorade", "2.25"),
                new ReceiptItem("Gatorade", "2.25"),
                new ReceiptItem("Gatorade", "2.25"),
                new ReceiptItem("Gatorade", "2.25")
        );
        receipt.setItems(items);

        int points = ReceiptService.calculatePoints(receipt);
        assertThat(points).isEqualTo(109);
    }
}
