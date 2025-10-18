package com.example.zerowaste.data.remote
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BrowseFoodItemApiService {

    /**
     * Fetches a paginated, filtered, and sorted list of food items.
     * Note: All filter parameters are nullable, so they are only sent if a value is provided.
     */
    @GET("api/browse-food/list")
    suspend fun getBrowseList(
        // Pagination parameters from BaseViewOption
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("searchQuery") searchQuery: String? = null,

        // Filter parameters from BrowseFoodItemReqDTO
        @Query("usersId") usersId: Long? = null,
        @Query("convertToDonation") convertToDonation: Boolean? = null,
        @Query("category") category: String? = null,
        @Query("expiryDate") expiryDate: String? = null, // Dates are sent as strings
        @Query("storageLocation") storageLocation: String? = null,
        @Query("actionType") actionType: String? = null,

        // Sorting parameter
        // Retrofit will automatically format this as ?sort=itemName&sort=-expiryDate
        @Query("sort") sort: List<String>? = null
    ): ApiResponse<PageWrapper<BrowseFoodItemResponse>>

    // --- NEW: Food Item Detail Endpoint ---
    @GET("api/food-inventory/{id}")
    suspend fun getFoodItemDetail(@Path("id") itemId: Long): ApiResponse<FoodItemDetailResponse>

    // --- NEW: Update Action Type Endpoint ---
    // This uses @PUT and sends the action type as a @Query parameter
    @PUT("api/browse-food/update/{id}")
    suspend fun updateFoodItemActionType(
        @Path("id") itemId: Long,
        @Query("foodItemActionType") actionType: String
    ): ApiResponse<Any> // Assuming we only care about the success/failure
}
