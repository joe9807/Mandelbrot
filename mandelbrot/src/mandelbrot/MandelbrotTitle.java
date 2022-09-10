package mandelbrot;

import org.eclipse.swt.widgets.Shell;

public class MandelbrotTitle {
	private static final String NAME = "Mandelbrot";
	private Shell shell;
	private int imagesSize;
	
	public MandelbrotTitle(Shell shell, int imagesSize) {
		this.shell = shell;
		this.imagesSize = imagesSize;
		shell.setText(NAME);
	}
	
	
	public void countImages(int value) {
		shell.setText(NAME+" - "+imagesSize+" : "+value);
	}
}
