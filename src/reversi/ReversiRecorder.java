package reversi;

public class ReversiRecorder {
	
	private static int[] BLACK_RESULT = {-1, -1, -1, -1, -1};
	private static int[] WHITE_RESULT = {-1, -1, -1, -1, -1};
	private static String[] P1_NAME = {"", "", "", "", ""};
	private static String[] P2_NAME = {"", "", "", "", ""};
	
	private ReversiPanel panel;
	private int blackNum = 0;
	private int whiteNum = 0;

	public static void reset(){
		for (int i=0; i<5; ++i) {
			BLACK_RESULT[i] = -1;
			WHITE_RESULT[i] = -1;
			P1_NAME[i] = "";
			P2_NAME[i] = "";
		}
	}
	
	public ReversiRecorder(ReversiPanel panel) {
		this.panel = panel;
	}
	
	private void sendResult() {
		ReversiPanel.setP1_NAME(P1_NAME);
		ReversiPanel.setP2_NAME(P2_NAME);
		ReversiPanel.setBLACK_RESULT(BLACK_RESULT);
		ReversiPanel.setWHITE_RESULT(WHITE_RESULT);
	}
	
	private void countNum(int[] blockState) {
		for (int i=0; i<64; ++i) {
			if (blockState[i] == 0) {
				blackNum++;
			} else if (blockState[i] == 1) {
				whiteNum++;
			}
		}
	}
	
	public void resultRecord(String[] playerName , int[] blockState) {
		countNum(blockState);
		for (int i=0; i<5; ++i) {
			if (P1_NAME[i] == "") {
				BLACK_RESULT[i] = blackNum;
				WHITE_RESULT[i] = whiteNum;
				P1_NAME[i] = playerName[0];
				P2_NAME[i] = playerName[1];
				sendResult();
				return;
			}
		}
		
		for (int i=0; i<4; ++i) {
			BLACK_RESULT[i] = BLACK_RESULT[i+1];
			WHITE_RESULT[i] = WHITE_RESULT[i+1];	
			P1_NAME[i] = P1_NAME[i+1];
			P2_NAME[i] = P2_NAME[i+1];
		}
		BLACK_RESULT[4] = blackNum;
		WHITE_RESULT[4] = whiteNum;
		P1_NAME[4] = playerName[0];
		P2_NAME[4] = playerName[1];
		sendResult();
		return;
	}
}
