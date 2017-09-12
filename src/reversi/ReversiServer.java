package reversi;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.omg.CORBA.TRANSACTION_UNAVAILABLE;


public class ReversiServer {

	private static final int X_MIN = 100;
	private static final int X_MAX = 500;
	private static final int Y_MIN = 100;
	private static final int Y_MAX = 500;
	private static final boolean TESTMODE = true;
	private static int CURRENT_TURN = 1;

	private ReversiPanel panel;
	private ReversiRecorder recorder;
	private ReversiFrame frame;
	private ReversiRule rule = new ReversiRule();
	private ReversiAI[] AI = new ReversiAI [2];
	private ReversiAPI[] API = new ReversiAPI [2];
	private int currentPlayer = 0; // 0 is black, 1 is white
	private int currentNum = -1; // current chess number

	private boolean gameOver = false;
	private boolean turnOn = true;
	private int[] playerType = {0, 0};  // first one is p1, second one is p2
										// 0 is man, 1-5 is local AI, -1 is API inputted
	private int[] blockState = new int[64];
	private String[] playerName = {"", ""};
	private ArrayList<Integer> remainNum = new ArrayList<>(); // remain number set
	private ArrayList<Integer> undeterminedNum = new ArrayList<>();

	public ReversiServer(int[] plrType, String[] lastPlayerName, ReversiPanel pnl, ReversiFrame frme){

		System.arraycopy(plrType, 0, playerType, 0, 2);
		CURRENT_TURN = 1;
		panel = pnl;
		frame = frme;
		recorder = new ReversiRecorder(panel);
		panel.addMouseListener(new MouseHandler());

		// set player string and set AI
		if (lastPlayerName[0] != "") {
			System.arraycopy(lastPlayerName, 0, playerName, 0, 2);
			System.out.println(playerName[0] + " " + playerName[1]);

			if (Math.signum(playerType[0]) == Math.signum(playerType[1])) {
				if (playerType[0] > 0) {
					AI[0] = new ReversiAI(playerType[0]);
					AI[1] = new ReversiAI(playerType[1]);
				} else if (playerType[0] < 0) {
					API[0] = new ReversiAPI();
					API[1] = new ReversiAPI();
				}
			} else {
				for (int i=0; i<2; ++i) {
					if (playerType[i] > 0) {
						AI[i] = new ReversiAI(playerType[i]);
					} else if (playerType[i] < 0) {
						API[i] = new ReversiAPI();
					}
				}
			}
		} else {
			if (Math.signum(playerType[0]) == Math.signum(playerType[1]) &&
					!((playerType[0] == 6 && playerType[1] != 6) || (playerType[0] != 6 && playerType[1] == 6))) {
				if (playerType[0] == 0) {
					playerName[0] = "PLR1";
					playerName[1] = "PLR2";
				} else if (playerType[0] > 0 && playerType[0] < 6) {
					playerName[0] = "COM1";
					playerName[1] = "COM2";
					AI[0] = new ReversiAI(playerType[0]);
					AI[1] = new ReversiAI(playerType[1]);
				} else if (playerType[0] == 6) {
					playerName[0] = "HgS I";
					playerName[1] = "HgS I";
					AI[0] = new ReversiAI(playerType[0]);
					AI[1] = new ReversiAI(playerType[1]);
				} else {
					playerName[0] = "ZB I";
					playerName[1] = "ZB I";
					API[0] = new ReversiAPI();
					API[1] = new ReversiAPI();
				}
			} else {
				for (int i=0; i<2; ++i) {
					if (playerType[i] == 0) {
						playerName[i] = "YOU";
					} else if (playerType[i] > 0 && playerType[i] < 6) {
						playerName[i] = "COM";
						AI[i] = new ReversiAI(playerType[i]);
					} else if (playerType[i] == 6) {
						playerName[i] = "HgS I";
						AI[i] = new ReversiAI(playerType[i]);
					} else {
						playerName[i] = "ZB I";
						API[i] = new ReversiAPI();
					}
				}
			}
		}

		// set initial blockstate
		for (int i=0; i<64; ++i) {
			blockState[i] = -1;
		}
		blockState[27] = 0;
		blockState[28] = 1;
		blockState[35] = 1;
		blockState[36] = 0;

		// set initial remain number
		for (int i=0; i<64; ++i) {
			if (i != 27 && i != 28 && i != 35 && i != 36) {
				remainNum.add(i);
			}
		}

		// set initial undetermined number
		for (int i=0; i<64; ++i) {
			if ( (i % 8 >= 2) && (i % 8 <= 5) && (i / 8 >= 2) && (i / 8 <= 5) && blockState[i] == -1) {
				undeterminedNum.add(i);
			}
		}

		buttonSettings();

		// send data to panel
		panel.setPlayerName(playerName);
		setPanelData();

		currentTurnInitial(false);
	}

	private void buttonSettings() {

		JButton backButton = new JButton("Back");
		JButton restartButton = new JButton("Restart");
		Font buttonFont = new Font("Arial", Font.PLAIN, 20);
		backButton.setFont(buttonFont);
		backButton.setBounds(650, 550, 120, 40);
		backButton.addActionListener(new backAction());

		restartButton.setFont(buttonFont);
		restartButton.setBounds(500, 550, 120, 40);
		restartButton.addActionListener(new restartAction());
		panel.add(backButton);
		panel.add(restartButton);
	}

