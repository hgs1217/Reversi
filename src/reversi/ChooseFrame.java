package reversi;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ChooseFrame extends JFrame {

	private JPanel buttonPanel;
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;
	private int[] playerType = {0, 0};  // first one is p1, second one is p2
										// 0 is man, 1-5 is local AI, -1 is API inputted
	private String[] playerName = {"", ""};
	private int[] POINT = {0, 0};
	
	public ChooseFrame(int[] playerType, String[] playerName, int[] point) {
		
		this.playerType = playerType;
		this.playerName = playerName;
		this.POINT = point;
		
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		buttonPanel = new JPanel();
		
		buttonPanel.setLayout(null);
		
		JLabel title = new JLabel("P1 ("+playerName[0]+") Choose a color", JLabel.CENTER);
		JButton blackButton = new JButton("Black");
		JButton whiteButton = new JButton("White");
		
		Font titleFont = new Font("Calibri", Font.BOLD, 50);
		Font buttonFont = new Font("Arial", Font.PLAIN, 30);
		
		title.setFont(titleFont);
		title.setBounds(100, 60, 600, 80);
		
		blackButton.setFont(buttonFont);
		blackButton.setBounds(300, 200, 200, 60);
		blackButton.addActionListener(new GameAction(0, this));
		
		whiteButton.setFont(buttonFont);
		whiteButton.setBounds(300, 300, 200, 60);
		whiteButton.addActionListener(new GameAction(1, this));
		
		buttonPanel.add(title);
		buttonPanel.add(blackButton);
		buttonPanel.add(whiteButton);
		add(buttonPanel);
	}
	
	private class GameAction implements ActionListener {
		
		private JFrame main;
		private int color;
		
		public GameAction(int color, JFrame frame) {
			main = frame;
			this.color = color;
		}
		
		private void exchangeData(int color){
			
			int black = playerType[0];
			int white = playerType[1];
			String blackName = playerName[0];
			String whiteName = playerName[1];
			int p1Point = POINT[0];
			int p2Point = POINT[1];
			
			if (color == 1) {
				playerType[1] = black;
				playerType[0] = white;
				playerName[1] = blackName;
				playerName[0] = whiteName;
				POINT[0] = p2Point;
				POINT[1] = p1Point;
			}
		}
		
		public void actionPerformed(ActionEvent event) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					exchangeData(color);
					JFrame frame = new ReversiFrame(playerType, playerName);
					frame.setTitle("Reversi");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.setVisible(true);
					main.setVisible(false);
				}
			});
		}
	}
}
