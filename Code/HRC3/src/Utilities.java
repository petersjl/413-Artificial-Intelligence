import java.io.*;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedList;

public class Utilities {
    private static final String stored_plan_path = "stored_plans.txt";

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

    public static void readPlans() {
        try {
            File filePlan=new File(stored_plan_path);
            boolean isNewFile = filePlan.createNewFile();

            if (isNewFile){
                Robot.allPlans = new HashMap<>();

            } else {
                FileInputStream fis=new FileInputStream(filePlan);
                ObjectInputStream ois=new ObjectInputStream(fis);
                HashMap<String,LinkedList<Action>> storedPlans=(HashMap<String,LinkedList<Action>>)ois.readObject();
                ois.close();
                fis.close();

                Robot.allPlans = storedPlans;
            }
        } catch(Exception ignored) {
        }
    }

    public static void writePlans() {
        HashMap<String, LinkedList<Action>> plans = (HashMap<String, LinkedList<Action>>) Robot.allPlans;
        try {

            File planFile=new File(stored_plan_path);
            FileOutputStream fos=new FileOutputStream(planFile);
            ObjectOutputStream oos=new ObjectOutputStream(fos);

            oos.writeObject(plans);
            oos.flush();
            oos.close();
            fos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
