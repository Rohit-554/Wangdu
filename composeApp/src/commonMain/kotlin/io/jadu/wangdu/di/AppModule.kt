package io.jadu.wangdu.di

import io.jadu.wangdu.network.ApiService
import io.jadu.wangdu.network.createHttpClient
import io.jadu.wangdu.ui.viewmodel.HomeViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun appModule(): Module = module {
    single<HttpClient> { createHttpClient() }
    single { ApiService(get()) }


    viewModel { HomeViewModel() }
}

val appModule = listOf(
    appModule(),
    platformModule()
)
