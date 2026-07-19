package com.example.sonor.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.sonor.audio.MusicController
import com.example.sonor.audio.MusicControllerImpl
import com.example.sonor.data.DatabaseHelper
import com.example.sonor.data.DatabaseHelperImpl
import com.example.sonor.domain.repository.SongRepository
import com.example.sonor.data.repository.SongRepositoryImpl
import com.example.sonor.domain.repository.AuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

val desktopModule: Module = module {
    // Database Driver
    single {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        // Create schema
        val schema = File("shared/src/commonMain/sqldelight/com/example/sonor/sqldelight/SonorDatabase.sq")
        if (schema.exists()) {
            // SQLDelight will handle schema creation automatically
        }
        driver
    }
    
    // Database Helper
    single<DatabaseHelper> { DatabaseHelperImpl(get()) }
    
    // Repository
    single<SongRepository> { SongRepositoryImpl(get()) }
    
    // Music Controller
    single<MusicController> { MusicControllerImpl() }
    
    // Auth Repository (placeholder implementation)
    single<AuthRepository> {
        object : AuthRepository {
            override val isAuthenticated = kotlinx.coroutines.flow.MutableStateFlow(false)
            override suspend fun login(email: String, password: String) = Result.success(Unit)
            override suspend fun signUp(email: String, password: String) = Result.success(Unit)
            override suspend fun logout() {}
        }
    }
}
