package com.example.sonor.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.sonor.audio.MusicController
import com.example.sonor.audio.MusicControllerImpl
import com.example.sonor.data.DatabaseHelper
import com.example.sonor.data.DatabaseHelperImpl
import com.example.sonor.data.repository.SongRepositoryImpl
import com.example.sonor.domain.repository.AuthRepository
import com.example.sonor.domain.repository.SongRepository
import com.example.sonor.sqldelight.SonorDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val androidModule: Module = module {
    // Database Driver
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = SonorDatabase.Schema,
            context = androidContext(),
            name = "sonor.db"
        )
    }

    // Database Instance
    single { SonorDatabase(get()) }
    
    // Database Helper (using the common implementation)
    single<DatabaseHelper> { DatabaseHelperImpl(get()) }
    
    // Repository
    single<SongRepository> { SongRepositoryImpl(androidContext(), get()) }
    
    // Music Controller
    single<MusicController> { MusicControllerImpl(androidContext()) }
    
    // Auth Repository implementation
    single<AuthRepository> {
        object : AuthRepository {
            private val _isAuthenticated = MutableStateFlow(false)
            override val isAuthenticated = _isAuthenticated
            
            override suspend fun login(email: String, password: String): Result<Unit> {
                _isAuthenticated.value = true
                return Result.success(Unit)
            }
            
            override suspend fun signUp(email: String, password: String): Result<Unit> {
                _isAuthenticated.value = true
                return Result.success(Unit)
            }
            
            override suspend fun logout() {
                _isAuthenticated.value = false
            }
        }
    }
}
