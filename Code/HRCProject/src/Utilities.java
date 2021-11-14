import java.lang.reflect.Array;

public class Utilities {
    public static void printArray(double[][] array){
        for(int i = 0; i < array.length; i++) {
            for(int j = 0; j < array[0].length; j++){
                String out = Double.toString(array[i][j]);
                if(out.length() > 5) out = out.substring(0, 6);
                System.out.print(out + ", ");
            }
            System.out.println("");
        }
    }
    public static void printArray(Action[][] array){
        for(int i = 0; i < array.length; i++) {
            for(int j = 0; j < array[0].length; j++){
                System.out.print(array[i][j] + ", ");
            }
            System.out.println("");
        }
    }
    public static double[][] copyDoubleArray(double[][] source){
        double[][] destination = new double[source.length][source[0].length];
        for(int i = 0; i < source.length; i++){
            for(int j = 0; j < source[0].length; j++){
                destination[i][j] = source[i][j];
            }
        }
        return destination;
    }
}
