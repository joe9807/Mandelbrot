package mandelbrot;

import lombok.Data;

@Data
public class MandelbrotParameters {
	private int height;
	private int width;
	private long scale = 400;
	private long maxIterations = 200;
	
	private double x1= -2;
	private double x2 = 1;
	private double y1 = -1.2;
	private double y2 = 1.2;
	
	private double step = 0.002;
	
	public MandelbrotParameters() {
		init();
	}
	
	public void init() {
		height = (int)((y2-y1)*scale);
		width = (int)((x2-x1)*scale);
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
	
	public void change(double xn1, double yn1, double xn2, double yn2) {
		double before = x2-x1;
		
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
		
		scale = (int)(scale*(before/(x2-x1)));
		step = step/(before/(x2-x1));
		
		init();
	}
	
	public String toString() {
		return String.format("height: %s; width; %s; scale: %s; iterations: %s; step: %e;\n", height, width, scale, maxIterations, step)+
				String.format(" x1: %e; x2: %e;\n y1: %e; y2: %e;", x1, x2, y1, y2);
	}
}