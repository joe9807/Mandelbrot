package mandelbrot;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Mandelbrot {
	private static Mandelbrot instance;
	private MandelbrotParameters parameters;
	
	private static synchronized Mandelbrot getInstance() {
		if (instance == null) instance = new Mandelbrot();
		return instance;
	}
	
	public String toString() {
		return parameters.toString();
	}

	public static void main(String[] args) {
		getInstance().run();
	}
	
	private void run () {
		Shell shell = new Shell(new Display(), SWT.CLOSE);
		parameters = new MandelbrotParameters(0, shell.getDisplay().getPrimaryMonitor().getClientArea().height-20);
		shell.setBounds(0, 0, parameters.getWidth(), parameters.getHeight());
		shell.setLayout(new FillLayout());
        shell.addMouseListener(new MouseListener() {
        	private double xn1;
        	private double yn1;
        	private double xn2;
        	private double yn2;
        	
			public void mouseDoubleClick(MouseEvent e) {}

			@Override
			public void mouseDown(MouseEvent e) {
				xn1 = parameters.getUnScaledX(e.x);
				yn1 = parameters.getUnScaledY(e.y);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				xn2 = parameters.getUnScaledX(e.x);
				yn2 = parameters.getUnScaledY(e.y);
				
				final Rectangle rect = shell.getDisplay().getPrimaryMonitor().getClientArea();
				parameters.change(xn1, yn1, xn2, yn2, rect.width, rect.height-20);
				
				shell.setBounds(0, 0, parameters.getWidth(), parameters.getHeight());
				
				drawImage(shell);
			}
        });
        
        
        shell.open();
        
        drawImage(shell);

		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}	
		}
	}
	
	private void drawImage(Shell shell) {
		ImageData imageData = new ImageData(parameters.getWidth(), parameters.getHeight(), 24, new PaletteData(0xFF , 0xFF00 , 0xFF0000));
		mandelbrot(imageData);
		new GC(shell).drawImage(new Image(shell.getDisplay(), imageData), 0, 0);
	}
	
	private void mandelbrot(ImageData imageData) {
		Date date = new Date();
		double x = parameters.getX1();
		double y = parameters.getY1();
		
		int[][] points = new int[imageData.width][imageData.height];
		while (true) {
			int scaledX = parameters.getScaledX(x);
			int scaledY = parameters.getScaledY(y);
			
			if (scaledX<imageData.width && scaledY<imageData.height) {
				points[scaledX][scaledY] = MandelbrotUtils.getColor(getIterations(x, y), parameters.getMaxIterations());
			}
			
			x+=parameters.getStep();
			if (x>=parameters.getX2()) {
				if (y>=parameters.getY2()) break;
				
				x = parameters.getX1();
				y+=parameters.getStep();
			}
		}
		
		setPixels(points, imageData);
		
		System.out.println("Calculating took: "+MandelbrotUtils.getTimeElapsed(new Date().getTime()-date.getTime()));
		System.out.println(this);
	}
	
	private void setPixels(int[][] points, ImageData imageData) {
		for (int x=0;x<imageData.width;x++) {
			for (int j=0;j<imageData.height;j++) {
				imageData.setPixel(x, j, points[x][j]);
			}
		}
	}
	
	private long getIterations(double x, double y) {
		double xn = x;
		double yn = y;
		double module = 0;
		long iterations = 0;
		
		for (;iterations<parameters.getMaxIterations();iterations++) {
			module = MandelbrotUtils.module(xn, yn);
			if (module>4) break;//definitely not in set
			
			double xn1 = xn*xn-yn*yn+x;
			yn = 2*xn*yn+y;
			
			xn = xn1;
		}
		
		return module<4?0:iterations;//in set/not in set
	}
}