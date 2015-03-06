/**
 * en-pos-maxent.bin uses tags from https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
 */

package open.sesame.swing;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import open.sesame.opennlp.NLPFactory;
import open.sesame.opennlp.PennTreebankPOS;
import open.sesame.opennlp.StopWords;
import open.sesame.opennlp.WordNet;

public class SwingGUI {
	private final String MODELS_DIRECTORY = System.getProperty("user.dir") + "/models/en/";
	private final PennTreebankPOS POS = new PennTreebankPOS();
	private final StopWords stopwords = new StopWords();
	private WordNet wordnet;
	
	JFrame frame;
	JCheckBox cbStopWords;
	JTextArea txtDocumentArea;
	JLabel lblSentence;
	JList<String> listNames;
	JTable tokenTable;
	DefaultTableModel modelTokenTable;
	DefaultListModel<String> modelNames;
	Box boxPanel;
	
	private String[] sentences;
	private int sentenceIndex = 0;

	public SwingGUI() {
		
		getWordNet(System.getenv("WORDNET_HOME" + File.separator + "dict"));
		
		//button bar
		JPanel pnlButtons = new JPanel(new FlowLayout());
		JButton btnProcess = new JButton("Process");
		cbStopWords = new JCheckBox("Include Stop Words");
		cbStopWords.setSelected(true);
		pnlButtons.add(btnProcess);
		pnlButtons.add(cbStopWords);
		
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
		listNames = new JList<String>();
		modelNames = new DefaultListModel<String>();
		listNames.setModel(modelNames);
		listNames.setFixedCellWidth(200);
		JScrollPane paneNames = new JScrollPane(listNames);
		pnlLists.add(paneNames);
		
		//table
		JPanel pnlTable = new JPanel();
		modelTokenTable = new DefaultTableModel();
		modelTokenTable.addColumn("Token");
		modelTokenTable.addColumn("POS");
		modelTokenTable.addColumn("Chunk");
		modelTokenTable.addColumn("Stems");
		tokenTable = new JTable(modelTokenTable);
		JScrollPane paneTable = new JScrollPane(tokenTable);
		pnlTable.add(paneTable);

		//table and list panel
		JPanel pnlTableAndLists = new JPanel(new FlowLayout());
		pnlTableAndLists.add(pnlTable);
		pnlTableAndLists.add(pnlLists);
		
		//box panel
		boxPanel = new Box(BoxLayout.Y_AXIS);
		boxPanel.add(pnlButtons);
		boxPanel.add(pnlTextArea);
		boxPanel.add(pnlSentence);
		boxPanel.add(pnlTableAndLists);
		
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
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // fullscreen
	}
	
	//open a wordnet connection
	private void getWordNet(String wordnetPath) {
		try {
			wordnet = new WordNet(wordnetPath);
		} catch(NullPointerException | IOException ex) {
			String retryPath = JOptionPane.showInputDialog(frame, "Path to local WordNet dict directory: ");
			if(retryPath == null)
				System.exit(0);
			else
				getWordNet(retryPath);
		}
	}
	
	private void updateView() {
		modelNames.removeAllElements();
		int rows = modelTokenTable.getRowCount();
		for(int i = rows - 1; i > -1; i--) {
			modelTokenTable.removeRow(i);
		}
		try {
			printSentence();
			String[] tokens = NLPFactory.getTokens(sentences[sentenceIndex], MODELS_DIRECTORY + "en-token.bin");
			if(!cbStopWords.isSelected()) {
				tokens = stopwords.removeStopWordsFromTokens(tokens);
			}
			String[] tags = NLPFactory.getPOS(tokens, MODELS_DIRECTORY + "en-pos-maxent.bin");
			String[] names = NLPFactory.getNames(tokens, MODELS_DIRECTORY + "en-ner-person.bin");
			String[] chunks = NLPFactory.getChunks(tokens, tags, MODELS_DIRECTORY + "en-chunker.bin");
			printTokenTable(tokens, tags, chunks);
			printNames(names);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void printSentence() {
		lblSentence.setText(sentences[sentenceIndex]);
	}
	
	private void printTokenTable(String[] tokens, String[] tags, String[] chunks) {
		if(tokens.length != tags.length && tokens.length != chunks.length) {
			throw new Error("tokens, tags, and chunks do not match length!");
		} else {
			for(int i = 0; i < tokens.length; i++) {
				modelTokenTable.addRow(new Object[]{tokens[i], POS.get(tags[i]), chunks[i]});
			}
		}
	}
	
	private void printNames(String[] names) {
		for(String name : names) {
			modelNames.addElement(name);
		}
	}
}
