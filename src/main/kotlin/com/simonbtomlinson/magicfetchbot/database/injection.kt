package com.simonbtomlinson.magicfetchbot.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dagger.Component
import dagger.Module
import dagger.Provides
import java.util.*
import javax.inject.Named
import javax.inject.Singleton
import javax.sql.DataSource


@Singleton
@Component(modules = arrayOf(DatabaseModule::class))
interface DatabaseComponent {
	fun connectionManager(): ConnectionManager
	fun databaseManager(): DatabaseManager
	fun magicSetDAO(): MagicSetDAO
	fun magicCardDAO(): MagicCardDAO
	fun magicPrintingDAO(): MagicPrintingDAO
}

@Module
class DatabaseModule(
		@get:Provides @get:Singleton @get:Named("databaseUser") val databaseUser: String,
		@get:Provides @get:Singleton @get:Named("databasePassword") val databasePassword: String,
		@get:Provides @get:Singleton @get:Named("databaseName") val databaseName: String,
		@get:Provides @get:Singleton @get:Named("databaseServerName") val serverName: String,
		@get:Provides @get:Singleton @get:Named("databasePortNumber") val portNumber: Int
) {

	@Provides
	@Singleton
	fun provideHikariConfig(
			@Named("databaseUser") databaseUser: String,
			@Named("databasePassword") databasePassword: String,
			@Named("databaseName") databaseName: String,
			@Named("databaseServerName") databaseServerName: String,
			@Named("databasePortNumber") databasePortNumber: Int
	): HikariConfig {
		val props = Properties()
		props["dataSourceClassName"] = "org.postgresql.ds.PGSimpleDataSource"
		props["dataSource.user"] = databaseUser
		props["dataSource.password"] = databasePassword
		props["dataSource.databaseName"] = databaseName
		props["dataSource.serverName"] = databaseServerName
		props["dataSource.portNumber"] = databasePortNumber
		return HikariConfig(props)
	}

	@Provides
	@Singleton
	fun provideDataSource(config: HikariConfig): DataSource = HikariDataSource(config)

	@Provides
	@Singleton
	fun provideConnectionManager(dataSource: DataSource) = ConnectionManager(dataSource)

	@Provides
	@Singleton
	fun provideMagicSetDAO(connectionManager: ConnectionManager) = MagicSetDAO(connectionManager)

	@Provides @Singleton
	fun provideMagicCardDAO(connectionManager: ConnectionManager) = MagicCardDAO(connectionManager)

	@Provides @Singleton
	fun provideMagicPrintingDAO(connectionManager: ConnectionManager) = MagicPrintingDAO(connectionManager)

	@Provides @Singleton
	fun provideDatabaseManager(connectionManager: ConnectionManager) = DatabaseManager(connectionManager)
}