import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;


public class C {
	
	public static void print(Object o) {
		//System.err.println(o.toString());
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out))); 
        
        int numTests = sc.nextInt();
        
        print(numTests + " tests");
		
        for (int i = 0; i < numTests; i++) {
            int numVariables = sc.nextInt();
            int numClauses = sc.nextInt();
			sc.nextLine();
			
			print(numVariables + " vars");
			print(numClauses + " clauses");
			
			ArrayList<String> clauses = new ArrayList<String>();	 
			
			for (int j = 0; j < numClauses; j++) { 
				String clause = sc.nextLine();
				clauses.add(clause);
			}
			
			print(clauses);
			
			DirHamCycle graph = new DirHamCycle(numVariables, numClauses, clauses);
			graph.init();
			
			pw.write(graph.toString());
			
			//pw.write("\n");
        }
        pw.close(); // do not forget to use this
        sc.close();
	}
}

class DirHamCycle {
	
	private int _numLiterals;
	private int _numClauses;
	private ArrayList<String> _clauses;
	
	private HashMap<String, HashSet<String>> _adjacencyList;
	private ArrayList<ArrayList<String>> _edges;
	
	private boolean _fail;
	
	public void print(Object o) {
		C.print(o);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		// make the first line
		sb.append(_adjacencyList.size());
		sb.append(" ");
		sb.append(_edges.size());
		sb.append("\n");
		
		// make the edges
		for (int i = 0; i < _edges.size(); i++) {
			ArrayList<String> edge = _edges.get(i);
			sb.append(edge.get(0));
			sb.append(" ");
			sb.append(edge.get(1));
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	public DirHamCycle(int numLiterals, int numClauses, ArrayList<String> clauses) {
		this._fail = !(numClauses == clauses.size());
		this._adjacencyList = new HashMap<String, HashSet<String>>();
		this._edges = new ArrayList<ArrayList<String>>();
		
		this._numLiterals = numLiterals;
		this._numClauses = numClauses;
		this._clauses = clauses;
	}

	public void init() {
		// do nothing if failed construction
		if (_fail) {
			return;
		}
		
		createLiteralRows();
		makeVerticalEdges();
		makeStartEndEdges();
		makeClauseEdges();
		
		Collections.sort(_edges, new EdgeComparator());
		//print(_edges);
	}

	private void makeClauseEdges() {
		// link edges for each clause
		for (int j = 0, k = 1; j < _clauses.size(); j++, k++) {
			String clause = "C" + k;
			
			String[] literals = _clauses.get(j).split("\\s+");
			
			for (String literal : literals) {
				int value = Integer.parseInt(literal);
				int index = Math.abs(value);
				boolean positive = value > 0;
				
				String leftk = "X" + index + "." + k + "." + "L";
				String rightk = "X" + index + "." + k + "." + "R";
				
				if (positive) {
					makeEdge(leftk, clause);
					makeEdge(clause, rightk);
				} else {
					makeEdge(rightk, clause);
					makeEdge(clause, leftk);
				}
			}
		}
	}

	private void makeStartEndEdges() {
		String start = "S";
		String end = "T";
		String Lfirst = "L" + 1;
		String Rfirst = "R" + 1;
		String Llast = "L" + _numLiterals;
		String Rlast = "R" + _numLiterals;
		
		makeEdge(start, Lfirst);
		makeEdge(start, Rfirst);
		
		makeEdge(Llast, end);
		makeEdge(Rlast, end);
		
		makeEdge(end, start);
	}

	private void makeVerticalEdges() {
		// create edge from Ln -> L(n+1)
		for (int n = 1; n < _numLiterals; n++) {
			String leftmost = "L" + n;
			String rightmost = "R" + n;
			String leftmostNext = "L" + (n + 1);
			String rightmostNext = "R" + (n + 1);
			
			makeEdge(leftmost, leftmostNext);
			makeEdge(leftmost, rightmostNext);
			makeEdge(rightmost, rightmostNext);
			makeEdge(rightmost, leftmostNext);
		}
	}

	private void createLiteralRows() {
		// create row for each literal
		for (int n = 1; n <= _numLiterals; n++) {
			// make edges for each clause Xn.k.L <-> Xn.k.R <-> Bn.k
			for (int k = 1; k <= _numClauses; k++) {
				String leftk = "X" + n + "." + k + "." + "L";
				String rightk = "X" + n + "." + k + "." + "R";
				String bufferk = "B" + n + "." + k;
				String bufferPrev = "B" + n + "." + (k - 1);

				doubleLink(bufferPrev, leftk);
				doubleLink(leftk, rightk);
				doubleLink(rightk, bufferk);
			}

			// make edge Ln <-> Bn.first
			String leftmost = "L" + n;
			String bufferFirst = "B" + n + "." + 0;
			doubleLink(leftmost, bufferFirst);
			
			// make edge Bn.last <-> Rn
			String bufferLast = "B" + n + "." + _numClauses;
			String rightmost = "R" + n;
			doubleLink(bufferLast, rightmost);
		}
	}
	
	private void doubleLink(String from, String to) {
		makeEdge(from, to);
		makeEdge(to, from);
	}
	
	private void makeEdge(String from, String to) {
		// store in the edge list
		if (_adjacencyList.containsKey(from) 
				&& _adjacencyList.get(from).contains(to)) {
			// from -> to exists, dont store
		} else {
			ArrayList<String> edge = new ArrayList<String>();
			edge.add(from);
			edge.add(to);
			_edges.add(edge);
		}

		// put from->to
		if (_adjacencyList.containsKey(from)) {
			HashSet<String> incidentEdges = _adjacencyList.get(from);
			incidentEdges.add(to);
		} else {
			HashSet<String> incidentEdges = new HashSet<String>();
			incidentEdges.add(to);
			_adjacencyList.put(from, incidentEdges);
		}
		
	}
	
}

class EdgeComparator implements Comparator<ArrayList<String>> {

	@Override
	public int compare(ArrayList<String> a, ArrayList<String> b) {
		if (a.get(0).compareTo(b.get(0)) < 0) {
			// a1 < b1
			// return a < b
			return -1;
		} else if (a.get(0).compareTo(b.get(0)) > 0) {
			// a1 > b1
			// return a > b
			return 1;
		} else {
			// a1 == b1
			if (a.get(1).compareTo(b.get(1)) < 0) {
				// a2 < b2
				// return a < b
				return -1;
			} else if (a.get(1).compareTo(b.get(1)) > 0) {
				// a2 > b2
				// return a > b
				return 1;
			} else {
				// a2 == b2
				// return a == b
				return 0;
			}
		}
	}	
}







