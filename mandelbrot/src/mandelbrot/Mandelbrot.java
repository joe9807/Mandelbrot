package mandelbrot;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Mandelbrot {
	private static int HEIGHT = 1000;
	private static int WIDTH = 3*HEIGHT/2;
	private static int SCALE = WIDTH/4;
	private static int PRECISION = 200;
	private static double STEP = 0.002;
	private int[][] points = new int[WIDTH][HEIGHT];
	
	private static Mandelbrot instance;
	
	private static synchronized Mandelbrot getInstance() {
		if (instance == null) instance = new Mandelbrot();
		return instance;
	}

	public static void main(String[] args) {
		getInstance().run();
	}
	
	
	private void run () {
		Display display = new Display();
		Shell shell = new Shell(display, SWT.CLOSE | SWT.BORDER);
		shell.setText("Mandelbrot");
		shell.setBounds(0, 0, WIDTH, HEIGHT);
		
        shell.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				mandelbrot(e.gc);
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
		double x = -2;
		double y = -1;
		
		int halfWidth = WIDTH/2;
		int halfHeight = HEIGHT/2;
		while (true) {
			int pointx = (int)(x*SCALE)+halfWidth;
			int pointy = (int)(y*SCALE)+halfHeight;
			
			points[pointx][pointy] = getPrecision(x, y);
			
			x+=STEP;
			if (x>=1 && y>=1) break;
			
			if (x>=1) {
				x = -2;
				y+=STEP;
			}
		}
		
		System.out.println("Calculating took: "+getTimeElapsed(new Date().getTime()-date.getTime()));
		date = new Date();
		drawPoints(gc);
		System.out.println("Rendering took: "+getTimeElapsed(new Date().getTime()-date.getTime()));
	}
	
	private void drawPoints(GC gc) {
		for (int i=0;i<WIDTH;i++) {
			for (int j=0;j<HEIGHT;j++) {
				setForeground(gc, points[i][j]);
				gc.drawPoint(i, j);
			}
		}
	}
	
	private int getPrecision(double x, double y) {
		double xn = x;
		double yn = y;
		int i=0;
		for (;i<PRECISION;i++) {
			if (module(xn, yn)>4) break;//definitely not in set
			
			double xn1 = xn*xn-yn*yn+x;
			yn = 2*xn*yn+y;
			
			xn = xn1;
		}
		
		return module(xn, yn)<4?0:i;//in set/not in set
	}
	
	private void setForeground(GC gc, int value) {
		Color color = new Color(gc.getDevice(), 0, 0, 0);
		if (value > 0) {//near to the set
			int rgb = (value*255)/PRECISION;
			color = new Color(gc.getDevice(), rgb, rgb, rgb);
		}
		
		if (gc.getForeground() != color) {
			gc.setForeground(color);
		}
	}
	
	private double module(double x, double y) {
		return x*x+y*y;
	}
	
	private String getTimeElapsed(long elapsed) {
        long milliseconds = elapsed % 1000;
        elapsed = elapsed / 1000;
        long seconds = elapsed % 60;
        elapsed = elapsed / 60;
        long minutes = elapsed % 60;
        elapsed = elapsed / 60;
        long hours = elapsed % 24;
        elapsed = elapsed / 24;
        long days = elapsed % 7;
        elapsed = elapsed / 7;
        long weeks = elapsed % 4;
        elapsed = elapsed / 4;
        long months = elapsed % 12;
        long years = elapsed / 12;
        
        String millisStr = (milliseconds != 0? (milliseconds + " ms") : "");
		String secondsStr = (seconds != 0 ? (seconds + " s ") : "");
		String minutesStr = (minutes != 0 ? (minutes + " m ") : "");
		String hoursStr = (hours != 0 ? (hours + " h ") : "");
		String daysStr = (days != 0 ? (days + " d ") : "");
		String weeksStr = (weeks != 0 ? (weeks + " w ") : "");
		String monthsStr = (months != 0 ? (months + " M ") : "");
		String yearsStr = (years != 0 ? (years + " y ") : "");
		
		String result = new StringBuilder(yearsStr)
			.append(monthsStr)
			.append(weeksStr)
			.append(daysStr)
			.append(hoursStr)
			.append(minutesStr)
			.append(secondsStr)
			.append(millisStr).toString();
		
		return result.isEmpty()?"0 ms":result;
	}
}