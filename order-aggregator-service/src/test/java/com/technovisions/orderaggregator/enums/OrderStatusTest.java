package com.technovisions.orderaggregator.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStatusTest {

    @ParameterizedTest
    @ValueSource(strings = {"CREATED", "created", "Preparing", "FINISHED"})
    void parse_succeeds_forValidValuesCaseInsensitive(String raw) {
        assertThat(OrderStatus.parse(raw)).isNotNull();
    }

    @Test
    void parse_throwsIllegalArgument_forUnknownValue() {
        assertThatThrownBy(() -> OrderStatus.parse("SHIPPED"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SHIPPED");
    }

    @Test
    void parse_throwsIllegalArgument_forBlankValue() {
        assertThatThrownBy(() -> OrderStatus.parse(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
