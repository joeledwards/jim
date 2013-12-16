package buzuli.jim;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This is the main class for the Jim editor. All of the cross-buffer and buffer editing logic is
 * contained herein.
 * 
 * @author Joel Edwards &lt;joeledwards@gmail.com&gt;
 */
@SuppressWarnings("serial")
public class JimEditor extends JFrame implements ActionListener, ChangeListener, KeyListener
{
	private static final int MIN_WINDOW_WIDTH = 600;
	private static final int MIN_WINDOW_HEIGHT = 480;
	private static final int STARTING_WINDOW_WIDTH = MIN_WINDOW_WIDTH;
	private static final int STARTING_WINDOW_HEIGHT = 600;
	
	// Class Components
	private String clipboard = null;
	private File defaultOpenDir = new File(".");
	private File defaultSaveDir = new File(".");
	private Terminator terminator = null;
	
	private boolean isDoubleBuffered = false;
	
	private JTabbedPane tabs = new JTabbedPane();
	
	private JPanel buttonPanel;
	private JButton openButton;
	private JButton saveButton;
	
	private JMenuBar menuBar = new JMenuBar();
	private JMenu fileMenu = new JMenu("File");
	private JMenu editMenu = new JMenu("Edit");
	
	// File Menu Items
	private JMenuItem newFileMenuItem = new JMenuItem("New", KeyEvent.VK_N);
	private JMenuItem openFileMenuItem = new JMenuItem("Open", KeyEvent.VK_O);
	private JMenuItem saveFileMenuItem = new JMenuItem("Save", KeyEvent.VK_S);
	private JMenuItem closeFileMenuItem = new JMenuItem("Close", KeyEvent.VK_C);
	private JMenuItem saveAsFileMenuItem = new JMenuItem("Save As", KeyEvent.VK_A);
	private JMenuItem quitFileMenuItem = new JMenuItem("Quit", KeyEvent.VK_Q);
	
	// Edit Menu Items
	private JMenuItem selectAllEditMenuItem = new JMenuItem("Select All", KeyEvent.VK_A);
	private JMenuItem copyEditMenuItem = new JMenuItem("Copy", KeyEvent.VK_C);
	private JMenuItem cutEditMenuItem = new JMenuItem("Cut", KeyEvent.VK_X);
	private JMenuItem pasteEditMenuItem = new JMenuItem("Paste", KeyEvent.VK_P);
	
	// Toolbar Items
	
	private JToolBar toolbar = new JToolBar();
	
