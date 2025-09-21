package dev.ansh.onboarding.onboarding.data.repository

import dev.ansh.onboarding.onboarding.data.api.EducationApi
import dev.ansh.onboarding.onboarding.data.model.ManualBuyEducationData
import dev.ansh.onboarding.onboarding.domain.EducationRepositoryInterface
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Repository for education data with in-memory caching
 */
class EducationRepository(
    private val api: EducationApi
) : EducationRepositoryInterface {
    
    private var cachedData: ManualBuyEducationData? = null
    private val cacheMutex = Mutex()
    
    /**
     * Loads education data from API with in-memory caching
     * @return ManualBuyEducationData containing onboarding configuration
     * @throws Exception if API call fails or response is invalid
     */
    override suspend fun loadEducation(): ManualBuyEducationData {
        return cacheMutex.withLock {
            cachedData ?: run {
                val response = api.getEducation()
                if (response.success) {
                    response.data.manualBuyEducationData.also { 
                        cachedData = it 
                    }
                } else {
                    throw IllegalStateException("API returned success: false")
                }
            }
        }
    }
    
    /**
     * Clears cached data - useful for testing or forced refresh
     */
    override suspend fun clearCache() {
        cacheMutex.withLock {
            cachedData = null
        }
    }
}
