package customer;

import customer.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Scanner;

public class customerInterface {
    public static void customerInterface(Scanner scan, Connection con){
        // gathering and displaying the available selection of rooms given the customer's inputs
        ArrayList<String> reservation_info = chooseHotel.chooseHotel(scan, con);
        String client_checker = "Y";

        // catching an error in the specifications given by the customer
        if (reservation_info.isEmpty() || client_checker.toUpperCase().equals("N")) {
            do {
                System.out.println("There seems to be an issue with the specified inputs you provided. Would you like to try again with different inputs? (Y or N)");
                client_checker = scan.nextLine();
                client_checker.toUpperCase();
                if (client_checker.equals("Y"))
                    reservation_info = chooseHotel.chooseHotel(scan, con); 
                else if (!client_checker.equals("N")){
                    client_checker = "Y";
                    System.out.println("Not a valid input (valid inputs = Y or N)");
                }
            } while (reservation_info.isEmpty() && client_checker.equals("N"));
        }

        // if the user would not like to fix their bad inputs, they probably want to just leave, so i will let them
        if (reservation_info.isEmpty()) {
            System.out.println("Ok, have a nice day! :)");
            System.exit(1);
        }

        // verifying that the customer is satisfied with the information they provided
        System.out.println("\nPlease verify that you entered your information regarding your reservation to your satisfaction.\n");
        System.out.println("Hotel Address: " + reservation_info.get(0));
        System.out.println("Room Type: " + reservation_info.get(1));
        System.out.println("Arrival Date (yyyy-MM-DD): " + reservation_info.get(2));
        System.out.println("Departure Date (yyyy-MM-DD): " + reservation_info.get(3));

        System.out.println("\nAre you satisfied with the above information?");
        System.out.print("(Y/N): ");
        client_checker = scan.nextLine();

        // Repeating the chooseHotel() call and error checking the inputs of the user
        if (client_checker.toUpperCase().equals("N")) {
            do {
                reservation_info = chooseHotel.chooseHotel(scan, con);
                // verifying that the customer is satisfied with the information they provided
                System.out.println("\nPlease verify that you entered your information regarding your reservation to your satisfaction\n.");
                System.out.println("Hotel Address: " + reservation_info.get(0));
                System.out.println("Room Type: " + reservation_info.get(1));
                System.out.println("Arrival Date (yyyy-MM-DD): " + reservation_info.get(2));
                System.out.println("Departure Date (yyyy-MM-DD): " + reservation_info.get(3));

                System.out.println("\nAre you satisfied with the above information?");
                System.out.println("(Y/N): ");
                client_checker = scan.nextLine();

                // catching an error in the specifications given by the customer
                if (reservation_info.isEmpty() && client_checker.equals("N")) {
                    do {
                        System.out.println("There seems to be an issue with the specified inputs you provided. Would you like to try again with different inputs? (Y or N)");
                        client_checker = scan.nextLine();
                        client_checker.toUpperCase();
                        if (client_checker.equals("Y"))
                            reservation_info = chooseHotel.chooseHotel(scan, con); 
                        else if (!client_checker.equals("N")){
                            client_checker = "Y";
                            System.out.println("Not a valid input (valid inputs = Y or N)");
                        }
                    } while (reservation_info.isEmpty() && client_checker.equals("N"));
                }
            } while (client_checker.equals("N")); 
        }
        
        // gathering customer information
        ArrayList<String> customer_info = knowYourCustomer.knowYourCustomer(scan, con);
        client_checker = "Y"; // needs to be reset to allow for correct logical movement going forward
        
        // catching an error in the specifications given by the user if they inputted their information incorrectly
        if (customer_info.isEmpty() || client_checker.toUpperCase().equals("N")) {
            do {
                System.out.println("There seems to be an issue with the specified inputs you provided. Would you like to try again with different inputs? (Y or N)");
                client_checker = scan.nextLine();
                client_checker.toUpperCase();
                if (client_checker.equals("Y"))
                    customer_info = knowYourCustomer.knowYourCustomer(scan, con); 
                else if (!client_checker.equals("N")){
                    client_checker = "Y";
                    System.out.println("Not a valid input (valid inputs = Y or N)");
                }
            } while (reservation_info.isEmpty() && client_checker.equals("N"));
        }

        System.out.println("customer_info: ");
        for (int i = 0; i < customer_info.size(); i++) {
            System.out.println(customer_info.get(i));
        }
        
        processCustomer.processCustomer(scan, con, customer_info);
    }
}
