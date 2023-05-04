package auxillary;

import java.util.Scanner;

public interface auxFuncs {
    public static int rangeChecker(Scanner scan, int init, int upperLimit, int lowerLimit, String message) {
        //Prompt the user to choose which interface is to be accessed
        try {
            while (init > upperLimit || init < lowerLimit) {
                System.out.println(message);
                init = Integer.parseInt(scan.nextLine());
            }
        } catch (Exception e){
            System.out.println(e);
            init = upperLimit + 1;
            rangeChecker(scan, init, upperLimit, lowerLimit, message);
        }
        return init;
    }
}
