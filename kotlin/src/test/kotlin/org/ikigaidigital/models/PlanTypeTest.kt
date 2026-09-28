package org.ikigaidigital.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

internal class PlanTypeTest {
    @ParameterizedTest(name = "should return {1} when value = \"{0}\"")
    @CsvSource(
        "basic, BASIC",
        "student, STUDENT",
        "premium, PREMIUM",
    )
    fun `should return matching plan type when value is a known lowercase plan type`(
        value: String,
        expectedPlanType: PlanType,
    ) {
        // execute
        val planType = PlanType.fromString(value)

        // verify
        assertThat(planType).isEqualTo(expectedPlanType)
    }

    @ParameterizedTest(name = "should return UNDEFINED when value = \"{0}\"")
    @ValueSource(strings = ["", " ", "other", "gold", "Basic", "STUDENT", "Premium", " basic", "basic ", "undefined"])
    fun `should return undefined when value is not an exact lowercase plan type`(value: String) {
        // execute
        val planType = PlanType.fromString(value)

        // verify
        assertThat(planType).isEqualTo(PlanType.UNDEFINED)
    }
}
