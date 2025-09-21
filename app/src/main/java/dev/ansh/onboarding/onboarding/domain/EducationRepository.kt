package dev.ansh.onboarding.onboarding.domain

import dev.ansh.onboarding.onboarding.data.model.ManualBuyEducationData

/**
 * Interface for education data repository
 */
interface EducationRepository {

    /**
     * Loads education data from API with in-memory caching
     * @return ManualBuyEducationData containing onboarding configuration
     * @throws Exception if API call fails or response is invalid
     */
    suspend fun loadEducation(): ManualBuyEducationData

    /**
     * Clears cached data - useful for testing or forced refresh
     */
    suspend fun clearCache()
}