package com.example.map

import android.app.Application
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.common.util.ByteConstants
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import okhttp3.OkHttpClient
import org.apache.http.conn.ssl.SSLSocketFactory
import java.util.concurrent.TimeUnit

class MapApplication : Application() {
	override fun onCreate() {
		super.onCreate()

		val diskCacheConfig = DiskCacheConfig.newBuilder(this)
			.setMaxCacheSize((200 * ByteConstants.MB).toLong())
			.build()

		val client = OkHttpClient.Builder()
			.followRedirects(true)
			.followSslRedirects(true)
			.hostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER)
			.readTimeout(DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
			.writeTimeout(DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
			.connectTimeout(DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
			.build()

		val imagePipelineConfig = OkHttpImagePipelineConfigFactory.newBuilder(this, client)
			.setMainDiskCacheConfig(diskCacheConfig)
			.setDownsampleEnabled(true)
			.build()

		Fresco.initialize(this, imagePipelineConfig)
	}

	companion object {
		private const val DEFAULT_TIMEOUT_MILLIS = 60_000L
	}
}