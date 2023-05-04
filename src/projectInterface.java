import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import javax.security.auth.callback.CallbackHandler;

import java.util.HashMap;

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

            // gathering and displaying the available selection of rooms given the customer's inputs
            ArrayList<String> reservation_info = chooseHotel(scan, con);
            String client_checker = "Y";

            // catching an error in the specifications given by the customer
            if (reservation_info.isEmpty() || client_checker.toUpperCase().equals("N")) {
                do {
                    System.out.println("There seems to be an issue with the specified inputs you provided. Would you like to try again with different inputs? (Y or N)");
                    client_checker = scan.nextLine();
                    client_checker.toUpperCase();
                    if (client_checker.equals("Y"))
                        reservation_info = chooseHotel(scan, con); 
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
                    reservation_info = chooseHotel(scan, con);
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
                                reservation_info = chooseHotel(scan, con); 
                            else if (!client_checker.equals("N")){
                                client_checker = "Y";
                                System.out.println("Not a valid input (valid inputs = Y or N)");
                            }
                        } while (reservation_info.isEmpty() && client_checker.equals("N"));
                    }
                } while (client_checker.equals("N")); 
            }
            
            // gathering customer information
            ArrayList<String> customer_info = knowYourCustomer(scan, con);
            client_checker = "Y"; // needs to be reset to allow for correct logical movement going forward
            
            // catching an error in the specifications given by the user if they inputted their information incorrectly
            if (customer_info.isEmpty() || client_checker.toUpperCase().equals("N")) {
                do {
                    System.out.println("There seems to be an issue with the specified inputs you provided. Would you like to try again with different inputs? (Y or N)");
                    client_checker = scan.nextLine();
                    client_checker.toUpperCase();
                    if (client_checker.equals("Y"))
                        customer_info = knowYourCustomer(scan, con); 
                    else if (!client_checker.equals("N")){
                        client_checker = "Y";
                        System.out.println("Not a valid input (valid inputs = Y or N)");
                    }
                } while (reservation_info.isEmpty() && client_checker.equals("N"));
            }
            
            if (customer_info.get(3).equals("1")){ // customer is returning
                processCustomer(scan, con, customer_info);
            }
            else if (customer_info.get(3).equals("0")){ // cusotmer is new
                processCustomer(scan, con, customer_info);
            }
            else {
                System.out.println("Ok, you somehow corrupted a very important variable to decide if a customer is returning or not. Please refrain.");
            }
            


            con.close();
        }
        scan.close();
    }

    /**
     * 
     * @param scan
     * @param con
     * @param mode (1 => returning customer) (0 => new customer)
     * @throws SQLException
     */
    public static void processCustomer(Scanner scan, Connection con, ArrayList<String> customer_info) {

        ResultSet rawCustomerInfoSet;
        ArrayList<String> customer_ids = new ArrayList<String>();
        ArrayList<Long> customer_phones = new ArrayList<Long>();

        // will be used to make sure that the two keys of the customers table will be valid at client side
        try(CallableStatement gatherCustomerKeys = con.prepareCall("begin customer_ids(?); end;")) {
            gatherCustomerKeys.registerOutParameter(1, Types.REF_CURSOR);
            gatherCustomerKeys.execute();
            rawCustomerInfoSet = (ResultSet)gatherCustomerKeys.getObject(1);

            while (rawCustomerInfoSet.next()) {
                customer_ids.add(rawCustomerInfoSet.getString(1));
                customer_phones.add(rawCustomerInfoSet.getLong(2));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        String customer_id = null;
        String uFirstName = customer_info.get(0);
        String uLastName = customer_info.get(1);
        long currPhoneNum = Long.parseLong(customer_info.get(3));
        long newPhoneNum = -1;
        int bldgNum = -1;
        String streetName = "";
        String cityName = "";
        String stateName = "";
        int zip = -1;
        long credCard = -1;

        String addAddress = null;
        String addCredCard = null;
        String changePhone = null;
        String exitFlag = null;

        if (customer_info.get(3).equals("1")) { // returning customer
            System.out.println("Hello, " + customer_info.get(0) + " " + customer_info.get(1) + "!");

            // gathering returning customer information to see if they would like to update any information
            try(CallableStatement ret_cus_info = con.prepareCall("{call display_customer_info(?,?,?,?,?,?,?,?,?,?)}"))
            {
                ret_cus_info.setString(1,customer_info.get(4)); // customer_id
                ret_cus_info.registerOutParameter(2, Types.VARCHAR); // first_name
                ret_cus_info.registerOutParameter(3, Types.VARCHAR); // last_name
                ret_cus_info.registerOutParameter(4, Types.NUMERIC); // phone_number
                ret_cus_info.registerOutParameter(5, Types.NUMERIC); // building_number
                ret_cus_info.registerOutParameter(6, Types.VARCHAR); // street
                ret_cus_info.registerOutParameter(7, Types.VARCHAR); // city
                ret_cus_info.registerOutParameter(8, Types.VARCHAR); // state
                ret_cus_info.registerOutParameter(9, Types.NUMERIC); // zip_code
                ret_cus_info.registerOutParameter(10, Types.NUMERIC); // credit_card 

                ret_cus_info.execute();

                bldgNum = ret_cus_info.getInt(5);
                streetName = ret_cus_info.getString(6);
                cityName = ret_cus_info.getString(7);
                stateName = ret_cus_info.getString(8);
                zip = ret_cus_info.getInt(9);
                credCard = ret_cus_info.getLong(10);

                // If the user does not have an address on file, ask if they'd like to enter an address to be kept on file
                if (bldgNum == (Integer)null) {
                    System.out.println("Currently you have no address on file.");
                    System.out.print("Would you like to add an address to recieve junk mail for the rest of time? (Y/N): ");
                    addAddress = scan.nextLine();

                    // error checking in case the user entered anything other than Y or N
                    if (!addAddress.toUpperCase().equals("Y") && !addAddress.toUpperCase().equals("N")){
                        do{
                            System.out.println("Currently you have no address on file.");
                            System.out.print("Would you like to add an address to recieve junk mail for the rest of time? (Y/N): ");
                            addAddress = scan.nextLine();
                        }while (!addAddress.toUpperCase().equals("Y") && !addAddress.toUpperCase().equals("N"));
                    }
                }

                // If the user would like to add an address, gather their information
                if (addAddress.equals("Y")){
                    // Prompting the user to enter their building number
                    System.out.print("Please enter your building number (< 6 chars long): ");
                    bldgNum = Integer.parseInt(scan.nextLine());

                    // Error checking user inputted building number
                    if (bldgNum > 999999 || bldgNum < 1 || bldgNum == (Integer)null){
                        do {
                            System.out.println("That number was far too large to be a building number.");
                            System.out.println("Please enter your building number in the correct format (< 6 chars long): ");
                            bldgNum = Integer.parseInt(scan.nextLine()); 
                        } while (bldgNum > 999999 || bldgNum < 1 || bldgNum == (Integer)null);
                    }
                
                    // Prompting the user to enter their street name
                    System.out.print("Please enter the street name of your address (< 15 chars long): ");
                    streetName = scan.nextLine();

                    // Error checking user inputted streetName
                    if (streetName.length() > 15 || streetName.length() < 1 || streetName == null){
                        do {
                            System.out.println("I'm sorry, we cannot store a street name that large/small. Consider truncating if too large.");
                            System.out.println("Please enter the street name of your address (< 15 chars long): ");
                            streetName = scan.nextLine(); 
                        } while (streetName.length() > 15 || streetName.length() < 1 || streetName == null);
                    }

                    // Prompting the user to enter their city name
                    System.out.println("Please enter the city associated with your address (< 15 chars long): ");
                    cityName = scan.nextLine();

                    // Error checking user inputted cityName
                    if (cityName.length() > 15 || cityName.length() < 1 || cityName == null){
                        do {
                            System.out.println("I'm sorry, we cannot store a city name that large/small. Consider truncating if too large.");
                            System.out.println("Please enter the city associated with your address (< 15 chars long): ");
                            cityName = scan.nextLine(); 
                        } while (cityName.length() > 15 || cityName.length() < 1 || cityName == null);
                    }
                
                    // Prompting the user to enter the state associated with their address
                    System.out.println("Please enter the abbreviated state associated with your address (< 2 chars long; Ex: CA for California): ");
                    stateName = scan.nextLine().toUpperCase();

                    // Error checking the inputted stateName
                    if (stateName.length() > 2 || stateName.length() < 2 || stateName == null){
                        do {
                            System.out.println("I'm sorry, we cannot store a state name that large/small. Consider truncating if too large.");
                            System.out.println("Please enter the state associated with your address (< 2 chars long): ");
                            cityName = scan.nextLine(); 
                        } while (stateName.length() > 2 || stateName.length() < 2 || stateName == null);
                    }

                    // Prompting the user to enter the zip code associated with their address
                    System.out.println("Please enter the zip code associated with your address (< 5 chars long)");
                    zip = Integer.parseInt(scan.nextLine());

                    // Error checking the inputted zip_code
                    if (zip > 99999 || zip < 0 || zip == (Integer)null){
                        do {
                            System.out.println("I'm sorry, we cannot store a city name that large/small. Consider truncating if too large.");
                            System.out.println("Please enter the city associated with your address (< 15 chars long): ");
                            cityName = scan.nextLine(); 
                        } while (zip > 99999 || zip < 0 || zip == (Integer)null);
                    }
                }

                // Asking the user if they would like to change the phone number they have on file
                if (newPhoneNum == -1) {
                    System.out.println("Would you like to change the phone number you currently have on file?");
                    System.out.println("Currently the phone number on file is " + currPhoneNum);
                    System.out.print("(Y/N): ");
                    changePhone = scan.nextLine();

                    // error checking in case the user entered anything other than Y or N
                    if (!changePhone.toUpperCase().equals("Y") && !changePhone.toUpperCase().equals("N")){
                        do{
                            System.out.println("Would you like to change the phone number you currently have on file?");
                            System.out.println("Currently the phone number on file is " + currPhoneNum);
                            System.out.print("(Y/N): ");
                            changePhone = scan.nextLine();
                        }while (!changePhone.toUpperCase().equals("Y") && !changePhone.toUpperCase().equals("N"));
                    }
                }
                
                // If the user would like to change their phone number on file, gather their information
                if (changePhone.toUpperCase().equals("Y")) {

                    // Prompting the user to enter their new phone number
                    System.out.println("Please enter the new phone number you would like to have on file.");
                    System.out.println("Please only enter the numbers WITHOUT any symbols ( ex: (), -)");
                    System.out.print("New phone number: ");

                    String sanitizer = scan.nextLine();

                    // Error checking the users input for new phone number
                    if (sanitizer.length() > 10 || sanitizer.length() < 10 || sanitizer == null) {
                        do {
                            // Prompting the user to enter their new phone number
                            System.out.println("The desired format is in form ########## with area code included.");
                            System.out.println("Please only enter the numbers WITHOUT any symbols ( ex: (), -)");
                            System.out.print("New phone number: ");
                            sanitizer = scan.nextLine();
                        } while ((sanitizer.length() > 10 || sanitizer.length() < 10 || sanitizer == null));
                    }

                    newPhoneNum = Long.parseLong(sanitizer);

                    // Checking if the phone number being inserted is already associated with another account
                    if (customer_phones.contains(newPhoneNum)) {
                        do {
                            System.out.println("That phone number already seems to be associated with an account. Please enter another phone number or type EXIT");
                            // Prompting the user to enter their new phone number
                            System.out.println("The desired format is in form ########## with area code included.");
                            System.out.println("Please only enter the numbers WITHOUT any symbols ( ex: (), -)");
                            System.out.print("Would you like to exit? Type EXIT: ");
                            exitFlag = scan.nextLine();
                            System.out.print("New phone number: ");
                            sanitizer = scan.nextLine();
                        } while((customer_phones.contains(newPhoneNum)) || (sanitizer.length() > 9 || sanitizer.length() < 9) || (!exitFlag.toUpperCase().equals("EXIT")));
                    }

                    newPhoneNum = Long.parseLong(sanitizer);
                    currPhoneNum = newPhoneNum;

                }

                // If the user does not have a credit card on file, ask if they'd like to enter a credit card to be kept on file
                if (credCard == (Long)null) {
                    System.out.println("Currently you have no credit card on file.");
                    System.out.print("Would you like to add an credit card to make payments smoother? (Y/N): ");
                    addCredCard = scan.nextLine();

                    // error checking in case the user entered anything other than Y or N
                    if (!addCredCard.toUpperCase().equals("Y") && !addCredCard.toUpperCase().equals("N")){
                        do{
                            System.out.println("Currently you have no credit card on file.");
                            System.out.print("Would you like to add a credit card to make payments smoother? (Y/N): ");
                            addCredCard = scan.nextLine();
                        }while (!addCredCard.toUpperCase().equals("Y") && !addCredCard.toUpperCase().equals("N"));
                    }
                }

                // If the user would like to add a credit card, gather their information
                if (addCredCard.equals("Y")) {
                    System.out.println("Please enter a credit card to be kept on file.");
                    System.out.println(" We can only accept credit cards of at most 16 digits.");
                    System.out.println("New credit card: ");
                    String sanitizer = scan.nextLine();

                    credCard = Long.parseLong(sanitizer);
                    
                    // Error checking the user input
                    if (sanitizer.length() < 13 || sanitizer.length() > 16 || credCard == (Long)null) {
                        do {
                            System.out.println("There seems to be an issue with your previous input.");
                            System.out.println("Please remember to only use number digits without spaces.");
                            System.out.println("New credit card: ");
                            sanitizer = scan.nextLine();
                            credCard = Long.parseLong(sanitizer);
                        } while((sanitizer.length() < 13 || sanitizer.length() > 16 || credCard == (Long)null));
                    }
                }
    
            } catch (Exception e) {
                e.printStackTrace();
            }

            // inputting existing customer information into the database
            try(CallableStatement inputInfo = con.prepareCall("{call customer_processing(?,?,?,?,?,?,?,?,?,?,?)}"))
            {
                inputInfo.setString(1,customer_info.get(4));
                inputInfo.setString(2, uFirstName);
                inputInfo.setString(3, uLastName);
                inputInfo.setLong(4, currPhoneNum);
                inputInfo.setInt(5, bldgNum);
                inputInfo.setString(6, streetName);
                inputInfo.setString(7, cityName);
                inputInfo.setString(8, stateName);
                inputInfo.setInt(9, zip);
                inputInfo.setLong(10, credCard);
                inputInfo.setInt(11, Integer.parseInt(customer_info.get(3)));

                boolean executed = inputInfo.execute();

                if (executed)
                    System.out.println("Congratulations, your account has been updated!");
                else {
                    System.out.println("There seems to have been an error in updating your account. Please retry if you would like to change your account still.");
                }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }
        else if (customer_info.get(3).equals("0")) { // new customer
            String userChoice = null;
            System.out.println("Hello, " + customer_info.get(0) + " " + customer_info.get(1) + "!");
            
            // Asking the user if they would like to add additional information to their account
            System.out.println("Would you like to add an address to your account on file?");
            System.out.print("(Y/N): ");
            userChoice = scan.nextLine();

            // Error checking the user's inputs
            if (!userChoice.toUpperCase().equals("Y") && !userChoice.toUpperCase().equals("N")) {
                do {
                    // Asking the user if they would like to add additional information to their account
                    System.out.println("Would you like to add an address to your account on file?");
                    System.out.print("(Y/N): ");
                    userChoice = scan.nextLine();
                } while (!userChoice.toUpperCase().equals("Y") && !userChoice.toUpperCase().equals("N"));
            }

            // If the user would like to add an address, gather their information
            if (userChoice.toUpperCase().equals("Y")) {
                // Prompting the user to enter their building number
                System.out.print("Please enter your building number (< 6 chars long): ");
                bldgNum = Integer.parseInt(scan.nextLine());

                // Error checking user inputted building number
                if (bldgNum > 999999 || bldgNum < 1 || bldgNum == (Integer)null){
                    do {
                        System.out.println("That number was far too large to be a building number.");
                        System.out.println("Please enter your building number in the correct format (< 6 chars long): ");
                        bldgNum = Integer.parseInt(scan.nextLine()); 
                    } while (bldgNum > 999999 || bldgNum < 1 || bldgNum == (Integer)null);
                }
            
                // Prompting the user to enter their street name
                System.out.print("Please enter the street name of your address (< 15 chars long): ");
                streetName = scan.nextLine();

                // Error checking user inputted streetName
                if (streetName.length() > 15 || streetName.length() < 1 || streetName == null){
                    do {
                        System.out.println("I'm sorry, we cannot store a street name that large/small. Consider truncating if too large.");
                        System.out.println("Please enter the street name of your address (< 15 chars long): ");
                        streetName = scan.nextLine(); 
                    } while (streetName.length() > 15 || streetName.length() < 1 || streetName == null);
                }

                // Prompting the user to enter their city name
                System.out.println("Please enter the city associated with your address (< 15 chars long): ");
                cityName = scan.nextLine();

                // Error checking user inputted cityName
                if (cityName.length() > 15 || cityName.length() < 1 || cityName == null){
                    do {
                        System.out.println("I'm sorry, we cannot store a city name that large/small. Consider truncating if too large.");
                        System.out.println("Please enter the city associated with your address (< 15 chars long): ");
                        cityName = scan.nextLine(); 
                    } while (cityName.length() > 15 || cityName.length() < 1 || cityName == null);
                }
            
                // Prompting the user to enter the state associated with their address
                System.out.println("Please enter the abbreviated state associated with your address (< 2 chars long; Ex: CA for California): ");
                stateName = scan.nextLine().toUpperCase();

                // Error checking the inputted stateName
                if (stateName.length() > 2 || stateName.length() < 2 || stateName == null){
                    do {
                        System.out.println("I'm sorry, we cannot store a state name that large/small. Consider truncating if too large.");
                        System.out.println("Please enter the state associated with your address (< 2 chars long): ");
                        cityName = scan.nextLine(); 
                    } while (stateName.length() > 2 || stateName.length() < 2|| stateName == null);
                }

                // Prompting the user to enter the zip code associated with their address
                System.out.println("Please enter the zip code associated with your address (< 5 chars long)");
                zip = Integer.parseInt(scan.nextLine());

                // Error checking the inputted zip_code
                if (zip > 99999 || zip < 0 || zip == (Integer)null){
                    do {
                        System.out.println("I'm sorry, we cannot store a city name that large/small. Consider truncating if too large.");
                        System.out.println("Please enter the city associated with your address (< 15 chars long): ");
                        cityName = scan.nextLine(); 
                    } while (zip > 99999 || zip < 0 || zip == (Integer)null);
                }
            }
        
            // Asking the user if they would like to add additional information to their account
            System.out.println("Would you like to add a credit card to your account on file?");
            System.out.print("(Y/N): ");
            userChoice = scan.nextLine();

            // Error checking the user's inputs
            if (!userChoice.toUpperCase().equals("Y") && !userChoice.toUpperCase().equals("N")) {
                do {
                    // Asking the user if they would like to add additional information to their account
                    System.out.println("Would you like to add a credit card to your account on file?");
                    System.out.print("(Y/N): ");
                    userChoice = scan.nextLine();
                } while (!userChoice.toUpperCase().equals("Y") && !userChoice.toUpperCase().equals("N"));
            }
            
            // Gather credit card information from user if they would like to add a credit card
            if (userChoice.toUpperCase().equals("Y")) {
                System.out.println("Please enter a credit card to be kept on file.");
                System.out.println(" We can only accept credit cards of at most 16 digits.");
                System.out.println("New credit card: ");
                String sanitizer = scan.nextLine();

                credCard = Long.parseLong(sanitizer);
                
                // Error checking the user input
                if (sanitizer.length() < 13 || sanitizer.length() > 16 || credCard == (Long)null) {
                    do {
                        System.out.println("There seems to be an issue with your previous input.");
                        System.out.println("Please remember to only use number digits without spaces.");
                        System.out.println("New credit card: ");
                        sanitizer = scan.nextLine();
                        credCard = Long.parseLong(sanitizer);
                    } while((sanitizer.length() < 13 || sanitizer.length() > 16 || credCard == (Long)null));
                }
            }
             
            // creating the customer_id for a new customer
            long count = 7000000000L;
            do {
                if(!customer_ids.contains(Long.toString(count))) {
                    customer_id = Long.toString(count);
                }
            }while(count < 8000000000L || customer_ids.contains(Long.toString(count)));

            // inputting existing customer information into the database
            try(CallableStatement inputInfo = con.prepareCall("{call customer_processing(?,?,?,?,?,?,?,?,?,?,?)}"))
            {
                inputInfo.setString(1, customer_id);
                inputInfo.setString(2, uFirstName);
                inputInfo.setString(3, uLastName);
                inputInfo.setLong(4, currPhoneNum);
                inputInfo.setInt(5, bldgNum);
                inputInfo.setString(6, streetName);
                inputInfo.setString(7, cityName);
                inputInfo.setString(8, stateName);
                inputInfo.setInt(9, zip);
                inputInfo.setLong(10, credCard);
                inputInfo.setInt(11, Integer.parseInt(customer_info.get(3)));

                boolean executed = inputInfo.execute();

                if (executed)
                    System.out.println("Congratulations, your account has been created!");
                else {
                    System.out.println("There seems to have been an error in creating your account. Please retry if you would like to change your account still.");
                }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Determines if a customer is a returning or new customer
     * @param scan
     * @param con
     * @return arraylist w/ first_name, last_name, phone_number, a status_code (1 = existing customer) (0 = new customer), and customer_id 
     */
    public static ArrayList<String> knowYourCustomer(Scanner scan, Connection con) {

        ResultSet rawCustomerInfoSet;
        ArrayList<String> customer_ids = new ArrayList<String>();
        ArrayList<Long> customer_phones = new ArrayList<Long>();

        // will be used to make sure that the two keys of the customers table will be valid at client side
        try(CallableStatement gatherCustomerKeys = con.prepareCall("begin customer_ids(?); end;")) {
            gatherCustomerKeys.registerOutParameter(1, Types.REF_CURSOR);
            gatherCustomerKeys.execute();
            rawCustomerInfoSet = (ResultSet)gatherCustomerKeys.getObject(1);

            while (rawCustomerInfoSet.next()) {
                customer_ids.add(rawCustomerInfoSet.getString(1));
                customer_phones.add(rawCustomerInfoSet.getLong(2));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        // initializing some variables we'll need later
        ArrayList<String> resultList = new ArrayList<String>();
        String customer_id;
        String uFname;
        String uLname;
        String phoneNum;
        int status_code;
        int existingCustomer;

        // just to make the user know what's going on
        System.out.println("Congratulations on selecting the hotel you'd like to stay in with us!");
        System.out.println("Next, we will need to collect your information to place your name under the reservation.");

        // Prompting the user to enter personally identifiable information
        System.out.println("Please enter your first and last name as you would like them stored and displayed.");
        System.out.print("First Name: ");
        uFname = scan.nextLine();

        System.out.print("\nLast Name: ");
        uLname = scan.nextLine();

        System.out.println("\nAdditionally, please enter the phone number you would like to be contacted with in the event of updates to your reservation.");
        System.out.print("Phone Number: ");
        phoneNum = scan.nextLine();

        // Error checking the users input for new phone number
        if (phoneNum.length() > 10 || phoneNum.length() < 10) {
            do {
                // Prompting the user to enter their new phone number
                System.out.println("The desired format is in form ########## with area code included.");
                System.out.println("Please only enter the numbers WITHOUT any symbols ( ex: (), -)");
                System.out.print("Phone number: ");
                phoneNum = scan.nextLine();
                System.out.println("phoneNum.length(): " + phoneNum.length());
            } while ((phoneNum.length() > 10 || phoneNum.length() < 10));
        }
        long phoneNumLong = Long.parseLong(phoneNum);

        // calling the is_a_customer procedure to gather customer information if present
        try(CallableStatement kyc = con.prepareCall("{call is_a_customer(?,?,?,?,?,?)}"))
        {
            kyc.setString(1, uFname); // also an out
            kyc.setString(2, uLname); // also an out
            kyc.setLong(3, phoneNumLong); // also an out
            kyc.setString(4, null); // only an in 
            
            kyc.registerOutParameter(1, Types.VARCHAR); // First Name
            kyc.registerOutParameter(2, Types.VARCHAR); // Last Name
            kyc.registerOutParameter(3, Types.NUMERIC); // Phone Number
            kyc.registerOutParameter(5, Types.NUMERIC); // status code
            kyc.registerOutParameter(6, Types.VARCHAR); // customer_id
            
            kyc.execute();

            status_code = kyc.getInt(5);
            customer_id = kyc.getString(6);

            if (status_code == 0)
                existingCustomer = 0;
            else {
                existingCustomer = 1;
            }
    
            resultList.add(uFname);
            resultList.add(uLname);
            resultList.add(phoneNum);
            resultList.add(Integer.toString(existingCustomer));
            resultList.add(customer_id);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return resultList;
    }
 
    /**
     * gathers user input for desired city and arrival/departure dates.
     * @param scan
     * @param con
     * @return arraylist: contains address of hotel and respective room type the customer would like to stay in w/ arrival and depart days and hotel_id 
     */
    public static ArrayList<String> chooseHotel(Scanner scan, Connection con) {
        int arrivalYear = -1;
        int arrivalMonth = -1;
        int arrivalDay = -1;
        int departYear = -1;
        int departMonth = -1;
        int departDay = -1;
        String city = "ethereum";
        ArrayList<String> validArr = new ArrayList<String>();
        ArrayList<String> resultList = new ArrayList<String>();

        try{

            // calling the hotel_cities procedure to gather an arrayList of all valid cities
            try(CallableStatement gather_hotel_cities = con.prepareCall("begin hotel_cities(?); end;"))
            {
                ResultSet valid_hotel_cities;
                gather_hotel_cities.registerOutParameter(1, Types.REF_CURSOR);
                gather_hotel_cities.execute();

                valid_hotel_cities = (ResultSet)gather_hotel_cities.getObject(1);
                // populating arraylist of cities with hotels in them
                while(valid_hotel_cities.next()){
                    validArr.add(valid_hotel_cities.getString(1));
                }
            }
            
            // beginning of interface interaction
            System.out.println("Hello, valued customer. Please enter the city you would like to book a stay in.");
            System.out.println("Here are the following cities in which we have hotels:\n");

            // displaying arraylist of cities with hotels in them
            for (int i = 0; i < validArr.size(); i++)
                System.out.println(validArr.get(i));
            System.out.println("\nPlease enter the city you would like to stay in below and remember to only select from the above choices");
            // prompting the user to enter a city where they would like to stay
            System.out.print("City: ");
            city = scan.nextLine();

            System.out.println(); // just so the command line doesn't become too crowded
            // checking if the specified city is in the set of valid hotel cities
            if (!validArr.contains(city)) {
                do {
                    System.out.println ("Please choose only from the valid cities presented again below:");

                    for (int i = 0; i < validArr.size(); i++)
                        System.out.println(validArr.get(i));
               
                    System.out.print("City: ");
                    city = scan.nextLine();
                    System.out.println(); // just so the console doesn't become too crowded
                } while (!validArr.contains(city));
            }

            ArrayList<String> hotelsInCity = new ArrayList<String>();
            ArrayList<String> cityHotelIds = new ArrayList<String>();

            // taking the city the user inputted to ask the user which hotel they would like to stay in
            try(CallableStatement hotels_in_city = con.prepareCall("begin hotels_per_city(?,?); end;")){

                ResultSet hotelsInCitySet;
                hotels_in_city.setString(1,city);
                hotels_in_city.registerOutParameter(2,Types.REF_CURSOR);
                hotels_in_city.execute();

                hotelsInCitySet = (ResultSet)hotels_in_city.getObject(2);
                while(hotelsInCitySet.next()) {
                    String hotel_address = Integer.toString(hotelsInCitySet.getInt("building_number")) + " " + hotelsInCitySet.getString("street") + " " + hotelsInCitySet.getString("city") + " " + hotelsInCitySet.getString("home_state") + " " + Integer.toString(hotelsInCitySet.getInt("zip_code"));
                    //System.out.println("hotel_address: " + hotel_address);
                    hotelsInCity.add(hotel_address);
                    cityHotelIds.add(hotelsInCitySet.getString("hotel_id"));
                }
            }
            
            int clientChoice = -1;
            String hotelIdChosen = "";
            String hotelAddressChosen = "";
            
            // ask the customer which hotel they would like to stay in 
            while(clientChoice < 0 || clientChoice > hotelsInCity.size()) {
                System.out.println("Enter the number associated with the hotel you would like to book a reservation in.\n");

                for (int i = 0; i < hotelsInCity.size(); i++) {
                    System.out.println(i + ".\t" + hotelsInCity.get(i));
                }

                clientChoice = Integer.parseInt(scan.nextLine());
            }
            hotelIdChosen = cityHotelIds.get(clientChoice);
            hotelAddressChosen = hotelsInCity.get(clientChoice);

            // prompting the user for the year that they would like to arrive and checking if valid
            arrivalYear = date_checker(scan, 1, arrivalYear, 0, 0, 0,0,0);
            System.out.println("Arrival Year info gathered\n");
            // prompting the user for the month that they would like to arrive and checking if valid
            arrivalMonth = date_checker(scan,2,arrivalMonth, 0,0,0,0,0);
            System.out.println("Arrival Month info gathered\n");
            // prompting the user for the day that they would like to arrive and checking if valid
            arrivalDay = date_checker(scan, 3, arrivalDay, arrivalYear, arrivalMonth,0,0,0);
            System.out.println("Arrival Day info gathered\n");
            // prompting the user for the year that they would like to depart and checking if valid
            departYear = date_checker(scan, 4, departYear, arrivalYear, arrivalMonth, arrivalDay, 0,0);
            System.out.println("Depart Year info gathered\n");
            int year_diff = departYear - arrivalYear;
            // prompting the user for the month that they would like to depart and checking if valid
            departMonth = date_checker(scan, 5, departMonth, arrivalYear, arrivalMonth, arrivalDay, (departYear-arrivalYear), 0);
            int month_diff = departMonth - arrivalMonth;
            System.out.println("Depart Month info gathered\n");
            // prompting the user for the day that they would like to depart and checking if valid
            departDay = date_checker(scan, 6, departDay, arrivalYear, arrivalMonth, arrivalDay, year_diff, month_diff);
            System.out.println("Depart Day info gathered\n");

            // Converting the date inputs into sql date types to be passed to the stored procedure
            String arrivalMonthString = Integer.toString(arrivalMonth);
            if (arrivalMonthString.length() < 2)
                arrivalMonthString = "0" + arrivalMonthString;
            
            String arrivalDayString = Integer.toString(arrivalDay);
            if (arrivalDayString.length() < 2)
                arrivalDayString = "0" + arrivalDayString;

            String arrivalYearString = Integer.toString(arrivalYear);
            
            String arrivalDateString = arrivalYearString + "-" + arrivalMonthString + "-" + arrivalDayString;
            Date arrivalDateLiteral = Date.valueOf(arrivalDateString);

            String departMonthString = Integer.toString(departMonth);
            if (departMonthString.length() < 2)
                departMonthString = "0" + departMonthString;
            
            String departDayString = Integer.toString(departDay);
            if (departDayString.length() < 2)
                departDayString = "0" + departDayString;
            
            String departYearString = Integer.toString(departYear);

            String departDateString = departYearString + "-" + departMonthString + "-" + departDayString;
            Date departDateLiteral = Date.valueOf(departDateString);

            // initializing variables to be declared in the try block below
            String desiredRoom = "";
            ArrayList<String> aRoomTypes = new ArrayList<String>();
            // calling the available_hotel procedure to gather an arrayList of all hotel rooms available in the city specified
            try(CallableStatement gather_hotel_rooms = con.prepareCall("begin available_hotels2(?,?,?,?,?,?,?,?,?); end;"))
            {
                int userChoice = -1;
                gather_hotel_rooms.setString(1, hotelIdChosen);
                gather_hotel_rooms.setDate(2, arrivalDateLiteral);
                gather_hotel_rooms.setDate(3, departDateLiteral);
                gather_hotel_rooms.registerOutParameter(4, Types.NUMERIC);
                gather_hotel_rooms.registerOutParameter(5, Types.NUMERIC);
                gather_hotel_rooms.registerOutParameter(6, Types.NUMERIC);
                gather_hotel_rooms.registerOutParameter(7, Types.NUMERIC);
                gather_hotel_rooms.registerOutParameter(8, Types.NUMERIC);
                gather_hotel_rooms.registerOutParameter(9, Types.NUMERIC);
                gather_hotel_rooms.execute();
                
                // gathering available rooms for the chosen hotel
                int aSingleRooms = gather_hotel_rooms.getInt(4);
                int aDoubleRooms = gather_hotel_rooms.getInt(5);
                int aDeluxeRooms = gather_hotel_rooms.getInt(6);
                int aStudioRooms = gather_hotel_rooms.getInt(7);
                int aPresRooms = gather_hotel_rooms.getInt(8);
                int aSuiteRooms = gather_hotel_rooms.getInt(9);

                // asking the customer which of the rooms they would like to stay in
                if (aSingleRooms > 0) { aRoomTypes.add("Single"); }
                if (aDoubleRooms > 0) { aRoomTypes.add("Double"); }
                if (aDeluxeRooms > 0) { aRoomTypes.add("Deluxe"); }
                if (aStudioRooms > 0) { aRoomTypes.add("Studio"); }
                if (aPresRooms > 0) { aRoomTypes.add("Presidential"); }
                if (aSuiteRooms > 0) { aRoomTypes.add("Suite"); }
                
                while(userChoice < 0 || userChoice > aRoomTypes.size() - 1){
                    System.out.println("Please enter the number associated with the room you would like to stay in.");
                    System.out.println("Only room types with rooms available for the duration of your stay are displayed.");

                    for (int i = 0; i < aRoomTypes.size(); i++) 
                        System.out.println(i + ".\t" + aRoomTypes.get(i));
                    
                    userChoice = Integer.parseInt(scan.nextLine());
                }
                desiredRoom = aRoomTypes.get(userChoice);
            }   
            resultList.add(hotelAddressChosen);
            resultList.add(desiredRoom);
            resultList.add(arrivalDateString);
            resultList.add(departDateString);
            resultList.add(hotelIdChosen);
            return resultList;
        }
        catch (InputMismatchException e){
            System.out.println("Input mismatch exception triggered in gather_available_hotels()");
            return resultList;
        } catch (SQLException e) {
            System.out.println("SQL exception triggered in gather_available_hotels()");
            return resultList;
        } catch (Exception e) {
            System.out.println("Exception triggered in gather_available_hotels");
            return resultList;
        }
    }

    /**
     * mode = 1 --> arrival_year
     *      does not require aux*
     * mode = 2 --> arrival_month
     *      does not require aux*
     * mode = 3 --> arrival_day
     *      requires auxMonth and auxYear
     * mode = 4 --> depart_year
     *      requires all aux* attributes
     * mode = 5 --> depart_month
     *      auxMonth needs to be arrivalMonth to check for same year reservations
     *      requires all aux* attributes
     * mode = 6 --> depart_day
     *      requires all aux* attributes
     * @param scan
     * @param mode
     * @param input
     * @param auxMonth only needed for checking day (30/31/28 or (29 leap year) days in a month)
     * @param auxYear only needed for checking day (leap year or not) or depart_year (>= arrival_year)
     * @param auxDay only needed for checking departure day and displaying dates
     * @return an input validated year/month/day value depending on the given mode
     */
    public static int date_checker(Scanner scan, int mode, int input, int auxYear, int auxMonth, int auxDay, int year_diff, int month_diff){
        if (mode == 1) {
           // checking if the arrival year is valid
           if (input < 2023) {
               do{
                   System.out.println("Enter your arrival year below in the format YYYY");
                   System.out.println("Please select a valid arrival year (arrival_year >= 2023)");
                   System.out.print("Arrival Year: ");
                   input = Integer.parseInt(scan.nextLine());
               } while (input < 2023);
            } 
            return input;
        }
        else if (mode == 2) {
            // checking if the arrival month is valid
            if (input > 13 || input < 1) {
                do{
                    System.out.println("Enter your arrival month below in the format MM");
                    System.out.println("Please select a valid arrival month (arrival_month < 13)");
                    System.out.print("Arrival Month: ");
                    input = Integer.parseInt(scan.nextLine());
                } while (input > 13 || input < 1);
            }
            return input;
        }
        else if (mode == 3){
            int greatestDay = 0;
            boolean leap_year = false;
            // checking if the arrival day is valid
            if (((auxYear % 4 == 0) && (auxYear % 100 != 0)) || (auxYear%400 == 0)) 
                leap_year = true;
            if (auxMonth == 1 || auxMonth == 3 || auxMonth == 5 || auxMonth == 7 || auxMonth == 8 || auxMonth == 10 || auxMonth == 12) 
                greatestDay = 31;
            else if (auxMonth == 2 && leap_year == true) 
                greatestDay = 29;
            else if (auxMonth == 2 && leap_year != true)
                greatestDay = 28;
            else
                greatestDay = 30;
            do{
                System.out.println("Enter your arrival day below in the format DD.");
                System.out.println("Please select a valid arrival day");
                System.out.print("Arrival Day: ");
                input = Integer.parseInt(scan.nextLine());
            } while (input > greatestDay || input < 1);
            return input;
        }
        else if (mode == 4) {
            // checking if the departure year is valid
            if (input < auxYear) {
                do{
                    System.out.println("Enter your departure year below in the format YYYY");
                    System.out.println("Please select a valid departure year (departure_year >= arrival_year)");
                    System.out.println("For reference, your arrival date is (in format DD/MM/YYYY): " + auxDay + "/" + auxMonth
                        + "/" + auxYear);
                    System.out.print("Departure Year: ");
                    input = Integer.parseInt(scan.nextLine());
                } while (input < auxYear);
            } 
            return input;
        }
        else if (mode == 5) {
            ArrayList<Integer> nonValidMonths = new ArrayList<Integer>();
            // checking if the departure month is valid
            if (year_diff < 1){ // if true, then year must be same year
                for (int i = 1; i < auxMonth;  i++) // add months preceeding arrivalMonth mapped at auxMonth
                    nonValidMonths.add(i);
            }
            do {
                System.out.println("Enter your departure year below in the format MM.");
                System.out.println("Please select a valid departure month (must be after your arrival if in same year");
                System.out.println("For reference your arrival date is (in format DD/MM/YYYY): " + auxDay + "/" + auxMonth
                + "/" + auxYear);
                System.out.print("Departure Month: ");
                input = Integer.parseInt(scan.nextLine());
            } while (nonValidMonths.contains(input) || input < 1);   
            return input;          
        } 
        else if (mode == 6) {
            ArrayList<Integer> nonValidDays = new ArrayList<Integer>();
            // checking if the departure day is valid
            if (year_diff == 0 && month_diff == 0){ // arrival and depart on same month of same year
                for (int i = 1; i < auxDay; i++)
                    nonValidDays.add(i);
                int greatestDay = 0;
                boolean leap_year = false;
                // checking if the arrival day is valid
                if (((auxYear % 4 == 0) && (auxYear % 100 != 0)) || (auxYear%400 == 0)) 
                    leap_year = true;
                if (auxMonth == 1 || auxMonth == 3 || auxMonth == 5 || auxMonth == 7 || auxMonth == 8 || auxMonth == 10 || auxMonth == 12) 
                    greatestDay = 31;
                else if (auxMonth == 2 && leap_year == true) 
                    greatestDay = 29;
                else if (auxMonth == 2 && leap_year != true)
                    greatestDay = 28;
                else
                    greatestDay = 30;
                for (int i = 1; i <= auxDay; i++) 
                    nonValidDays.add(i);
                do{
                    System.out.println("Enter your departure day below in the format DD.");
                    System.out.println("Please select a valid departure day");
                    System.out.println("For reference your arrival date is (in format DD/MM/YYYY): " + auxDay + "/" + auxMonth
                    + "/" + auxYear);
                    System.out.print("Departure Day: ");
                    input = Integer.parseInt(scan.nextLine());
                } while (nonValidDays.contains(input) || input < 1 || input > greatestDay);
                return input;
            }
            else {
                int greatestDay = 0;
                boolean leap_year = false;
                int newMonth = auxMonth + month_diff;
                // checking if the arrival day is valid
                if (((auxYear % 4 == 0) && (auxYear % 100 != 0)) || (auxYear%400 == 0)) 
                    leap_year = true;
                if (newMonth == 1 || newMonth == 3 || newMonth == 5 || newMonth == 7 || newMonth == 8 || newMonth == 10 || newMonth == 12) 
                    greatestDay = 31;
                else if (newMonth == 2 && leap_year == true) 
                    greatestDay = 29;
                else if (newMonth == 2 && leap_year != true)
                    greatestDay = 28;
                else
                    greatestDay = 30;
                do{
                    System.out.println("Enter your departure day below in the format DD.");
                    System.out.println("Please select a valid departure day");
                    System.out.println("For reference your arrival date is (in format DD/MM/YYYY): " + auxDay + "/" + auxMonth
                    + "/" + auxYear);
                    System.out.print("Departure Day: ");
                    input = Integer.parseInt(scan.nextLine());
                } while (input > greatestDay || input < 1);
                return input;
            }
        }
        return input;
    }
}