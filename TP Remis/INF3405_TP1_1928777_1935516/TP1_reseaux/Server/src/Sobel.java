import java.awt.image.BufferedImage;
import java.io.IOException;

public class Sobel {
    
	public static BufferedImage process(BufferedImage image) throws IOException 
	{
	  System.out.println("applying filter");
	
	  int x = image.getWidth();
	  int y = image.getHeight();
	
	  int[][] edgeColors = new int[x][y];
	  int maxGradient = -1;
	
	  for (int i = 1; i < x - 1; i++) {
	      for (int j = 1; j < y - 1; j++) {
	
	          int val00 = getGrayScale(image.getRGB(i - 1, j - 1));
	          int val01 = getGrayScale(image.getRGB(i - 1, j));
	          int val02 = getGrayScale(image.getRGB(i - 1, j + 1));
	
	          int val10 = getGrayScale(image.getRGB(i, j - 1));
	          int val11 = getGrayScale(image.getRGB(i, j));
	          int val12 = getGrayScale(image.getRGB(i, j + 1));
	
	          int val20 = getGrayScale(image.getRGB(i + 1, j - 1));
	          int val21 = getGrayScale(image.getRGB(i + 1, j));
	          int val22 = getGrayScale(image.getRGB(i + 1, j + 1));
	
	          int gx =  ((-1 * val00) + (0 * val01) + (1 * val02)) 
	                  + ((-2 * val10) + (0 * val11) + (2 * val12))
	                  + ((-1 * val20) + (0 * val21) + (1 * val22));
	
	          int gy =  ((-1 * val00) + (-2 * val01) + (-1 * val02))
	                  + ((0 * val10) + (0 * val11) + (0 * val12))
	                  + ((1 * val20) + (2 * val21) + (1 * val22));
	
	          double gval = Math.sqrt((gx * gx) + (gy * gy));
	          int g = (int) gval;
	
	          if(maxGradient < g) 
	          {
	              maxGradient = g;
	          }
	          edgeColors[i][j] = g;
	      }
      }

      double scale = 255.0 / maxGradient;

      for (int i = 1; i < x - 1; i++) {
          for (int j = 1; j < y - 1; j++) {
              int edgeColor = edgeColors[i][j];
              edgeColor = (int)(edgeColor * scale);
              edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;

              image.setRGB(i, j, edgeColor);
          }
      }
      
      fillOutlineWithZeros(image, x, y);
      
      System.out.println("Finished");
      
      return image;
  }
	
	private static BufferedImage fillOutlineWithZeros(BufferedImage image, int x, int y)
	{
		for (int i = 0; i < x; i++)
		{
			image.setRGB(i, 0, 0);
			image.setRGB(i, y-1, 0);
		}
		
		for (int j = 0; j < y; j++)
		{
			image.setRGB(0, j, 0);
			image.setRGB(x-1, j, 0);
		}
		
		return image;
	}

  private static int getGrayScale(int rgb) 
  {
      int r = (rgb >> 16) & 0xff;
      int g = (rgb >> 8) & 0xff;
      int b = (rgb) & 0xff;

      //from https://en.wikipedia.org/wiki/Grayscale, calculating luminance
      int gray = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);

      return gray;
  }
}