
public class MainProgram {

	public static void main(String args[]) throws Exception {
		String[][] input = new String[4][3];
		input[0][0] = "inputBoard1.txt";
		input[0][1] = "outputBoard1.txt";
		input[0][2] = "3";
		input[1][0] = "inputBoard2.txt";
		input[1][1] = "outputBoard2.txt";
		input[1][2] = "3";
		input[2][0] = "inputBoard3.txt";
		input[2][1] = "outputBoard3.txt";
		input[2][2] = "3";
		input[3][0] = "inputBoard4.txt";
		input[3][1] = "outputBoard4.txt";
		input[3][2] = "3";

		for(int i = 0; i < 4; i++) {
			args = input[i];
			MiniMaxOpening m = new MiniMaxOpening();
			m.initialize(args);
			m.run();
			m.printResult(args[1]);
		}
		for(int i = 0; i < 4; i++) {
			args = input[i];
			MiniMaxGame m = new MiniMaxGame();
			m.initialize(args);
			m.run();
			m.printResult(args[1]);
		}
		for(int i = 0; i < 4; i++) {
			args = input[i];
			ABOpening m = new ABOpening();
			m.initialize(args);
			m.run();
			m.printResult(args[1]);
		}
		for(int i = 0; i < 4; i++) {
			args = input[i];
			ABGame m = new ABGame();
			m.initialize(args);
			m.run();
			m.printResult(args[1]);
		}
		for(int i = 0; i < 4; i++) {
			args = input[i];
			MiniMaxOpeningBlack m = new MiniMaxOpeningBlack();
			m.initialize(args);
			m.run();
			m.printResult(args[1]);
		}
		for(int i = 0; i < 4; i++) {
			args = input[i];
			MiniMaxGameBlack m = new MiniMaxGameBlack();
			m.initialize(args);
			m.run();
			m.printResult(args[1]);
		}
	}
}
