package reversi;

import java.util.ArrayList;

public class ReversiRule {
	
	static private int judgeIndexX[] = {-1, 1, 0, 0, -1, 1, -1, 1};
	static private int judgeIndexY[] = {0, 0, -1, 1, -1, -1, 1, 1};
	static public long[] timeCNT = new long[10]; // for test
	static public int[] cnt = new int[10];
	static public long[] timeAva = new long[10];
	
	public boolean judgeAction(int currentNum, int currentPlayer, int[] blockState) {
		
		boolean properLocation = false;
		
		properLocation = wholeJudge(currentNum, 0, blockState, currentPlayer);
		
		if (properLocation) {
			blockState[currentNum] = currentPlayer;
		}
		
		return properLocation;
	}
	
	public boolean wholeJudge(int num, int testmode, int[] block, int tmpCurrentPlayer) {
		
		long startTime, endTime, differenceTime;
		startTime = System.currentTimeMillis();
		
		int currentLine = num / 8;
		int currentColumn = num % 8;
		ArrayList<Integer> willEat = new ArrayList<>();
		
		endTime = System.currentTimeMillis();
		differenceTime = endTime-startTime;
		timeCNT[0] += differenceTime;
		cnt[0] += 1;
		
		// 8 directions
		if (testmode != 0) { // optimize the efficiency of the test judgement
			
			startTime = System.currentTimeMillis();
			for (int i=0; i<8; ++i) {
				boolean judge = judgeModel(judgeIndexX[i], judgeIndexY[i], block, tmpCurrentPlayer, testmode, currentColumn,
											currentLine, willEat);
				if (judge) {
					endTime = System.currentTimeMillis();
					differenceTime = endTime-startTime;
					timeCNT[1] += differenceTime;
					cnt[1] += 1;
					return true;
				}
			}
			endTime = System.currentTimeMillis();
			differenceTime = endTime-startTime;
			timeCNT[2] += differenceTime;
			cnt[2] += 1;
			return false;
		} else {
			startTime = System.currentTimeMillis();
			for (int i=0; i<8; ++i) {
				judgeModel(judgeIndexX[i], judgeIndexY[i], block, tmpCurrentPlayer, testmode, currentColumn,
							currentLine, willEat);
			}
			endTime = System.currentTimeMillis();
			differenceTime = endTime-startTime;
			timeCNT[3] += differenceTime;
			cnt[3] += 1;

			if (!willEat.isEmpty()) {
				for (int i : willEat) {
					block[i] = tmpCurrentPlayer;
				}
				return true;
			}
			return false;
		}
	}
		
	private boolean judgeModel(int colOperation, int lineOperation, int[] block, int tmpCurrentPlayer, int testmode,
							   int tmpColumn, int tmpLine, ArrayList<Integer> willEat) {
		// testmode = 0  非测试模式          testmode = 1 普通测试模式              testmode = 2 行动力检测模式
		ArrayList<Integer> toBeEaten = new ArrayList<>();
		boolean isEat = false;
		boolean haveEat = false;
		while (true) {
			tmpColumn += colOperation;
			tmpLine += lineOperation;
			if (tmpColumn >= 0 && tmpColumn < 8 && tmpLine >= 0 && tmpLine < 8) {
				int tmpNum = tmpLine * 8 + tmpColumn;
				if (block[tmpNum] == -1) {
					break;
				} else {
					if (block[tmpNum] != tmpCurrentPlayer) {
						if (testmode != 2) {
							toBeEaten.add(tmpNum);  // the chess will be eaten
						}
						if (!haveEat) {
							haveEat = true;
						}
					} else {
						isEat = true;  // find another same color, so the middle chesses will be eaten
						break;
					}
				}
			} else {
				break;
			}
		}
		
		if (testmode == 2){
			return haveEat && isEat;
		}
		
		if (isEat) {
			for (int i : toBeEaten) {
				willEat.add(i);
			}
		}
		return !willEat.isEmpty();
	}
	
	public boolean testRemain(int currentPlayer, int[] blockState, ArrayList<Integer> undeterminedNum) {
		for (int i : undeterminedNum) {
			if (blockState[i] == -1) {
				boolean isRemain = wholeJudge(i, 1, blockState, currentPlayer);
				if (isRemain) {
					return true;
				}
			}
		}
		return false;
	}
}
