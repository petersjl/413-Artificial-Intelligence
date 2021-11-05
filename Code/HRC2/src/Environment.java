
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;

/**
 * The world in which this simulation exists. As a base
 * world, this produces a 10x10 room of tiles. In addition,
 * 20% of the room is covered with "walls" (tiles marked as IMPASSABLE).
 * 
 * This object will allow the agent to explore the world and is how
 * the agent will retrieve information about the environment.
 * DO NOT MODIFY.
 * @author Adam Gaweda, Michael Wollowski
 */
public class Environment {
	private Tile[][] tiles;
	private int rows, cols;
	private LinkedList<Position> targets = new LinkedList<>();
	private LinkedList<Position> dirtyTiles = new LinkedList<>();
	private ArrayList<Robot> robots;
	private Action[][] policy;
	private final double discount = 0.95;
	private final double epsilon = 0.0001;
	private final double minChangeInMatrixAllowed;
	private double [][] rewards;

	public Environment(LinkedList<String> map, ArrayList<Robot> robots) { 
		this.cols = map.get(0).length();
		this.rows = map.size();
		this.tiles = new Tile[rows][cols];
		int numRobots = 0;
		for (int row = 0; row < this.rows; row++) {
			for (int col = 0; col < this.cols; col++) {
				char tile = map.get(row).charAt(col);
				switch(tile) {
				case 'R': tiles[row][col] = new Tile(TileStatus.CLEAN); {
					robots.add(new Robot(this, row, col, numRobots));
					numRobots++;
					break;
				}
				case 'D': tiles[row][col] = new Tile(TileStatus.DIRTY); dirtyTiles.add(new Position(row, col)); break;
				case 'C': tiles[row][col] = new Tile(TileStatus.CLEAN); break;
				case 'W': tiles[row][col] = new Tile(TileStatus.IMPASSABLE); break;
				case 'T': tiles[row][col] = new Tile(TileStatus.TARGET); targets.add(new Position(row, col)); break;
				}
			}
		}
		this.robots = robots;
		Robot.setCurrentTargets((LinkedList<Position>) dirtyTiles.clone());
		this.minChangeInMatrixAllowed = epsilon * (1 - discount) / discount;
		//this.policy = valueToPolicy(getUtilityMatrix());
	}
	
	/* Traditional Getters and Setters */
	public Tile[][] getTiles() { return tiles; }
	
	public int getRows() { return this.rows; }
	
	public int getCols() { return this.cols; }

	public LinkedList<Position> getTargets(){
		return (LinkedList<Position>) this.targets.clone();
	}

	public LinkedList<Position> getDirtyTiles() { return (LinkedList<Position>) this.dirtyTiles.clone();}
	
	public ArrayList<Robot> getRobots(){
		return (ArrayList<Robot>) this.robots.clone();
	}
	
	public Action[][]getPolicy(){
		return policy;
	}

	public Action getPolicyAtPosition(int row, int col) { return this.policy[row][col]; }

	/*
	 * Returns a the status of a tile at a given [row][col] coordinate
	 */
	public TileStatus getTileStatus(int row, int col) {
		if (row < 0 || row >= rows || col < 0 || col >= cols) return TileStatus.IMPASSABLE;
		return tiles[row][col].getStatus();
	}

	public TileStatus getTileStatusWithRobots(int row, int col) {
		if (row < 0 || row >= rows || col < 0 || col >= cols) return TileStatus.IMPASSABLE;
		for(Robot r : this.robots){
			if(r.getPosRow() == row && r.getPosCol() == col) return TileStatus.IMPASSABLE;
		}
		return tiles[row][col].getStatus();
	}

