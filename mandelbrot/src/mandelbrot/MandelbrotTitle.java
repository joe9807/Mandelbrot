package mandelbrot;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Shell;

public class MandelbrotTitle {
	private static final String NAME = "Mandelbrot";
	private Shell shell;
	
	public MandelbrotTitle(Shell shell, int imagesSize, MandelbrotParameters parameters) {
		this.shell = shell;
		shell.setText(NAME);
	}
	
	
	public void countImagesTitle(int imagesSize, int imagesValue, String timeElapsed) {
		shell.setText(NAME+" - "+imagesSize+" : "+imagesValue+(timeElapsed != null?" : "+timeElapsed:StringUtils.EMPTY));
	}
	
	public void mouseMoveTitle(double xn, double yn) {
		shell.setText(NAME+" ("+String.format("%,.2f; %,.2f", xn, yn)+")");
	}
}
