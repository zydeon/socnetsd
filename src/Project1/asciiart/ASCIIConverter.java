package asciiart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ASCIIConverter {
	
	public String convertAndResize(String imageSource) throws IOException {
		return convertAndResize(imageSource, 96, 72);
	}
	
	public String convertAndResize(String imageSource, int h, int w) throws IOException {
		BufferedImage image = ImageIO.read(new File(imageSource));
		return convert(resize(image, h, w));
	}
	
	public String convert(String imageSource) throws IOException {
		BufferedImage image = ImageIO.read(new File(imageSource));
		return convert(image);
	}

	public String convert(BufferedImage image) {
		// Source: http://richiefrost.blogspot.pt/2012/05/convert-image-file-to-ascii-art-in-java.html
		
		StringBuilder sb = new StringBuilder();

		int width = image.getWidth();
		int height = image.getHeight();
		Color color;
		
		
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				// Get the color for each pixel
				color = new Color(image.getRGB(j, i));
				int red = color.getRed();
				int green = color.getGreen();
				int blue = color.getBlue();
				// Use the luminance formula in the project outline to get each
				// pixel's luminance, aka "brightness"
				double luminance = (0.3 * red + 0.59 * green + 0.11 * blue) / 255;

				// Set the default output to " " so everything that's not dark
				// enough to be considered just turns up as blank space
				String output = " ";

				// For the lighter pixels, represent them with an "^"
				if (luminance <= 0.75 && luminance > 0.5)
					output = ".";
				// Second lightest pixels get a "G" assigned to them
				if (luminance <= 0.5 && luminance > 0.25)
					output = "/";
				// Darkest pixels get assigned the darkest ASCII character, "@"
				if (luminance <= 0.25)
					output = "#";

				// Print the line of pixels now converted to ASCII characters
				sb.append(output);
			}
			// Print out a new line for the next line of pixels to be checked
			sb.append("\n");
		}

		return sb.toString();
	}
	
	public static BufferedImage resize(BufferedImage img, int newW, int newH) {  
        int w = img.getWidth();  
        int h = img.getHeight();  
        BufferedImage dimg = new BufferedImage(newW, newH, img.getType());  
        Graphics2D g = dimg.createGraphics();  
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);  
        g.dispose();  
        return dimg;  
       }
}
