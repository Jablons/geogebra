package org.geogebra.common.awt;

import org.geogebra.common.factories.AwtFactory;
import org.geogebra.common.kernel.arithmetic.MyDouble;

public abstract class GColor implements GPaint, Comparable<GColor> {

	public static GColor WHITE;
	public static GColor BLACK;
	public static GColor RED;
	public static GColor ORANGE;
	public static GColor YELLOW;
	public static GColor GREEN;
	public static GColor BLUE;
	public static GColor CYAN;
	public static GColor DARK_CYAN;
	public static GColor DARK_GREEN;
	public static GColor MAGENTA;
	public static GColor LIGHTEST_GRAY;
	public static GColor LIGHT_GRAY;
	public static GColor GRAY;
	public static GColor DARK_GRAY;
	public static GColor PURPLE;

	public static void initColors(AwtFactory f) {
		WHITE = f.newColor(255, 255, 255);
		BLACK = f.newColor(0, 0, 0);
		RED = f.newColor(255, 0, 0);
		ORANGE = f.newColor(255, 127, 0);
		YELLOW = f.newColor(255, 255, 0);
		GREEN = f.newColor(0, 255, 0);
		BLUE = f.newColor(0, 0, 255);
		CYAN = f.newColor(0, 255, 255);
		DARK_CYAN = f.newColor(99, 219, 219);
		DARK_GREEN = f.newColor(0, 127, 0);
		MAGENTA = f.newColor(255, 0, 255);
		LIGHTEST_GRAY = f.newColor(230, 230, 230);
		LIGHT_GRAY = f.newColor(192, 192, 192);
		GRAY = f.newColor(128, 128, 128);
		DARK_GRAY = f.newColor(68, 68, 68);
		PURPLE = f.newColor(102, 102, 255);
	}

	public abstract int getRed();

	public abstract int getBlue();

	public abstract int getGreen();

	public abstract int getAlpha();

	/* float[] */
	public abstract void getRGBColorComponents(float[] rgb);

	// public Color(float r, float g, float b, float alpha);
	public static int HSBtoRGB(float hue, float saturation, float brightness) {
		int r = 0, g = 0, b = 0;
		if (saturation == 0) {
			r = g = b = (int) (brightness * 255.0f + 0.5f);
		} else {
			float h = (hue - (float) Math.floor(hue)) * 6.0f;
			float f = h - (float) java.lang.Math.floor(h);
			float p = brightness * (1.0f - saturation);
			float q = brightness * (1.0f - saturation * f);
			float t = brightness * (1.0f - (saturation * (1.0f - f)));
			switch ((int) h) {
			case 0:
				r = (int) (brightness * 255.0f + 0.5f);
				g = (int) (t * 255.0f + 0.5f);
				b = (int) (p * 255.0f + 0.5f);
				break;
			case 1:
				r = (int) (q * 255.0f + 0.5f);
				g = (int) (brightness * 255.0f + 0.5f);
				b = (int) (p * 255.0f + 0.5f);
				break;
			case 2:
				r = (int) (p * 255.0f + 0.5f);
				g = (int) (brightness * 255.0f + 0.5f);
				b = (int) (t * 255.0f + 0.5f);
				break;
			case 3:
				r = (int) (p * 255.0f + 0.5f);
				g = (int) (q * 255.0f + 0.5f);
				b = (int) (brightness * 255.0f + 0.5f);
				break;
			case 4:
				r = (int) (t * 255.0f + 0.5f);
				g = (int) (p * 255.0f + 0.5f);
				b = (int) (brightness * 255.0f + 0.5f);
				break;
			case 5:
				r = (int) (brightness * 255.0f + 0.5f);
				g = (int) (p * 255.0f + 0.5f);
				b = (int) (q * 255.0f + 0.5f);
				break;
			}
		}
		return 0xff000000 | (r << 16) | (g << 8) | (b << 0);
	}

	public abstract GColor darker();

	public abstract GColor brighter();

	public static String getColorString(GColor fillColor) {
		return "rgba(" + fillColor.getRed() + "," + fillColor.getGreen() + ","
				+ fillColor.getBlue() + "," + (fillColor.getAlpha() / 255d)
				+ ")";
	}

	/**
	 * This method could return Long, but it returns Integer for
	 * backwards-compatibility, even if it's negative
	 * 
	 * @return int
	 */
	public int getRGB() {
		// must use longs to avoid negative overflow
		int red = MyDouble.truncate0to255(getRed());
		int green = MyDouble.truncate0to255(getGreen());
		int blue = MyDouble.truncate0to255(getBlue());
		int alpha = MyDouble.truncate0to255(getAlpha());
		return ((alpha * 256 + red) * 256 + green) * 256 + blue;
	}

	/**
	 * 
	 * @return gray scale value corresponding to this color (0 to 255)
	 */
	public double getGrayScale() {
		return getGrayScale(getRed(), getGreen(), getBlue());
	}

	private static double getGrayScale(int red2, int green2, int blue2) {
		return 0.2989 * red2 + 0.5870 * green2 + 0.1140 * blue2;
	}

	/**
	 * Create a more readable (=darker) version of a color, to make it readable on white background.
	 * Does not change the color, if it already fulfills the requirements.
	 *
	 * Uses the W3C standard for contrast and brightness.
	 *
	 * @param color the base color
	 * @param factory used to create the new color
	 * @return a darker version of the input color that can be read on white background
	 */
	public static GColor updateForWhiteBackground(GColor color, AwtFactory factory){

		int fgRed = color.getRed();
		int fgGreen = color.getGreen();
		int fgBlue = color.getBlue();
		// prevent endless loop
		int loopCounter = 0;
		int difference = 5;
		while (!checkColorRatioWhite(fgRed, fgGreen, fgBlue, GColor.WHITE)
				&& loopCounter < 50) {
			// create a slightly darker version of the color
			fgRed = Math.max(fgRed - difference, 0);
			fgGreen = Math.max(fgGreen - difference, 0);
			fgBlue = Math.max(fgBlue - difference, 0);
			loopCounter++;
		}

		if (!checkColorRatioWhite(fgRed, fgGreen, fgBlue, GColor.WHITE)) {
			// If the color could not be set correctly, the font color is set to black.
			return GColor.BLACK;
		}

		return factory.newColor(fgRed, fgGreen, fgBlue);
	}

	/**
	 * uses the color contrast ratio of the W3C, which can be found at:
	 * http://www.w3.org/TR/WCAG20-TECHS/G18.html
	 * http://web.mst.edu/~rhall/web_design/color_readability.html
	 *
	 * @param foreground the text color
	 * @param background the background color
	 * @return if the contrast ration sufficient (true) or not (false)
	 */
	private static boolean checkColorRatioWhite(int fgRed, int fgGreen,
			int fgBlue,
			GColor background) {
		int diff_hue = 3 * 255 - fgRed - fgBlue - fgGreen;

		double diff_brightness = 255
				- GColor.getGrayScale(fgGreen, fgRed, fgBlue);

		return diff_brightness > 125 && diff_hue > 500;
	}

	public int compareTo(GColor c){
		if (getRed() < c.getRed()){
			return -1;
		}
		if (getRed() > c.getRed()){
			return 1;
		}
		if (getGreen() < c.getGreen()){
			return -1;
		}
		if (getGreen() > c.getGreen()){
			return 1;
		}
		if (getBlue() < c.getBlue()){
			return -1;
		}
		if (getBlue() > c.getBlue()){
			return 1;
		}
		if (getAlpha() < c.getAlpha()){
			return -1;
		}
		if (getAlpha() > c.getAlpha()){
			return 1;
		}


		return 0;
	}
}
