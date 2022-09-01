package mandelbrot;

import lombok.Data;

@Data
public class MandelbrotParameters {
	private int height;
	private int width;
	private long maxIterations = 200;

	private double step;
	private double scale;
	
	private double x1= -2;
	private double x2 = 1;
	private double y1 = -1.2;
	private double y2 = 1.2;
	
	public MandelbrotParameters(int screenWidth, int screenHeight) {
		init(screenWidth, screenHeight);
	}
	
	public void init(int screenWidth, int screenHeight) {
		double scalePrev = scale;
		if (screenHeight != 0) {
			scale = screenHeight/height();
			height = screenHeight;
			width = (int)(width()*scale);
		} else {
			scale = screenWidth/width();
			width = screenWidth;
			height = (int)(height()*scale);
		}
		
		if (step == 0) {
			step = 0.002;
		} else {
			step = scalePrev*step/scale;
		}
	}
	
	public int getScaledX(double x) {
		return (int)(x*scale-x1*scale);
	}
	
	public int getScaledY(double y) {
		return (int)(y*scale-y1*scale);
	}
	
	public double getUnScaledX(double x) {
		return (x/scale)+x1;
	}
	
	public double getUnScaledY(double y) {
		return (y/scale)+y1;
	}
	
	public void change(double xn1, double yn1, double xn2, double yn2, int screenWidth, int screenHeight) {
		if (xn1<xn2) {
			x1 = xn1;
			x2 = xn2;
		} else {
			x1 = xn2;
			x2 = xn1;
		}
		
		if (yn1<yn2) {
			y1 = yn1;
			y2 = yn2;
		} else {
			y1 = yn2;
			y2 = yn1;
		}
		
		if (width()/height()>(Double.valueOf(screenWidth)/screenHeight)) {
			init(screenWidth, 0);
		} else {
			init(0, screenHeight);
		}
	}
	
	private double width() {
		return x2-x1;
	}
	
	private double height() {
		return y2-y1;
	}
	
	public String toString() {
		return String.format("height: %s; width; %s; scale: %s; iterations: %s; step: %e;\n", height, width, scale, maxIterations, step)+
				String.format(" x1: %e; x2: %e;\n y1: %e; y2: %e;", x1, x2, y1, y2);
	}
}