	private void setPanelData() {
		panel.setBlockState(blockState);
		panel.setCurrentNum(currentNum);
		panel.setCurrentPlayer(currentPlayer);
	}

	private void refreshUndeterminedNum (int tmpCurrentNum, int[] tmpBlockState, ArrayList<Integer> tmpUndeterminedNum) {
		int direction[] = {-9, -8, -7, -1, 1, 7, 8, 9};
		if (tmpUndeterminedNum.contains(tmpCurrentNum)) {
			for (int i=0; i<tmpUndeterminedNum.size(); ++i) {
				if (tmpUndeterminedNum.get(i) == tmpCurrentNum) {
					tmpUndeterminedNum.remove(i);
					break;
				}
			}
		}
		for (int i=0; i<8; ++i) {
			int tmpNum = tmpCurrentNum + direction[i];
			if (tmpNum >= 0 && tmpNum < 64) {
				if (tmpBlockState[tmpNum] == -1 && !tmpUndeterminedNum.contains(tmpNum)) {
					tmpUndeterminedNum.add(tmpNum);
				}
			}
		}
	}

	private void currentTurnInitial(boolean isClicked){
		// examine whether it is man turn or ai turn or others

		if (playerType[currentPlayer] == 0) {
			if (isClicked) {
				currentTurn();
			}
		} else if (playerType[currentPlayer] > 0) {
			getAIIndex();
		} else {
			getAPIIndex();
		}
	}

	private void currentTurn() {

		boolean properLocation = false;
		boolean willJudge = (blockState[currentNum] == -1 && !gameOver);

		if (willJudge) {

			properLocation = rule.judgeAction(currentNum, currentPlayer, blockState);
			if (properLocation) {

				refreshUndeterminedNum(currentNum, blockState, undeterminedNum);
				CURRENT_TURN++;

				if (currentPlayer == 0) {
					currentPlayer = 1;
				} else {
					currentPlayer = 0;
				}

				// examine whether the other player can move
				boolean isRemain = rule.testRemain(currentPlayer, blockState, undeterminedNum);
				if (!isRemain) {
					panel.setNoRemain(true);
					if (currentPlayer == 0) {
						currentPlayer = 1;
					} else {
						currentPlayer = 0;
					}
					boolean otherRemain = rule.testRemain(currentPlayer, blockState, undeterminedNum);
					if (!otherRemain) {
						// game over
						panel.setGameOver(true);
						gameOver = true;
						recorder.resultRecord(playerName, blockState);
						try {
							AI[0].setMapInitial();
							AI[1].setMapInitial();
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				} else {
					panel.setNoRemain(false);
				}

				setPanelData();
				panel.repaint();
			}

			/*for (int i : undeterminedNum) {
				System.out.print(i + " ");
			}
			System.out.println("");*/

			if (!gameOver && turnOn) {
				currentTurnInitial(false);
			}
		}
	}

	private void getAIIndex() {
		// AI turn
		new Thread(new Runnable() {
			public void run() {

				try {
					long startTime = System.currentTimeMillis();

					// if return an illegal number, it may cause errors.
					currentNum = AI[currentPlayer].AIOperation(currentPlayer, blockState, undeterminedNum);

					long endTime = System.currentTimeMillis();
					long differenceTime = endTime-startTime;

					if (differenceTime < 1000 && !TESTMODE) {
						Thread.sleep(1000);
					} else {
						Thread.sleep(1);
					}

					currentTurn();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void getAPIIndex() {
		// API turn
		new Thread(new Runnable() {
			public void run() {

				try {
					Thread.sleep(1000);
					long startTime = System.currentTimeMillis();

					// if return an illegal number, it may cause errors.
					currentNum = API[currentPlayer].getResult(blockState, currentPlayer);

					long endTime = System.currentTimeMillis();
					long differenceTime = endTime-startTime;

					if (differenceTime < 1000 && !TESTMODE) {
						Thread.sleep(1000);
					} else {
						Thread.sleep(1);
					}

					currentTurn();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public class MouseHandler extends MouseAdapter {

		public void mousePressed (MouseEvent event) {

			if (playerType[currentPlayer] == 0) {
				double x = event.getX();
				double y = event.getY();

				if (x >= X_MIN && x < X_MAX && y >= Y_MIN && y < Y_MAX) {
					currentNum = ((int)(x - 100) / 50 + (int)(y - 100) / 50 * 8);
					currentTurnInitial(true);
				}
			}
		}
	}

	private class backAction implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					JFrame newFrame = new mainFrame();
					newFrame.setTitle("Reversi");
					newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					newFrame.setVisible(true);
					ReversiPanel.reset();
					ReversiRecorder.reset();
					frame.setVisible(false);
					turnOn = false;
				}
			});
		}
	}

	private class restartAction implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					JFrame newFrame = new ChooseFrame(playerType, playerName, ReversiPanel.getPOINT());
					newFrame.setTitle("Reversi");
					newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					newFrame.setVisible(true);
					frame.setVisible(false);
					turnOn = false;
				}
			});
		}
	}

	public static int getCURRENT_TURN() {
		return CURRENT_TURN;
	}


}
