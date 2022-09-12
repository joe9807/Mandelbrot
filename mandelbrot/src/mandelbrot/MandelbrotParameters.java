package mandelbrot;

import org.eclipse.swt.graphics.Rectangle;

import lombok.Data;

@Data
public class MandelbrotParameters {
	private int height;
	private int width;
	private int maxIterations = 400;

	private double scale;
	
	private double x1;
	private double x2;
	private double y1;
	private double y2;
	
	public MandelbrotParameters(Rectangle screenResolution) {
		x1= -2;
		x2 = 1;
		y1 = -1.2;
		y2 = 1.2;
		
		init(screenResolution.width, screenResolution.height);
	}
	
	public void init(int screenWidth, int screenHeight) {
		if (screenHeight != 0) {
			scale = screenHeight/height();
			height = screenHeight;
			width = (int)(width()*scale);
		} else {
			scale = screenWidth/width();
			width = screenWidth;
			height = (int)(height()*scale);
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
	
	private double width() {
		return x2-x1;
	}
	
	private double height() {
		return y2-y1;
	}
	
	private double centerX() {
		return (x2+x1)/2;
	}
	
	private double centerY() {
		return (y2+y1)/2;
	}
	
	private double rectRatio() {
		return width()/height();
	}
	
	public String toString() {
		return String.format("width: %s; height: %s; Shell Ratio: %,.10f;\n", width, height, Double.valueOf(width)/height)+
				String.format("width: %s; height: %s; Set Ratio: %,.10f;\n\n", width(), height(), width()/height())+
				String.format("x1: %,.15f; x2: %,.15f;\ny1: %,.15f; y2: %,.15f;\n", x1, x2, y1, y2)+
				String.format("center: %,.15f; %,.15f;\n\n", centerX(), centerY())+
				String.format("scale: %,.0f", scale);
	}
	
	public void reduce(Rectangle rect) {
		double stepx = width()/20;
		double stepy = stepx/rectRatio();
		x1+=stepx; x2-=stepx;
		y1+=stepy; y2-=stepy;
		
		maxIterations += maxIterations/100;
		init(0, rect.height);
	}
	
	public boolean change(double xn1, double yn1, double xn2, double yn2, Rectangle screenResolution) {
		if (xn1-xn2 == 0 || yn1 - yn2 == 0) return false;
		
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
		
		if (rectRatio()>(Double.valueOf(screenResolution.width)/screenResolution.height)) {
			init(screenResolution.width, 0);
		} else {
			init(0, screenResolution.height);
		}
		
		return true;
	}
	
	public void increase(double newCenterX, double newCenterY) {
		double tox = newCenterX<centerX()?newCenterX-x1:x2-newCenterX;
		double toy = newCenterY<centerY()?newCenterY-y1:y2-newCenterY;
		double ratio = Double.valueOf(width)/height;
		
		if ((tox/toy)>ratio) {
			//count height then width
			if (newCenterY<centerY()) {
				y2 = 2*newCenterY-y1;
			} else {
				y1 = 2*newCenterY-y2;
			}
			
			double stepx = height()*ratio/2;
			x1=newCenterX-stepx; 
			x2=newCenterX+stepx;
			init(0, height);
		} else {
			//count width then height
			
			if (newCenterX<centerX()) {
				x2 = 2*newCenterX-x1;
			} else {
				x1 = 2*newCenterX-x2;
			}
			
			double stepy = width()/ratio/2;
			y1=newCenterY-stepy; 
			y2=newCenterY+stepy;
			init(width, 0);
		}
	}
}