package com.technovisions.orderaggregator.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateCustomerRequestValidationTest {

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
        CreateCustomerRequest request = new CreateCustomerRequest("Jane Doe", "jane@example.com", "+1 234-567-8900");

        Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void validate_rejectsBlankFullName(String fullName) {
        CreateCustomerRequest request = new CreateCustomerRequest(fullName, "jane@example.com", "+1234567890");

        Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fullName"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-an-email", "missing-at-sign.com", "@no-local-part.com", "spaces in@email.com"})
    void validate_rejectsMalformedEmail(String email) {
        CreateCustomerRequest request = new CreateCustomerRequest("Jane Doe", email, "+1234567890");

        Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "phone!!", "12", "not-a-phone-number-at-all-way-too-long-1234567890"})
    void validate_rejectsMalformedPhone(String phone) {
        CreateCustomerRequest request = new CreateCustomerRequest("Jane Doe", "jane@example.com", phone);

        Set<ConstraintViolation<CreateCustomerRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("phone"));
    }
}
