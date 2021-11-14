
/**
 * A simple object representing the Tiles in the
 * environment.
 * DO NOT MODIFY.
 * @author Adam Gaweda
 */
public class Tile {
	private TileStatus status;
	
	public Tile(TileStatus status) {
		this.status = status;
	}
	
	public TileStatus getStatus() { return status; }
	public void cleanTile() { status = TileStatus.CLEAN; }
	public void soilTile() { status = TileStatus.DIRTY; }
	public String toString() { return ""+status.toString().charAt(0); }
}
