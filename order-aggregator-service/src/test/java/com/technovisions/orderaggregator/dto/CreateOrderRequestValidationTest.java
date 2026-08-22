package com.technovisions.orderaggregator.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateOrderRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    @Test
    void validate_hasNoViolations_forFullyValidRequest() {
        CreateOrderRequest request = new CreateOrderRequest(1L, "Widget", 1, new BigDecimal("0.00"));

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void validate_rejectsZeroQuantity() {
        CreateOrderRequest request = new CreateOrderRequest(1L, "Widget", 0, BigDecimal.TEN);

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("quantity"));
    }

    @Test
    void validate_rejectsNegativeQuantity() {
        CreateOrderRequest request = new CreateOrderRequest(1L, "Widget", -5, BigDecimal.TEN);

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("quantity"));
    }

    @Test
    void validate_rejectsNegativePrice() {
        CreateOrderRequest request = new CreateOrderRequest(1L, "Widget", 1, new BigDecimal("-0.01"));

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("price"));
    }

    @Test
    void validate_rejectsBlankProductName() {
        CreateOrderRequest request = new CreateOrderRequest(1L, "  ", 1, BigDecimal.TEN);

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("productName"));
    }

    @Test
    void validate_rejectsMissingCustomerId() {
        CreateOrderRequest request = new CreateOrderRequest(null, "Widget", 1, BigDecimal.TEN);

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("customerId"));
    }
}
