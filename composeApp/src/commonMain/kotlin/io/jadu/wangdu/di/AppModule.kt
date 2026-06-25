package io.jadu.wangdu.di

import io.jadu.wangdu.data.repository.WhiteBoardRepositoryImpl
import io.jadu.wangdu.domain.repository.WhiteBoardRepository
import io.jadu.wangdu.network.ApiService
import io.jadu.wangdu.network.httpClient
import io.jadu.wangdu.ui.viewmodel.WhiteBoardViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun appModule(): Module = module {
    single<HttpClient> { httpClient() }
    single { ApiService(get()) }
    viewModel { WhiteBoardViewModel(get()) }
    single<WhiteBoardRepository> { WhiteBoardRepositoryImpl(get()) }
}

val appModule = listOf(
    appModule(),
    platformModule()
)
