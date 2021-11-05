import java.util.LinkedList;

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
	private boolean waiting;
	private Action[][] policy;
	private boolean finished = false;
	private int id;
	
	/**
	    Initializes a Robot on a specific tile in the environment. 
	*/
	
	public Robot (Environment env, int posRow, int posCol, int id) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.waiting = true;
		this.id = id;
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
		if(finished) return Action.DO_NOTHING;
		if (env.getTileStatus(this.posRow, this.posCol) == TileStatus.DIRTY) {
			this.waiting = true;
			return Action.CLEAN;
		}
		if(waiting) {
			Position closestTarget = findClosestTarget();

			if (closestTarget == null){
				finished = true;
				return Action.DO_NOTHING;

			}
			System.out.println("\n\nFinding policy for robot " + id);
			policy = env.generatePolicy(closestTarget);
		}

		return policy[posRow][posCol];
	}

	private Position findClosestTarget() {
		LinkedList<Position> targets = env.getDirtyTiles();
		int minDistance = Integer.MAX_VALUE;
		Position minTarget = null;
		for(Position target: targets){
			int distance = Math.abs(this.posRow - target.row) + Math.abs(this.posCol - target.col);
			if( minDistance > distance ){
				minDistance = distance;
				minTarget = target;
			}
		}
		return minTarget;
	}

}