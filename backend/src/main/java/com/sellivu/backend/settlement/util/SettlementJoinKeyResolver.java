package com.sellivu.backend.settlement.util;

public final class SettlementJoinKeyResolver {

    private SettlementJoinKeyResolver() {
    }

    public static String resolve(String productOrderNo, String orderNo) {
        if (productOrderNo != null && !productOrderNo.trim().isEmpty()) {
            return "P:" + productOrderNo.trim();
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            return "O:" + orderNo.trim();
        }
        return null;
    }
}