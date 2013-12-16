package buzuli.jim;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class FacebookFormatter extends JFrame
{
	private static final int MIN_WINDOW_WIDTH = 600;
	private static final int MIN_WINDOW_HEIGHT = 480;
	private static final int STARTING_WINDOW_WIDTH = MIN_WINDOW_WIDTH;
	private static final int STARTING_WINDOW_HEIGHT = 600;
	
	private EditorPanel editorPane;
	
	public FacebookFormatter()
	{
		super();
		
		setLayout(new BorderLayout());
		setTitle("Facebook Formatter");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new Terminator());
		setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));
		setBounds(128, 128, STARTING_WINDOW_WIDTH, STARTING_WINDOW_HEIGHT);
		
		add(getEditorPane());
		setVisible(true);
	}
	
	private EditorPanel getEditorPane()
	{
		if (editorPane == null)
			editorPane = new EditorPanel();
		
		return editorPane;
	}
	
	public static void main(String args[])
	{
		new FacebookFormatter();
	}
	
	private class Terminator extends WindowAdapter
	{
		public void windowClosing(WindowEvent e)
		{
			// TODO: perform any necessary cleanup (halt threads, close file handles, etc.)
		}
	}
}
