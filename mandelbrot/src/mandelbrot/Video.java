package mandelbrot;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Video implements Runnable {
	
	public String path;
	
	public Video(String path) {
		this.path=path;
	}

	@Override
	public void run() {
		createVideo();
	}

	private void createVideo() {
		try {
			Files.copy(Paths.get("makeVideo.bat"), Paths.get(path+"/"+"makeVideo.bat"), StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				Desktop.getDesktop().open(new File(path));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
