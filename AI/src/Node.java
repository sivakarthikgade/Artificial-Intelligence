import java.util.ArrayList;
import java.util.List;

public class Node {

	int depth;
	char[] board;
	int staticEst;
	int minStaticEst = 10000000;
	int maxStaticEst = -10000000;
	boolean staticEval = false;
	char type;
	Node parent;
	List<Node> children;
	
	public Node() {
		board = new char[23];
		children = new ArrayList<Node>();
	}
}
