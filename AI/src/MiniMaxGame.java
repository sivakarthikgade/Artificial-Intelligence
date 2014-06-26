import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sivakarthik
 * This class contains logic to play MiniMax mid,end game.
 */
public class MiniMaxGame {

	public static final int posCnt = 23;
	char[] inBoard = new char[23];
	int inWCnt, inBCnt;
	char[] outBoard = new char[23];
	int outWCnt, outBCnt;
	int treeDepth;
	int posEvalCnt = 0;
	Node root;
	
	public MiniMaxGame() {
		
	}

	/**
	 * This method contains the initialization logic. It reads input board, validates and captures it. It saves depth passed in the input arguments.
	 * @param args
	 * @throws IOException
	 */
	public void initialize(String[] args) throws IOException {
		if(args.length < 3) {
			System.out.println("Invalid Number of Arguments. Need inputboard path, outputboard path, depth");
			System.exit(1);
		}

		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		String inBoard = br.readLine();
		inBoard = inBoard.trim();
		inBoard = inBoard.toLowerCase();
		if(!isValid(inBoard)) {
			System.out.println("Invalid input board. The board can contain only 23 characters belonging to set {w/W,x/X,b/B}. w/W or b/B can't occur more than 9 times each.");
			System.exit(1);
		}
		populateBoardFromString(this.inBoard, inBoard);
		br.close();

		this.treeDepth = Integer.parseInt(args[2]);
		if(this.treeDepth < 1) {
			System.out.println("Depth has to be positive");
			System.exit(1);
		}
	}
	
	private boolean isValid(String boardStr) {
		if(boardStr.length() != 23) {
			return false;
		}
		
		char[] board = new char[23];
		populateBoardFromString(board, boardStr);

		int wCnt = 0, bCnt = 0;
		for(int i = 0; i < posCnt; i++) {
			if(board[i] == 'w') {
				wCnt++;
				if(wCnt > 9)
					return false;
			} else if(board[i] == 'b') {
				bCnt++;
				if(bCnt > 9)
					return false;
			} else if(board[i] != 'x') {
				return false;
			}
		}

		return true;
	}
	
	private void populateBoardFromString(char[] board, String boardStr) {
		for(int i = 0; i < posCnt; i++) {
			board[i] = boardStr.charAt(i);
		}
	}

	private String getBoardString(char[] board) {
		StringBuilder boardStr = new StringBuilder();
		for(int i = 0; i < posCnt; i++) {
			boardStr.append(board[i]);
		}
		return boardStr.toString();
	}

	/**
	 * This method contains the starting calls to the whole algorithm. Creates the root node and invokes the recursive methods with it as input.
	 */
	public void run() {
		this.root = new Node();
		copyBoard(this.inBoard, this.root.board);
		this.root.depth = 0;
		this.root.type = 'w';
		
		Node n = new Node();
		copyBoard(this.root.board, n.board);
		n.type = 'b';

		if(getOccCnt(this.root.board, 'b') < 3) {
			System.out.println("You Won!");
			System.exit(0);
		} else if(getOccCnt(this.root.board, 'w') < 3) {
			System.out.println("You Lost!");
			System.exit(0);
		} else if(generatePossibleBoards(n).size() == 0) {
			System.out.println("You Won!");
			System.exit(0);
		} else if(generatePossibleBoards(this.root).size() == 0) {
			System.out.println("You Lost!");
			System.exit(0);
		}
		
		findBestOutputBoard(this.root);
	}
	
	private int getOccCnt(char[] board, char c) {
		int cnt = 0;
		for(char a: board) {
			if(a == c)
				cnt++;
		}
		return cnt;
	}

