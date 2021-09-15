import java.util.*;

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
	public List<Position> bfsM() {
		//create list of targets from env
		LinkedList<Position> targets = env.getTargets();
		//create holder for full path
		LinkedList<Position> fullPath = new LinkedList<>();
		//set the current position
		Position currentPos = new Position(getPosRow(), getPosCol());
		//add current position to full path
		fullPath.add(currentPos);
		//while some targets are left to find.
		while(!targets.isEmpty()){
			//find the path to a target
			LinkedList<Position> path = bfsHelper(currentPos);
			//if no path was found, no path exists
			if(path == null){
				pathFound = false;
				return null;
			}
			//remove the start position
			path.poll();
			//set the current position to where the robot ended
			currentPos = path.peekLast();
			//add the found path to the full path
			fullPath.addAll(path);
			//remove the target
			targets.poll();
			env.setTileStatus(currentPos, TileStatus.CLEAN);
			env.cleanEnvironment();
		}
		pathFound = true;
		pathLength = fullPath.size() - 1;
		this.path = fullPath;
		return fullPath;
	}

	private LinkedList<Position> bfsHelper(Position currentPos){
		//create the queue of paths
		LinkedList<LinkedList<Position>> queue = new LinkedList<>();
		//and add the first path which is the start state of the robot
		LinkedList<Position> first = new LinkedList<>();
		first.push(currentPos);
		queue.push(first);
		env.setTileStatus(currentPos, TileStatus.DIRTY);
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
				return current;
			}
			if(down == TileStatus.TARGET){
				//System.out.println("Down target");
				current.add(new Position(spot.getRow() + 1, spot.getCol()));
				return current;
			}
			if(left == TileStatus.TARGET){
				//System.out.println("Left target");
				current.add(new Position(spot.getRow(), spot.getCol() - 1));
				return current;
			}
			if(right == TileStatus.TARGET){
				//System.out.println("Right target");
				current.add(new Position(spot.getRow(), spot.getCol() + 1));
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
	 * This method implements A* search. It populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method 
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 *
	 * @return
	 */
	public LinkedList<Position> astar() {
		LinkedList<Position> path = astarhelper(env.getTargets().getFirst(), new Position(posRow,posCol));
		if(path != null) {
			this.path = path;
			this.pathFound = true;
			this.pathLength = path.size() - 1;
			return path;
		}
		else{
			pathFound = false;
			return null;
		}
	}

	public LinkedList<Position> astarhelper(Position target, Position startpos) {
		//create the queue of paths
		PriorityQueue<PriorityPath> queue = new PriorityQueue<PriorityPath>(5, new pathComparator());
		//and add the first path which is the start state of the robot
		LinkedList<Position> first = new LinkedList<>();
		first.push(startpos);
		queue.add(new PriorityPath(first, distance(target, startpos)));
		env.setTileStatus(startpos, TileStatus.DIRTY);
		openCount++;
		while(!queue.isEmpty()){
			//get the next path so far
			LinkedList<Position> current = queue.poll().path;
			//get the current position of that path
			Position spot = current.getLast();
			if(spot.getRow() == target.getRow() && spot.getCol() == target.getCol()){
				return current;
			}
			//System.out.println("Checking position: " + spot.getRow() + ", " + spot.getCol());
			//check the status of all surrounding positions
			TileStatus up = env.getTileStatus(spot.getRow() - 1, spot.getCol());
			TileStatus down = env.getTileStatus(spot.getRow() + 1, spot.getCol());
			TileStatus left = env.getTileStatus(spot.getRow(), spot.getCol() - 1);
			TileStatus right = env.getTileStatus(spot.getRow(), spot.getCol() + 1);

			//check for passable adjacent spots
			Position newPos;
			if(up == TileStatus.CLEAN || up == TileStatus.TARGET){
//				System.out.println("Up clean");
				LinkedList<Position> newPath = (LinkedList<Position>) current.clone();
				newPos = new Position(spot.getRow() - 1, spot.getCol());
				newPath.add(newPos);
				openCount++;
				queue.add(new PriorityPath(newPath, distance(target, newPos)));
				env.setTileStatus(newPos, TileStatus.DIRTY);
			}
			if(down == TileStatus.CLEAN || down == TileStatus.TARGET){
//				System.out.println("Down clean");
				LinkedList<Position> newPath = (LinkedList<Position>) current.clone();
				newPos = new Position(spot.getRow() + 1, spot.getCol());
				newPath.add(newPos);
				openCount++;
				queue.add(new PriorityPath(newPath, distance(target, newPos)));
				env.setTileStatus(newPos, TileStatus.DIRTY);
			}
			if(left == TileStatus.CLEAN || left == TileStatus.TARGET){
//				System.out.println("Left clean");
				LinkedList<Position> newPath = (LinkedList<Position>) current.clone();
				newPos = new Position(spot.getRow(), spot.getCol() - 1);
				newPath.add(newPos);
				openCount++;
				queue.add(new PriorityPath(newPath, distance(target, newPos)));
				env.setTileStatus(newPos, TileStatus.DIRTY);
			}
			if(right == TileStatus.CLEAN || right == TileStatus.TARGET){
//				System.out.println("Right clean");
				LinkedList<Position> newPath = (LinkedList<Position>) current.clone();
				newPos = new Position(spot.getRow(), spot.getCol() + 1);
				newPath.add(newPos);
				openCount++;
				queue.add(new PriorityPath(newPath, distance(target, newPos)));
				this.env.setTileStatus(newPos, TileStatus.DIRTY);
			}
		}
		pathFound = false;
		return null;
	}
	
	/** 
	 * This method implements A* search for maps with multiple targets. It 
	 * populates the path LinkedList
	 * and sets pathFound to true, if a path has been found. IMPORTANT: This method 
	 * increases the openCount field every time your code adds a node to the open
	 * data structure, i.e. the queue or priorityQueue
	 *
	 * @return
	 */
	public LinkedList<Position> astarM() {
		//create list of targets from env
		LinkedList<Position> targets = env.getTargets();
		//create holder for full path
		LinkedList<Position> fullPath = new LinkedList<>();
		//set the current position
		Position currentPos = new Position(getPosRow(), getPosCol());
		//add current position to full path
		fullPath.add(currentPos);
		//while some targets are left to find.
		while(!targets.isEmpty()){
			Position closest = findClosestTarget(currentPos, targets);
//			System.out.println("Finding target at " + closest.getRow() + " ," + closest.getCol());
			//find the path to a target
			LinkedList<Position> path = astarhelper(closest, currentPos);
			//if no path was found, no path exists
			if(path == null){
				System.out.println("No path found");
				pathFound = false;
				return null;
			}
			//remove the start position
			path.poll();
			//set the current position to where the robot ended
			currentPos = path.peekLast();
			//add the found path to the full path
			fullPath.addAll(path);
			//remove the target
			targets.remove(closest);
			env.setTileStatus(currentPos, TileStatus.CLEAN);
			env.cleanEnvironment();
		}
		pathFound = true;
		pathLength = fullPath.size() - 1;
		this.path = fullPath;
		return fullPath;
	}

	private Position findClosestTarget(Position currentPos, LinkedList<Position> targets) {
		Position closest = targets.getFirst();
		double closestDistance = distance(currentPos, closest);
		for(int i = 1; i < targets.size(); i++){
			double nextDistance = distance(currentPos, targets.get(i));
			if(nextDistance < closestDistance) {
				closestDistance = nextDistance;
				closest = targets.get(i);
			}
		}
		return closest;
	}

	private static class PriorityPath{
		public LinkedList<Position> path;
		public double score;
		public PriorityPath(LinkedList<Position> path, double score){
			this.path = path;
			this.score = score;
		}
	}

	private static class pathComparator implements Comparator<PriorityPath>{

		@Override
		public int compare(PriorityPath o1, PriorityPath o2) {
			if(o1.score < o2.score) return -1;
			else if(o1.score == o2.score) return 0;
			else return 1;
		}
	}

	private double distance(Position p1, Position p2){
		return Math.hypot(Math.abs(p1.getRow() - p2.getRow()), Math.abs(p1.getCol() - p2.getCol()));
	}



}