package com.example.zerowaste.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface FoodInventoryApiService {

    /**
     * Gets a single, detailed food item.
     * Maps to: @GetMapping("/{id}")
     */
    @GET("api/food-inventory/{id}")
    suspend fun getFoodItem(@Path("id") itemId: Long): ApiResponse<FoodItemResponse>

    /**
     * Creates a new food item.
     * Maps to: @PostMapping("/create")
     */
    @POST("api/food-inventory/create")
    suspend fun createFoodItem(@Body item: FoodItemRequest): ApiResponse<FoodItemResponse>

    /**
     * Updates an existing food item.
     * Maps to: @PutMapping("/{id}")
     */
    @PUT("api/food-inventory/{id}")
    suspend fun updateFoodItem(
        @Path("id") itemId: Long,
        @Body item: FoodItemRequest
    ): ApiResponse<FoodItemResponse>

    /**
     * Deletes a food item.
     * Maps to: @DeleteMapping("/{id}")
     */
    @DELETE("api/food-inventory/{id}")
    suspend fun deleteFoodItem(@Path("id") itemId: Long): ApiResponse<String>

}