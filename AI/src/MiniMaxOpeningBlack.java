import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sivakarthik
 * This class contains logic to play MiniMax black opening game.
 */
public class MiniMaxOpeningBlack {

	public static final int posCnt = 23;
	char[] inBoard = new char[23];
	int inWCnt, inBCnt;
	char[] outBoard = new char[23];
	int outWCnt, outBCnt;
	int treeDepth;
	int posEvalCnt = 0;
	Node root;
	
	public MiniMaxOpeningBlack() {
		
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
			System.out.println("Depth has to be positive.");
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

	private void flipWB(char[] board) {
		for(int i = 0; i < posCnt; i++) {
			if(board[i] == 'w')
				board[i] = 'b';
			else if(board[i] == 'b')
				board[i] = 'w';
		}
	}
	
	/**
	 * This method contains the starting calls to the whole algorithm. Creates the root node and invokes the recursive methods with it as input.
	 */
	public void run() {
		this.root = new Node();
		copyBoard(this.inBoard, this.root.board);
		flipWB(this.root.board);
		this.root.depth = 0;
		this.root.type = 'w';
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
		if(node.depth == this.treeDepth) {
			node.staticEst = getStaticEstimate(node.board);
			copyBoard(node.board, selBoard);
			this.posEvalCnt++;
		} else {
			if(node.type == 'w')
				node.staticEst = Integer.MIN_VALUE;
			else if(node.type == 'b')
				node.staticEst = Integer.MAX_VALUE;
			
			for(char[] l: generatePossibleBoards(node)) {
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
	
	/**
	 * This method takes a node as input and generates and returns all boards possible from the given input node's board.
	 * @param node
	 * @return
	 */
	private List<char[]> generatePossibleBoards(Node node) {
		List<char[]> L = new ArrayList<char[]>();

		for(int i = 0; i < posCnt; i++) {
			if(node.board[i] == 'x') {
				char[] newBoard = new char[23];
				copyBoard(node.board, newBoard);
				
				newBoard[i] = node.type;
				
				if(isCloseMill(newBoard, i)) {
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
		
		return L;
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
		int wCnt = 0, bCnt = 0;
		for(int i = 0; i < posCnt; i++) {
			if(board[i] == 'w')
				wCnt++;
			else if(board[i] == 'b')
				bCnt++;
		}
		return wCnt - bCnt;
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
		copyBoard(this.root.board, this.outBoard);
		flipWB(this.outBoard);
		String outBoardStr = getBoardString(this.outBoard);
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
		MiniMaxOpeningBlack m = new MiniMaxOpeningBlack();
		m.initialize(args);
		m.run();
		m.printResult(args[1]);
	}

}
