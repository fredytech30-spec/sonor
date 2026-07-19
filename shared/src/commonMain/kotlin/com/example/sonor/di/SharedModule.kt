package com.example.sonor.di

import com.example.sonor.presentation.viewmodel.AuthViewModel
import com.example.sonor.presentation.viewmodel.HomeViewModel
import com.example.sonor.presentation.viewmodel.LibraryViewModel
import com.example.sonor.presentation.viewmodel.PlaylistViewModel
import com.example.sonor.presentation.viewmodel.SearchViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sharedModule: Module = module {
    // ViewModels — registered as ViewModel so Koin creates them via ViewModelFactory
    viewModel { HomeViewModel(get(), get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { LibraryViewModel(get()) }
    viewModel { PlaylistViewModel(get()) }
    viewModel { SearchViewModel(get()) }
}
