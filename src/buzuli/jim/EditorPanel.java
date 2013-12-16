package buzuli.jim;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class EditorPanel extends JPanel
{
	private JEditorPane editorPane;
	private JScrollPane editorScrollPane;
	
	public EditorPanel()
	{
		super();
		
		setLayout(new BorderLayout());
		add(getEditorScrollPane(), BorderLayout.CENTER);
	}
	
	private JEditorPane getEditorPane()
	{
		if (editorPane == null)
			editorPane = new JEditorPane();
		
		return editorPane;
	}
	
	private JScrollPane getEditorScrollPane()
	{
		if (editorScrollPane == null)
			editorScrollPane = new JScrollPane(getEditorPane());
		
		return editorScrollPane;
	}
}
