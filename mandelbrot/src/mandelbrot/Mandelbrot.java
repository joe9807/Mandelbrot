package mandelbrot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class Mandelbrot {
	private static Mandelbrot instance;
	private MandelbrotParameters parameters;
	private MandelbrotTitle title;
	private MandelbrotMode mode = MandelbrotMode.PIXELS;
	private Label label;
	private Shell shell;
	private List<Image> images = new ArrayList<Image>();
	private int imagesSize = 40;
	
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
		shell = new Shell(new Display(), SWT.CLOSE);
		title = new MandelbrotTitle(shell, imagesSize);
		parameters = new MandelbrotParameters(0, shell.getDisplay().getPrimaryMonitor().getClientArea().height);
		shell.setBounds(0, 0, parameters.getWidth(), parameters.getHeight());
		shell.setLayout(new FillLayout());
		label = new Label(shell, SWT.NONE);
        label.addMouseListener(new MouseListener() {
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
				if (parameters.change(xn1, yn1, xn2, yn2, rect)) {
					shell.setBounds(0, 0, parameters.getWidth(), parameters.getHeight());
					createAndDrawImage(true);
				}
			}
        });
        
        
        setMenu();
        shell.open();
        
        createAndDrawImage(true);

		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}	
		}
	}
	
	private void setMenu() {
		Menu popupMenu = new Menu(label);
	    MenuItem resetItems = new MenuItem(popupMenu, SWT.NONE);
	    resetItems.setText("Reset");
	    resetItems.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				parameters = new MandelbrotParameters(0, shell.getDisplay().getPrimaryMonitor().getClientArea().height);
				shell.setBounds(0, 0, parameters.getWidth(), parameters.getHeight());
				createAndDrawImage(true);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
	    
	    MenuItem menuItemMode = new MenuItem(popupMenu, SWT.NONE);
	    menuItemMode.setText("Switch to "+mode.anotherMode());
	    menuItemMode.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mode = mode.anotherMode();
				menuItemMode.setText("Switch to "+mode.anotherMode());
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
	    
	    MenuItem menuItemSet = new MenuItem(popupMenu, SWT.NONE);
	    menuItemSet.setText("Create/Play Set");
	    menuItemSet.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createAndPlaySet(menuItemSet);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
	    label.setMenu(popupMenu);
	}
	
	private void createAndPlaySet(MenuItem menuItemSet) {
		menuItemSet.setEnabled(false);
		
		for (int i=0;i<imagesSize;i++) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					parameters.reduce(shell.getDisplay().getPrimaryMonitor().getClientArea());
					images.add(createAndDrawImage(false));
					title.countImages(imagesSize-images.size());
					
					if (images.size() == imagesSize) {
						playSet(menuItemSet);
					}
				}
			});
		}
	}
	
	private void playSet(MenuItem menuItemSet) {
		MandelbrotUtils.sleep();
		
		images.stream().forEach(image->{
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					label.setImage(image);
					
					MandelbrotUtils.sleep();
					
					images.remove(image);
					if (images.size() == 0) {
						menuItemSet.setEnabled(true);
					}
					
					title.countImages(images.size());
				}
			});
		});
	}
	
	private Image createAndDrawImage(boolean draw) {
		ImageData imageData = new ImageData(parameters.getWidth(), parameters.getHeight(), 24, new PaletteData(0xFF , 0xFF00 , 0xFF0000));
		
		mandelbrot(imageData);
		
		Image image = new Image(label.getDisplay(), imageData);
		if (draw) {
			label.setImage(image);
		}
		return image;
	}
	
	private void mandelbrot(ImageData imageData) {
		if (mode.isPixels()) {
			drawFromPixels(imageData);
		} else {
			drawFromStep(imageData);
		}
	}
	
	private void drawFromStep(ImageData imageData) {
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
	
	private void drawFromPixels(ImageData imageData) {
		for (int x=0;x<imageData.width;x++) {
			final int xx = x;
			IntStream.range(0, imageData.height).parallel().forEach(y->{
				imageData.setPixel(xx, y, MandelbrotUtils.getColor(getIterations(parameters.getUnScaledX(xx), parameters.getUnScaledY(y)), parameters.getMaxIterations()));
			});
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
			if (module>4) return iterations;//definitely not in set
			
			double xn1 = xx-yy+x;
			yn = 2*xn*yn+y;
			
			xn = xn1;
		}
		
		return module<4?0:iterations;//in set/not in set
	}
}