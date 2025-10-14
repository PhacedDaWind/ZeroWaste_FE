package com.example.zerowaste.data.remote
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BrowseFoodItemApiService {

    @GET("api/browse-food/list")
    suspend fun getBrowseFoodItems(
        @Query("usersId") usersId: Long,
        @Query("category") category: String? = null,
        @Query("storageLocation") storageLocation: String? = null,
        @Query("convertToDonation") convertToDonation: Boolean? = null
    ): Response<PageWrapperVO<BrowseFoodItemResponse>>
}
