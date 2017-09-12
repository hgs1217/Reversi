package reversi;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ReversiFrame extends JFrame {
	
	public ReversiFrame(int[] playerType, String[] playerName) {
		
		ReversiPanel panel = new ReversiPanel(playerType);
		panel.setLayout(null);
		add(panel);
		pack();
		
		new ReversiServer(playerType, playerName, panel, this);
	}
}
