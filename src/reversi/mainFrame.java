package reversi;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class mainFrame extends JFrame {
	
	private JPanel buttonPanel;
	private static final int DEFAULT_WIDTH = 1000;
	private static final int DEFAULT_HEIGHT = 700;
	private String[] comboboxItem = {"Man", "AI LV1", "AI LV2", "AI LV3", "AI LV4", "AI LV5", "HgS I", "ZB I"};
	private int[] playerType = {0, 0};
	
	public mainFrame() {
		
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		buttonPanel = new JPanel();
		
		buttonPanel.setLayout(null);
		
		JLabel title = new JLabel("Reversi", JLabel.CENTER);
		JLabel author = new JLabel("by: HgS_1217_", JLabel.CENTER);
		JLabel version = new JLabel("v1.21", JLabel.CENTER);
		JLabel black = new JLabel("Black", JLabel.CENTER);
		JLabel white = new JLabel("White", JLabel.CENTER);
		JComboBox<String> player1 = new JComboBox<>();
		JComboBox<String> player2 = new JComboBox<>();
		
		for (int i=0; i<comboboxItem.length; ++i) {
			player1.addItem(comboboxItem[i]);
			player2.addItem(comboboxItem[i]);
		}
		
		JButton startButton = new JButton("Start");
		
		Font titleFont = new Font("Calibri", Font.BOLD, 60);
		Font authorFont = new Font("Calibri", Font.PLAIN, 20);
		Font buttonFont = new Font("Arial", Font.PLAIN, 25);
		Font bwFont = new Font("Calibri", Font.BOLD, 40);
		
		title.setFont(titleFont);
		title.setBounds(100, 30, 800, 80);
		
		author.setFont(authorFont);
		author.setBounds(800, 600, 200, 80);
		
		version.setFont(buttonFont);
		version.setBounds(650, 60, 200, 80);
		
		black.setFont(bwFont);
		black.setBounds(200, 200, 200, 80);
		
		white.setFont(bwFont);
		white.setBounds(600, 200, 200, 80);
		
		startButton.setFont(buttonFont);
		startButton.setBounds(400, 450, 200, 50);
		startButton.addActionListener(new SwitchAction(this));
		
		player1.setFont(buttonFont);
		player1.setBounds(200, 300, 200, 50);
		player1.addItemListener(new PlayerItemListener(player1, 0));
		
		player2.setFont(buttonFont);
		player2.setBounds(600, 300, 200, 50);
		player2.addItemListener(new PlayerItemListener(player2, 1));
		
		buttonPanel.add(title);
		buttonPanel.add(author);
		buttonPanel.add(version);
		buttonPanel.add(black);
		buttonPanel.add(white);
		buttonPanel.add(startButton);
		buttonPanel.add(player1);
		buttonPanel.add(player2);
		add(buttonPanel);
	}
	
	private class SwitchAction implements ActionListener {
		
		private JFrame main;
		
		public SwitchAction(JFrame frame) {
			main = frame;
		}
		
		public void actionPerformed(ActionEvent event) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					JFrame frame;
					String[] emptyString = {"", ""};
					frame = new ReversiFrame(playerType, emptyString);
					frame.setTitle("Reversi");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.setVisible(true);
					main.setVisible(false);
				}
			});
		}
	}
	
	private class PlayerItemListener implements ItemListener {

		private JComboBox<String> playerCombobox;
		private int playerNum;
		
		public PlayerItemListener(JComboBox<String> playerCombobox, int playerNum) {
			// TODO Auto-generated constructor stub
			this.playerCombobox = playerCombobox;
			this.playerNum = playerNum;
		}
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			// TODO Auto-generated method stub
			int index = playerCombobox.getSelectedIndex();
			switch (index) {
			case 0: 
			case 1:
			case 2:
			case 3:
			case 4:
			case 5: 
			case 6: playerType[playerNum] = index; break;
			default: playerType[playerNum] = -1; break;
			}
		}
	}
}
