package com.example.map.tools

import android.graphics.Bitmap
import android.net.Uri
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequestBuilder
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executors
import kotlin.coroutines.resume

object FrescoTools {

	suspend fun fetchBitmap(url: String): Bitmap? {
		return fetchBitmap(Uri.parse(url))
	}

	private suspend fun fetchBitmap(uri: Uri): Bitmap? {
		return suspendCancellableCoroutine { cancellableContinuation ->
			if (Fresco.hasBeenInitialized()) {
				val dataSource = createDataSource(uri)
				dataSource.subscribe(object : BaseBitmapDataSubscriber() {
					public override fun onNewResultImpl(bitmap: Bitmap?) {
						cancellableContinuation.resume(bitmap)
						close()
					}

					override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
						cancellableContinuation.resume(null)
						close()
					}

					private fun close() {
						dataSource.close()
					}
				}, Executors.newSingleThreadExecutor())
			} else {
				cancellableContinuation.resume(null)
			}
		}
	}

	private fun createDataSource(uri: Uri): DataSource<CloseableReference<CloseableImage>> {
		return Fresco.getImagePipeline()
			.fetchDecodedImage(ImageRequestBuilder.newBuilderWithSource(uri).build(), null)
	}
}