package com.example.map.di

//@InstallIn(SingletonComponent::class)
//@Module
//object ReposModule {
//	@Singleton
//	@Provides
//	fun provideLocationRepo(
//		locationDao: LocationDao,
//		locationManager: BackgroundLocationManager,
//		@Dispatcher(AppDispatchers.IO) ioDispatcher: CoroutineDispatcher,
//		scope: CoroutineScope
//	): LocationRepo {
//		return LocationRepo(locationDao, locationManager, scope, ioDispatcher)
//	}
//}