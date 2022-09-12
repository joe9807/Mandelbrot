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
	
	
	public void setImagesTitle(int imagesSize, int imagesValue, String timeElapsed) {
		shell.setText(NAME+StringUtils.SPACE+imagesSize+" : "+imagesValue+(timeElapsed != null?" : "+timeElapsed:StringUtils.EMPTY));
	}
}
