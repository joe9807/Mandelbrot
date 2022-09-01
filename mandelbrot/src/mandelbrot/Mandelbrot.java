package mandelbrot;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class Mandelbrot {
	private static Mandelbrot instance;
	private MandelbrotParameters parameters;
	private Image image;
	
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
				if (e.button != 1) return;
				
				xn1 = parameters.getUnScaledX(e.x);
				yn1 = parameters.getUnScaledY(e.y);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (e.button != 1) return;
				
				xn2 = parameters.getUnScaledX(e.x);
				yn2 = parameters.getUnScaledY(e.y);
				
				final Rectangle rect = shell.getDisplay().getPrimaryMonitor().getClientArea();
				parameters.change(xn1, yn1, xn2, yn2, rect.width, rect.height-20);
				
				shell.setBounds(0, 0, parameters.getWidth(), parameters.getHeight());
				
				drawImage(shell);
			}
        });
        
        
        setMenu(shell);
        shell.open();
        
        drawImage(shell);

		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}	
		}
	}
	
	private void setMenu(Shell shell) {
		Menu popupMenu = new Menu(shell);
	    MenuItem resetItems = new MenuItem(popupMenu, SWT.NONE);
	    resetItems.setText("Reset");
	    resetItems.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				parameters = new MandelbrotParameters(0, shell.getDisplay().getPrimaryMonitor().getClientArea().height-20);
				shell.setBounds(0, 0, parameters.getWidth(), parameters.getHeight());
				drawImage(shell);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
	    
	    MenuItem refreshItem = new MenuItem(popupMenu, SWT.NONE);
	    refreshItem.setText("Redraw");
	    refreshItem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new GC(shell).drawImage(image, 0, 0);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
	    shell.setMenu(popupMenu);
	}
	
	private void drawImage(Shell shell) {
		shell.setText("Calculating ");
		ImageData imageData = new ImageData(parameters.getWidth(), parameters.getHeight(), 24, new PaletteData(0xFF , 0xFF00 , 0xFF0000));
		
		Date date = new Date();
		mandelbrot(imageData);
		shell.setText(shell.getText()+" ---- "+MandelbrotUtils.getTimeElapsed(new Date().getTime()-date.getTime())+"; ");
		
		date = new Date();
		
		image = new Image(shell.getDisplay(), imageData);
		new GC(shell).drawImage(image, 0, 0);
		shell.setText(shell.getText()+" Draw image ---- "+MandelbrotUtils.getTimeElapsed(new Date().getTime()-date.getTime()));
		
		System.out.println(this);
	}
	
	private void mandelbrot(ImageData imageData) {
		double x = parameters.getX1();
		double y = parameters.getY1();
		
		while (true) {
			int scaledX = parameters.getScaledX(x);
			int scaledY = parameters.getScaledY(y);
			
			if (scaledX<imageData.width && scaledY<imageData.height) {
				imageData.setPixel(scaledX, scaledY, MandelbrotUtils.getColor(getIterations(x, y), parameters.getMaxIterations()));
			}
			
			x+=parameters.getStep();
			if (x>=parameters.getX2()) {
				if (y>=parameters.getY2()) break;
				
				x = parameters.getX1();
				y+=parameters.getStep();
			}
		}
		
	}
	
	private int getIterations(double x, double y) {
		double xn = x;
		double yn = y;
		double module = 0;
		int iterations = 0;
		
		for (;iterations<parameters.getMaxIterations();iterations++) {
			double xx = xn*xn;
			double yy = yn*yn;
			module = xx+yy;
			if (module>4) break;//definitely not in set
			
			double xn1 = xx-yy+x;
			yn = 2*xn*yn+y;
			
			xn = xn1;
		}
		
		return module<4?0:iterations;//in set/not in set
	}
}