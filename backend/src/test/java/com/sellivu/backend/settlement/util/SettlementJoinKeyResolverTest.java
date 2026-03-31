package com.sellivu.backend.settlement.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettlementJoinKeyResolverTest {

    @Test
    @DisplayName("productOrderNo가 있으면 P: 접두사로 joinKey를 만든다")
    void resolve_shouldUseProductOrderNoFirst() {
        assertEquals("P:PO123", SettlementJoinKeyResolver.resolve("PO123", "O999"));
    }

    @Test
    @DisplayName("productOrderNo가 공백이면 orderNo로 O: joinKey를 만든다")
    void resolve_shouldFallbackToOrderNo() {
        assertEquals("O:O999", SettlementJoinKeyResolver.resolve("   ", "O999"));
    }

    @Test
    @DisplayName("앞뒤 공백을 trim 처리한다")
    void resolve_shouldTrimValues() {
        assertEquals("P:PO123", SettlementJoinKeyResolver.resolve("  PO123  ", "  O999  "));
        assertEquals("O:O999", SettlementJoinKeyResolver.resolve(null, "  O999  "));
    }

    @Test
    @DisplayName("둘 다 null 또는 공백이면 null을 반환한다")
    void resolve_shouldReturnNullWhenBothMissing() {
        assertNull(SettlementJoinKeyResolver.resolve(null, null));
        assertNull(SettlementJoinKeyResolver.resolve("", "   "));
        assertNull(SettlementJoinKeyResolver.resolve("   ", null));
    }
}