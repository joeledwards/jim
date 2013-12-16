package buzuli.jim;
import java.awt.Image;

import javax.swing.ImageIcon;

/**
 * This class provides static methods for fetching various resources (such as icons) related to the editor.
 * 
 * @author Joel Edwards &lt;joeledwards@gmail.com&gt;
 */
public class Resources
{
	/**
	 * Loads an image from the specified location as an {@link ImageIcon} scaled to the specified dimensions.
	 * 
	 * @param path the path to the image
	 * @param height the height of the resulting {@link ImageIcon}
	 * @param width the width of the resulting {@link ImageIcon}
	 * 
	 * @return the new {@link ImageIcon}
	 */
	public static ImageIcon getAsImageIcon(String path, int height, int width)
	{
		ImageIcon originalIcon = new ImageIcon(ClassLoader.getSystemResource(path));
		Image originalImage = originalIcon.getImage();
		Image scaledImage = originalImage.getScaledInstance(height, width, Image.SCALE_SMOOTH);
		ImageIcon scaledIcon = new ImageIcon(scaledImage);
		
		return scaledIcon;
	}
}
