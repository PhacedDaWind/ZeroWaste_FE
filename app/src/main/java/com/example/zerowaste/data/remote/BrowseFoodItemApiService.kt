package com.example.zerowaste.data.remote

import com.example.zerowaste.enums.FoodItemActionType
import retrofit2.http.*

/**
 * Retrofit interface perfectly matching your `BrowseFoodItemController.java`.
 */
interface FoodApiService {

    @GET("api/browse-food/list")
    suspend fun getBrowseList(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("usersId") usersId: Long?,
        @Query("convertToDonation") convertToDonation: Boolean?,
        @Query("category") category: String?,
        @Query("expiryDate") expiryDate: String?,
        @Query("storageLocation") storageLocation: String?,
        @Query("actionType") actionType: FoodItemActionType?
    ): ResponseDTO<PageWrapperVO<BrowseFoodItemResDTO>>

    @PUT("api/browse-food/update/{id}")
    suspend fun chooseActionType(
        @Path("id") id: Long,
        @Query("foodItemActionType") foodItemActionType: FoodItemActionType
    ): ResponseDTO<Unit> // Assuming success response data is not needed
}
