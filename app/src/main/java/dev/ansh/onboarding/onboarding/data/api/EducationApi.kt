package dev.ansh.onboarding.onboarding.data.api

import dev.ansh.onboarding.onboarding.data.model.EducationResponse
import retrofit2.http.GET

/**
 * Retrofit API interface for education metadata
 */
interface EducationApi {
    
    /**
     * Fetches education metadata from the JAR app
     * @return EducationResponse containing onboarding configuration and content
     */
    @GET("_assets/shared/education-metadata.json")
    suspend fun getEducation(): EducationResponse
}
