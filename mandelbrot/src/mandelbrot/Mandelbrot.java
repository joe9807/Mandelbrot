package mandelbrot;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Mandelbrot {
	private static int HEIGHT = 1000;
	private static int WIDTH = 1700;
	private static int PRECISION = 200;
	private static double SCALE = 400;
	private static double STEP = 0.002;
	
	private static Mandelbrot instance;
	
	private static synchronized Mandelbrot getInstance() {
		if (instance == null) instance = new Mandelbrot();
		return instance;
	}

	public static void main(String[] args) {
		getInstance().run();
	}
	
	
	private void run () {
		Shell shell = new Shell(Display.getCurrent(), SWT.CLOSE);
		shell.setText("Mandelbrot");
		shell.setBounds(0, 0, WIDTH, HEIGHT);
		shell.setLayout(new FillLayout());
		
        shell.addShellListener(new ShellAdapter() {
        	@Override
            public void shellActivated(ShellEvent shellevent) {
                draw(shell);
            }
        });
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}	
		}
	}
	
	private void draw(Shell shell) {
		Canvas canvas = new Canvas(shell, SWT.BORDER);
		canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				mandelbrot(e.gc);
			}
		});
	}
	
	private void mandelbrot(GC gc) {
		double x = -2;
		double y = -1;
		while (true) {
			int precision = getPrecision(x, y);
			int pointx = (int)(x*SCALE)+WIDTH/2;
			int pointy = (int)(y*SCALE)+HEIGHT/2;
			
			setBackground(gc, precision);
			gc.drawPoint(pointx, pointy);
			
			x+=STEP;
			if (x>=1 && y>=1) break;
			
			if (x>1) {
				x = -2;
				y+=STEP;
			}
		}
	}
	
	private int getPrecision(double x, double y) {
		double xn = x;
		double yn = y;
		int i=0;
		for (;i<PRECISION;i++) {
			if (module(xn, yn)>4) return i;//not in set
			
			double xn1 = xn*xn-yn*yn+x;
			double yn1 = 2*xn*yn+y;
			
			xn = xn1;
			yn = yn1;
		}
		
		return module(xn, yn)<4?-1:i;//in set/not in set
	}
	
	private void setBackground(GC gc, int value) {
		if (value <=0) {//in the set
			gc.setForeground(new Color(gc.getDevice(), 0, 0, 0));
		} else {//near to the set
			int color = (value*255)/PRECISION;
			gc.setForeground(new Color(gc.getDevice(), color, color, color));
		}
	}
	
	private double module(double x, double y) {
		return x*x+y*y;
	}
}