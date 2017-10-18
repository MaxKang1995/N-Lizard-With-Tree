package n_lizard_with_tree;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Project {
	
	private static final String I_NAME = "input.txt";
	private static final String O_NAME = "output.txt";
	private static final int MAX_ITERATION = 10000;
	private static final double T_MAX = 10;
	private static final double T_MIN = 0.000001;
	private static final double RATE = 0.99;
	
	private static String method = null;
	private static int n = 0;
	private static int p = 0;
	private static int[][] nursery = null;	// Don't change value
	private static int[][] tmp = null;		// For final use
	
	private static BufferedWriter bw = null;
	private static BufferedReader br = null;
	
	private static Queue<State> queue;		// For BFS use
	
	public static void main(String[] args) {
		try {
			br = new BufferedReader(new FileReader(I_NAME));
			bw = new BufferedWriter(new FileWriter(O_NAME));
			
			// Reading data
			method = br.readLine();
			n = Integer.parseInt(br.readLine());
			p = Integer.parseInt(br.readLine());
			nursery = new int[n][n];
			tmp = new int[n][n];
			int tree_num = 0;
			for (int i = 0; i < n; i++) {
				String eles = br.readLine();
				if (eles.length() != n) throw new Exception("Wrong input data");
				for (int j = 0; j < n; j++) {
					nursery[i][j] = eles.charAt(j) - '0';
					tmp[i][j] = nursery[i][j];
					if (nursery[i][j] == 2) tree_num++;
				}
			}
			
			br.close();
			
			// Fail situations
			if ((tree_num == 0 && p > n) || n * n - tree_num < p || (n < 3 && p > 1)) {
				fail();
				return;
			}

			if (method.equals("BFS"))
				bfs();
			else if (method.equals("DFS"))
				dfs(0, 0, 0);
			else if (method.equals("SA"))
				sa();
			else
				fail();
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
		
	}
	
	/*
	 * BFS
	 */
	private static void bfs() throws IOException {
		queue = new LinkedList<>();
		queue.add(new State());
		
		while (!queue.isEmpty()) {
			State s = queue.poll();
			List<Integer[]> positions = s.positions;
			int count = s.count;
			int last_i = s.last_i;
			int last_j = s.last_j;

			// Build a temporary nursery
			tmp = new int[n][];
			for (int i = 0; i < n; i++) tmp[i] = nursery[i].clone();
			for (Integer[] pair : positions) tmp[pair[0]][pair[1]] = 1;
			
			for (int i = last_i; i < n; i++) {
				int j = 0;
				if (i == last_i) j = last_j;
				for (; j < n; j++) {
					if (tmp[i][j] == 0 && isValid(i, j)) {
						if (count + 1 == p) {
							tmp[i][j] = 1;
							succeed();
							queue.clear();
							return;
						}
						queue.add(new State(positions, i, j, count + 1));	
					}
				}
			}
		}
		
		fail();
	}
	
	/*
	 * DFS
	 */
	private static boolean dfs(int last_i, int last_j, int count) throws IOException {
		if (count == p) {
			succeed();
			return true;
		}
		
		for (int i = last_i; i < n; i++) {
			int j = 0;
			if (i == last_i) j = last_j;
			for (; j < n; j++) {
				if (tmp[i][j] == 0 && isValid(i, j)) {
					tmp[i][j] = 1;
					if (dfs(i, j, count + 1)) return true;
					tmp[i][j] = 0;
				}
			}
		}
		
		if (count == 0) fail();
		return false;
	}
	
	/*
	 * SA
	 */
	private static void sa() throws IOException {
		for (int iter = 0; iter < MAX_ITERATION; iter++) {
	        System.out.println("Iteration " + iter);
			if (!saInitial()) { fail(); return; }
	        if (check()) { succeed(); return; }
			double t = T_MAX;
			
		    while (t > T_MIN) {
		        int[][] h = new int[n][n];
		        int cur_state = -1;
		        int cur_i = 0;
		        int cur_j = 0;
		        
		        for (int i = 0; i < n; i++) {
		            for (int j = 0; j < n; j++) {
		                h[i][j] = findCollision(i, j);
		                
		                if (tmp[i][j] == 1 && cur_state < h[i][j]) {
		                		cur_state = h[i][j];
		                		cur_i = i;
		                		cur_j = j;
		                }
		            }
		        }

		        // Choose next state randomly
		        boolean isBetter = false;
		        int next_i = new Random().nextInt(n);
		        int next_j = new Random().nextInt(n);
		        while (tmp[next_i][next_j] != 0) {
		            next_i = new Random().nextInt(n);
			        next_j = new Random().nextInt(n);
		        }
		        int next_state = h[next_i][next_j];
		        
		        int E = next_state - cur_state;
		        if (E < 0) isBetter = true;
		        else if (Math.exp(-1 * E / t) > (new Random().nextInt(MAX_ITERATION) * 1.0 / MAX_ITERATION)) 
		        		isBetter = true;

		        if (isBetter) {
		        		tmp[cur_i][cur_j] = 0;
		        		tmp[next_i][next_j] = 1;
		        }
		        
		        if (check()) { succeed(); return; }

		        t *= RATE;
		    }
		}
		
		fail();
	}
	
	/*
	 * Helpers
	 */
	private static boolean saInitial() {
		int count = 0;
		tmp = new int[n][];
		for (int i = 0; i < n; i++) tmp[i] = nursery[i].clone();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (tmp[i][j] == 0) {
					tmp[i][j] = 1;
					count++;
					if (count == p) return true;
				}
			}
		}
		return false;
	}
	
	private static int findCollision(int i, int j) {
		int count = 0;
		for (int k = i - 1; k >= 0; k--) { if (tmp[k][j] == 1) count++; if (tmp[k][j] == 2) break; }
		for (int k = i + 1; k < n; k++)  { if (tmp[k][j] == 1) count++; if (tmp[k][j] == 2) break; }
		for (int k = j - 1; k >= 0; k--) { if (tmp[i][k] == 1) count++; if (tmp[i][k] == 2) break; }
		for (int k = j + 1; k < n; k++)  { if (tmp[i][k] == 1) count++; if (tmp[i][k] == 2) break; }
		
		for (int p = i - 1, q = j - 1; p >= 0 && q >= 0; p--, q--) {
			if (tmp[p][q] == 1) count++;
			if (tmp[p][q] == 2) break;
		}
		for (int p = i - 1, q = j + 1; p >= 0 && q < n; p--, q++) {
			if (tmp[p][q] == 1) count++;
			if (tmp[p][q] == 2) break;
		}
		for (int p = i + 1, q = j - 1; p < n && q >= 0; p++, q--) {
			if (tmp[p][q] == 1) count++;
			if (tmp[p][q] == 2) break;
		}
		for (int p = i + 1, q = j + 1; p < n && q < n; p++, q++) {
			if (tmp[p][q] == 1) count++;
			if (tmp[p][q] == 2) break;
		}
		return count;
	}
	
	private static boolean check() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (tmp[i][j] == 1 && !isValid(i, j)) return false;
			}
		}
		return true;
	}
	
	private static void fail() throws IOException {
		System.out.println("Fail");
		bw.write("FAIL");
		bw.close();
	}
	
	private static void succeed() throws IOException {
		System.out.println("Succeed");
		bw.write("OK");
		bw.newLine();
		for (int[] line : tmp) {
			StringBuilder sb = new StringBuilder();
			for (int num : line) sb.append(num);
			bw.write(sb.append("\n").toString());
		}
		bw.close();
	}
	
	private static boolean isValid(int i, int j) {
		for (int k = i - 1; k >= 0; k--) { if (tmp[k][j] == 1) return false; if (tmp[k][j] == 2) break; }
		for (int k = i + 1; k < n; k++)  { if (tmp[k][j] == 1) return false; if (tmp[k][j] == 2) break; }
		for (int k = j - 1; k >= 0; k--) { if (tmp[i][k] == 1) return false; if (tmp[i][k] == 2) break; }
		for (int k = j + 1; k < n; k++)  { if (tmp[i][k] == 1) return false; if (tmp[i][k] == 2) break; }
		
		for (int p = i - 1, q = j - 1; p >= 0 && q >= 0; p--, q--) {
			if (tmp[p][q] == 1) return false;
			if (tmp[p][q] == 2) break;
		}
		for (int p = i - 1, q = j + 1; p >= 0 && q < n; p--, q++) {
			if (tmp[p][q] == 1) return false;
			if (tmp[p][q] == 2) break;
		}
		for (int p = i + 1, q = j - 1; p < n && q >= 0; p++, q--) {
			if (tmp[p][q] == 1) return false;
			if (tmp[p][q] == 2) break;
		}
		for (int p = i + 1, q = j + 1; p < n && q < n; p++, q++) {
			if (tmp[p][q] == 1) return false;
			if (tmp[p][q] == 2) break;
		}
		return true;
	}
	
	public static class State {
		public List<Integer[]> positions;
		public int count;
		public int last_i;
		public int last_j;
		
		public State() {
			this.positions = new ArrayList<>();
			this.count = 0;
		}
		
		public State(List<Integer[]> positions, int i, int j, int count) {
			this.positions = new ArrayList<>();
			this.positions.addAll(positions);
			this.positions.add(new Integer[] { i, j });
			this.last_i = i;
			this.last_j = j;
			this.count = count;
		}
	}
}
