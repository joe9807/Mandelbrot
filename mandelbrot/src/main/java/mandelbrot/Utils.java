package mandelbrot;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class Utils {
	public static void compute(ImageData imageData, Parameters parameters){
		IntStream.range(0, imageData.width).parallel().forEach(x->{
			double unScaledX = parameters.getUnScaledX(x);
			IntStream.range(0, imageData.height).parallel().forEach(y->{
				int color = Utils.getColor(Utils.getPointIterations(unScaledX, parameters.getUnScaledY(y), parameters), parameters);
				imageData.setPixel(x, y, color);
			});
		});
	}

	public static int getPointIterations(double x, double y, Parameters parameters) {
		double xn = x;
		double yn = y;
		double module = 0;
		int iterations = 0;

		for (;iterations<parameters.getIterations();iterations++) {
			double xx = xn*xn;
			double yy = yn*yn;
			if ((module=xx+yy)>4) return iterations;//definitely not in set

			yn = 2*xn*yn+y;
			xn = xx-yy+x;
		}

		return module<4?0:iterations;//in set/not in set
	}

	public static int getColor(int iterations, Parameters parameters) {
		if (iterations > 0) {//near to the set
			int r = iterations*parameters.getR()/parameters.getIterations();
			int g = iterations*parameters.getG()/parameters.getIterations();
			int b = iterations*parameters.getB()/parameters.getIterations();
			return (b << 16) | (g << 8) | r;
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
		
		File sets = new File("sets");
		sets.mkdir();
		int max = Arrays.stream(sets.listFiles()).map(imageDir->{
			String number = imageDir.getName().replaceAll("set", StringUtils.EMPTY);
			return Integer.valueOf(number);
		}).max(Integer::compare).orElse(0);
		
		File imagesDir = new File("sets/set"+(max+1));
		imagesDir.mkdir();

		ImageLoader saver = new ImageLoader();
		AtomicInteger pos = new AtomicInteger(0);
		
		images.forEach(image->{
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