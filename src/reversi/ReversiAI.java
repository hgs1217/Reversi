package reversi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.rowset.Joinable;
import javax.swing.text.StyledEditorKit.UnderlineAction;
import javax.xml.bind.SchemaOutputResolver;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.w3c.dom.css.ElementCSSInlineStyle;

import com.kenai.jffi.Array;

public class ReversiAI {
	
	private static final int[] MINMAX_DEPTH = {0, 2, 4, 6, 6, 8}; 
	private static final double EVALUATE_SCALAR = 1;
	private static final double MOVEMENT_SCALAR = 5;
	private static final boolean[] movementOPEN = {false, false, false, false, true, true};
	private static final boolean openRandom = false;   // test mode
	private static final boolean openDynamicSteps = true;  // test mode
	
	private ReversiRule rule = new ReversiRule();
	private int level = 1;
	private int currentPlayer = 0;
	private int oppoPlayer = 1;
	private int currentNum = -1;
	private int currentTurn = 1;
	private int remainCount = 60;
	private int currentMaxDepth = 2;
	private int[] blockState = new int[64];
	private ArrayList<Integer> bestLocationSet; // best location points' set
	private ArrayList<Integer> undeterminedNum = new ArrayList<>();
	private ArrayList<Integer> originalNum = new ArrayList<>();
	private HashMap<String, Double> substitutionMap = new HashMap<>();
	private HashMap<String, Integer> substitutionDepth = new HashMap<>();
	private HashMap<String, Integer> substitutionLocation = new HashMap<>();
	private HashMap<Integer, ArrayList<Integer>> historyMap = new HashMap<>();
	
	private long totalTime = 0; // for test
	private int turnCNT = 0;
	private long recordTime = 0; // for test
	private long hashTime = 0;
	private long calculateTime = 0;
	private long testTime = 0;
	private int[] cnt = new int[3]; // for test
	
	public ReversiAI (int level) {
		this.level = level;
	}
	
	private void setOppoPlayer() {
		if (currentPlayer == 0) {
			oppoPlayer = 1;
		} else {
			oppoPlayer = 0;
		}
	}
	
