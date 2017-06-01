package com.simonbtomlinson.magicfetchbot.dagger

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(CommonModule::class))
interface CommonComponent {
	fun objectMapper(): ObjectMapper
}