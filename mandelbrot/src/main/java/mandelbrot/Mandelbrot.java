package mandelbrot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.ColorDialog;
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
	private final List<Image> images = new ArrayList<>();
	
	private double xn1;
	private double yn1;
	private double xn2;
	private double yn2;

	private LANG lang = LANG.JAVA;
	
	private static synchronized Mandelbrot getInstance() {
		if (instance == null) instance = new Mandelbrot();
		return instance;
	}
	
	public String toString() {
		return parameters.toString();
	}

	public static void main(String[] args) {
		getInstance().run(args);
	}
	
	private void run (String[] args) {
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
					title = new MandelbrotTitle(shell, parameters);
					createAndDrawImage(true);
				}
			}
        });
        
        setMenu(args);
        shell.open();
        
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}	
		}
	}
	
	private void reset() {
		images.clear();
		Parameters newParameters = new Parameters(shell.getDisplay().getPrimaryMonitor().getClientArea());
		if (parameters != null) {
			newParameters.setB(parameters.getB());
			newParameters.setG(parameters.getG());
			newParameters.setR(parameters.getR());
		}
		parameters = newParameters;
		shell.setBounds(0, 0, parameters.getWidth(), parameters.getHeight());
		title = new MandelbrotTitle(shell, parameters);
		title.setLang(lang);
		createAndDrawImage(true);
	}
	
	private void createAndPlaySet(double xTo, double yTo) {
		double xNow = parameters.centerX();
		double yNow = parameters.centerY();
		double centerStep = 20;
		
		Parameters oldParameters = parameters.clone();
		for (int i=0;i<=centerStep;i++) {
			final int ii=i;
			double xShift = i*Math.abs(xNow-xTo)/centerStep;
			double yShift = i*Math.abs(yNow-yTo)/centerStep;
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					parameters.increase(xNow+(xTo>xNow?1:-1)*xShift, yNow+(yTo>yNow?1:-1)*yShift);
					images.add(createAndDrawImage(false));
					
					if (ii != centerStep) {
						parameters = oldParameters;
					} else {
						createAndPlaySet();
					}
				}
			});
		}
	}
	
	private void setMenu(String[] args) {
		Menu popupMenu = new Menu(label);
		
		MenuItem colorPick = new MenuItem(popupMenu, SWT.NONE);
		colorPick.setText(Constants.COLOR_PICK);
		colorPick.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ColorDialog colorDialog = new ColorDialog(shell);
				colorDialog.setRGB(new RGB(parameters.getR(), parameters.getG(), parameters.getB()));
	            colorDialog.setText("Select a Color");
	            
	            RGB rgb = colorDialog.open();
	            if (rgb != null) {
	            	parameters.setR(rgb.red);
	            	parameters.setG(rgb.green);
	            	parameters.setB(rgb.blue);
	            	
	            	createAndDrawImage(true);
	            }
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
		
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
					
					switch (dialog.open()) {
						case 0: playSet(); return;
						case 1: break;
						default: return;
					}
				}
				
				reset();
				if (args != null && args.length != 0) {
					xn2 = Double.parseDouble(args[0].split("=")[1]);
					yn2 = Double.parseDouble(args[1].split("=")[1]);
				}
				createAndPlaySet(xn2, yn2);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
	    });
	    
	    label.setMenu(popupMenu);
	    choice();

		if (args != null && args.length != 0){
			menuItemGoHere.notifyListeners(SWT.Selection, null);
		} else {
			resetItems.notifyListeners(SWT.Selection, null);
		}
	}

	private void choice(){
		MessageDialog dialog = new MessageDialog(shell, Constants.APP_LANG, null, Constants.SELECT_LANG, MessageDialog.INFORMATION, Constants.LANG_BUTTONS, 0);
		lang = dialog.open() == 0? LANG.JAVA:LANG.KOTLIN;
	}
	
	private void createAndPlaySet() {
		Date startDate = new Date();
		for (int i=0;i<1000;i++) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (parameters.isTheEnd()) return;
					
					parameters.reduce(shell.getDisplay().getPrimaryMonitor().getClientArea());
					images.add(createAndDrawImage(false));
					
					if (parameters.isTheEnd()) {
						String timeElapsed = Utils.getTimeElapsed(new Date().getTime()-startDate.getTime());
						System.out.println(lang+" "+parameters.centerToString()+" "+timeElapsed);

						if (MessageDialog.openConfirm(shell, Constants.PARAMETERS_TITLE, parameters.toString()+"\n\nTotal time elapsed: "+timeElapsed+"\n\nPress OK to start rendering.")){
							playSet();
						} else {
							if (MessageDialog.openConfirm(shell, Constants.SAVE_TITLE, Constants.SAVE_MESSAGE)) {
								Utils.saveImages(images, title);
							} else {
								reset();
							}
						}
					}
				}
			});
		}
	}
	
	private void playSet() {
		images.forEach(image->{
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
		
		Date startDate = new Date();
		mandelbrot(imageData, parameters);
		title.setImagesTitle(images.size(), Utils.getTimeElapsed(new Date().getTime()-startDate.getTime()));
		
		Image image = new Image(shell.getDisplay(), imageData);
		if (draw) {
			label.setImage(image);
		}
		return image;
	}

	private void mandelbrot(ImageData imageData, Parameters parameters) {
		if (lang == LANG.JAVA){
			Utils.compute(imageData, parameters);
		} else if (lang == LANG. KOTLIN){
			MKotlin.compute(imageData, parameters);
		}
	}
}