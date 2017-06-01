package com.simonbtomlinson.magicfetchbot.dagger

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class CommonModule {

	@Provides @Singleton
	fun provideObjectMapper(): ObjectMapper {
		return jacksonObjectMapper()
				.findAndRegisterModules()
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
	}
}