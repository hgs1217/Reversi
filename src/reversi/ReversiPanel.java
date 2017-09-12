package reversi;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

public class ReversiPanel extends JPanel {

	// default settings
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;
	private static final int X_MIN = 100;
	private static final int X_MAX = 500;
	private static final int Y_MIN = 100;
	private static final int Y_MAX = 500;
	private static final int CIRCLE_RADIUS = 20;
	private static int[] POINT = {0, 0};
	private static int[] BLACK_RESULT = {-1, -1, -1, -1, -1};
	private static int[] WHITE_RESULT = {-1, -1, -1, -1, -1};
	private static String[] P1_NAME = {"", "", "", "", ""};
	private static String[] P2_NAME = {"", "", "", "", ""};
	
	private Font font = new Font("Arial", Font.BOLD, 30);
	private Font smallFont = new Font("Calibri", Font.PLAIN, 16);
	private Font middleFont = new Font("Arial", Font.PLAIN, 18);
	private Font winnerFont = new Font("Calibri", Font.ITALIC, 40);
	
	// default variables
	private int[] blockState = new int[64];
	private int blackNum = 0;
	private int whiteNum = 0;
	private int currentPlayer = 0;
	private int currentNum = -1;
	private int[] playerType = {0, 0};  // first one is p1, second one is p2
										// 0 is man, 1-5 is local AI, -1 is API inputted
	private boolean noRemain = false;
	private boolean gameOver = false;
	private boolean gameOverCalculation = false;
	
	private String[] playerName = {"", ""};
	
	public ReversiPanel(int[] playerType){
		// default settings
		System.arraycopy(playerType, 0, this.playerType, 0, 2);
	}
	
	public static void reset(){
		POINT[0] = 0;
		POINT[1] = 0;
		for (int i=0; i<5; ++i) {
			BLACK_RESULT[i] = -1;
			WHITE_RESULT[i] = -1;
			P1_NAME[i] = "";
			P2_NAME[i] = "";
		}
	}
	
	private void gameOverDraw(Graphics2D g2) {
		
		String[] playerNameFormed = new String [2];
		for (int i=0; i<2; ++i) {
			playerNameFormed[i] = playerName[i];
			if (playerName[i].length() == 3) {
				if (i == 0) {
					playerNameFormed[i] += " ";
				} else {
					playerNameFormed[i] = " " + playerNameFormed[i];
				}
			}	
		}
		
		if (gameOver && !gameOverCalculation) {
			
			gameOverCalculation = true; // in case minimize the window will cause misaddition.
			g2.setFont(winnerFont);
			g2.setColor(Color.RED);
			
			if (blackNum > whiteNum) {
				g2.drawString("Winner!", 600, 150);
				POINT[0] += 1;
				g2.setFont(font);
				g2.drawString(String.valueOf(POINT[0]), 220, 60);
				g2.drawString(playerNameFormed[0], 100, 60);
				if (playerType[0] > 0) {
					g2.setFont(middleFont);
					g2.drawString("(AI LV" + playerType[0] + ")", 110, 80);
				}
				g2.setColor(Color.BLACK);
				g2.setFont(font);
				g2.drawString(String.valueOf(POINT[1]), 340, 60);
				g2.drawString(playerNameFormed[1], 400, 60);
				if (playerType[1] > 0) {
					g2.setFont(middleFont);
					g2.drawString("(AI LV" + playerType[1] + ")", 410, 80);
				}
			}
			if (blackNum < whiteNum) {
				g2.drawString("Winner!", 600, 360);
				POINT[1] += 1;
				g2.setFont(font);
				g2.drawString(String.valueOf(POINT[1]), 340, 60);
				g2.drawString(playerNameFormed[1], 400, 60);
				if (playerType[1] > 0) {
					g2.setFont(middleFont);
					g2.drawString("(AI LV" + playerType[1] + ")", 410, 80);
				}
				g2.setColor(Color.BLACK);
				g2.setFont(font);
				g2.drawString(String.valueOf(POINT[0]), 220, 60);
				g2.drawString(playerNameFormed[0], 100, 60);
				if (playerType[0] > 0) {
					g2.setFont(middleFont);
					g2.drawString("(AI LV" + playerType[0] + ")", 110, 80);
				}
			}
			if (blackNum == whiteNum) {
				g2.setColor(Color.BLACK);
				g2.drawString("Draw!", 600, 260);
				g2.setFont(font);
				g2.drawString(String.valueOf(POINT[0]), 340, 60);
				g2.drawString(playerNameFormed[0], 100, 60);
				if (playerType[0] > 0) {
					g2.setFont(middleFont);
					g2.drawString("(AI LV" + playerType[0] + ")", 110, 80);
				}
				g2.setFont(font);
				g2.drawString(String.valueOf(POINT[1]), 340, 60);
				g2.drawString(playerNameFormed[1], 400, 60);
				if (playerType[1] > 0) {
					g2.setFont(middleFont);
					g2.drawString("(AI LV" + playerType[1] + ")", 410, 80);
				}
			}
		} else {
			g2.setFont(font);
			g2.drawString(String.valueOf(POINT[0]), 220, 60);
			g2.drawString(String.valueOf(POINT[1]), 340, 60);
			g2.drawString(playerNameFormed[0], 100, 60);
			if (playerType[0] > 0) {
				g2.setFont(middleFont);
				g2.drawString("(AI LV" + playerType[0] + ")", 110, 80);
			}
			g2.setFont(font);
			g2.drawString(playerNameFormed[1], 400, 60);
			if (playerType[1] > 0) {
				g2.setFont(middleFont);
				g2.drawString("(AI LV" + playerType[1] + ")", 410, 80);
			}
		}
	}
	
