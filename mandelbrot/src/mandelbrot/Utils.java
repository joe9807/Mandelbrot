package mandelbrot;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class Utils {
	public static int getColor(int iterations, int maxIterations) {
		if (iterations > 0) {//near to the set
			int rgb = iterations*255/maxIterations;
			return (rgb << 16) | (rgb << 8) | rgb;
		}
		
		return 0;
	}
	
	public static String getTimeElapsed(long elapsed) {
        long milliseconds = elapsed % 1000;
        elapsed = elapsed / 1000;
        long seconds = elapsed % 60;
        elapsed = elapsed / 60;
        long minutes = elapsed % 60;
        elapsed = elapsed / 60;
        long hours = elapsed % 24;
        elapsed = elapsed / 24;
        long days = elapsed % 7;
        elapsed = elapsed / 7;
        long weeks = elapsed % 4;
        elapsed = elapsed / 4;
        long months = elapsed % 12;
        long years = elapsed / 12;
        
        String millisStr = (milliseconds != 0? (milliseconds + " ms") : "");
		String secondsStr = (seconds != 0 ? (seconds + " s ") : "");
		String minutesStr = (minutes != 0 ? (minutes + " m ") : "");
		String hoursStr = (hours != 0 ? (hours + " h ") : "");
		String daysStr = (days != 0 ? (days + " d ") : "");
		String weeksStr = (weeks != 0 ? (weeks + " w ") : "");
		String monthsStr = (months != 0 ? (months + " M ") : "");
		String yearsStr = (years != 0 ? (years + " y ") : "");
		
		String result = new StringBuilder(yearsStr)
			.append(monthsStr)
			.append(weeksStr)
			.append(daysStr)
			.append(hoursStr)
			.append(minutesStr)
			.append(secondsStr)
			.append(millisStr).toString();
		
		return result.isEmpty()?"0 ms":result;
	}
	
	public static void sleep() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
	}
	
	public static void saveImages(List<Image> images, MandelbrotTitle title) {
		if (images == null || images.size() == 0) return;
		
		int max = Arrays.asList(new File("sets").listFiles()).stream().map(imageDir->{
			String number = imageDir.getName().replaceAll("set", StringUtils.EMPTY);
			return Integer.valueOf(number);
		}).max(Integer::compare).orElse(0);
		
		File imagesDir = new File("sets/set"+(max+1));
		imagesDir.mkdir();

		ImageLoader saver = new ImageLoader();
		AtomicInteger pos = new AtomicInteger(0);
		
		images.stream().forEach(image->{
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					saver.data = new ImageData[] { image.getImageData() };
					saver.save(String.format(imagesDir.getAbsolutePath()+"/"+Constants.IMAGE_FORMAT, pos.getAndIncrement()), SWT.IMAGE_PNG);
					title.setImagesTitle(pos.get(), null);
					
					if (pos.get() == images.size()) {
						new Thread(new Video(imagesDir.getAbsolutePath())).start();
					}
				}
			});
		});
	}
}