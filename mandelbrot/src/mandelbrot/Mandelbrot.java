package mandelbrot;

import java.util.ArrayList;
import java.util.Date;
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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class Mandelbrot {
	private static Mandelbrot instance;
	private MandelbrotParameters parameters;
	private MandelbrotTitle title;
	private Label label;
	private Shell shell;
	private List<Image> images = new ArrayList<Image>();
	private int imagesSize = 80;
	
	private double xn1;
	private double yn1;
	private double xn2;
	private double yn2;
	
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
		shell = new Shell(new Display(), SWT.CLOSE | SWT.RESIZE);
		shell.setLayout(new FillLayout());
		parameters = new MandelbrotParameters(shell.getDisplay().getPrimaryMonitor().getClientArea());
		title = new MandelbrotTitle(shell, imagesSize, parameters);
		label = new Label(shell, SWT.NONE);
        label.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {}

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button != 1) return;
				
				xn1 = parameters.getUnScaledX(e.x);
				yn1 = parameters.getUnScaledY(e.y);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				xn2 = parameters.getUnScaledX(e.x);
				yn2 = parameters.getUnScaledY(e.y);
				
				if (e.button == 1 && parameters.change(xn1, yn1, xn2, yn2, shell.getDisplay().getPrimaryMonitor().getClientArea())) {
					shell.setBounds(0, 0, parameters.getWidth(), parameters.getHeight());
					createAndDrawImage(true);
					title = new MandelbrotTitle(shell, imagesSize, parameters);
				}
			}
        });
        
        setMenu();
        shell.open();
        
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
				parameters = new MandelbrotParameters(shell.getDisplay().getPrimaryMonitor().getClientArea());
				shell.setBounds(0, 0, parameters.getWidth(), parameters.getHeight());
				createAndDrawImage(true);
				title = new MandelbrotTitle(shell, imagesSize, parameters);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
	    
	    MenuItem menuItemSet = new MenuItem(popupMenu, SWT.NONE);
	    menuItemSet.setText("Create/Play Set");
	    menuItemSet.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				parameters.increase(xn2, yn2);
				images.add(createAndDrawImage(true));
				createAndPlaySet(menuItemSet);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
	    
	    
	    MenuItem menuItemShowParams = new MenuItem(popupMenu, SWT.NONE);
	    menuItemShowParams.setText("Show Parameters");
	    menuItemShowParams.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MessageBox dialog = new MessageBox(shell);
				dialog.setText("Mandelbrot Parameters");
				dialog.setMessage(parameters.toString());
				dialog.open();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
	    label.setMenu(popupMenu);
	    
	    resetItems.notifyListeners(SWT.Selection, null);
	}
	
	private void createAndPlaySet(MenuItem menuItemSet) {
		menuItemSet.setEnabled(false);
		
		for (int i=0;i<imagesSize;i++) {
			final int pos = i;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					parameters.reduce(shell.getDisplay().getPrimaryMonitor().getClientArea());
					Date startDate = new Date();
					images.add(createAndDrawImage(false));
					title.setImagesTitle(imagesSize, imagesSize-images.size(), MandelbrotUtils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
					
					if (pos == imagesSize-1) {
						playSet(menuItemSet);
					}
				}
			});
		}
	}
	
	private void playSet(MenuItem menuItemSet) {
		final int imagesSize = images.size();
		images.stream().forEach(image->{
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					label.setImage(image);
					MandelbrotUtils.sleep();
					images.remove(image);
					
					title.setImagesTitle(imagesSize, images.size(), null);
					if (images.size() == 0) {
						menuItemSet.setEnabled(true);
					}
				}
			});
		});
	}
	
	private Image createAndDrawImage(boolean draw) {
		ImageData imageData = new ImageData(parameters.getWidth(), parameters.getHeight(), 24, new PaletteData(0xFF , 0xFF00 , 0xFF0000));
		
		mandelbrot(imageData);
		
		Image image = new Image(shell.getDisplay(), imageData);
		if (draw) {
			label.setImage(image);
		}
		return image;
	}
	
	private void mandelbrot(ImageData imageData) {
		drawFromPixels(imageData);
	}
	
	private void drawFromPixels(ImageData imageData) {
		for (int x=0;x<imageData.width;x++) {
			final int xx = x;
			final double unScaledX = parameters.getUnScaledX(xx);
			IntStream.range(0, imageData.height).parallel().forEach(y->{
				imageData.setPixel(xx, y, MandelbrotUtils.getColor(getIterations(unScaledX, parameters.getUnScaledY(y)), parameters.getMaxIterations()));
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
			if ((module=xx+yy)>4) return iterations;//definitely not in set
			
			double xn1 = xx-yy+x;
			yn = 2*xn*yn+y;
			
			xn = xn1;
		}
		
		return module<4?0:iterations;//in set/not in set
	}
}