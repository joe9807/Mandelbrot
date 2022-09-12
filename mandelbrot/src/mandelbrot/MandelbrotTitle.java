package mandelbrot;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Shell;

public class MandelbrotTitle {
	private static final String NAME = "Mandelbrot";
	private Shell shell;
	
	public MandelbrotTitle(Shell shell, MandelbrotParameters parameters) {
		this.shell = shell;
		shell.setText(NAME);
	}
	
	
	public void setImagesTitle(int imagesSize, String timeElapsed) {
		shell.setText(NAME+StringUtils.SPACE+imagesSize+(timeElapsed != null?" : "+timeElapsed:StringUtils.EMPTY));
	}
	
	public void mouseMoveTitle(double xn, double yn) {
		shell.setText(NAME+" ("+String.format("%,.10f; %,.10f", xn, yn)+")");
	}
}
