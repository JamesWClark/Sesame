package open.sesame.swing;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import open.sesame.nlp.NLPFactory;

public class SwingGUI {
	private final String MODELS_DIRECTORY = System.getProperty("user.dir") + "/models/en/";
	
	JFrame frame;
	JTextArea txtDocumentArea;
	JLabel lblSentence;
	JList<String> listTokens;
	DefaultListModel<String> modelTokens;
	JList<String> listNames;
	DefaultListModel<String> modelNames;
	JList<String> listPOS;
	DefaultListModel<String> modelPOS;
	
	private String[] sentences;
	private int sentenceIndex = 0;

	public SwingGUI() {
		
		//button bar
		JPanel pnlButtons = new JPanel(new FlowLayout());
		JButton btnProcess = new JButton("Process");
		pnlButtons.add(btnProcess);
		
		//text area
		JPanel pnlTextArea = new JPanel();
		int rows = 10, columns = 50;
		txtDocumentArea = new JTextArea(rows, columns);
		txtDocumentArea.setLineWrap(true);
		txtDocumentArea.setWrapStyleWord(true);
		pnlTextArea.add(txtDocumentArea);
		
		//sentence selector
		JPanel pnlSentence = new JPanel(new FlowLayout());
		JButton btnPrev = new JButton("<");
		JButton btnNext = new JButton(">");
		lblSentence = new JLabel();
		pnlSentence.add(btnPrev);
		pnlSentence.add(btnNext);
		pnlSentence.add(lblSentence);
		
		//list views
		JPanel pnlLists = new JPanel(new FlowLayout());
		listTokens = new JList<String>();
		listNames = new JList<String>();
		listPOS = new JList<String>();
		modelTokens = new DefaultListModel<String>();
		modelNames = new DefaultListModel<String>();
		modelPOS = new DefaultListModel<String>();
		listTokens.setModel(modelTokens);
		listNames.setModel(modelNames);
		listPOS.setModel(modelPOS);
		JScrollPane paneTokens = new JScrollPane(listTokens);
		JScrollPane paneNames = new JScrollPane(listNames);
		JScrollPane panePOS = new JScrollPane(listPOS);
		pnlLists.add(paneTokens);
		pnlLists.add(paneNames);
		pnlLists.add(panePOS);
		
		Box boxPanel = new Box(BoxLayout.Y_AXIS);
		boxPanel.add(pnlButtons);
		boxPanel.add(pnlTextArea);
		boxPanel.add(pnlSentence);
		boxPanel.add(pnlLists);
		
		btnProcess.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					sentences = NLPFactory.getSentences(txtDocumentArea.getText(), MODELS_DIRECTORY + "en-sent.bin");
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				sentenceIndex = 0;
				updateView();
			}
		});
		
		btnPrev.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				sentenceIndex--;
				updateView();
			}
		});

		btnNext.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				sentenceIndex++;
				updateView();
			}
		});
		
		frame = new JFrame("Sesame");
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(boxPanel);
		frame.pack();
		frame.setResizable(true);
		frame.setVisible(true);
		// http://www.java-forums.org/awt-swing/3491-jframe-center-screen.html
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int w = frame.getSize().width;
		int h = frame.getSize().height;
		int x = (dim.width - w) / 2;
		int y = (dim.height - h) / 2;
		frame.setLocation(x, y);
	}
	private void updateView() {
		modelTokens.removeAllElements();
		modelNames.removeAllElements();
		modelPOS.removeAllElements();
		try {
			printSentence();
			String[] tokens = NLPFactory.getTokens(sentences[sentenceIndex], MODELS_DIRECTORY + "en-token.bin");
			printTokens(tokens);
			String[] names = NLPFactory.getNames(tokens, MODELS_DIRECTORY + "en-ner-person.bin");
			printNames(names);
			String[] tags = NLPFactory.getPOS(tokens, MODELS_DIRECTORY + "en-pos-maxent.bin");
			printPOS(tags);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	private void printSentence() {
		lblSentence.setText("<html><p>" + sentences[sentenceIndex] + "</p></html>");
	}
	private void printTokens(String[] tokens) {
		for(String token : tokens) {
			modelTokens.addElement(token);
		}
	}
	private void printNames(String[] names) {
		for(String name : names) {
			modelNames.addElement(name);
		}
	}
	private void printPOS(String[] tags) {
		for(String tag : tags) {
			modelPOS.addElement(tag);
		}
	}
}
