import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.time.*;
import java.time.format.DateTimeParseException;
import customer.knowYourCustomer;
import customer.processCustomer;
import auxillary.auxFuncs;

public class projectInterface {
    public static void main (String[] arg) 
    throws SQLException, IOException, java.util.InputMismatchException, java.lang.ClassNotFoundException {

        String username = "vjc225";
        String password = "P880886356";
        Scanner scan = new Scanner(System.in);
        try {
            //System.out.println("Enter your username: ");
            //username = scan.nextLine();
            //System.out.println("Enter your password: ");
            //password = scan.nextLine();

            System.out.println("username: " + username);
            System.out.println("password: " + password);
        } 
        catch (InputMismatchException e) {
            System.out.println("Username and password should both be of type string");
        }
        
        try (
            Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", username, password);
        ) {
            System.out.println("connection successfully made.");

            String message = "Please enter the number associated with the interface you would like to access.\n0:\tCustomer\n1:\tFront Desk Agent\n2:\tHousekeeping";

            int userC = auxillary.auxFuncs.rangeChecker(scan, 5, 2, 0, message);

            if (userC == 0){ //customer_interface
                customer.customerInterface.customerInterface(scan, con);
            }
            else if (userC == 1) { // front_desk_interface

            }
            else if (userC == 2) { // housekeeping interface

            }
            else {
                System.out.println("Bruh");
            }

            

            

            con.close();
        }
        scan.close();
    }


}