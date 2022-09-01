package mandelbrot;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Mandelbrot {
	private int height;
	private int width;
	private int scale = 400;
	private int iterations;
	
	private double x1= -2;
	private double x2 = 1;
	private double y1 = -1.2;
	private double y2 = 1.2;
	
	private double step = 0.002;
	
	private static Mandelbrot instance;
	
	private static synchronized Mandelbrot getInstance() {
		if (instance == null) instance = new Mandelbrot();
		return instance;
	}
	
	public void init() {
		height = (int)((y2-y1)*scale);
		width = (int)((x2-x1)*scale);
		iterations = 200;
	}
	
	public String toString() {
		return String.format("height: %s; width; %s; scale: %s; iterations: %s; step: %e;\n", height, width, scale, iterations, step)+
				String.format(" x1: %e; x2: %e;\n y1: %e; y2: %e;", x1, x2, y1, y2);
	}

	public static void main(String[] args) {
		getInstance().run();
	}
	
	
	private void run () {
		Display display = new Display();
		
		init();
		
		Shell shell = new Shell(display, SWT.CLOSE);
		shell.setBounds(0, 0, width, height);
		
        shell.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				shell.setText("Mandelbrot ------ "+Mandelbrot.this+" ---- Calculating");
				mandelbrot(e.gc);
				shell.setText(shell.getText()+" ---- Done");
			}
		});
        
        shell.addMouseListener(new MouseListener() {
        	private double xn1;
        	private double yn1;
        	private double xn2;
        	private double yn2;
        	
			public void mouseDoubleClick(MouseEvent e) {}

			@Override
			public void mouseDown(MouseEvent e) {
				xn1 = Double.valueOf(e.x)/scale;
				yn1 = Double.valueOf(e.y)/scale;
			}

			@Override
			public void mouseUp(MouseEvent e) {
				xn2 = Double.valueOf(e.x)/scale;
				yn2 = Double.valueOf(e.y)/scale;
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
				init();
				
				step = step/(before/(x2-x1));
				
				shell.redraw();
			}
        });
        
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}	
		}
	}
	
	private void mandelbrot(GC gc) {
		Date date = new Date();
		double x = x1;
		double y = y1;
		
		while (true) {
			int scaledx = (int)(x*scale-x1*scale);
			int scaledy = (int)(y*scale-y1*scale);
			
			setForeground(gc, getPrecision(x, y));
			gc.drawPoint(scaledx, scaledy);
			
			x+=step;
			if (x>=x2 && y>=y2) break;
			
			if (x>=x2) {
				x = x1;
				y+=step;
			}
		}
		
		System.out.println("Calculating took: "+MandelbrotUtils.getTimeElapsed(new Date().getTime()-date.getTime()));
		System.out.println(this);
	}
	
	private int getPrecision(double x, double y) {
		double xn = x;
		double yn = y;
		int i=0;
		for (;i<iterations;i++) {
			if (MandelbrotUtils.module(xn, yn)>4) break;//definitely not in set
			
			double xn1 = xn*xn-yn*yn+x;
			yn = 2*xn*yn+y;
			
			xn = xn1;
		}
		
		return MandelbrotUtils.module(xn, yn)<4?0:i;//in set/not in set
	}
	
	private void setForeground(GC gc, int value) {
		Color color = new Color(gc.getDevice(), 0, 0, 0);
		if (value > 0) {//near to the set
			int rgb = (value*255)/iterations;
			color = new Color(gc.getDevice(), rgb, rgb, rgb);
		}
		
		if (gc.getForeground() != color) {
			gc.setForeground(color);
		}
	}
}