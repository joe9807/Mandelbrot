package mandelbrot;

public enum MandelbrotMode {
	PIXELS("Pixels"), STEPS("Steps");
	
	private String value;
	
	public String getValue() {
		return value;
	}

	MandelbrotMode(String value) {
		this.value = value;
	}
	
	public MandelbrotMode anotherMode() {
		return this == PIXELS?STEPS:PIXELS;
	}
	
	public boolean isPixels() {
		return this == PIXELS;
	}
}