	/* Counts number of tiles that are not walls */
	public int getNumTiles() {
		int count = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (this.tiles[row][col].getStatus() != TileStatus.IMPASSABLE)
                    count++;
            }
        }
        return count;
    }
	
	/* Cleans the tile at coordinate [x][y] */
	public void cleanTile(int x, int y) {
		tiles[x][y].cleanTile();
		dirtyTiles.remove(new Position(x, y));
	}
	
	/* Counts number of clean tiles */
	public int getNumCleanedTiles() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (this.tiles[i][j].getStatus() == TileStatus.CLEAN)
                    count++;
            }
        }
        return count;
    }
	
	/* Counts number of dirty tiles */
	public int getNumDirtyTiles() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (this.tiles[i][j].getStatus() == TileStatus.DIRTY)
                    count++;
            }
        }
        return count;
    }

	public Action[][] generatePolicy(Position target){
		return valueToPolicy(getUtilityMatrixSingleTarget(target));
	}

	/* Determines if a particular [row][col] coordinate is within
	 * the boundaries of the environment. This is a rudimentary
	 * "collision detection" to ensure the agent does not walk
	 * outside the world (or through walls).
	 */
	public boolean validPos(int row, int col) {
	    return row >= 0 && row < rows && col >= 0 && col < cols &&
	    		tiles[row][col].getStatus() != TileStatus.IMPASSABLE;
	}

	private double [][] getUtilityMatrix(){
		this.rewards = initializeWithRewardValues();
		double [][] utilityMatrix = initializeWithRewardValues();
		double[][] previousUtilityMatrix;

		while (true){
			previousUtilityMatrix = Utilities.copyDoubleArray(utilityMatrix);
			double currentMaxChangeState = Integer.MIN_VALUE;

			for (int i = 0; i < rows; i++){
				for (int j = 0; j < cols; j++){

					if (getTileStatus(i,j) != TileStatus.DIRTY){
						ArrayList <Double> qValues = getQValuesForActions(i, j, previousUtilityMatrix);
						utilityMatrix[i][j] = rewards[i][j] + discount * Collections.max(qValues);
						double changeInState = Math.abs(utilityMatrix[i][j] - previousUtilityMatrix[i][j]);

						if (changeInState > currentMaxChangeState){
							currentMaxChangeState = changeInState;
						}
					}
				}
			}
			if (currentMaxChangeState < minChangeInMatrixAllowed){
				break;
			}
		}
		//Utilities.printArray(utilityMatrix);
		return utilityMatrix;
	}

	private double [][] getUtilityMatrixSingleTarget(Position target){
		int targetI = target.row;
		int targetJ = target.col;
		this.rewards = initializeWithRewardValuesSingleTarget(targetI, targetJ);
		double [][] utilityMatrix = initializeWithRewardValuesSingleTarget(targetI, targetJ);
		double[][] previousUtilityMatrix;

		while (true){
			previousUtilityMatrix = Utilities.copyDoubleArray(utilityMatrix);
			double currentMaxChangeState = Integer.MIN_VALUE;

			for (int i = 0; i < rows; i++){
				for (int j = 0; j < cols; j++){
					boolean isTarget = getTileStatus(i,j) == TileStatus.DIRTY && i == targetI && j == targetJ;
					if (!isTarget){
						ArrayList <Double> qValues = getQValuesForActions(i, j, previousUtilityMatrix);
						utilityMatrix[i][j] = rewards[i][j] + discount * Collections.max(qValues);
						double changeInState = Math.abs(utilityMatrix[i][j] - previousUtilityMatrix[i][j]);

						if (changeInState > currentMaxChangeState){
							currentMaxChangeState = changeInState;
						}
					}
				}
			}
			if (currentMaxChangeState < minChangeInMatrixAllowed){
				break;
			}
		}
		Utilities.printArray(utilityMatrix);
		return utilityMatrix;
	}

	private ArrayList<Double> getQValuesForActions(int i, int j, double[][] previousUtilityMatrix) {

		ArrayList<Double> qValuesForActions = new ArrayList<>();

		Position currentPlace = new Position(i,j);

		qValuesForActions.add(calculatePriority(previousUtilityMatrix, currentPlace, Direction.LEFT));
		qValuesForActions.add(calculatePriority(previousUtilityMatrix, currentPlace, Direction.RIGHT));
		qValuesForActions.add(calculatePriority(previousUtilityMatrix, currentPlace, Direction.UP));
		qValuesForActions.add(calculatePriority(previousUtilityMatrix, currentPlace, Direction.DOWN));

		return qValuesForActions;
	}

	private double [][] initializeWithRewardValues() {

		double [][] utilityMatrix = new double[rows][cols];
		for (int i = 0; i < rows; i++){
			for (int j = 0; j < cols; j++){

				if (getTileStatus(i,j) == TileStatus.DIRTY){
					utilityMatrix[i][j] = 1.0;
				} else if (getTileStatus(i,j) == TileStatus.CLEAN){
					utilityMatrix[i][j] = -0.04;
				} else { //Impassable tiles (will avoid, just giving value for sake of consistency
					utilityMatrix[i][j] = 0.0;
				}
			}
		}
		return utilityMatrix;
	}

	private double [][] initializeWithRewardValuesSingleTarget(int targetI, int targetJ) {

		double [][] utilityMatrix = new double[rows][cols];
		for (int i = 0; i < rows; i++){
			for (int j = 0; j < cols; j++){

				if (getTileStatus(i,j) == TileStatus.DIRTY && i == targetI && j == targetJ){
					utilityMatrix[i][j] = 1.0;
				} else if (getTileStatus(i,j) == TileStatus.CLEAN || getTileStatus(i,j) == TileStatus.DIRTY){
					utilityMatrix[i][j] = -0.04;
				} else { //Impassable tiles (will avoid, just giving value for sake of consistency
					utilityMatrix[i][j] = 0.0;
				}
			}
		}
		return utilityMatrix;
	}

	/**
	 Turns a value matrix into a policy
	 @param values double array of utility values
	 @return double array of actions for each position
	  */
	private Action[][] valueToPolicy(double[][] values){
		Action[][] actions = new Action[this.rows][this.cols];
		for(int i = 0; i < this.rows; i ++){
			for(int j = 0; j < this.cols; j++){
				if(this.getTileStatus(i,j) == TileStatus.IMPASSABLE) continue;
				Position current = new Position(i, j);
				double up = calculatePriority(values, current, Direction.UP);
				double down = calculatePriority(values, current, Direction.DOWN);
				double left = calculatePriority(values, current, Direction.LEFT);
				double right = calculatePriority(values, current, Direction.RIGHT);
				double max = -10;
				Direction maxDir = Direction.NONE;
				if((up > max) && (getTileStatus(i - 1, j) != TileStatus.IMPASSABLE)) {max = up; maxDir = Direction.UP;}
				if((down > max) && (getTileStatus(i + 1, j) != TileStatus.IMPASSABLE)) {max = down; maxDir = Direction.DOWN;}
				if((left > max) && (getTileStatus(i, j - 1) != TileStatus.IMPASSABLE)) {max = left; maxDir = Direction.LEFT;}
				if((right > max) && (getTileStatus(i, j + 1) != TileStatus.IMPASSABLE)) {maxDir = Direction.RIGHT;}
				switch (maxDir){
					case UP : actions[i][j] = Action.MOVE_UP; break;
					case DOWN: actions[i][j] = Action.MOVE_DOWN; break;
					case LEFT: actions[i][j] = Action.MOVE_LEFT; break;
					case RIGHT: actions[i][j] = Action.MOVE_RIGHT; break;
					case NONE: actions[i][j] = Action.DO_NOTHING; break;
				}
			}
		}
		Utilities.printArray(actions);
		return actions;
	}

	/**
	 Given a values array, current position, and direction, calculates the
	 priority of going in that direction
	 @param values double array of utility values
	 @param current the current position
	 @param direction the direction in which to check
	 @return the double priority value of moving in the given direction
	 */
	private double calculatePriority(double[][] values, Position current, Direction direction){
		double up = (getTileStatus(current.row - 1, current.col)!=TileStatus.IMPASSABLE)?values[current.row - 1][current.col] : values[current.row][current.col];
		double down = (getTileStatus(current.row + 1, current.col)!=TileStatus.IMPASSABLE)?values[current.row + 1][current.col] : values[current.row][current.col];
		double left = (getTileStatus(current.row, current.col - 1)!=TileStatus.IMPASSABLE)?values[current.row][current.col - 1] : values[current.row][current.col];
		double right = (getTileStatus(current.row, current.col + 1)!=TileStatus.IMPASSABLE)?values[current.row][current.col + 1] : values[current.row][current.col];
		switch (direction){
			case UP : return ((.8 * up) + (.1 * left) + (.1 * right));
			case DOWN: return ((.8 * down) + (.1 * left) + (.1 * right));
			case LEFT: return ((.8 * left) + (.1 * up) + (.1 * down));
			case RIGHT: return ((.8 * right) + (.1 * up) + (.1 * down));
		}
		return 0;
	}

	private enum Direction{
		UP,
		DOWN,
		LEFT,
		RIGHT,
		NONE
	}


}