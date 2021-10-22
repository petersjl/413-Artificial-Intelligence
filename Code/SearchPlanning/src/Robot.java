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
		System.out.println("Did not find a path in bfsM");
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
//	public List<Position> bfsM() {
//		//create list of targets from env
//		LinkedList<Position> targets = env.getTargets();
//		//create holder for full path
//		LinkedList<Position> fullPath = new LinkedList<>();
//		//set the current position
//		Position currentPos = new Position(getPosRow(), getPosCol());
//		//add current position to full path
//		fullPath.add(currentPos);
//		//while some targets are left to find.
//		while(!targets.isEmpty()){
//			//find the path to a target
//			LinkedList<Position> path = bfsHelper(currentPos);
//			//if no path was found, no path exists
//			if(path == null){
//				pathFound = false;
//				return null;
//			}
//			//remove the start position
//			path.poll();
//			//set the current position to where the robot ended
//			currentPos = path.peekLast();
//			//add the found path to the full path
//			fullPath.addAll(path);
//			//remove the target
//			targets.poll();
//			env.setTileStatus(currentPos, TileStatus.CLEAN);
//			env.cleanEnvironment();
//		}
//		pathFound = true;
//		pathLength = fullPath.size() - 1;
//		this.path = fullPath;
//		return fullPath;
//	}

	public List<Position> bfsM() {
		LinkedList<BetterPosition> targets = BetterPosition.convertFromPositions(env.getTargets());
		BetterPosition startPos = new BetterPosition(getPosRow(), getPosCol());
		LinkedList<BetterPosition> startPath = new LinkedList<>();
		startPath.add(startPos);
		LinkedList<EnvState> open = new LinkedList<>();
		LinkedList<EnvState> closed = new LinkedList<>();
		EnvState start = new EnvState(startPos, startPath, targets);
		open.add(start);
		openCount++;
		while(!open.isEmpty()){
			EnvState current = open.pop();
			TileStatus currentTile = env.getTileStatus(current.pos.getRow() - 1, current.pos.getCol());
			BetterPosition newPos;
			if(!(currentTile == TileStatus.IMPASSABLE)){
				newPos = new BetterPosition(current.pos.getRow() - 1, current.pos.getCol());
				if(addNewState(open, closed, current, currentTile, newPos)) return foundPath(open);
			}
			currentTile = env.getTileStatus(current.pos.getRow() + 1, current.pos.getCol());
			if(!(currentTile == TileStatus.IMPASSABLE)){
				newPos = new BetterPosition(current.pos.getRow() + 1, current.pos.getCol());
				if(addNewState(open, closed, current, currentTile, newPos)) return foundPath(open);
			}
			currentTile = env.getTileStatus(current.pos.getRow(), current.pos.getCol() - 1);
			if(!(currentTile == TileStatus.IMPASSABLE)){
				newPos = new BetterPosition(current.pos.getRow(), current.pos.getCol() - 1);
				if(addNewState(open, closed, current, currentTile, newPos)) return foundPath(open);
			}
			currentTile = env.getTileStatus(current.pos.getRow(), current.pos.getCol() + 1);
			if(!(currentTile == TileStatus.IMPASSABLE)){
				newPos = new BetterPosition(current.pos.getRow(), current.pos.getCol() + 1);
				if(addNewState(open, closed, current, currentTile, newPos)) return foundPath(open);
			}
			closed.add(current);
		}
		pathFound = false;
		return null;
	}

	private LinkedList<Position> foundPath(LinkedList<EnvState> open){
		LinkedList<Position> finalPath = BetterPosition.toPositions(open.getLast().path);
		pathFound = true;
		pathLength = finalPath.size() - 1;
		this.path = finalPath;
		return finalPath;
	}

	private boolean addNewState(LinkedList<EnvState> open, LinkedList<EnvState> closed, EnvState current, TileStatus currentTile, BetterPosition newPos){
		LinkedList<BetterPosition> newTargets = (LinkedList<BetterPosition>) current.targets.clone();
		LinkedList<BetterPosition> newPath = ((LinkedList<BetterPosition>) current.path.clone());
		newPath.add(newPos);
		if (currentTile == TileStatus.TARGET){
			newTargets.remove(newPos);
		}
		EnvState newState = new EnvState(newPos, newPath, newTargets);
		if(closed.contains(newState) || open.contains(newState)) {
			return false;
		}
		open.add(newState);
		openCount++;
		return newTargets.size() == 0;
	}

	private class EnvState{
		BetterPosition pos;
		LinkedList<BetterPosition> path;
		LinkedList<BetterPosition> targets;
		int score;
		public EnvState(BetterPosition pos, LinkedList<BetterPosition> path, LinkedList<BetterPosition> targets){
			this.pos = pos;
			this.path = path;
			this.targets = targets;
		}

		public EnvState(BetterPosition pos, LinkedList<BetterPosition> path, LinkedList<BetterPosition> targets, int score){
			this.pos = pos;
			this.path = path;
			this.targets = targets;
		}

		@Override
		public boolean equals(Object o){
			if(o.getClass() != getClass()) return false;
			EnvState other = (EnvState) o;
			if(this.pos.getRow() != other.pos.getRow()) return false;
			if(this.pos.getCol() != other.pos.getCol()) return false;
			return this.targets.equals(other.targets);
		}

	}

	private static class BetterPosition{
		private int row;
		private int col;

		public BetterPosition(int row, int col){
			this.row = row;
			this.col = col;
		}

		public BetterPosition(Position pos){
			this.row = pos.getRow();
			this.col = pos.getCol();
		}

		int getRow() {
			return row;
		}

		int getCol() {
			return col;
		}

		void setRow(int row) {
			this.row = row;
		}

		void setCol(int col) {
			this.col = col;
		}

		public static LinkedList<BetterPosition> convertFromPositions(LinkedList<Position> positions){
			Iterator<Position> iterator = positions.iterator();
			LinkedList<BetterPosition> betterPositions = new LinkedList<>();
			while(iterator.hasNext()){
				Position pos = iterator.next();
				betterPositions.add(new BetterPosition(pos.getRow(), pos.getCol()));
			}
			return betterPositions;
		}

		public static LinkedList<Position> toPositions(LinkedList<BetterPosition> betterPositions){
			Iterator<BetterPosition> iterator = betterPositions.iterator();
			LinkedList<Position> positions = new LinkedList<>();
			while(iterator.hasNext()){
				BetterPosition pos = iterator.next();
				positions.add(new Position(pos.getRow(), pos.getCol()));
			}
			return positions;
		}

		@Override
		public boolean equals(Object o){
			if(o.getClass() != getClass()) return false;
			BetterPosition other = (BetterPosition) o;
			if(this.getRow() != other.getRow()) return false;
			return this.getCol() == other.getCol();
		}
	}

