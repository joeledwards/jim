package buzuli.jim;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * This class represents an editor tab which contains a buffer.
 * 
 * @author Joel Edwards &lt;joeledwards@gmail.com&gt;
 */
@SuppressWarnings("serial")
public class EditorBufferTab extends JPanel
		implements ActionListener, CaretListener, ChangeListener, DocumentListener
{
	private static ImageIcon closeTabIcon = Resources.getAsImageIcon("buzuli/jim/icons/close.png", 10, 10);
	private static ImageIcon modifiedTabIcon = Resources.getAsImageIcon("buzuli/jim/icons/modified.png", 16, 16);
	private static ImageIcon freshTabIcon = Resources.getAsImageIcon("buzuli/jim/icons/fresh.png", 16, 16);
	private static ImageIcon savedTabIcon = Resources.getAsImageIcon("buzuli/jim/icons/saved.png", 16, 16);
	private static Dimension closeTabButtonSize = new Dimension(closeTabIcon.getIconWidth() + 8,
			closeTabIcon.getIconHeight() + 8);
	private static int untitledCount = 0;
	
	private boolean fresh = true;
	private boolean saved = true;
	private File file = null;
	private JimEditor parent = null;
	private JTabbedPane pane = null;
	private JPanel tabComponent = null;
	private JLabel tabIcon = null;// for icon FINISH!
	private JLabel tabLabel = null;
	private JButton tabButton = null;
	private JTextArea area = new JTextArea();
	private JScrollPane scrollArea = new JScrollPane(area);
	
	public EditorBufferTab(JimEditor parent, JTabbedPane pane, File file, boolean isDoubleBuffered)
	{
		super(new BorderLayout(), isDoubleBuffered);
		assert (parent != null);
		this.parent = parent;
		this.pane = pane;
		add(scrollArea, BorderLayout.CENTER);
		
		String title = "Untitled" + ++untitledCount;
		int index = pane.getTabCount();
		pane.insertTab(null, null, this, "", index);
		
		tabComponent = new JPanel(new BorderLayout());
		tabIcon = new JLabel(freshTabIcon);
		tabLabel = new JLabel(title);
		tabButton = new JButton(closeTabIcon);
		tabButton.setPreferredSize(closeTabButtonSize);
		
		tabComponent.add(tabIcon, BorderLayout.WEST);
		tabComponent.add(tabLabel, BorderLayout.CENTER);
		tabComponent.add(tabButton, BorderLayout.EAST);
		pane.setTabComponentAt(index, tabComponent);
		
		tabButton.addActionListener(this);
		area.getDocument().addDocumentListener(this);
		area.addCaretListener(this);
		area.addKeyListener(parent);
		
		if (file != null)
			setFile(file);
	}
	
	private void setSaved(boolean saved)
	{
		this.fresh = false;
		this.saved = saved;
		
		if (saved)
			tabIcon.setIcon(savedTabIcon);
		else
			tabIcon.setIcon(modifiedTabIcon);
		
		parent.updateGui();
	}
	
	public boolean isFresh()
	{
		return fresh;
	}
	
	public boolean isSaved()
	{
		return saved;
	}
	
	public File getFile()
	{
		return file;
	}
	
	public JTextArea getArea()
	{
		return area;
	}
	
	public void setFile(File file)
	{
		File last = this.file;
		this.file = file;
		
		if (open())
		{
			tabLabel.setText(file.getName());
			setSaved(true);
		}
		else
		{
			this.file = last;
			open();
		}
	}
	
	public void saveToFile(File file)
	{
		System.out.println("saveToFile(): " + file);
		boolean write = true;
		
		if (file.exists())
		{
			JOptionPane confirm =
					new JOptionPane("The file already exists. Do you wish to overwrite it?",
							JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							UIManager.getIcon("OptionPane.questionIcon"));
			JDialog dialog = confirm.createDialog("Overwrite File");
			dialog.setVisible(true);
			Object selectedValue = confirm.getValue();
			
			if (selectedValue != null)
			{
				int value = (Integer) selectedValue;
				
				if (value != JOptionPane.YES_OPTION)
					write = false;
			}
		}
		
		if (write)
		{
			File last = this.file;
			this.file = file;
			
			if (store())
			{
				tabLabel.setText(file.getName());
				setSaved(true);
			}
			else
				this.file = last;
		}
	}
	
	public void save()
	{
		if (store())
			setSaved(true);
	}
	
	private boolean open()
	{
		boolean result = true;
		
		try
		{
			StringBuffer contents = new StringBuffer();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			char[] buffer = new char[4096];
			int numRead = 0;
			
			while ((numRead = reader.read(buffer)) != -1)
			{
				String data = String.valueOf(buffer, 0, numRead);
				contents.append(data);
				buffer = new char[4096];
			}
			
			reader.close();
			
			area.setText(contents.toString());
		}
		catch (Exception e)
		{
			result = false;
		}
		
		return result;
	}
	
	private boolean store()
	{
		boolean result = true;
		
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(area.getText());
			writer.close();
		}
		catch (Exception e)
		{
			result = false;
		}
		
		return result;
	}
	
	public void close()
	{
		boolean remove = true;
		
		if (!isSaved())
		{
			JOptionPane confirm =
					new JOptionPane("This tab's contents have been modified."
							+ "Do you wish to close without saving?",
							JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							UIManager.getIcon("OptionPane.questionIcon"));
			JDialog dialog = confirm.createDialog("Tab Contents Modified");
			dialog.setVisible(true);
			Object selectedValue = confirm.getValue();
			
			if (selectedValue != null)
			{
				int value = (Integer) selectedValue;
				
				if (value != JOptionPane.YES_OPTION)
					remove = false;
			}
		}
		
		if (remove)
			pane.removeTabAt(pane.indexOfComponent(this));
	}
	
	// Delegate methods
	public void stateChanged(ChangeEvent evt)
	{
		parent.updateGui();
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();
		
		if (source == tabButton)
			close();
		
		parent.updateGui();
	}
	
	public void insertUpdate(DocumentEvent evt)
	{
		setSaved(false);
	}
	
	public void removeUpdate(DocumentEvent evt)
	{
		setSaved(false);
	}
	
	public void changedUpdate(DocumentEvent evt)
	{
		setSaved(false);
	}
	
	public void caretUpdate(CaretEvent evt)
	{
		parent.updateGui();
	}
}
