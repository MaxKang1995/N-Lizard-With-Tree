package the_fruit_rage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Project {
	
	private static final String I_NAME = "input.txt";
	private static final String O_NAME = "output.txt";

	private static int n = 0;	// width and height of board
	private static int p = 0;	// # of fruit types
	private static float t = 0;	// remaining time
	private static char[][] board = null;
	
	private static BufferedWriter bw = null;
	private static BufferedReader br = null;
	
	private static final int[] MAX_DEPTH = new int[] { 6, 6, 6, 5, 5, 5, 5, 5, 5, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3,
			3, 3, 3, 3 };
	private static final int[] MAX_DEPTH_ON_FRUIT_TYPE = new int[] { 6, 6, 5, 5, 4, 4, 4, 3, 3 };
	
	public static void main(String[] args) {
		try {
			Date time_s = new Date();
			br = new BufferedReader(new FileReader(I_NAME));
			bw = new BufferedWriter(new FileWriter(O_NAME));
			
			// Reading data
			n = Integer.parseInt(br.readLine());
			p = Integer.parseInt(br.readLine());
			t = Float.parseFloat(br.readLine());
			board = new char[n][n];
			int star_count = 0;
			for (int i = 0; i < n; i++) {
				char[] elec = br.readLine().toCharArray();
				board[i] = elec.clone();
				for (int j = 0; j < n; j++) {
					if (board[i][j] == '*') star_count++;
				}
			}
			
			br.close();

			int limited_depth = MAX_DEPTH[n - 1];
			limited_depth = (limited_depth == MAX_DEPTH_ON_FRUIT_TYPE[p - 1]) ? limited_depth - 1 : limited_depth;
			limited_depth = (t < 1) ? 1 : (t < 20) ? limited_depth - 1 : limited_depth;
			int[] action = maxAction(board, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, n * n - star_count, limited_depth);
			output(action[0], action[1]);
			Date time_e = new Date();
			System.out.println(time_e.getTime() - time_s.getTime());
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
		
	}
	
	private static int[] maxAction(char[][] board, int alpha, int beta, int score_diff, int fruit_left, int iter) {
		if (fruit_left == 0 || iter == 0) return new int[] { -1, -1, score_diff };
		int max = Integer.MIN_VALUE;
		int[] move = new int[3];
		boolean[][] record = new boolean[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (board[i][j] == '*' || record[i][j]) continue;
				char[][] tmp = new char[n][];
				for (int k = 0; k < n; k++) tmp[k] = board[k].clone();
				int count = replace(tmp, record, i, j, board[i][j]);
				gravityFall(tmp);
				int[] res = minAction(tmp, alpha, beta, score_diff + count * count, fruit_left - count, iter - 1);
				if (max < res[2]) {
					max = res[2];
					move[0] = i;
					move[1] = j;
					move[2] = max;
				}
				if (max > beta) return move;
				alpha = Math.max(alpha, max);
			}
		}
		return move;
	}
	
	private static int[] minAction(char[][] board, int alpha, int beta, int score_diff, int fruit_left, int iter) {
		if (fruit_left == 0 || iter == 0) return new int[] { -1, -1, score_diff };
		int min = Integer.MAX_VALUE;
		int[] move = new int[3];
		boolean[][] record = new boolean[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (board[i][j] == '*' || record[i][j]) continue;
				char[][] tmp = new char[n][];
				for (int k = 0; k < n; k++) tmp[k] = board[k].clone();
				int count = replace(tmp, record, i, j, board[i][j]);
				gravityFall(tmp);
				int[] res = maxAction(tmp, alpha, beta, score_diff - count * count, fruit_left - count, iter - 1);
				if (min > res[2]) {
					min = res[2];
					move[0] = i;
					move[1] = j;
					move[2] = min;
				}
				if (min < alpha) return move;
				beta = Math.min(beta, min);
			}
		}
		return move;
	}
	
	private static int replace(char[][] board, boolean[][] record, int i, int j, char c) {
		int value = 1;
		board[i][j] = '*';
		record[i][j] = true;
		if (i > 0 && board[i - 1][j] == c) value += replace(board, record, i - 1, j, c);
		if (j > 0 && board[i][j - 1] == c) value += replace(board, record, i, j - 1, c);
		if (i < n - 1 && board[i + 1][j] == c) value += replace(board, record, i + 1, j, c);
		if (j < n - 1 && board[i][j + 1] == c) value += replace(board, record, i, j + 1, c);
		return value;
	}
	
	private static void gravityFall(char[][] board) {
		for (int j = 0; j < n; j++) {
			for (int i = n - 1, k = n - 2; k >= 0; i--, k--) {
				if (board[i][j] == '*') {
					while (k >= 0 && board[k][j] == '*') k--;
					if (k >= 0) {
						board[i][j] = board[k][j];
						board[k][j] = '*';
					}
				}
			}
		}
	}
	
	private static void output(int i, int j) throws IOException {
		System.out.println("Finished");
		bw.write(String.valueOf((char) (j + 'A')) + String.valueOf(i + 1));
		bw.newLine();
		replace(board, new boolean[n][n], i, j, board[i][j]);
		gravityFall(board);
		for (char[] line : board) bw.write(String.valueOf(line) + "\n");
		bw.close();
	}
}