	/**
	 * This method takes a node as input, and calculates all the possible moves under it till the input tree depth.
	 * This method is initially invoked by the root node of the tree, and it gets recursively invoked for every other node of the tree as well.
	 * @param node
	 */
	public void findBestOutputBoard(Node node) {
		char[] selBoard = new char[23];
		if((node.depth == this.treeDepth) || (getOccCnt(node.board, 'w') <= 2) || (getOccCnt(node.board, 'b') <= 2)) {
			node.staticEst = getStaticEstimate(node.board);
			copyBoard(node.board, selBoard);
			this.posEvalCnt++;
		} else {
			if(node.type == 'w')
				node.staticEst = Integer.MIN_VALUE;
			else if(node.type == 'b')
				node.staticEst = Integer.MAX_VALUE;
			
			List<char[]> L = generatePossibleBoards(node);
			for(char[] l: L) {
				Node child = new Node();
				child.depth = node.depth + 1;
				child.type = (node.type == 'w' ? 'b' : 'w');
				copyBoard(l, child.board);
				
				findBestOutputBoard(child);
				
				if(node.type == 'w') {
					if(child.staticEst > node.staticEst) {
						node.staticEst = child.staticEst;
						selBoard = child.board;
					}
				} else {
					if(child.staticEst < node.staticEst) {
						node.staticEst = child.staticEst;
						selBoard = child.board;
					}
				}
			}
		}
		
		if(node.depth == 0)
			copyBoard(selBoard, node.board);
	}
	
	private List<Integer> getPosOfChar(char[] board, char c) {
		List<Integer> posList = new ArrayList<Integer>();
		for(int i = 0; i < posCnt; i++) {
			if(board[i] == c) {
				posList.add(i);
			}
		}
		return posList;
	}

	/**
	 * This method takes a node as input and generates and returns all boards possible from the given input node's board.
	 * @param node
	 * @return
	 */
	private List<char[]> generatePossibleBoards(Node node) {
		List<char[]> L = new ArrayList<char[]>();

		for(int i = 0; i < posCnt; i++) {
			if(node.board[i] == node.type) {
				if(getOccCnt(node.board, 'w') < 3)
					return L;
				
				List<Integer> nbrs = getNeighbour(i);
				if(getOccCnt(node.board, node.type) == 3) {
					nbrs = getPosOfChar(node.board, 'x');
				}
				
				for(int n: nbrs) {
					if(node.board[n] == 'x') {
						char[] newBoard = new char[23];
						copyBoard(node.board, newBoard);
						newBoard[i] = 'x';
						newBoard[n] = node.type;
						if(isCloseMill(newBoard, n)) {
							for(int j = 0; j < 23; j++) {
								if(newBoard[j]!=node.type && newBoard[j]!='x') {
									char[] tempBoard = new char[23];
									copyBoard(newBoard, tempBoard);
									if(!isCloseMill(tempBoard, j)) {
										tempBoard[j] = 'x';
										L.add(tempBoard);
									} else {
										if(getOccCnt(tempBoard, (node.type=='w')?'b':'w') == 3) {
											tempBoard[j] = 'x';
											L.add(tempBoard);
										}
									}
								}
							}
						} else {
							L.add(newBoard);
						}
					}
				}
			}
		}
		
		return L;
	}
	