	public JimEditor(String title)
	{
		super(title);
		
		setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));
		setBounds(128, 128, STARTING_WINDOW_WIDTH, STARTING_WINDOW_HEIGHT);
		
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		
		createMenu();
		setJMenuBar(menuBar);
		
		setLayout(new BorderLayout());
		
		add(toolbar, BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
		add(getButtonPanel(), BorderLayout.SOUTH);
		
		createNewTab(null);
		pack();
		
		terminator = new Terminator(this, tabs);
		this.addWindowListener(terminator);
		
		// Make sure keyboard shortcut combinations work everywhere
		this.addKeyListener(this);
		this.getContentPane().addKeyListener(this);
		tabs.addKeyListener(this);
		
		updateGui();
	}
	
	private JPanel getButtonPanel()
	{
		if (buttonPanel == null)
		{
			buttonPanel = new JPanel(new BorderLayout());
			buttonPanel.add(getOpenButton(), BorderLayout.WEST);
			buttonPanel.add(getSaveButton(), BorderLayout.EAST);
		}
		
		return buttonPanel;
	}
	
	private JButton getOpenButton()
	{
		if (openButton == null)
		{
			openButton = new JButton("Open");
			openButton.addActionListener(this);
			openButton.addKeyListener(this);
		}
		
		return openButton;
	}
	
	private JButton getSaveButton()
	{
		if (saveButton == null)
		{
			saveButton = new JButton("Save");
			saveButton.addActionListener(this);
			saveButton.addKeyListener(this);
		}
		
		return saveButton;
	}
	
	private void createMenu()
	{
		// File Menu
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		fileMenu.add(newFileMenuItem);
		fileMenu.add(openFileMenuItem);
		fileMenu.add(saveFileMenuItem);
		fileMenu.add(saveAsFileMenuItem);
		fileMenu.add(closeFileMenuItem);
		fileMenu.add(quitFileMenuItem);
		newFileMenuItem.addActionListener(this);
		openFileMenuItem.addActionListener(this);
		saveFileMenuItem.addActionListener(this);
		saveAsFileMenuItem.addActionListener(this);
		closeFileMenuItem.addActionListener(this);
		quitFileMenuItem.addActionListener(this);
		
		// Edit Menu
		editMenu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(editMenu);
		editMenu.add(selectAllEditMenuItem);
		editMenu.add(copyEditMenuItem);
		editMenu.add(cutEditMenuItem);
		editMenu.add(pasteEditMenuItem);
		selectAllEditMenuItem.addActionListener(this);
		copyEditMenuItem.addActionListener(this);
		cutEditMenuItem.addActionListener(this);
		pasteEditMenuItem.addActionListener(this);
	}
	
	// Tab Operations
	private void createNewTab(File file)
	{
		EditorBufferTab tab = new EditorBufferTab(this, tabs, file, isDoubleBuffered);
		tabs.setSelectedIndex(tabs.indexOfComponent(tab));
	}
	
	private void closeCurrentTab()
	{
		int index = tabs.getSelectedIndex();
		if (index >= 0)
		{
			EditorBufferTab tab = (EditorBufferTab) tabs.getComponentAt(index);
			tab.close();
		}
	}
	
	private int getOpenIndex(File file)
	{
		int total = tabs.getTabCount();
		EditorBufferTab tab = null;
		for (int i = 0; i < total; i++)
		{
			tab = (EditorBufferTab) tabs.getComponentAt(i);
			if ((tab.getFile() != null) && tab.getFile().equals(file))
			{
				return i;
			}
		}
		return -1;
	}
	
	// File Operations
	private void open()
	{
		// select file to open
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Open File");
		fileChooser.setCurrentDirectory(defaultOpenDir);
		fileChooser.setMultiSelectionEnabled(false);
		int returnVal = fileChooser.showOpenDialog(this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			defaultOpenDir = fileChooser.getCurrentDirectory();
			File file = fileChooser.getSelectedFile();
			
			int openIndex = getOpenIndex(file);
			
			if (openIndex < 0)
			{
				EditorBufferTab tab = (EditorBufferTab) (tabs.getSelectedComponent());
				
				if ((tab != null) && tab.isFresh())
					tab.setFile(file);
				else
					createNewTab(file);
			}
			else
				tabs.setSelectedIndex(openIndex);
		}
	}
	
	private void save()
	{
		int index = tabs.getSelectedIndex();
		
		if (index < 0)
			return;
		
		EditorBufferTab tab = (EditorBufferTab) tabs.getComponentAt(index);
		
		if (tab.getFile() == null)
			saveAs();
		else
			tab.save();
	}
	
	private void saveAs()
	{
		int index = tabs.getSelectedIndex();
		
		if (index < 0)
			return;
		
		EditorBufferTab tab = (EditorBufferTab) tabs.getComponentAt(index);
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save As File");
		fileChooser.setCurrentDirectory(defaultSaveDir);
		fileChooser.setMultiSelectionEnabled(false);
		int returnVal = fileChooser.showSaveDialog(this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			defaultSaveDir = fileChooser.getCurrentDirectory();
			File file = fileChooser.getSelectedFile();
			tab.saveToFile(file);
		}
	}
	
	// Edit Operations
	private void selectAll()
	{
		int index = tabs.getSelectedIndex();
		
		if (index < 0)
			return;
		
		EditorBufferTab tab = (EditorBufferTab) tabs.getComponentAt(index);
		tab.getArea().selectAll();
	}
	
	private void editOperation(EditOperationEnum editOperation)
	{
		int index = tabs.getSelectedIndex();
		
		if (index >= 0)
		{
			EditorBufferTab tab = (EditorBufferTab) tabs.getComponentAt(index);
			JTextArea area = tab.getArea();
			int selectionStart = area.getSelectionStart();
			int selectionEnd = area.getSelectionEnd();
			int selectionWidth = selectionEnd - selectionStart;
			int caretPosition = area.getCaretPosition();
			
			if (selectionWidth > 0)
			{
				switch(editOperation)
				{
					case COPY:
						clipboard = area.getSelectedText();
						break;
						
					case CUT:
						clipboard = area.getSelectedText();
						area.replaceSelection("");
						break;
						
					case PASTE:
						area.replaceSelection(clipboard);
						break;
				}
			}
			else if ((editOperation == EditOperationEnum.PASTE) && (caretPosition >= 0))
				area.insert(clipboard, caretPosition);
		}
	}
	
	// Quit the Application
	private void quit()
	{
		terminator.windowClosing(null);
	}
	
	// Keep the GUI up-to-date after various events
	public void updateGui()
	{
		int index = tabs.getSelectedIndex();
		if (index >= 0)
		{
			EditorBufferTab tab = (EditorBufferTab) tabs.getComponentAt(index);
			if (tab.isFresh())
			{
				selectAllEditMenuItem.setEnabled(false);
				saveFileMenuItem.setEnabled(false);
				saveAsFileMenuItem.setEnabled(false);
			}
			else
			{
				selectAllEditMenuItem.setEnabled(true);
				saveAsFileMenuItem.setEnabled(true);
				if (tab.isSaved())
				{
					saveFileMenuItem.setEnabled(false);
				}
				else
				{
					saveFileMenuItem.setEnabled(true);
				}
			}
			closeFileMenuItem.setEnabled(true);
			
			JTextArea area = tab.getArea();
			int selectionStart = area.getSelectionStart();
			int selectionEnd = area.getSelectionEnd();
			int selectionWidth = selectionEnd - selectionStart;
			if (selectionWidth > 0)
			{
				cutEditMenuItem.setEnabled(true);
				copyEditMenuItem.setEnabled(true);
			}
			else
			{
				cutEditMenuItem.setEnabled(false);
				copyEditMenuItem.setEnabled(false);
			}
			
			int caretPosition = area.getCaretPosition();
			if (((caretPosition >= 0) || (selectionWidth > 0)) && (clipboard != null))
			{
				pasteEditMenuItem.setEnabled(true);
			}
			else
			{
				pasteEditMenuItem.setEnabled(false);
			}
		}
		else
		{
			closeFileMenuItem.setEnabled(false);
			saveFileMenuItem.setEnabled(false);
			saveAsFileMenuItem.setEnabled(false);
		}
	}
	
	// ActionListener
	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();
		
		if (source == getOpenButton())
			open();
		else if (source == getSaveButton())
			save();
		else if (source == openFileMenuItem)
			open();
		else if (source == saveFileMenuItem)
			save();
		else if (source == saveAsFileMenuItem)
			saveAs();
		else if (source == newFileMenuItem)
			createNewTab(null);
		else if (source == closeFileMenuItem)
			closeCurrentTab();
		else if (source == quitFileMenuItem)
			quit();
		else if (source == selectAllEditMenuItem)
			selectAll();
		else if (source == copyEditMenuItem)
			editOperation(EditOperationEnum.COPY);
		else if (source == cutEditMenuItem)
			editOperation(EditOperationEnum.CUT);
		else if (source == pasteEditMenuItem)
			editOperation(EditOperationEnum.PASTE);
		
		updateGui();
	}
	
	// StateListener
	public void stateChanged(ChangeEvent evt)
	{
		Object source = evt.getSource();
		if (source == tabs)
		{
			updateGui();
		}
	}
	
	// KeyListener
	public void keyPressed(KeyEvent evt)
	{
		int modifiers = evt.getModifiersEx();
		int keyCode = evt.getKeyCode();
		
		// Ctrl + <key>
		int onmask = KeyEvent.CTRL_DOWN_MASK;
		int offmask = KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK;
		
		if ((modifiers & (onmask | offmask)) == onmask)
		{
			switch(keyCode)
			{
				case KeyEvent.VK_S:
					save();
					break;
					
				case KeyEvent.VK_A:
					selectAll();
					break;
					
				case KeyEvent.VK_Q:
					quit();
					break;
					
				case KeyEvent.VK_N:
					createNewTab(null);
					break;
					
				case KeyEvent.VK_O:
					open();
					break;
					
				case KeyEvent.VK_W:
					closeCurrentTab();
					break;
			}
		}
		
		// Ctrl + Shift + <key>
		onmask = KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK;
		offmask = KeyEvent.ALT_DOWN_MASK;
		
		if ((modifiers & (onmask | offmask)) == onmask)
		{
			if (keyCode == KeyEvent.VK_S)
				saveAs();
		}
		
		updateGui();
	}
	
	public void keyReleased(KeyEvent evt)
	{
		updateGui();
	}
	
	public void keyTyped(KeyEvent evt)
	{
		updateGui();
	}
	
	public static void main(String[] args)
	{
		JimEditor window = new JimEditor("Buzuli Jim");
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.setVisible(true);
	}
	
	enum EditOperationEnum
	{
		COPY, CUT, PASTE
	}
	
	enum MenuCategory
	{
		FILE("File"),
		EDIT("Edit");
		
		private String name;
		
		private MenuCategory(String name)
		{
			this.name = name;
		}
		
		public String getName()
		{
			return name;
		}
	}
	
	enum MenuItem
	{
		NEW_BUFFER(MenuCategory.FILE, "New", KeyEvent.VK_N),
		OPEN_BUFFER(MenuCategory.FILE, "Open...", KeyEvent.VK_O),
		SAVE_BUFFER(MenuCategory.FILE, "Save", KeyEvent.VK_S),
		CLOSE_BUFFER(MenuCategory.FILE, "Close", KeyEvent.VK_C),
		SAVE_BUFFER_AS(MenuCategory.FILE, "Save As...", KeyEvent.VK_A),
		QUIT_EDITOR(MenuCategory.FILE, "Quit", KeyEvent.VK_Q),
		
		SELECT_ALL(MenuCategory.EDIT, "Select All", KeyEvent.VK_A),
		COPY(MenuCategory.EDIT, "Copy", KeyEvent.VK_C),
		CUT(MenuCategory.EDIT, "Cut", KeyEvent.VK_X),
		PASTE(MenuCategory.EDIT, "Paste", KeyEvent.VK_P);
		
		private MenuCategory menuCategory;
		private JMenuItem component;
		
		private MenuItem(MenuCategory menuCategory, String name, int keyEvent)
		{
			this.menuCategory = menuCategory;
			component = new JMenuItem(name, keyEvent);
		}
		
		public MenuCategory getMenuCategory()
		{
			return menuCategory;
		}
		
		public JMenuItem getComponent()
		{
			return component;
		}
	}
}
