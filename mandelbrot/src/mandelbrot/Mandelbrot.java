package mandelbrot;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Mandelbrot {
	private static Mandelbrot instance;
	private MandelbrotParameters parameters = new MandelbrotParameters();
	
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
		Display display = new Display();
		
		parameters.init();
		
		Shell shell = new Shell(display, SWT.CLOSE);
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
				
				parameters.change(xn1, yn1, xn2, yn2);
				
				shell.setBounds(0, 0, parameters.getWidth(), parameters.getHeight());
			}
        });
        
        shell.open();
        
        shell.setText("Mandelbrot ------ "+Mandelbrot.this+" ---- Calculating");
		ImageData imageData = new ImageData(parameters.getWidth(), parameters.getHeight(), 24, new PaletteData(0xFF , 0xFF00 , 0xFF0000));
		mandelbrot(imageData);
		Image image = new Image(display, imageData);
		GC gc = new GC(shell);
		gc.drawImage(image, 0, 0);

		shell.setText(shell.getText()+" ---- Done");
        
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}	
		}
		
		display.dispose();
	}
	
	private void mandelbrot(ImageData imageData) {
		Date date = new Date();
		double x = parameters.getX1();
		double y = parameters.getY1();
		
		while (true) {
			int scaledX = parameters.getScaledX(x);
			int scaledY = parameters.getScaledY(y);
			
			if (scaledX<imageData.width && scaledY<imageData.height) {
				imageData.setPixel(scaledX, scaledY, MandelbrotUtils.getColor(getIterations(x, y), parameters.getMaxIterations()));
			}
			
			x+=parameters.getStep();
			if (x>=parameters.getX2() && y>=parameters.getY2()) break;
			
			if (x>=parameters.getX2()) {
				x = parameters.getX1();
				y+=parameters.getStep();
			}
		}
		
		System.out.println("Calculating took: "+MandelbrotUtils.getTimeElapsed(new Date().getTime()-date.getTime()));
		System.out.println(this);
	}
	
	private int getIterations(double x, double y) {
		double xn = x;
		double yn = y;
		int iterations = 0;
		for (;iterations<parameters.getMaxIterations();iterations++) {
			if (MandelbrotUtils.module(xn, yn)>4) break;//definitely not in set
			
			double xn1 = xn*xn-yn*yn+x;
			yn = 2*xn*yn+y;
			
			xn = xn1;
		}
		
		return MandelbrotUtils.module(xn, yn)<4?0:iterations;//in set/not in set
	}
}