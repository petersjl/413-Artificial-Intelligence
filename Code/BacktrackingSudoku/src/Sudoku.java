import java.io.File;
        import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Stack;

public class Sudoku {

    private static int boardSize = 0;
    private static int partitionSize = 0;

    public static ArrayList solveFromFile(String filename){
        File inputFile = new File(filename);
        Scanner input = null;
        int[][] vals = null;

        int temp = 0;
        int count = 0;

        try {
            input = new Scanner(inputFile);
            temp = input.nextInt();
            boardSize = temp;
            partitionSize = (int) Math.sqrt(boardSize);
            System.out.println("Boardsize: " + temp + "x" + temp);
            vals = new int[boardSize][boardSize];

            System.out.println("Input:");
            int i = 0;
            int j = 0;
            while (input.hasNext()){
                temp = input.nextInt();
                count++;
                System.out.printf("%3d", temp);
                vals[i][j] = temp;
                if (temp == 0) {
                    // TODO
                }
                j++;
                if (j == boardSize) {
                    j = 0;
                    i++;
                    System.out.println();
                }
                if (j == boardSize) {
                    break;
                }
            }
            input.close();
        } catch (FileNotFoundException exception) {
            System.out.println("Input file not found: " + filename);
            ArrayList result = new ArrayList();
            result.add(false);
            return result;
        }
        if (count != boardSize*boardSize) throw new RuntimeException("Incorrect number of inputs.");

//        ArrayList<ArrayList<HashSet<Integer>>> available = setup(vals);
        boolean solved = solve(vals, 0, 0);

        // Output
        if (!solved) {
            System.out.println("No solution found.");
            ArrayList result = new ArrayList();
            result.add(false);
            return result;
        }
        System.out.println("\nOutput\n");
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                System.out.printf("%3d", vals[i][j]);
            }
            System.out.println();
        }

        //Create file with output
        String dest = "Output/" + inputFile.getName().replace(".txt", "") + "Solution.txt";
        try {
            File output = new File(dest);
            if (!output.createNewFile()) {
                output.delete();
                output.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error creating output file for" + dest);
        }

        try {
            FileWriter writer = new FileWriter(dest);
            String line = "";
            for(int i = 0; i < boardSize; i++){
                for(int j = 0; j < boardSize; j++){
                    line += vals[i][j] + " ";
                }
                line += "\n";
                writer.write(line);
                line = "";
            }
            System.out.println("Wrote finished puzzle to " + dest);
            writer.close();
        } catch (IOException e) {
            System.out.println("Error writing to file" + dest);
        }

        ArrayList result = new ArrayList();
        result.add(true);
        result.add(vals);
        return result;

    }

//    public static ArrayList<ArrayList<HashSet<Integer>>> setup(int[][] vals){
//        //Create a 2D array of hashsets
//        ArrayList<ArrayList<HashSet<Integer>>> remaining = new ArrayList<ArrayList<HashSet<Integer>>>();
//        for(int i = 0; i < boardSize; i++){
//            ArrayList<HashSet<Integer>> temp = new ArrayList<HashSet<Integer>>();
//            for(int j = 0; j < boardSize; j++) {
//                HashSet<Integer> hash = null;
//                //If the puzzle already has a number we make an empty hashset
//                if(vals[i][j] != 0){
//                    hash = new HashSet<>(1);
//                }
//                //else we make a hashset of 1-9
//                else{
//                    hash = new HashSet<>(9);
//                    int count = 1;
//                    while(count < 10) {
//                        hash.add(count);
//                        count++;
//                    }
//                    //then we remove any value on its x or y axis
//                    int x;
//                    for(x = 0; x < boardSize; x++){
//                        hash.remove(vals[x][j]);
//                        hash.remove(vals[i][x]);
//                    }
//                    //and remove all values in it's partition
//                    for(int m = (i / partitionSize) * partitionSize; m < ((i / partitionSize) + 1) * partitionSize; m++){
//                        for(int n = (j / partitionSize) * partitionSize; n < ((j / partitionSize) + 1) * partitionSize; n++){
//                            hash.remove(vals[m][n]);
//                        }
//                    }
//                }
//                temp.add(hash);
//            }
//            remaining.add(temp);
//        }
//        return remaining;
//    }

    public static boolean solve(int vals[][], int i, int j){
        // If we have done all rows, it is solved
        if(i == boardSize) return true;
        // If we have done all cols, we move to the next row
        if(j == boardSize) return solve(vals, i + 1, 0);
        // If the val is not 0, it is a given number so it should not be changed
        if(vals [i][j] != 0) return solve(vals, i, j + 1);
        // Now we make a stack with values 1-9
        Stack<Integer> stack = new Stack<Integer>();
        int count = 1;
        while(count <= boardSize) {
            stack.push(count);
            count++;
        }
        //then we remove any value on its x or y axis
        int x;
        for(x = 0; x < boardSize; x++){
            stack.removeElement(vals[x][j]);
            stack.removeElement(vals[i][x]);
        }
        //and remove all values in it's partition
        for(int m = (i / partitionSize) * partitionSize; m < ((i / partitionSize) + 1) * partitionSize; m++){
            for(int n = (j / partitionSize) * partitionSize; n < ((j / partitionSize) + 1) * partitionSize; n++){
                stack.removeElement(vals[m][n]);
            }
        }
        boolean solved = false;
        while(!solved){
            // If the stack is empty, we have nothing else to try and backtrack
            if(stack.isEmpty()){
               vals[i][j] = 0;
               return false;
            }
            else{
                vals[i][j] = stack.pop();
                solved = solve(vals, i, j + 1);
            }
        }
        return true;
    }

}