	private void setMaxDepth(){
		if (openDynamicSteps) {
			if (currentTurn >= 49) {
				currentMaxDepth = MINMAX_DEPTH[level-1] + 6;
			} else if (currentTurn >= 45) {
				currentMaxDepth = MINMAX_DEPTH[level-1] + 4;
			} else if (currentTurn >= 39) {
				currentMaxDepth = MINMAX_DEPTH[level-1] + 2;
			} else {
				currentMaxDepth = MINMAX_DEPTH[level-1];
			}
		} else {
			currentMaxDepth = MINMAX_DEPTH[level-1];
		}
		return;
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
	
	private void substitutionMoveTop(int goodNum, ArrayList<Integer> tmpUndeterminedNum) {
		if (tmpUndeterminedNum.contains(goodNum)) {
			int index = 0;
			for (int i=0; i<tmpUndeterminedNum.size(); ++i) {
				if (tmpUndeterminedNum.get(i) == goodNum) {
					index = i;
					break;
				}
			}
			tmpUndeterminedNum.remove(index);
			tmpUndeterminedNum.add(0, goodNum);
		}
	}
	
	private void historyMoveTop(int tmpCurrentTurn, ArrayList<Integer> tmpUndeterminedNum) {
		
		try {
			ArrayList<Integer> goodNumSet = historyMap.get(tmpCurrentTurn);
			for (int goodNum : goodNumSet) {
				if (tmpUndeterminedNum.contains(goodNum)) {
					int index = 0;
					for (int i=0; i<tmpUndeterminedNum.size(); ++i) {
						if (tmpUndeterminedNum.get(i) == goodNum) {
							index = i;
							break;
						}
					}
					tmpUndeterminedNum.remove(index);
					tmpUndeterminedNum.add(0, goodNum);
				}
			}
		} catch (Exception e) {
			
		}
	}
	
	/*private void listMoveAhead(int element, ArrayList<Integer> arrayList) {
		int index = 1;
		if (arrayList.contains(element)) {
			for (int i=0; i<arrayList.size(); ++i) {
				if (arrayList.get(i) == element) {
					index = i;
					break;
				}
			}
			if (index > 0) {
				arrayList.remove(index);
				arrayList.add(index-1, element);
			}
		}
	}*/
	
	private boolean specialScoreModification(int[] tmpBlockState, int index){
		
		int certainColor = tmpBlockState[index];
		switch (index) {
		case 9:
			if (tmpBlockState[0] == certainColor) {
				return true;
			}
		case 14:
			if (tmpBlockState[7] == certainColor) {
				return true;
			}
		case 49:
			if (tmpBlockState[56] == certainColor) {
				return true;
			}
		case 54:  
			if (tmpBlockState[63] == certainColor) {
				return true;
			}
		case 1:
			if (tmpBlockState[0] == certainColor) {
				return true;
			}
			if (tmpBlockState[2] == certainColor) {
				for (int i=3; i<8; ++i) {
					if (tmpBlockState[i] == -1) {
						return true;
					} else {
						if (tmpBlockState[i] != certainColor) {
							return false;
						}
					}
				}
				return true;
			}
			for (int i=0; i<8; ++i) {
				if (tmpBlockState[i] == -1) {
					return false;
				}
			}
			return true;
		case 6:
			if (tmpBlockState[7] == certainColor) {
				return true;
			}
			if (tmpBlockState[5] == certainColor) {
				for (int i=4; i>-1; --i) {
					if (tmpBlockState[i] == -1) {
						return true;
					} else {
						if (tmpBlockState[i] != certainColor) {
							return false;
						}
					}
				}
				return true;
			}
			for (int i=0; i<8; ++i) {
				if (tmpBlockState[i] == -1) {
					return false;
				}
			}
			return true;
		case 8:
			if (tmpBlockState[0] == certainColor) {
				return true;
			}
			if (tmpBlockState[16] == certainColor) {
				for (int i=24; i<64; i+=8) {
					if (tmpBlockState[i] == -1) {
						return true;
					} else {
						if (tmpBlockState[i] != certainColor) {
							return false;
						}
					}
				}
				return true;
			}
			for (int i=0; i<64; i+=8) {
				if (tmpBlockState[i] == -1) {
					return false;
				}
			}
			return true;
		case 15:
			if (tmpBlockState[7] == certainColor) {
				return true;
			}
			if (tmpBlockState[23] == certainColor) {
				for (int i=31; i<64; i+=8) {
					if (tmpBlockState[i] == -1) {
						return true;
					} else {
						if (tmpBlockState[i] != certainColor) {
							return false;
						}
					}
				}
				return true;
			}
			for (int i=7; i<64; i+=8) {
				if (tmpBlockState[i] == -1) {
					return false;
				}
			}
			return true;
		case 48:
			if (tmpBlockState[56] == certainColor) {
				return true;
			}
			if (tmpBlockState[40] == certainColor) {
				for (int i=32; i>-1; i-=8) {
					if (tmpBlockState[i] == -1) {
						return true;
					} else {
						if (tmpBlockState[i] != certainColor) {
							return false;
						}
					}
				}
				return true;
			}
			for (int i=0; i<64; i+=8) {
				if (tmpBlockState[i] == -1) {
					return false;
				}
			}
			return true;
		case 55:
			if (tmpBlockState[63] == certainColor) {
				return true;
			}
			if (tmpBlockState[47] == certainColor) {
				for (int i=39; i>-1; i-=8) {
					if (tmpBlockState[i] == -1) {
						return true;
					} else {
						if (tmpBlockState[i] != certainColor) {
							return false;
						}
					}
				}
				return true;
			}
			for (int i=7; i<64; i+=8) {
				if (tmpBlockState[i] == -1) {
					return false;
				}
			}
			return true;
		case 57:
			if (tmpBlockState[56] == certainColor) {
				return true;
			}
			if (tmpBlockState[58] == certainColor) {
				for (int i=59; i<64; ++i) {
					if (tmpBlockState[i] == -1) {
						return true;
					} else {
						if (tmpBlockState[i] != certainColor) {
							return false;
						}
					}
				}
				return true;
			}
			for (int i=56; i<64; ++i) {
				if (tmpBlockState[i] == -1) {
					return false;
				}
			}
			return true;
		case 62:
			if (tmpBlockState[63] == certainColor) {
				return true;
			}
			if (tmpBlockState[61] == certainColor) {
				for (int i=60; i>55; --i) {
					if (tmpBlockState[i] == -1) {
						return true;
					} else {
						if (tmpBlockState[i] != certainColor) {
							return false;
						}
					}
				}
				return true;
			}
			for (int i=56; i<64; ++i) {
				if (tmpBlockState[i] == -1) {
					return false;
				}
			}
			return true;
		}
		
		return false;
	}
	
	private int getNum(int[] tmpBlockState) {
		int difference, tmpOppoNum = 0, tmpYouNum = 0;
		for (int i=0; i<64; ++i) {
			if (tmpBlockState[i] != -1) {
				if (tmpBlockState[i] == currentPlayer) {
					++tmpYouNum;
				} else {
					++tmpOppoNum;
				}
			}
		}
		difference = tmpYouNum - tmpOppoNum;
		return difference;
	}
	
	private boolean allKill(int[] tmpBlockState) {
		// the case computer will immediately win
		for (int i=0; i<64; ++i) {
			if (tmpBlockState[i] == oppoPlayer) {
				return false;
			}
		}
		return true;
	}

	private int getEvaluateScore(int[] tmpBlockState){
		
		final int corner = 120, halfstar = -40, star = -80, side = 10, secondside = 1, innercorner = 5, innerside = 2, inner = 1;
		
		final int[] pointScore = {corner, 	halfstar, 	side, 	side, 	side, 	side, 	halfstar, 	corner,
							      halfstar, star, secondside, secondside, secondside, secondside, star, halfstar,
								  side, secondside, innercorner, innerside, innerside, innercorner, secondside, side,
								  side, secondside, innerside, inner, inner, innerside, secondside, side,
								  side, secondside, innerside, inner, inner, innerside, secondside, side,
								  side, secondside, innercorner, innerside, innerside, innercorner, secondside, side,
								  halfstar, star, secondside, secondside, secondside, secondside, star, halfstar,
								  corner, 	halfstar, 	side, 	side, 	side, 	side, 	halfstar, 	corner};
		int score = 0;
		
		for (int i=0; i<64; ++i) {
			boolean special = false;
			if (tmpBlockState[i] != -1) {
				
				special = specialScoreModification(tmpBlockState, i);
				
				if (tmpBlockState[i] == currentPlayer) {
					if (special) {
						switch (i) {  // some special rules of score calculation about the negative locations
						case 9:  
						case 14:
						case 49:
						case 54:  
							score += ( -star + side);
							break;
						case 1:  
						case 6:  
						case 8:  
						case 15:  
						case 48:  
						case 55:  
						case 57:  
						case 62:  
							score += ( -halfstar + side);
							break;
						default: break;
						}
					}
					score += pointScore[i];
				} else {
					if (special) {
						switch (i) {  // some special rules of score calculation about the negative locations
						case 9:  
						case 14:
						case 49:
						case 54:  
							score -= ( -star + side);
							break;
						case 1:  
						case 6:  
						case 8:  
						case 15:  
						case 48:  
						case 55:  
						case 57:  
						case 62:  
							score -= ( -halfstar + side);
							break;
						default: break;
						}
					}
					score -= pointScore[i];
				}
			}
		}

		return score;
	}
	
	private int getMovementScore(int[] tmpBlockState, ArrayList<Integer> nextTmpUndeterminedNum){
		long startTime = System.currentTimeMillis();
		
		int movementScore = 0;
		int currentScore = 0;
		int oppoScore = 0;
		
		for (int i : nextTmpUndeterminedNum) {
			
			if (tmpBlockState[i] == -1) {
				boolean currentProper = rule.wholeJudge(i, 2, tmpBlockState, currentPlayer);
				if (currentProper) {
					currentScore += 1;
				}
				
				boolean oppoProper = rule.wholeJudge(i, 2, tmpBlockState, oppoPlayer);
				if (oppoProper) {
					oppoScore += 1;
				}	
			}
		}
		
		if (currentScore == 0) {
			return -1000;
		} else if (oppoScore == 0) {
			return 1000;
		} else {
			movementScore = currentScore - oppoScore;
		}
		
		long endTime = System.currentTimeMillis();
		long differenceTime = endTime-startTime;
		testTime += differenceTime;
		
		return movementScore;
	}
	
	private double getTotalScore(int[] tmpBlockState, ArrayList<Integer> nextTmpUndeterminedNum){
		
		long startTime = System.currentTimeMillis();

		double total = 0;
		int evaluateScore = 0, movementScore = 0;
		
		evaluateScore = getEvaluateScore(tmpBlockState);
		
		if (movementOPEN[level-1]){
			movementScore = getMovementScore(tmpBlockState, nextTmpUndeterminedNum);
		}
		
		total = evaluateScore * EVALUATE_SCALAR + movementScore * MOVEMENT_SCALAR;
		
		long endTime = System.currentTimeMillis();
		long differenceTime = endTime-startTime;
		calculateTime += differenceTime;
		
		return total;
	}
	
	private double searchMap(String hashKey, int depth, ArrayList<Integer> tmpUndeterminedNum) {
		
		if (substitutionDepth.containsKey(hashKey)){
			if (substitutionDepth.get(hashKey) == depth) {
				cnt[1]++;
				return substitutionMap.get(hashKey);
			} else {
				cnt[2]++;
				int goodNum = substitutionLocation.get(hashKey);
				substitutionMoveTop(goodNum, tmpUndeterminedNum);
			}
		}
		historyMoveTop(currentTurn+depth-1, tmpUndeterminedNum);
		return 0.01;
	}
	
	private double minMax(int[] tmpBlocks, ArrayList<Integer> tmpUndeterminedNum, int depth, double alphaOrBeta){
		// odd depth get parent's beta, even depth get parent's alpha
		double alpha = -10000;
		double beta = 10000;
		int[] nextTmpBlocks = new int[64];
		ArrayList<Integer> nextTmpUndeterminedNum;
		
		int tmpCurrentPlayer;
		int tmpOppoPlayer;
		if (depth % 2 == 1) {
			if (currentPlayer == 0) {
				tmpCurrentPlayer = 0;
				tmpOppoPlayer = 1;
			} else {
				tmpCurrentPlayer = 1;
				tmpOppoPlayer = 0;
			}
		} else {
			if (currentPlayer == 0) {
				tmpCurrentPlayer = 1;
				tmpOppoPlayer = 0;
			} else {
				tmpCurrentPlayer = 0;
				tmpOppoPlayer = 1;
			}
		}

		String hashKey = getHashKey(tmpBlocks, tmpCurrentPlayer);
		double searchResult = searchMap(hashKey, depth, tmpUndeterminedNum);
		if (searchResult != 0.01) {
			return searchResult;
		}
		
		if (depth % 2 == 1) {
			beta = alphaOrBeta;
			for (int j : tmpUndeterminedNum) {
				
				boolean isProper = false;
				for (int i=0; i<64; ++i) {
					nextTmpBlocks[i] = tmpBlocks[i];  // nextTmpBlocks copies blockState to analog the situation in the next depth
				}
				nextTmpUndeterminedNum = new ArrayList<>(tmpUndeterminedNum);
				
				isProper = rule.wholeJudge(j, 0, nextTmpBlocks, tmpCurrentPlayer);  // judge whether j is a proper location
				cnt[0]++;
				nextTmpBlocks[j] = tmpCurrentPlayer;
				if (isProper) {  // if j is a proper location, calculate the score
					
					refreshUndeterminedNum(j, nextTmpBlocks, nextTmpUndeterminedNum);
					
					if (depth == currentMaxDepth || nextTmpUndeterminedNum.size() == 0) {
						double score;
						if (remainCount <= currentMaxDepth) {
							score = getNum(nextTmpBlocks);
						} else {
							score = getTotalScore(nextTmpBlocks, nextTmpUndeterminedNum);
						}
						
						if (score > alpha) {
							alpha = score;
							substitutionRecord(hashKey, score, j, depth); 
							if (depth == 1) {
								bestLocationSet = new ArrayList<>();
								bestLocationSet.add(j);
							}
						} else if (score == alpha) {
							substitutionRecord(hashKey, score, j, depth); 
							if (depth == 1) {
								bestLocationSet.add(j);
							}
						}
					} else {
						if (remainCount <= currentMaxDepth || alpha < beta) {
							double score = 0.01;
							score = minMax(nextTmpBlocks, nextTmpUndeterminedNum, depth+1, alpha);
							
							if (score == 0.01) {
								new InterruptedException().printStackTrace(); // report error!
							}
							
							if (score > alpha) {
								alpha = score;
								substitutionRecord(hashKey, score, j, depth); 
								if (depth == 1) {
									bestLocationSet = new ArrayList<>();
									bestLocationSet.add(j);
								}
							} else if (score == alpha) {
								substitutionRecord(hashKey, score, j, depth); 
								if (depth == 1) {
									bestLocationSet.add(j);
								}
							}
						}
					}
				}
			}
			if (depth == 1) {
				return alpha;
			}
			// �����˼�������ɿ��µ����
			if (alpha == -10000) {
				if (remainCount <= currentMaxDepth) {
					alpha = getNum(nextTmpBlocks);
				} else {
					alpha = getTotalScore(nextTmpBlocks, tmpUndeterminedNum);
				}
			}
			return alpha;
		}
		
		if (currentMaxDepth == 1) {
			if (remainCount <= currentMaxDepth) {
				beta = getNum(tmpBlocks);
			} else {
				beta = getTotalScore(tmpBlocks, tmpUndeterminedNum);
			}
			return beta;
		}
		
		alpha = alphaOrBeta;
		for (int j : tmpUndeterminedNum) {
			
			boolean isProper = false;
			for (int i=0; i<64; ++i) {
				nextTmpBlocks[i] = tmpBlocks[i];  // nextTmpBlocks copies blockState to analog the situation in the next depth
			}
			nextTmpUndeterminedNum = new ArrayList<>(tmpUndeterminedNum);

			isProper = rule.wholeJudge(j, 0, nextTmpBlocks, tmpCurrentPlayer);  // judge whether j is a proper location
			cnt[0]++;
			nextTmpBlocks[j] = tmpCurrentPlayer;
			if (isProper) {  // if j is a proper location, calculate the score
				
				refreshUndeterminedNum(j, nextTmpBlocks, nextTmpUndeterminedNum);
				
				if ( (depth == currentMaxDepth) || (nextTmpUndeterminedNum.size() == 0)) {
					double score;
					if (remainCount <= currentMaxDepth) {
						score = getNum(nextTmpBlocks);
					} else {
						score = getTotalScore(nextTmpBlocks, nextTmpUndeterminedNum);
					}
					
					if (score < beta && score != -10000) {
						beta = score;
						substitutionRecord(hashKey, score, j, depth);
					} else if (score >= beta) {
						historyRecord(currentTurn+depth-1, j);
					}
				} else {
					if (remainCount <= currentMaxDepth || alpha < beta) {
						double score = 0.01;
						score = minMax(nextTmpBlocks, nextTmpUndeterminedNum, depth+1, beta);
						
						if (score == 0.01) {
							new InterruptedException().printStackTrace(); // report error!
						}
						
						if (score < beta && score != -10000) {
							beta = score;
							substitutionRecord(hashKey, score, j, depth); 
						} else if (score >= beta) {
							historyRecord(currentTurn+depth-1, j);
						}
					}
				}
			}
		}
		// �����˶Է�������µ����
		if (beta == 10000) {
			if (remainCount <= currentMaxDepth) {
				beta = getNum(nextTmpBlocks);
			} else {
				beta = getTotalScore(nextTmpBlocks, tmpUndeterminedNum);
			}
		}
		return beta;
	}
	
	private String getHashKey(int[] tmpBlockState, int tmpCurrentPlayer) {
		
		long startTime = System.currentTimeMillis();
		
		String hashKey = String.valueOf(tmpCurrentPlayer);
		StringBuilder stringBuilder = new StringBuilder(hashKey);
		for (int i=0; i<64; ++i) {
			if (tmpBlockState[i] == -1) {
				stringBuilder.append("9");
			} else {
				stringBuilder.append(String.valueOf(tmpBlockState[i]));
			}
		}
		hashKey = stringBuilder.toString();
		long endTime = System.currentTimeMillis();
		long differenceTime = endTime-startTime;
		hashTime += differenceTime;
		
		return hashKey;
	}
	
	private void substitutionRecord(String hashKey, double score, int location, int depth) {
		
		long startTime = System.currentTimeMillis();
		
		substitutionMap.put(hashKey, score);
		substitutionDepth.put(hashKey, depth);
		substitutionLocation.put(hashKey, location);
		
		long endTime = System.currentTimeMillis();
		long differenceTime = endTime-startTime;
		recordTime += differenceTime;
		
		//System.out.println(recordTime + "\t record \t" + level);*/
	}
	
	private void historyRecord (int tmpCurrentTurn, int goodNum) {
		
		ArrayList<Integer> goodNumSet = new ArrayList<>();
		
		if (historyMap.containsKey(tmpCurrentTurn)) {
			goodNumSet = historyMap.get(tmpCurrentTurn);
		}
		
		if (!goodNumSet.contains(goodNum)) {
			goodNumSet.add(goodNum);
		}
		historyMap.put(tmpCurrentTurn, goodNumSet);
	}
	
	public void AI1Operation(){
		
		Random rand = new Random();
		ArrayList<Integer> tmpRemainNumSize = new ArrayList<>();
		for (int i=0; i<undeterminedNum.size(); ++i) {
			tmpRemainNumSize.add(i);   // Form the size of the remainNum
		}
			
		while (tmpRemainNumSize.size() != 0) {
			int index = rand.nextInt(undeterminedNum.size());
			//System.out.println(String.valueOf(index) + "\t" + String.valueOf(remainNum.size()));
			for (int i=0; i<tmpRemainNumSize.size(); ++i) {
				if (undeterminedNum.get(i) == index) {
					tmpRemainNumSize.remove(i);  // Remove the num which has been got.
					break;
				}
			}
			currentNum = undeterminedNum.get(index);
			boolean isProper = rule.judgeAction(currentNum, currentPlayer, blockState);
			if (isProper) {
				break;
			}
		}
	}
	
	public void AI23456Operation(){
		
		int index = 0;
		bestLocationSet = new ArrayList<>();
		
		minMax(blockState, undeterminedNum, 1, 10000);
		
		if (openRandom) {
			Random rand = new Random();
			index = rand.nextInt(bestLocationSet.size());
		} else {
			index = 0;
		}
		
		currentNum = bestLocationSet.get(index);
		System.out.println("!" + currentNum + "\t" + bestLocationSet.size());
	}
	
	public int AIOperation(int currentPlayer, int[] blockState, ArrayList<Integer> undeterminedNum){
		
		// data initial settings
		System.arraycopy(blockState, 0, this.blockState, 0, 64);
		this.currentPlayer = currentPlayer;
		this.originalNum = undeterminedNum;
		this.undeterminedNum = new ArrayList<>(undeterminedNum);
		this.bestLocationSet = new ArrayList<>();
		currentTurn = ReversiServer.getCURRENT_TURN();
		remainCount = 60 - currentTurn;
		turnCNT++;
		
		setOppoPlayer();
		setMaxDepth();
		
		long startTime = System.currentTimeMillis();
		
		switch (level){
		case 1: AI1Operation(); break;
		case 2:
		case 3:
		case 4:
		case 5:
		case 6: AI23456Operation(); break;
		}
		
		long endTime = System.currentTimeMillis();
		long differenceTime = endTime-startTime;
		totalTime += differenceTime;
		for (int i=0; i<=5; ++i) {
			if (ReversiRule.cnt[i] != 0) {
				ReversiRule.timeAva[i] = ReversiRule.timeCNT[i] * 1000000 / ReversiRule.cnt[i];
			} else {
				ReversiRule.timeAva[i] = 0;
			}
		}
		System.out.println(differenceTime + "\t" + currentTurn  + "\t" + totalTime/turnCNT + "\t" + recordTime + 
									"\t" + hashTime + "\t" + calculateTime + "\t" + testTime + "\t" + cnt[0] + 
									"\t" + cnt[1] + "\t" + cnt[2] + "\t\t" + ReversiRule.timeAva[0] + "\t" + 
									ReversiRule.timeAva[1] + "\t" + ReversiRule.timeAva[2] + "\t" +
									ReversiRule.timeAva[3] + "\t" + ReversiRule.timeAva[4] + "\t" +
									ReversiRule.timeAva[5]);
		recordTime = 0;
		hashTime = 0;
		calculateTime = 0;
		testTime = 0;

		return currentNum;
	}
	
	public void setMapInitial() {
		substitutionDepth = new HashMap<>();
		substitutionLocation = new HashMap<>();
		substitutionMap = new HashMap<>();
		historyMap = new HashMap<>();
	}
}
