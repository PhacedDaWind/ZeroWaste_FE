package com.example.zerowaste.enums

/**
 * A Kotlin enum that mirrors the Java backend enum `FoodItemActionType.java`.
 */
enum class FoodItemActionType(val label: String) {
    MARK_AS_USED("Mark as used"),
    PLAN_FOR_MEAL("Plan for meal"),
    FLAG_FOR_DONATION("Flag for donation")
}
