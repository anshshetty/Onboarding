package dev.ansh.onboarding.onboarding.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.ansh.onboarding.onboarding.data.api.EducationApi
import dev.ansh.onboarding.onboarding.data.repository.EducationRepositoryImpl
import dev.ansh.onboarding.onboarding.domain.EducationRepository
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Network configuration and dependency injection
 */
object NetworkModule {
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    fun provideOkHttpClient(cacheDir: File): OkHttpClient {
        val cache = Cache(
            directory = File(cacheDir, "http_cache"),
            maxSize = 10L * 1024L * 1024L // 10 MB
        )
        
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://myjar.app/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    fun provideEducationApi(retrofit: Retrofit): EducationApi {
        return retrofit.create(EducationApi::class.java)
    }
    
    fun provideEducationRepository(api: EducationApi): EducationRepository {
        return EducationRepositoryImpl(api)
    }
}
