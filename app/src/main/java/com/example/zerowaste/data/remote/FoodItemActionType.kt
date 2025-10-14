// FoodItemActionType.kt
enum class FoodItemActionType(val label: String) {
    MARK_AS_USED("Mark as used"),
    PLAN_FOR_MEAL("Plan for meal"),
    FLAG_FOR_DONATION("Flag for donation");

    companion object {
        fun fromValue(value: String): FoodItemActionType? {
            return values().find { it.name == value }
        }

        fun getLabelForValue(value: String?): String {
            return fromValue(value ?: "")?.label ?: "No Action"
        }
    }
}