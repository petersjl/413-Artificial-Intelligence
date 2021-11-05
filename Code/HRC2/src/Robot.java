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
	private static LinkedList<Position> currentTargets = null;
	public static void setCurrentTargets(LinkedList<Position> targets){currentTargets = targets;}
	private Position myTarget;
	
	/**
	    Initializes a Robot on a specific tile in the environment. 
	*/
	
	public Robot (Environment env, int posRow, int posCol, int id) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.waiting = true;
		this.id = id;
		myTarget = null;
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
		if(waiting) {
			Position closestTarget = findClosestTarget();

			if (closestTarget == null){
				finished = true;
				return Action.DO_NOTHING;

			}else{
				//System.out.println("Robot " + id +" is now looking for target " + closestTarget);
			}
			waiting = false;
			currentTargets.remove(closestTarget);
			myTarget = closestTarget;
			//System.out.println("Remaining targets: " + currentTargets);
			System.out.println("\n\nFinding policy for robot " + id);
			policy = env.generatePolicy(closestTarget);
		}
		if (myTarget.equals(new Position(posRow,posCol))) {
			this.waiting = true;
			return Action.CLEAN;
		}

		return policy[posRow][posCol];
	}

	private Position findClosestTarget() {
		int minDistance = Integer.MAX_VALUE;
		Position minTarget = null;
		for(Position target: currentTargets){
			int distance = Math.abs(this.posRow - target.row) + Math.abs(this.posCol - target.col);
			if( minDistance > distance ){
				minDistance = distance;
				minTarget = target;
			}
		}
		return minTarget;
	}

}