package mandelbrot;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
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
		title = new MandelbrotTitle(shell, parameters);
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
        
        label.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				double nx = parameters.getUnScaledX(e.x);
				double yn = parameters.getUnScaledY(e.y);
				title.mouseMoveTitle(nx, yn);
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
		parameters = new MandelbrotParameters(shell.getDisplay().getPrimaryMonitor().getClientArea());
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
	    resetItems.setText("Reset");
	    resetItems.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				reset();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
	    
	    MenuItem menuItemSet = new MenuItem(popupMenu, SWT.NONE);
	    menuItemSet.setText("Create/Play Set");
	    menuItemSet.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//createAndPlaySet(-0.9943837320, -0.2997867617);
				//createAndPlaySet(-0.788547487133255, -0.150889365472422);
				//createAndPlaySet(-0.994405775524320, -0.300139532909510);
				
				createAndPlaySet(-0.655642634968885, -0.379125624450925);
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
	    
	    
	    MenuItem menuItemCopyCoords = new MenuItem(popupMenu, SWT.NONE);
	    menuItemCopyCoords.setText("Copy Coordinates");
	    menuItemCopyCoords.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		        StringSelection selection = new StringSelection(String.format("parameters.increase(%.15f, %.15f);", xn2, yn2));
		        clipboard.setContents(selection, null);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
	    
	    MenuItem menuItemGoHere = new MenuItem(popupMenu, SWT.NONE);
	    menuItemGoHere.setText("Go Here");
	    menuItemGoHere.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
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
					title.setImagesTitle(images.size(), MandelbrotUtils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
					
					if (parameters.isTheEnd()) {
						playSet();
					}
				}
			});
		}
	}
	
	private void playSet() {
		boolean result = MessageDialog.openConfirm(shell, "Mandelbrot Parameters", parameters.toString()+"\n\nPress OK to start rendering.");

		if (result){
			images.stream().forEach(image->{
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						label.setImage(image);
						MandelbrotUtils.sleep();
						
						images.remove(image);
						title.setImagesTitle(images.size(), null);
					}
				});
			});
		} else {
			reset();
		}
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
				imageData.setPixel(xx, y, MandelbrotUtils.getColor(getPointIterations(unScaledX, parameters.getUnScaledY(y)), parameters.getIterations()));
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