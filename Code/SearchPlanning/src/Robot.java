import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
	Represents an intelligent agent moving through a particular room.	
	The robot only has one sensor - the ability to get the status of any  
	tile in the environment through the command env.getTileStatus(row, col).
	@author Adam Gaweda, Michael Wollowski
*/

public class Robot {
	private Environment env;
	private int posRow;
	private int posCol;
	private LinkedList<Action> path;
	private boolean pathFound;
	private long openCount;
	private int pathLength;
	
	/**
	    Initializes a Robot on a specific tile in the environment. 
	*/
	
	public Robot (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.path = new LinkedList<>();
		this.pathFound = false;
		this.openCount = 0;
		this.pathLength = 0;
	}
	
	public boolean getPathFound(){
		return this.pathFound;
	}
	
	public long getOpenCount(){
		return this.openCount;
	}
	
	public int getPathLength(){
		return this.pathLength;
	}
	
	public void resetOpenCount() {
		this.openCount = 0;
	}
	
	public int getPosRow() { return posRow; }
	public int getPosCol() { return posCol; }
	public void incPosRow() { posRow++; }
	public void decPosRow() { posRow--; }
	public void incPosCol() { posCol++; }
    public void decPosCol() { posCol--; }
	
	/**
	   Returns the next action to be taken by the robot. A support function 
	   that processes the path LinkedList that has been populates by the
	   search functions.
	*/
	public Action getAction () {
		//TODO: Implement this method
	    return Action.DO_NOTHING;
	}
	
	/** 
	 * This method implements breadth-first search. It populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method 
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 * 
	 */
	public void bfs() {
		//TODO: Implement this method
	}
	
	/** 
	 * This method implements breadth-first search for maps with multiple targets.
	 * It populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method 
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 * 
	 */
	public void bfsM() {
		//TODO: Implement this method
	}


	/** 
	 * This method implements A* search. It populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method 
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 * 
	 */
	public void astar() {

	}
	
	/** 
	 * This method implements A* search for maps with multiple targets. It 
	 * populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method 
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 * 
	 */
	public void astarM() {

	}
	
	


}