	private void chessboardDraw(Graphics2D g2) {
		
		String location = System.getProperty("user.dir");
		String pyLocation = location + "\\pychess\\";
		
		String nowPlayer;
		if (currentPlayer == 0) {
			nowPlayer = "Black";
		} else {
			nowPlayer = "White";
		}
		
		// draw the basic structure
		for (int x=150; x<=450; x+=50) {
			Line2D line = new Line2D.Double(x, Y_MIN, x, Y_MAX);
			g2.draw(line);
		}
		
		for (int y=150; y<=450; y+=50) {
			Line2D line = new Line2D.Double(X_MIN, y, X_MAX, y);
			g2.draw(line);
		}
		
		g2.setStroke(new BasicStroke(5f));
		g2.draw(new Line2D.Double(100, Y_MIN, 100, Y_MAX));
		g2.draw(new Line2D.Double(500, Y_MIN, 500, Y_MAX));
		g2.draw(new Line2D.Double(X_MIN, 100, X_MAX, 100));
		g2.draw(new Line2D.Double(X_MIN, 500, X_MAX, 500));
		
		g2.setFont(middleFont);
		g2.drawString("("+playerName[0]+")", 620, 220);
		g2.drawString("("+playerName[1]+")", 620, 320);

		g2.setFont(font);
		g2.drawString("Black: ", 600, 200);
		g2.drawString("White: ", 600, 300);
		g2.drawString("Now: ", 600, 60);
		g2.drawString(":", 285, 60);
		
		g2.drawString(Integer.toString(blackNum), 725, 200);
		g2.drawString(Integer.toString(whiteNum), 725, 300);
		
		g2.drawString(nowPlayer, 700, 60);
		// no move
		if (noRemain && !gameOver) {
			g2.setFont(smallFont);
			if (currentPlayer == 1) {
				g2.drawString("Black has no chess to move!", 580, 100);
			} else {
				g2.drawString("White has no chess to move!", 580, 100);
			}
		}
		
		// game over
		gameOverDraw(g2);
		
		// last games record
		g2.setFont(middleFont);
		g2.setColor(Color.BLACK);
		g2.drawString("Last 5 games:", 520, 400);
		
		g2.setStroke(new BasicStroke(1f));
		Ellipse2D circle = new Ellipse2D.Double();
		circle.setFrameFromCenter(530, 418, 520, 408);
		g2.fill(circle);
		circle.setFrameFromCenter(760, 418, 750, 408);
		g2.draw(circle);
		
		// last games graph
		for (int i=0; i<5; ++i) {
			if (P1_NAME[i] != "") {
				g2.setColor(Color.BLACK);
				g2.drawString(":", 646, 425+25*i);
				if (BLACK_RESULT[i] > WHITE_RESULT[i]) {
					g2.drawString(P2_NAME[i], 690, 425+25*i);
					g2.drawString(String.valueOf(WHITE_RESULT[i]), 660, 425+25*i);
					g2.setColor(Color.RED);
					g2.drawString(P1_NAME[i], 560, 425+25*i);
					g2.drawString(String.valueOf(BLACK_RESULT[i]), 620, 425+25*i);
				}
				if (BLACK_RESULT[i] < WHITE_RESULT[i]) {
					g2.drawString(P1_NAME[i], 560, 425+25*i);
					g2.drawString(String.valueOf(BLACK_RESULT[i]), 620, 425+25*i);
					g2.setColor(Color.RED);
					g2.drawString(P2_NAME[i], 690, 425+25*i);
					g2.drawString(String.valueOf(WHITE_RESULT[i]), 660, 425+25*i);
				}
				if (BLACK_RESULT[i] == WHITE_RESULT[i]) {
					g2.drawString(P1_NAME[i], 560, 425+25*i);
					g2.drawString(String.valueOf(BLACK_RESULT[i]), 620, 425+25*i);
					g2.drawString(P2_NAME[i], 690, 425+25*i);
					g2.drawString(String.valueOf(WHITE_RESULT[i]), 660, 425+25*i);
				}
			}
		}
	}
	
