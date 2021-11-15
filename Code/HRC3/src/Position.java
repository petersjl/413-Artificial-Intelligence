
/**
 * DO NOT MODIFY.
 *
 * @author Michael Wollowski
 */

import java.util.Objects;

public class Position {
    public int row;  // Make private and add setters and getters MIW
    public int col;  // Make private and add setters and getters MIW

    Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public boolean equals(Object x) {
        Position otherBoardPosition = (Position) x;
        return (otherBoardPosition.row == this.row) && (otherBoardPosition.col == this.col);
    }
}