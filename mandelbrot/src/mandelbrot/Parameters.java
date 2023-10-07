package mandelbrot;

import org.eclipse.swt.graphics.Rectangle;

import lombok.Data;

@Data
public class Parameters {
	private static final double MAX_SCALE = Math.pow(10, 17);
	
	private int height;
	private int width;
	private int iterations;

	private double scale;
	private double x1;
	private double x2;
	private double y1;
	private double y2;
	
	public Parameters(Rectangle screenResolution) {
		x1= -2.1;
		x2 = 1;
		y1 = -1.2;
		y2 = 1.2;
		
		if (screenResolution != null) {
			init(screenResolution.width, screenResolution.height);
		}
	}
	
	public Parameters clone() {
		Parameters parameters = new Parameters(null);
		parameters.setX1(x1);
		parameters.setX2(x2);
		parameters.setY1(y1);
		parameters.setY2(y2);
		parameters.setIterations(iterations);
		parameters.setScale(scale);
		parameters.setWidth(width);
		parameters.setHeight(height);
		return parameters;
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
		
		iterations = (int)(250*Math.log10(scale));
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
	
	public double centerX() {
		return (x2+x1)/2;
	}
	
	public double centerY() {
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
				String.format("iterations: %d\n", iterations)+
				String.format("scale: %,.0f", scale);
	}
	
	public void reduce(Rectangle rect) {
		double stepx = width()/20;
		double stepy = stepx/rectRatio();
		x1+=stepx; x2-=stepx;
		y1+=stepy; y2-=stepy;
		
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
	
	public boolean isTheEnd() {
		return scale>MAX_SCALE;
	}
}