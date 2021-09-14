import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Function;

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
	private LinkedList<Position> path;
	private boolean pathFound;
	private long openCount;
	private int pathLength;
	private Iterator<Position> pathWalk;
	
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
		if(!pathFound) return Action.DO_NOTHING;
		if(pathWalk == null) {
			pathWalk = path.iterator();
			//remove the start point
			pathWalk.next();
		}
		if(!pathWalk.hasNext()) return Action.DO_NOTHING;
		Position pos = pathWalk.next();
		if(pos.getRow() < this.posRow) return Action.MOVE_UP;
		if(pos.getRow() > this.posRow) return Action.MOVE_DOWN;
		if(pos.getCol() < this.posCol) return Action.MOVE_LEFT;
		if(pos.getCol() > this.posCol) return Action.MOVE_RIGHT;
		return Action.DO_NOTHING;
	}
	
	/** 
	 * This method implements breadth-first search. It populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method 
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 *
	 * @return
	 */
	public LinkedList<Position> bfs() {
		//create the queue of paths
		LinkedList<LinkedList<Position>> queue = new LinkedList<>();
		//and add the first path which is the start state of the robot
		LinkedList<Position> first = new LinkedList<>();
		first.push(new Position(posRow,posCol));
		queue.push(first);
		env.setTileStatus(new Position(posRow,posCol), TileStatus.DIRTY);
		openCount++;
		while(!queue.isEmpty()){
			//get the next path so far
			LinkedList<Position> current = queue.poll();
			//get the current position of that path
			Position spot = current.getLast();
			//System.out.println("Checking position: " + spot.getRow() + ", " + spot.getCol());
			//check the status of all surrounding positions
			TileStatus up = env.getTileStatus(spot.getRow() - 1, spot.getCol());
			TileStatus down = env.getTileStatus(spot.getRow() + 1, spot.getCol());
			TileStatus left = env.getTileStatus(spot.getRow(), spot.getCol() - 1);
			TileStatus right = env.getTileStatus(spot.getRow(), spot.getCol() + 1);
			//check for target hits
			if(up == TileStatus.TARGET){
				//System.out.println("Up target");
				current.add(new Position(spot.getRow() - 1, spot.getCol()));
				path = current;
				pathFound = true;
				pathLength = current.size() - 1;
				return current;
			}
			if(down == TileStatus.TARGET){
				//System.out.println("Down target");
				current.add(new Position(spot.getRow() + 1, spot.getCol()));
				path = current;
				pathFound = true;
				pathLength = current.size() - 1;
				return current;
			}
			if(left == TileStatus.TARGET){
				//System.out.println("Left target");
				current.add(new Position(spot.getRow(), spot.getCol() - 1));
				path = current;
				pathFound = true;
				pathLength = current.size() - 1;
				return current;
			}
			if(right == TileStatus.TARGET){
				//System.out.println("Right target");
				current.add(new Position(spot.getRow(), spot.getCol() + 1));
				path = current;
				pathFound = true;
				pathLength = current.size() - 1;
				return current;
			}
			//check for passable adjacent spots
			Position newPos;
			if(up == TileStatus.CLEAN){
//				System.out.println("Up clean");
				LinkedList<Position> newPath = (LinkedList<Position>) current.clone();
				newPos = new Position(spot.getRow() - 1, spot.getCol());
				newPath.add(newPos);
				openCount++;
				queue.add(newPath);
				env.setTileStatus(newPos, TileStatus.DIRTY);
			}
			if(down == TileStatus.CLEAN){
//				System.out.println("Down clean");
				LinkedList<Position> newPath = (LinkedList<Position>) current.clone();
				newPos = new Position(spot.getRow() + 1, spot.getCol());
				newPath.add(newPos);
				openCount++;
				queue.add(newPath);
				env.setTileStatus(newPos, TileStatus.DIRTY);
			}
			if(left == TileStatus.CLEAN){
//				System.out.println("Left clean");
				LinkedList<Position> newPath = (LinkedList<Position>) current.clone();
				newPos = new Position(spot.getRow(), spot.getCol() - 1);
				newPath.add(newPos);
				openCount++;
				queue.add(newPath);
				env.setTileStatus(newPos, TileStatus.DIRTY);
			}
			if(right == TileStatus.CLEAN){
//				System.out.println("Right clean");
				LinkedList<Position> newPath = (LinkedList<Position>) current.clone();
				newPos = new Position(spot.getRow(), spot.getCol() + 1);
				newPath.add(newPos);
				openCount++;
				queue.add(newPath);
				this.env.setTileStatus(newPos, TileStatus.DIRTY);
			}
		}
		pathFound = false;
		return null;
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