//	private LinkedList<Position> bfsHelper(Position currentPos){
//		//create the queue of paths
//		LinkedList<LinkedList<Position>> queue = new LinkedList<>();
//		//and add the first path which is the start state of the robot
//		LinkedList<Position> first = new LinkedList<>();
//		first.push(currentPos);
//		queue.push(first);
//		env.setTileStatus(currentPos, TileStatus.DIRTY);
//		openCount++;
//		while(!queue.isEmpty()){
//			//get the next path so far
//			LinkedList<Position> current = queue.poll();
//			//get the current position of that path
//			Position spot = current.getLast();
//			//System.out.println("Checking position: " + spot.getRow() + ", " + spot.getCol());
//			//check the status of all surrounding positions
//			TileStatus up = env.getTileStatus(spot.getRow() - 1, spot.getCol());
//			TileStatus down = env.getTileStatus(spot.getRow() + 1, spot.getCol());
//			TileStatus left = env.getTileStatus(spot.getRow(), spot.getCol() - 1);
//			TileStatus right = env.getTileStatus(spot.getRow(), spot.getCol() + 1);
//			//check for target hits
//			if(up == TileStatus.TARGET){
//				//System.out.println("Up target");
//				current.add(new Position(spot.getRow() - 1, spot.getCol()));
//				return current;
//			}
//			if(down == TileStatus.TARGET){
//				//System.out.println("Down target");
//				current.add(new Position(spot.getRow() + 1, spot.getCol()));
//				return current;
//			}
//			if(left == TileStatus.TARGET){
//				//System.out.println("Left target");
//				current.add(new Position(spot.getRow(), spot.getCol() - 1));
//				return current;
//			}
//			if(right == TileStatus.TARGET){
//				//System.out.println("Right target");
//				current.add(new Position(spot.getRow(), spot.getCol() + 1));
//				return current;
//			}
//			//check for passable adjacent spots
//			Position newPos;
//			if(up == TileStatus.CLEAN){
////				System.out.println("Up clean");
//				LinkedList<Position> newPath = (LinkedList<Position>) current.clone();
//				newPos = new Position(spot.getRow() - 1, spot.getCol());
//				newPath.add(newPos);
//				openCount++;
//				queue.add(newPath);
//				env.setTileStatus(newPos, TileStatus.DIRTY);
//			}
//			if(down == TileStatus.CLEAN){
////				System.out.println("Down clean");
//				LinkedList<Position> newPath = (LinkedList<Position>) current.clone();
//				newPos = new Position(spot.getRow() + 1, spot.getCol());
//				newPath.add(newPos);
//				openCount++;
//				queue.add(newPath);
//				env.setTileStatus(newPos, TileStatus.DIRTY);
//			}
//			if(left == TileStatus.CLEAN){
////				System.out.println("Left clean");
//				LinkedList<Position> newPath = (LinkedList<Position>) current.clone();
//				newPos = new Position(spot.getRow(), spot.getCol() - 1);
//				newPath.add(newPos);
//				openCount++;
//				queue.add(newPath);
//				env.setTileStatus(newPos, TileStatus.DIRTY);
//			}
//			if(right == TileStatus.CLEAN){
////				System.out.println("Right clean");
//				LinkedList<Position> newPath = (LinkedList<Position>) current.clone();
//				newPos = new Position(spot.getRow(), spot.getCol() + 1);
//				newPath.add(newPos);
//				openCount++;
//				queue.add(newPath);
//				this.env.setTileStatus(newPos, TileStatus.DIRTY);
//			}
//		}
//		pathFound = false;
//		return null;
//	}


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
//	public LinkedList<Position> astarM() {
//		//create list of targets from env
//		LinkedList<Position> targets = env.getTargets();
//		//create holder for full path
//		LinkedList<Position> fullPath = new LinkedList<>();
//		//set the current position
//		Position currentPos = new Position(getPosRow(), getPosCol());
//		//add current position to full path
//		fullPath.add(currentPos);
//		//while some targets are left to find.
//		while(!targets.isEmpty()){
//			Position closest = findClosestTarget(currentPos, targets);
////			System.out.println("Finding target at " + closest.getRow() + " ," + closest.getCol());
//			//find the path to a target
//			LinkedList<Position> path = astarhelper(closest, currentPos);
//			//if no path was found, no path exists
//			if(path == null){
//				System.out.println("No path found");
//				pathFound = false;
//				return null;
//			}
//			//remove the start position
//			path.poll();
//			//set the current position to where the robot ended
//			currentPos = path.peekLast();
//			//add the found path to the full path
//			fullPath.addAll(path);
//			//remove the target
//			targets.remove(closest);
//			env.setTileStatus(currentPos, TileStatus.CLEAN);
//			env.cleanEnvironment();
//		}
//		pathFound = true;
//		pathLength = fullPath.size() - 1;
//		this.path = fullPath;
//		return fullPath;
//	}

	public LinkedList<Position> astarM() {
		LinkedList<BetterPosition> targets = BetterPosition.convertFromPositions(env.getTargets());
		BetterPosition startPos = new BetterPosition(getPosRow(), getPosCol());
		LinkedList<BetterPosition> startPath = new LinkedList<>();
		startPath.add(startPos);
		PriorityQueue<EnvState> open = new PriorityQueue<EnvState>(5, new manhattanComparator());
		LinkedList<EnvState> closed = new LinkedList<>();
		EnvState start = new EnvState(startPos, startPath, targets);
		open.add(start);
		openCount++;
		while(!open.isEmpty()){
			EnvState current = open.poll();
			TileStatus currentTile = env.getTileStatus(current.pos.getRow() - 1, current.pos.getCol());
			BetterPosition newPos;
			EnvState nextState;
			if(!(currentTile == TileStatus.IMPASSABLE)){
				newPos = new BetterPosition(current.pos.getRow() - 1, current.pos.getCol());
				nextState = addNewState(open, closed, current, currentTile, newPos);
				if(nextState != null) return foundPath(nextState);
			}
			currentTile = env.getTileStatus(current.pos.getRow() + 1, current.pos.getCol());
			if(!(currentTile == TileStatus.IMPASSABLE)){
				newPos = new BetterPosition(current.pos.getRow() + 1, current.pos.getCol());
				nextState = addNewState(open, closed, current, currentTile, newPos);
				if(nextState != null) return foundPath(nextState);
			}
			currentTile = env.getTileStatus(current.pos.getRow(), current.pos.getCol() - 1);
			if(!(currentTile == TileStatus.IMPASSABLE)){
				newPos = new BetterPosition(current.pos.getRow(), current.pos.getCol() - 1);
				nextState = addNewState(open, closed, current, currentTile, newPos);
				if(nextState != null) return foundPath(nextState);
			}
			currentTile = env.getTileStatus(current.pos.getRow(), current.pos.getCol() + 1);
			if(!(currentTile == TileStatus.IMPASSABLE)){
				newPos = new BetterPosition(current.pos.getRow(), current.pos.getCol() + 1);
				nextState = addNewState(open, closed, current, currentTile, newPos);
				if(nextState != null) return foundPath(nextState);
			}
			closed.add(current);
		}
		pathFound = false;
		return null;
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

	private int findDistanceToFurthestTarget(BetterPosition currentPos, LinkedList<BetterPosition> targets) {
		if(targets.size() == 0) return 0;
		int furthest = mDistance(currentPos, targets.get(0));
		for(int i = 1; i < targets.size(); i++){
			int nextDistance = mDistance(currentPos, targets.get(i));
			if(nextDistance > furthest) {
				furthest = nextDistance;
			}
		}
		return furthest;
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

	private EnvState addNewState(PriorityQueue<EnvState> open, LinkedList<EnvState> closed, EnvState current, TileStatus currentTile, BetterPosition newPos){
		LinkedList<BetterPosition> newTargets = (LinkedList<BetterPosition>) current.targets.clone();
		LinkedList<BetterPosition> newPath = ((LinkedList<BetterPosition>) current.path.clone());
		newPath.add(newPos);
		if (currentTile == TileStatus.TARGET){
			newTargets.remove(newPos);
		}
		int newScore = findDistanceToFurthestTarget(newPos,newTargets) + newPath.size();
		EnvState newState = new EnvState(newPos, newPath, newTargets, newScore);
		if(closed.contains(newState) || open.contains(newState)) {
			return null;
		}
		open.add(newState);
		openCount++;
		return (newTargets.size() == 0) ? newState : null;
	}

	private LinkedList<Position> foundPath(EnvState state){
		LinkedList<Position> finalPath = BetterPosition.toPositions(state.path);
		pathFound = true;
		pathLength = finalPath.size() - 1;
		this.path = finalPath;
		return finalPath;
	}

	private int mDistance(BetterPosition o1, BetterPosition o2){
		return Math.abs(o1.getRow() - o2.getRow()) + Math.abs(o1.getCol() - o2.getCol());
	}

	private static class manhattanComparator implements Comparator<EnvState>{

		@Override
		public int compare(EnvState o1, EnvState o2) {
			if(o1.score < o2.score) return -1;
			else if(o1.score == o2.score) return 0;
			else return 1;
		}
	}

	private double distance(Position p1, Position p2){
		return Math.hypot(Math.abs(p1.getRow() - p2.getRow()), Math.abs(p1.getCol() - p2.getCol()));
	}



}