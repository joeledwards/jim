package buzuli.jim;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

/**
 * This class handles the termination of the editor. In the event that there are tabs open which
 * have never been saved or have been modified since they were last save, then the user will be
 * prompted for resolution before the editor is closed.
 * 
 * @author Joel Edwards &lt;joeledwards@gmail.com&gt;
 */
public class Terminator extends WindowAdapter
{
	private JimEditor window = null;
	private JTabbedPane tabs = null;
	
	public Terminator(JimEditor window, JTabbedPane tabs)
	{
		this.window = window;
		this.tabs = tabs;
	}
	
	private boolean unsavedTabs()
	{
		int total = tabs.getTabCount();
		EditorBufferTab tab = null;
		
		for (int i = 0; i < total; i++)
		{
			tab = (EditorBufferTab) tabs.getComponentAt(i);
			
			if (!tab.isSaved() && !tab.isFresh())
				return true;
		}
		
		return false;
	}
	
	public void windowClosing(WindowEvent evt)
	{
		boolean close = true;
		
		if (unsavedTabs())
		{
			JOptionPane confirm = new JOptionPane("There are tabs with modified content. Do you still wish to quit?",
							JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							UIManager.getIcon("OptionPane.questionIcon"));
			JDialog dialog = confirm.createDialog("Unsaved Tabs");
			dialog.setVisible(true);
			Object selectedValue = confirm.getValue();
			
			if (selectedValue != null)
			{
				int value = (Integer) selectedValue;
				
				if (value != JOptionPane.YES_OPTION)
					close = false;
			}
		}
		
		if (close)
		{
			window.setVisible(false);
			System.exit(0);
		}
	}
}