	/**
	 * This method returns the list of neighbor position for the given input position.
	 * @param pos
	 * @return
	 */
	private List<Integer> getNeighbour(int pos){
		List<Integer> nbrs = new ArrayList<Integer>();
		switch(pos) {
			case 0:
				nbrs.add(1);
				nbrs.add(3);
				nbrs.add(8);
				break;
							
			case 1:
				nbrs.add(0);
				nbrs.add(2);
				nbrs.add(4);
				break;
							
			case 2: 
				nbrs.add(1);
				nbrs.add(5);
				nbrs.add(13);
				break;
							
			case 3: 
				nbrs.add(0);
				nbrs.add(4);
				nbrs.add(6);
				nbrs.add(9);
				break;
					
			case 4: 
				nbrs.add(1);
				nbrs.add(3);
				nbrs.add(5);
				break;
					
			case 5: 
				nbrs.add(2);
				nbrs.add(4);
				nbrs.add(7);
				nbrs.add(12);
				break;
					
			case 6: 
				nbrs.add(3);
				nbrs.add(7);
				nbrs.add(10);
				break;	
					
			case 7: 
				nbrs.add(5);
				nbrs.add(6);
				nbrs.add(11);
				break;
		
			case 8: 
				nbrs.add(0);
				nbrs.add(9);
				nbrs.add(20);
				break;
					
			case 9: 
				nbrs.add(3);
				nbrs.add(8);
				nbrs.add(10);
				nbrs.add(17);
				break;
					
			case 10:
				nbrs.add(6);
				nbrs.add(9);
				nbrs.add(14);	
				break;
					
			case 11:
				nbrs.add(7);
				nbrs.add(12);
				nbrs.add(16);
				break;
					
			case 12:
				nbrs.add(5);
				nbrs.add(11);
				nbrs.add(13);
				nbrs.add(19);
				break;
					
			case 13:
				nbrs.add(2);
				nbrs.add(12);
				nbrs.add(22);
				break;
					
			case 14:
				nbrs.add(10);
				nbrs.add(15);
				nbrs.add(17);
				break;		
					
			case 15:
				nbrs.add(14);
				nbrs.add(16);
				nbrs.add(18);
				break;
					
			case 16:
				nbrs.add(11);
				nbrs.add(15);
				nbrs.add(19);
				break;
					
			case 17:
				nbrs.add(9);
				nbrs.add(14);
				nbrs.add(18);
				nbrs.add(20);
				break;
					
			case 18:
				nbrs.add(15);
				nbrs.add(17);
				nbrs.add(19);
				nbrs.add(21);
				break;	
					
			case 19:
				nbrs.add(12);
				nbrs.add(16);
				nbrs.add(18);
				nbrs.add(22);
				break;
					
					
			case 20:
				nbrs.add(8);
				nbrs.add(17);
				nbrs.add(21);
				break;
					
			case 21:
				nbrs.add(18);
				nbrs.add(20);
				nbrs.add(22);
				break;	
					
			case 22:
				nbrs.add(13);
				nbrs.add(19);
				nbrs.add(21);
				break;
			
			default:
				System.out.println("Should not end up here !!!");
				break;
		}
		return nbrs;
	}

	/**
	 * This method checks if a mill exists using the input position of the input board.
	 * @param board
	 * @param pos
	 * @return
	 */
	private boolean isCloseMill(char[] board, int pos) {
		char c = board[pos];
		boolean result = false;

		if(c == 'x')
			return result;

		switch(pos) {

		case 0:
			if((c == board[1] && c == board[2]) ||	(c == board[3] && c == board[6]) || (c == board[8] && c == board[20])) {
					result = true;
				}
			break;
		
		case 1: if(c == board[0] && c == board[2]) {
					result =true;
				}
				break;
				
		case 2: 
			if((c == board[0] && c == board[1]) || (c == board[5] && c == board[7]) || (c == board[13] && c == board[22])) {
					result = true;
				}
				break;
				
		case 3:
			if((c == board[0] && c == board[6]) || (c == board[4] && c == board[5]) || (c == board[9] && c == board[17])) {
					result = true;
				}
				break;
				
		case 4: if(c == board[3] && c == board[5]) {
					result =true;
				}
				break;
				
		case 5: 
			if((c == board[7] && c == board[2]) || (c == board[3] && c == board[4]) || (c == board[12] && c == board[19])) {
					result = true;
				}
				break;
				
		case 6: 
			if((c == board[10] && c == board[14]) || (c == board[0] && c == board[3])) {
					result =true;
				}
				break;	
				
		case 7:
			if((c == board[11] && c == board[16]) || (c == board[2] && c == board[5])) {
					result =true;
				}
				break;
		
		case 8:
			if((c == board[9] && c == board[10]) || (c == board[0] && c == board[20])) {
					result = true;
				}
				break;
				
		case 9: 
			if((c == board[3] && c == board[17]) || (c == board[8] && c == board[10])) {
					result = true;
				}
				break;
				
		case 10: 
			if((c == board[8] && c == board[9]) || (c == board[6] && c == board[14])) {
					result =true;
				}
				break;
				
		case 11: 
			if((c == board[12] && c == board[13]) || (c == board[7] && c == board[16])) {
					result =true;
				}
				break;
				
		case 12: 
			if((c == board[11] && c == board[13]) || (c == board[5] && c == board[19])) {
					result =true;
				}
				break;
				
		case 13: 
			if((c == board[11] && c == board[12]) || (c == board[2] && c == board[22])) {
					result =true;
				}
				break;
				
		case 14: 
			if((c == board[15] && c == board[16]) || (c == board[17] && c == board[20]) || (c == board[6] && c == board[10])) {
					result =true;
				}
				break;		
				
		case 15: 
			if((c == board[18] && c == board[21]) || (c == board[14] && c == board[16])) {
					result =true;
				}
				break;
				
		case 16: 
			if((c == board[7] && c == board[11]) || (c == board[19] && c == board[22]) || (c == board[14] && c == board[15])) {
					result =true;
				}
				break;
				
		case 17: 
			if((c == board[3] && c == board[9]) || (c == board[14] && c == board[20]) || (c == board[18] && c == board[19])) {
					result =true;
				}
				break;
				
		case 18: 
			if((c == board[15] && c == board[21]) || (c == board[17] && c == board[19])) {
					result =true;
				}
				break;	
				
		case 19: 
			if((c == board[5] && c == board[12]) || (c == board[16] && c == board[22]) || (c == board[17] && c == board[18])) {
					result =true;
				}
				break;
				
				
		case 20: 
			if((c == board[0] && c == board[8]) || (c == board[14] && c == board[17]) || (c == board[21] && c == board[22])) {
					result =true;
				}
				break;
				
		case 21:
			if((c == board[15] && c == board[18]) || (c == board[20] && c == board[22])) {
					result =true;
				}
				break;	
				
		case 22:
			if((c == board[2] && c == board[13]) || (c == board[16] && c == board[19]) || (c == board[20] && c == board[21])) {
					result =true;
				}
				break;

		default:
			System.out.println("Should Not Land Up Here!!!");
			break;
		}
		
		return result;
	}
	
