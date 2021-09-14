import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class SudokuTest {

    @Test
    void solve9Easy() {
        ArrayList result = Sudoku.solveFromFile("Data/sudoku9Easy.txt");
        assertEquals(true, result.get(0));
        assertEquals(true, isSolved((int[][]) result.get(1)));
    }

    @Test
    void solve9Empty() {
        ArrayList result = Sudoku.solveFromFile("Data/sudoku9Empty.txt");
        assertEquals(true, result.get(0));
        assertEquals(true, isSolved((int[][]) result.get(1)));
    }

    @Test
    void solve9Hard() {
        ArrayList result = Sudoku.solveFromFile("Data/sudoku9Hard.txt");
        assertEquals(true, result.get(0));
        assertEquals(true, isSolved((int[][]) result.get(1)));
    }

    @Test
    void solve9Hardest() {
        ArrayList result = Sudoku.solveFromFile("Data/sudoku9Hardest.txt");
        assertEquals(true, result.get(0));
        assertEquals(true, isSolved((int[][]) result.get(1)));
    }

    @Test
    void solve9Medium() {
        ArrayList result = Sudoku.solveFromFile("Data/sudoku9Medium.txt");
        assertEquals(true, result.get(0));
        assertEquals(true, isSolved((int[][]) result.get(1)));
    }

    @Test
    void solve9OneSpot() {
        ArrayList result = Sudoku.solveFromFile("Data/sudoku9OneSpot.txt");
        assertEquals(true, result.get(0));
        assertEquals(true, isSolved((int[][]) result.get(1)));
    }

    @Test
    void solve9Unsolvable() {
        ArrayList result = Sudoku.solveFromFile("Data/sudoku9Unsolvable.txt");
        assertEquals(false, result.get(0));
    }

    @Test
    void solve9VeryHard() {
        ArrayList result = Sudoku.solveFromFile("Data/sudoku9VeryHard.txt");
        assertEquals(true, result.get(0));
        assertEquals(true, isSolved((int[][]) result.get(1)));
    }

    @Test
    void solve16Medium() {
        ArrayList result = Sudoku.solveFromFile("Data/sudoku16Medium.txt");
        assertEquals(true, result.get(0));
        assertEquals(true, isSolved((int[][]) result.get(1)));
    }

    @Test
    void solve16ReallyHard() {
        ArrayList result = Sudoku.solveFromFile("Data/sudoku16ReallyHard.txt");
        assertEquals(true, result.get(0));
        assertEquals(true, isSolved((int[][]) result.get(1)));
    }

    @Test
    void solve16Empty() {
        ArrayList result = Sudoku.solveFromFile("Data/sudokuEmpty16.txt");
        assertEquals(true, result.get(0));
        assertEquals(true, isSolved((int[][]) result.get(1)));
    }

    @Test
    void solve25Empty() {
        ArrayList result = Sudoku.solveFromFile("Data/sudokuEmpty25.txt");
        assertEquals(true, result.get(0));
        assertEquals(true, isSolved((int[][]) result.get(1)));
    }

    @Test
    void random(){
        long i = 0L;
        while(i < 1000000000000L) {
            i++;
            System.out.println(i);
        }
    }

    boolean isSolved(int[][] vals){
        int boardSize = vals.length;
        int partitionSize = (int) Math.sqrt(vals.length);
        HashSet<Integer> set = new HashSet<>(boardSize);
        for (int i = 0; i < boardSize; i ++){
            for (int j = 0; j < boardSize; j++){
                set.clear();
                int x;
                for (x = 0; x < boardSize; x++){
                    if(!set.add(vals[i][x])) return false;
                }
                set.clear();
                for (x = 0; x < boardSize; x++){
                    if(!set.add(vals[x][j])) return false;
                }
                set.clear();
                for(int m = (i / partitionSize) * partitionSize; m < ((i / partitionSize) + 1) * partitionSize; m++){
                    for(int n = (j / partitionSize) * partitionSize; n < ((j / partitionSize) + 1) * partitionSize; n++){
                        if(!set.add(vals[m][n])) return false;
                    }
                }
            }
        }
        return true;
    }
}