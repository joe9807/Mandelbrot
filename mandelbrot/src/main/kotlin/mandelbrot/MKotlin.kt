package mandelbrot

import org.eclipse.swt.graphics.ImageData

class MKotlin {
	companion object{
		@JvmStatic fun compute(imageData: ImageData, parameters:Parameters){
			for (x in 0 until imageData.width){
				val unScaledX = parameters.getUnScaledX(x.toDouble());
				for (y in 0 until imageData.height){
					imageData.setPixel(x, y, Utils.getColor(Utils.getPointIterations(unScaledX, parameters.getUnScaledY(y.toDouble()), parameters), parameters))
				}
			}
		}
	}
}