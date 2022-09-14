package mandelbrot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Shell; 

public class Mandelbrot {
	private static Mandelbrot instance;
	private Parameters parameters;
	private MandelbrotTitle title;
	private Label label;
	private Shell shell;
	private List<Image> images = new ArrayList<Image>();
	
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
					title = new MandelbrotTitle(shell, parameters);
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
	
	private void reset() {
		parameters = new Parameters(shell.getDisplay().getPrimaryMonitor().getClientArea());
		shell.setBounds(0, 0, parameters.getWidth(), parameters.getHeight());
		createAndDrawImage(true);
		title = new MandelbrotTitle(shell, parameters);
	}
	
	private void createAndPlaySet(double xTo, double yTo) {
		parameters.increase(xTo, yTo);
		
		images.add(createAndDrawImage(true));
		createAndPlaySet();
	}
	
	private void setMenu() {
		Menu popupMenu = new Menu(label);
		MenuItem resetItems = new MenuItem(popupMenu, SWT.NONE);
	    resetItems.setText(Constants.RESET_TEXT);
	    resetItems.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				reset();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
	    
	    MenuItem menuItemShowParams = new MenuItem(popupMenu, SWT.NONE);
	    menuItemShowParams.setText(Constants.SHOW_TEXT);
	    menuItemShowParams.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ParametersDialog dialog = new ParametersDialog(shell, parameters);
				if (dialog.open() == 0) {
					parameters.setIterations(dialog.getIterations());
					createAndDrawImage(true);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
	    
	    MenuItem menuItemGoHere = new MenuItem(popupMenu, SWT.NONE);
	    menuItemGoHere.setText(Constants.GO_TEXT);
	    menuItemGoHere.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (images.size() != 0) {
					MessageDialog dialog = new MessageDialog(shell, Constants.DIALOG_TITLE, null, Constants.CONFIRM_MESSAGE, MessageDialog.WARNING, Constants.CONFIRM_BUTTONS, 0);
					
					int result = dialog.open();
					if (result == 0) {
						playSet();
						return;
					} else if (result == 1) {
						images.clear();
					} else {
						return;
					}
				}
				
				reset();
				createAndPlaySet(xn2, yn2);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
	    
	    label.setMenu(popupMenu);
	    
	    resetItems.notifyListeners(SWT.Selection, null);
	}
	
	private void createAndPlaySet() {
		for (int i=0;i<1000;i++) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (parameters.isTheEnd()) return;
					
					parameters.reduce(shell.getDisplay().getPrimaryMonitor().getClientArea());
					Date startDate = new Date();
					images.add(createAndDrawImage(false));
					title.setImagesTitle(images.size(), Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
					
					if (parameters.isTheEnd()) {
						if (MessageDialog.openConfirm(shell, Constants.PARAMETERS_TITLE, parameters.toString()+"\n\nPress OK to start rendering.")){
							playSet();
						} else {
							reset();
						}
					}
				}
			});
		}
	}
	
	private void playSet() {
		images.stream().forEach(image->{
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					label.setImage(image);
					Utils.sleep();
					
					int index = images.indexOf(image);
					title.setImagesTitle(images.size()-index, null);
					
					if (index == images.size()-1) {
						if (MessageDialog.openConfirm(shell, Constants.SAVE_TITLE, Constants.SAVE_MESSAGE)) {
							Utils.saveImages(images, title);
						}
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
				imageData.setPixel(xx, y, Utils.getColor(getPointIterations(unScaledX, parameters.getUnScaledY(y)), parameters.getIterations()));
			});
		}
	}
	
	private int getPointIterations(double x, double y) {
		double xn = x;
		double yn = y;
		double module = 0;
		int iterations = 0;
		
		for (;iterations<parameters.getIterations();iterations++) {
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