	/**
	 * This method contains the logic to calculate the static estimate for the given input board.
	 * @param board
	 * @return
	 */
	public int getStaticEstimate(char[] board) {
		int wCnt = 0;
		int bCnt = 0;
		int statEst;
		int numBlkMoves = 0;

		for(int i = 0; i < posCnt; i++) {
			if(board[i] == 'w' )
				wCnt++;
			else if(board[i] == 'b')
				bCnt++;
		}
		
		Node n = new Node();
		copyBoard(board, n.board);
		n.type = 'b';
		List<char[]> L = generatePossibleBoards(n);
		
		numBlkMoves = L.size();
		
		if(bCnt <= 2) {
			statEst = 10000;
		} else if(wCnt <= 2) {
			statEst = -10000;
		} else if(numBlkMoves == 0) {
			statEst = 10000;
		} else {
			statEst = (1000*(wCnt - bCnt)) - numBlkMoves;
		}
		return statEst;
	}

	private void copyBoard(char[] in, char[] out) {
		for(int i = 0; i < in.length; i++) {
			out[i] = in[i];
		}
	}
	
	/**
	 * This method is responsible for printing the output in desired format. It also writes the output string into the passed in output file.
	 * @param outputFilePath
	 * @throws IOException
	 */
	public void printResult(String outputFilePath) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath));
		String inBoardStr = getBoardString(this.inBoard);
		String outBoardStr = getBoardString(this.root.board);
		inBoardStr = upperWB(inBoardStr);
		outBoardStr = upperWB(outBoardStr);
		bw.write(outBoardStr);
		bw.flush();
		bw.close();
		System.out.println("Input Board: "+inBoardStr);
		System.out.println("Board Position: "+outBoardStr);
		System.out.println("Positions evaluated by static estimation: "+this.posEvalCnt);
		System.out.println("MINIMAX Estimate: "+this.root.staticEst);
	}
	
	private String upperWB(String board) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < posCnt; i++) {
			char c = board.charAt(i);
			if(c == 'w')
				c = 'W';
			else if(c == 'b')
				c = 'B';
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * This is the main method. It contain calls to make initialization based on input arguments.
	 * Invokes the run method which takes care of the algorithm execution, and calls printResult which takes care of printing the required details to output.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException {
		MiniMaxGame m = new MiniMaxGame();
		m.initialize(args);
		m.run();
		m.printResult(args[1]);
	}

}
