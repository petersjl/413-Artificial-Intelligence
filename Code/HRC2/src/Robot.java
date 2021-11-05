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
	private boolean toCleanOrNotToClean;
	
	/**
	    Initializes a Robot on a specific tile in the environment. 
	*/

	
	public Robot (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.toCleanOrNotToClean = false;
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
		if (toCleanOrNotToClean) {
			toCleanOrNotToClean = false;
			return Action.CLEAN;
		}
		toCleanOrNotToClean = true;
		int foo = (int)(Math.random()*4);
		switch(foo) {
        case 0:
        	return Action.MOVE_RIGHT;
        case 1:
        	return Action.MOVE_LEFT;
        case 2:
        	return Action.MOVE_UP;
        case 3:
        	return Action.MOVE_DOWN;
        default:
        	return Action.CLEAN;
		}
	}


}