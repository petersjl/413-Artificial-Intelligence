
import java.util.LinkedList;

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
	
	public Environment(LinkedList<String> map, Position robotPos) { // modify for multiple robots MIW
		this.cols = map.get(0).length();
		this.rows = map.size();
		this.tiles = new Tile[rows][cols];
		for (int row = 0; row < this.rows; row++) {
			for (int col = 0; col < this.cols; col++) {
				char tile = map.get(row).charAt(col);
				switch(tile) {
				case 'R': tiles[row][col] = new Tile(TileStatus.CLEAN); robotPos.row = row; robotPos.col = col; break;
				case 'D': tiles[row][col] = new Tile(TileStatus.DIRTY); break;
				case 'C': tiles[row][col] = new Tile(TileStatus.CLEAN); break;
				case 'W': tiles[row][col] = new Tile(TileStatus.IMPASSABLE); break;
				case 'T': tiles[row][col] = new Tile(TileStatus.TARGET); targets.add(new Position(row, col)); break;
				}
			}
		}
	}
	
	/* Traditional Getters and Setters */
	public Tile[][] getTiles() { return tiles; }
	public int getRows() { return this.rows; }
	public int getCols() { return this.cols; }

	public LinkedList<Position> getTargets(){
		return (LinkedList<Position>) this.targets.clone();
	}

	/*
	 * Returns a the status of a tile at a given [row][col] coordinate
	 */
	public TileStatus getTileStatus(int row, int col) {
		if (row < 0 || row >= rows || col < 0 || col >= cols) return TileStatus.IMPASSABLE; 
		else return tiles[row][col].getStatus();
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
	}
	
	/* Counts number of cleaned tiles */
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

	/* Determines if a particular [row][col] coordinate is within
	 * the boundaries of the environment. This is a rudimentary
	 * "collision detection" to ensure the agent does not walk
	 * outside the world (or through walls).
	 */
	public boolean validPos(int row, int col) {
	    return row >= 0 && row < rows && col >= 0 && col < cols &&
	    		tiles[row][col].getStatus() != TileStatus.IMPASSABLE;
	}

}
