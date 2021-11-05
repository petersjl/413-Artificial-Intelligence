
/** 
 * DO NOT MODIFY.
 * @author Michael Wollowski
 */
public class Position {
		public int row;  // Make private and add setters and getters MIW
		public int col;  // Make private and add setters and getters MIW
		
		Position(int row, int col){
			this.row = row;
			this.col = col;
		}

	@Override
	public boolean equals(Object o){
		if(o.getClass() != getClass()) return false;
		Position other = (Position) o;
		if(this.row != other.row) return false;
		return this.col == other.col;
	}

	@Override
	public String toString(){
			return "(" + row + "," + col + ")";
	}
}