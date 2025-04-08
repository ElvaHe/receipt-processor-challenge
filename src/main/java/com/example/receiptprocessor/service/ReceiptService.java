package com.example.receiptprocessor.service;

import com.example.receiptprocessor.model.Receipt;
import com.example.receiptprocessor.model.ReceiptItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class ReceiptService {
    private static boolean LARGE_LANGUAGE_MODEL = false;

    public static boolean isLargeLanguageModel() {
        return LARGE_LANGUAGE_MODEL;
    }

    public static void setLargeLanguageModel(boolean largeLanguageModel) {
        LARGE_LANGUAGE_MODEL = largeLanguageModel;
    }

    public static int calculatePoints(Receipt receipt) {
        int points = 0;

        // One point for every alphanumeric character in the retailer name.
        String retailer = receipt.getRetailer();
        for (int i = 0; i < retailer.length(); i++) {
            if (Character.isLetterOrDigit(retailer.charAt(i))) {
                points++;
            }
        }

        // Parse the total as a BigDecimal.
        BigDecimal total = new BigDecimal(receipt.getTotal());

        // 50 points if the total is a round dollar amount with no cents.
        if (total.stripTrailingZeros().scale() <= 0 || total.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            points += 50;
        }

        // 25 points if the total is a multiple of 0.25.
        BigDecimal quarter = new BigDecimal("0.25");
        if (total.remainder(quarter).compareTo(BigDecimal.ZERO) == 0) {
            points += 25;
        }

        // 5 points for every two items on the receipt.
        if (receipt.getItems() != null) {
            points += (receipt.getItems().size() / 2) * 5;

            // If the trimmed length of the item description is a multiple of 3,
            // multiply the price by 0.2 and round up to the nearest integer.
            for (ReceiptItem item : receipt.getItems()) {
                if (item.getShortDescription() != null) {
                    String description = item.getShortDescription().trim();
                    if (description.length() % 3 == 0) {
                            BigDecimal itemPrice = new BigDecimal(item.getPrice());
                            BigDecimal itemPoints = itemPrice.multiply(new BigDecimal("0.2"));
                            int roundedPoints = itemPoints.setScale(0, RoundingMode.CEILING).intValue();
                            points += roundedPoints;
                    }
                }
            }
        }

        // 5 points if the total is greater than 10.00.
        if (LARGE_LANGUAGE_MODEL && total.compareTo(new BigDecimal("10.00")) > 0) {
            points += 5;
        }

        // 6 points if the day in the purchase date is odd.
        LocalDate purchaseDate = LocalDate.parse(receipt.getPurchaseDate(), DateTimeFormatter.ISO_DATE);
        if (purchaseDate.getDayOfMonth() % 2 == 1) {
            points += 6;
        }


        // 10 points if the time of purchase is after 2:00pm and before 4:00pm.
        LocalTime purchaseTime = LocalTime.parse(receipt.getPurchaseTime(), DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime start = LocalTime.of(14, 0);
        LocalTime end = LocalTime.of(16, 0);
        if (!purchaseTime.isBefore(start) && purchaseTime.isBefore(end)) {
            points += 10;
        }

        return points;
    }
}