	// Override paintComponent
	public void paintComponent(Graphics g) {
		
		Graphics2D g2 = (Graphics2D) g;
		g.clearRect(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
		
		// draw and count circles
		blackNum = 0;
		whiteNum = 0;
		g2.setStroke(new BasicStroke(3f));
		
		for (int i=0; i<64; ++i) {
			double x = 126 + 50 * (i % 8);
			double y = 126 + 50 * (i / 8);
			Ellipse2D circle = new Ellipse2D.Double();
			circle.setFrameFromCenter(x, y, x+CIRCLE_RADIUS, y+CIRCLE_RADIUS);
			if (blockState[i] == 0) {
				g2.fill(circle);
				blackNum++;
			}
			if (blockState[i] == 1) {
				g2.draw(circle);
				whiteNum++;
			}
		}
		if (currentNum >= 0) {
			double x = 126 + 50 * (currentNum % 8);
			double y = 126 + 50 * (currentNum / 8);
			Ellipse2D pointEm = new Ellipse2D.Double();
			pointEm.setFrameFromCenter(x, y, x+3, y+3);
			g2.setColor(Color.RED);
			g2.fill(pointEm);
			g2.setColor(Color.BLACK);
		}
		
		chessboardDraw(g2);
	}

	public Dimension getPreferredSize() {
		return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public void setNoRemain(boolean noRemain) {
		this.noRemain = noRemain;
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}

	public static void setP1_NAME(String[] p1_NAME) {
		System.arraycopy(p1_NAME, 0, P1_NAME, 0, 5);
	}

	public static void setP2_NAME(String[] p2_NAME) {
		System.arraycopy(p2_NAME, 0, P2_NAME, 0, 5);
	}

	public void setPlayerName(String[] playerName) {
		System.arraycopy(playerName, 0, this.playerName, 0, 2);
	}

	public void setBlockState(int[] blockState) {
		System.arraycopy(blockState, 0, this.blockState, 0, 64);
	}

	public void setCurrentPlayer(int currentPlayer) {
		this.currentPlayer = currentPlayer;
	}

	public void setCurrentNum(int currentNum) {
		this.currentNum = currentNum;
	}

	public static void setBLACK_RESULT(int[] bLACK_RESULT) {
		BLACK_RESULT = bLACK_RESULT;
	}

	public static void setWHITE_RESULT(int[] wHITE_RESULT) {
		WHITE_RESULT = wHITE_RESULT;
	}

	public static int[] getPOINT() {
		return POINT;
	}
}


