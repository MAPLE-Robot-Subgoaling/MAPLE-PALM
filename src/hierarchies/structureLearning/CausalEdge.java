package hierarchies.structureLearning;

public class CausalEdge {

	/**
	 * start and end index of the edge
	 */
	private int start, end;
	private String edgeVariable;
	
	public CausalEdge(int start, int end, String var) {
		this.start = start;
		this.end = end;
		this.edgeVariable = var;
	}
	
	public int getStart() {
		return start;
	}
	
	public int getEnd() {
		return end;
	}
	
	public String getRelavantVariable() {
		return edgeVariable;
	}
}

