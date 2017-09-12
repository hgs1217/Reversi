package reversi;

import java.awt.EventQueue;

import javax.swing.JFrame;

public class main {

	public static void main (String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new mainFrame();
				frame.setTitle("Reversi");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}
}
