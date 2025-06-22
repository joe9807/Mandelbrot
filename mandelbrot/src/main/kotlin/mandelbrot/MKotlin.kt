package mandelbrot

import kotlinx.coroutines.*
import org.eclipse.swt.graphics.ImageData

class MKotlin {
	companion object{
		@JvmStatic
		suspend fun computeSuspend(imageData: ImageData, parameters:Parameters) = coroutineScope {
			(0 until imageData.width).map { x ->
				async(Dispatchers.Default) {
					for (y in 0 until imageData.height) {
						imageData.setPixel(x, y, Utils.getColor(Utils.getPointIterations(parameters.getUnScaledX(x.toDouble()), parameters.getUnScaledY(y.toDouble()), parameters), parameters))
					}
				}
			}.awaitAll()
		}

		@JvmStatic
		fun compute(imageData: ImageData, parameters:Parameters) = runBlocking {
			List(imageData.width){ x->
				launch(Dispatchers.Default){
					for (y in 0 until imageData.height){
						imageData.setPixel(x, y, Utils.getColor(Utils.getPointIterations(parameters.getUnScaledX(x.toDouble()), parameters.getUnScaledY(y.toDouble()), parameters), parameters))
					}
				}
			}
		}

		@JvmStatic
		fun computeRegular(imageData: ImageData, parameters:Parameters) {
			for (x in 0 until imageData.width){
				for (y in 0 until imageData.height){
					imageData.setPixel(x, y, Utils.getColor(Utils.getPointIterations(parameters.getUnScaledX(x.toDouble()), parameters.getUnScaledY(y.toDouble()), parameters), parameters))
				}
			}
		}
	}
}