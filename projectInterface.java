import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.time.*;
import java.time.format.DateTimeParseException;


public class projectInterface {
    public static void main (String[] arg) 
    throws SQLException, IOException, java.util.InputMismatchException, java.lang.ClassNotFoundException {

        String username = "vjc225";
        String password = "P880886356";
        Scanner scan = new Scanner(System.in);
        try {
            //System.out.println("Enter your username: ");
            //username = scan.nextLine().trim();
            //System.out.println("Enter your password: ");
            //password = scan.nextLine().trim();

            System.out.println("username: " + username);
            System.out.println("password: " + password);
        } 
        catch (InputMismatchException e) {
            System.out.println("Username and password should both be of type string");
        }
        
        try (
            Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", username, password);
        ) {
            System.out.println("connection successfully made.\n\n");

            int userC = -1;
            do {
                System.out.println("Welcome to the Hotel California!");
                String message = "Please enter the number associated with the interface you would like to access.\n0:\tCustomer\n1:\tFront Desk Agent\n2:\tHousekeeping\n3:\tExit\n\n\nChoice: ";
                userC = rangeChecker(scan, 5, 3, 0, message);

                if (userC == 0){ //customer_interface
                    int status = -1;
                    status = customerInterface(scan, con);               
                }
                else if (userC == 1) { // front_desk_interface

                }
                else if (userC == 2) { // housekeeping interface

                }
            } while(userC != 3);
            
            

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
     * @return arrayList:
     * First Name                       0
     * Last Name                        1
     * Phone Number                     2
     * null -- deleted status_code      3
     * customer_id                      4
     * cred_card                        5
     * 
     */
    public static ArrayList<String> processCustomer(Scanner scan, Connection con, ArrayList<String> customer_info) {

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

        String customer_id = "-1";
        String uFirstName = customer_info.get(0);
        String uLastName = customer_info.get(1);
        long currPhoneNum = Long.parseLong(customer_info.get(2));
        long newPhoneNum = -1;
        int bldgNum = -1;
        String streetName = "";
        String cityName = "";
        String stateName = "";
        int zip = -1;
        long credCard = -1;

        String addAddress = "";
        String addCredCard = "";
        String changePhone = "";
        String exitFlag = "";

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

                // System.out.println("customer_id: " + customer_info.get(4));
                // System.out.println("building_number: " + bldgNum);
                // System.out.println("street_name: " + streetName);
                // System.out.println("city_name: " + cityName);
                // System.out.println("stateName: " + stateName);
                // System.out.println("zip_code: " + zip);
                // System.out.println("credit_card: " + credCard);

                System.out.println();

                // Display customer information 
                System.out.println("We were able to find an account you had with us in the past.\n");
                System.out.println("Name: " + customer_info.get(0) + " "+ customer_info.get(1));
                System.out.println("Phone Number: " + currPhoneNum);
                if (bldgNum > 0)
                    System.out.println("Address: " + bldgNum + " " + streetName + " " + cityName + " " + stateName + " " + zip);
                else {
                    System.out.println("Address: ");
                }
                if (credCard > 0)
                    System.out.println("Credit Card: " + credCard);
                else {
                    System.out.println("Credit Card: ");
                }
                System.out.println();

                // If the user does not have an address on file, ask if they'd like to enter an address to be kept on file
                if (bldgNum < 0) {
                    System.out.println("Currently you have no address on file.");
                    System.out.print("Would you like to add an address to recieve junk mail for the rest of time? (Y/N): ");
                    addAddress = scan.nextLine().trim();

                    // error checking in case the user entered anything other than Y or N
                    if (!addAddress.toUpperCase().equals("Y") && !addAddress.toUpperCase().equals("N")){
                        do{
                            System.out.println("Currently you have no address on file.");
                            System.out.print("Would you like to add an address to recieve junk mail for the rest of time? (Y/N): ");
                            addAddress = scan.nextLine().trim();
                        }while (!addAddress.toUpperCase().equals("Y") && !addAddress.toUpperCase().equals("N"));
                    }
                } else {
                    System.out.println("Would you like to update your address on file?");
                    System.out.println("Current address: " + bldgNum + " " + streetName + " " + cityName + " " + stateName + " " + zip);
                    System.out.print("(Y/N): ");
                    addAddress = scan.nextLine().trim();

                    // error checking in case the user entered anything other than Y or N
                    if (!addAddress.toUpperCase().equals("Y") && !addAddress.toUpperCase().equals("N")){
                        do{
                            System.out.print("Would you like to update your address on file? (Y/N): ");
                            System.out.println("Current address: " + bldgNum + " " + streetName + " " + cityName + " " + stateName + " " + zip);
                            addAddress = scan.nextLine().trim();
                        }while (!addAddress.toUpperCase().equals("Y") && !addAddress.toUpperCase().equals("N"));
                    }
                }

                // If the user would like to add an address, gather their information
                if (addAddress.equals("Y")){
                    // Prompting the user to enter their building number
                    System.out.print("Please enter your building number (< 5 chars long): ");
                    bldgNum = Integer.parseInt(scan.nextLine().trim());

                    // Error checking user inputted building number
                    if (bldgNum > 99999 || bldgNum < 1){
                        do {
                            System.out.println("That number was far too large to be a building number.");
                            System.out.print("Please enter your building number in the correct format (< 5 chars long): ");
                            bldgNum = Integer.parseInt(scan.nextLine().trim()); 
                        } while (bldgNum > 99999 || bldgNum < 1);
                    }
                
                    // Prompting the user to enter their street name
                    System.out.print("Please enter the street name of your address (< 15 chars long): ");
                    streetName = scan.nextLine().trim();

                    // Error checking user inputted streetName
                    if (streetName.length() > 15 || streetName.length() < 1 || streetName == null){
                        do {
                            System.out.println("I'm sorry, we cannot store a street name that large/small. Consider truncating if too large.");
                            System.out.print("Please enter the street name of your address (< 15 chars long): ");
                            streetName = scan.nextLine().trim(); 
                        } while (streetName.length() > 15 || streetName.length() < 1 || streetName == null);
                    }

                    // Prompting the user to enter their city name
                    System.out.print("Please enter the city associated with your address (< 15 chars long): ");
                    cityName = scan.nextLine().trim();

                    // Error checking user inputted cityName
                    if (cityName.length() > 15 || cityName.length() < 1 || cityName == null){
                        do {
                            System.out.println("I'm sorry, we cannot store a city name that large/small. Consider truncating if too large.");
                            System.out.print("Please enter the city associated with your address (< 15 chars long): ");
                            cityName = scan.nextLine().trim(); 
                        } while (cityName.length() > 15 || cityName.length() < 1 || cityName == null);
                    }
                
                    // Prompting the user to enter the state associated with their address
                    System.out.println("Please enter the abbreviated state associated with your address (< 2 chars long; Ex: CA for California): ");
                    stateName = scan.nextLine().trim().toUpperCase();

                    // Error checking the inputted stateName
                    if (stateName.length() > 2 || stateName.length() < 2 || stateName == null){
                        do {
                            System.out.println("I'm sorry, we cannot store a state name that large/small. Consider truncating if too large.");
                            System.out.print("Please enter the state associated with your address (< 2 chars long): ");
                            cityName = scan.nextLine().trim(); 
                        } while (stateName.length() > 2 || stateName.length() < 2 || stateName == null);
                    }

                    // Prompting the user to enter the zip code associated with their address
                    System.out.print("Please enter the zip code associated with your address (< 5 chars long): ");
                    zip = Integer.parseInt(scan.nextLine().trim());

                    // Error checking the inputted zip_code
                    if (zip > 99999 || zip < 0){
                        do {
                            System.out.println("I'm sorry, we cannot store a city name that large/small. Consider truncating if too large.");
                            System.out.print("Please enter the city associated with your address (< 15 chars long): ");
                            cityName = scan.nextLine().trim(); 
                        } while (zip > 99999 || zip < 0);
                    }
                }

                // Asking the user if they would like to change the phone number they have on file
                if (newPhoneNum == -1) {
                    System.out.println("Would you like to change the phone number you currently have on file?");
                    System.out.println("Currently the phone number on file is " + currPhoneNum);
                    System.out.print("(Y/N): ");
                    changePhone = scan.nextLine().trim();

                    // error checking in case the user entered anything other than Y or N
                    if (!changePhone.toUpperCase().equals("Y") && !changePhone.toUpperCase().equals("N")){
                        do{
                            System.out.println("Would you like to change the phone number you currently have on file?");
                            System.out.println("Currently the phone number on file is " + currPhoneNum);
                            System.out.print("(Y/N): ");
                            changePhone = scan.nextLine().trim();
                        }while (!changePhone.toUpperCase().equals("Y") && !changePhone.toUpperCase().equals("N"));
                    }
                }
                
                // If the user would like to change their phone number on file, gather their information
                if (changePhone.toUpperCase().equals("Y")) {

                    // Prompting the user to enter their new phone number
                    System.out.println("Please enter the new phone number you would like to have on file.");
                    System.out.println("Please only enter the numbers WITHOUT any symbols ( ex: (), -)");
                    System.out.print("New phone number: ");

                    String sanitizer = scan.nextLine().trim();

                    // Error checking the users input for new phone number
                    if (sanitizer.length() > 10 || sanitizer.length() < 10 || sanitizer == null || sanitizer.substring(0,1).equals("0")) {
                        do {
                            // Prompting the user to enter their new phone number
                            System.out.println("The desired format is in form ########## with area code included.");
                            System.out.println("Please note that leading zero's (0#########) are not permitted");
                            System.out.println("Please only enter the numbers WITHOUT any symbols ( ex: (), -)");
                            System.out.print("New phone number: ");
                            sanitizer = scan.nextLine().trim();
                        } while ((sanitizer.length() > 10 || sanitizer.length() < 10 || sanitizer == null || sanitizer.substring(0,1).equals("0")));
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
                            exitFlag = scan.nextLine().trim();
                            System.out.print("New phone number: ");
                            sanitizer = scan.nextLine().trim();
                        } while((customer_phones.contains(newPhoneNum)) || (sanitizer.length() > 9 || sanitizer.length() < 9) || (!exitFlag.toUpperCase().equals("EXIT")) || sanitizer.substring(0,1).equals("0") );
                    }

                    newPhoneNum = Long.parseLong(sanitizer);
                    currPhoneNum = newPhoneNum;

                }

                // If the user does not have a credit card on file, ask if they'd like to enter a credit card to be kept on file
                if (credCard == -1) {
                    System.out.println("Currently you have no credit card on file.");
                    System.out.print("Would you like to add an credit card to make payments smoother? (Y/N): ");
                    addCredCard = scan.nextLine().trim();

                    // error checking in case the user entered anything other than Y or N
                    if (!addCredCard.toUpperCase().equals("Y") && !addCredCard.toUpperCase().equals("N")){
                        do{
                            System.out.println("Currently you have no credit card on file.");
                            System.out.print("Would you like to add a credit card to make payments smoother? (Y/N): ");
                            addCredCard = scan.nextLine().trim();
                        }while (!addCredCard.toUpperCase().equals("Y") && !addCredCard.toUpperCase().equals("N"));
                    }
                }

                // If the user would like to add a credit card, gather their information
                if (addCredCard.equals("Y")) {
                    System.out.println("Please enter a credit card to be kept on file.");
                    System.out.println(" We can only accept credit cards of at most 16 digits.");
                    System.out.print("New credit card: ");
                    String sanitizer = scan.nextLine().trim();

                    credCard = Long.parseLong(sanitizer);
                    
                    // Error checking the user input
                    if (sanitizer.length() < 13 || sanitizer.length() > 16) {
                        do {
                            System.out.println("There seems to be an issue with your previous input.");
                            System.out.println("Please remember to only use number digits without spaces.");
                            System.out.print("New credit card: ");
                            sanitizer = scan.nextLine().trim();
                            credCard = Long.parseLong(sanitizer);
                        } while((sanitizer.length() < 13 || sanitizer.length() > 16));
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
                // System.out.println("Just before inserting, currPhoneNumL: " + currPhoneNum);
                inputInfo.setLong(4, currPhoneNum);
                inputInfo.setInt(5, bldgNum);
                inputInfo.setString(6, streetName);
                inputInfo.setString(7, cityName);
                inputInfo.setString(8, stateName);
                inputInfo.setInt(9, zip);
                inputInfo.setLong(10, credCard);
                inputInfo.setInt(11, Integer.parseInt(customer_info.get(3)));

                inputInfo.execute();

            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }


            System.out.println("Congratulations on updating your account! Your new account information is as follows: ");
            //System.out.println("customer_id: " + customer_info.get(4));
            try (CallableStatement confirmUpdate = con.prepareCall("{call display_customer_info(?,?,?,?,?,?,?,?,?,?)}")) {
                confirmUpdate.setString(1, customer_info.get(4)); // customer_id
                confirmUpdate.registerOutParameter(2, Types.VARCHAR); // first_name
                confirmUpdate.registerOutParameter(3, Types.VARCHAR); // last_name
                confirmUpdate.registerOutParameter(4, Types.NUMERIC); // phoneNum
                confirmUpdate.registerOutParameter(5, Types.NUMERIC); // building_number
                confirmUpdate.registerOutParameter(6, Types.VARCHAR); // street_name
                confirmUpdate.registerOutParameter(7, Types.VARCHAR); // city_name
                confirmUpdate.registerOutParameter(8, Types.VARCHAR); // state_name
                confirmUpdate.registerOutParameter(9, Types.NUMERIC); // zip_code
                confirmUpdate.registerOutParameter(10, Types.NUMERIC); // credit_card
                confirmUpdate.execute();

                currPhoneNum = confirmUpdate.getLong(4);
                bldgNum = confirmUpdate.getInt(5);
                streetName = confirmUpdate.getString(6);
                cityName = confirmUpdate.getString(7);
                stateName = confirmUpdate.getString(8);
                zip = confirmUpdate.getInt(9);
                credCard = confirmUpdate.getLong(10);

                System.out.println("Name: " + uFirstName + " " + uLastName);
                System.out.println("Phone Number: " + currPhoneNum);
                if (bldgNum > 0) 
                    System.out.println("Current address: " + bldgNum + " " + streetName + " " + cityName + " " + stateName + " " + zip);
                else { System.out.println("Current address: "); }
                if (credCard > 0) {
                    System.out.println("Credit Card: " + credCard);
                    String credCardString = Long.toString(credCard);
                    customer_info.remove(3); // removing status code
                    customer_info.add(credCardString);
                }
                else { 
                    customer_info.remove(3);
                    customer_info.add("-1");
                    System.out.println("Credit Card: "); 
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
            userChoice = scan.nextLine().trim();

            // Error checking the user's inputs
            if (!userChoice.toUpperCase().equals("Y") && !userChoice.toUpperCase().equals("N")) {
                do {
                    // Asking the user if they would like to add additional information to their account
                    System.out.println("Would you like to add an address to your account on file?");
                    System.out.print("(Y/N): ");
                    userChoice = scan.nextLine().trim();
                } while (!userChoice.toUpperCase().equals("Y") && !userChoice.toUpperCase().equals("N"));
            }

            // If the user would like to add an address, gather their information
            if (userChoice.toUpperCase().equals("Y")) {
                // Prompting the user to enter their building number
                System.out.print("Please enter your building number (< 5 chars long): ");
                bldgNum = Integer.parseInt(scan.nextLine().trim());

                // Error checking user inputted building number
                if (bldgNum > 99999 || bldgNum < 1){
                    do {
                        System.out.println("That number was far too large to be a building number.");
                        System.out.print("Please enter your building number in the correct format (< 5 chars long): ");
                        bldgNum = Integer.parseInt(scan.nextLine().trim()); 
                    } while (bldgNum > 99999 || bldgNum < 1);
                }
            
                // Prompting the user to enter their street name
                System.out.print("Please enter the street name of your address (< 15 chars long): ");
                streetName = scan.nextLine().trim();

                // Error checking user inputted streetName
                if (streetName.length() > 15 || streetName.length() < 1 || streetName == null){
                    do {
                        System.out.println("I'm sorry, we cannot store a street name that large/small. Consider truncating if too large.");
                        System.out.print("Please enter the street name of your address (< 15 chars long): ");
                        streetName = scan.nextLine().trim(); 
                    } while (streetName.length() > 15 || streetName.length() < 1 || streetName == null);
                }

                // Prompting the user to enter their city name
                System.out.print("Please enter the city associated with your address (< 15 chars long): ");
                cityName = scan.nextLine().trim();

                // Error checking user inputted cityName
                if (cityName.length() > 15 || cityName.length() < 1 || cityName == null){
                    do {
                        System.out.println("I'm sorry, we cannot store a city name that large/small. Consider truncating if too large.");
                        System.out.print("Please enter the city associated with your address (< 15 chars long): ");
                        cityName = scan.nextLine().trim(); 
                    } while (cityName.length() > 15 || cityName.length() < 1 || cityName == null);
                }
            
                // Prompting the user to enter the state associated with their address
                System.out.print("Please enter the abbreviated state associated with your address (< 2 chars long; Ex: CA for California): ");
                stateName = scan.nextLine().trim().toUpperCase();

                // Error checking the inputted stateName
                if (stateName.length() > 2 || stateName.length() < 2 || stateName == null){
                    do {
                        System.out.println("I'm sorry, we cannot store a state name that large/small. Consider truncating if too large.");
                        System.out.print("Please enter the state associated with your address (< 2 chars long): ");
                        cityName = scan.nextLine().trim(); 
                    } while (stateName.length() > 2 || stateName.length() < 2|| stateName == null);
                }

                // Prompting the user to enter the zip code associated with their address
                System.out.print("Please enter the zip code associated with your address (< 5 chars long): ");
                zip = Integer.parseInt(scan.nextLine().trim());

                // Error checking the inputted zip_code
                if (zip > 99999 || zip < 0){
                    do {
                        System.out.println("I'm sorry, we cannot store a city name that large/small. Consider truncating if too large.");
                        System.out.print("Please enter the city associated with your address (< 15 chars long): ");
                        cityName = scan.nextLine().trim(); 
                    } while (zip > 99999 || zip < 0);
                }
            }
        
            // Asking the user if they would like to add additional information to their account
            System.out.println("Would you like to add a credit card to your account on file?");
            System.out.print("(Y/N): ");
            userChoice = scan.nextLine().trim();

            // Error checking the user's inputs
            if (!userChoice.toUpperCase().equals("Y") && !userChoice.toUpperCase().equals("N")) {
                do {
                    // Asking the user if they would like to add additional information to their account
                    System.out.println("Would you like to add a credit card to your account on file?");
                    System.out.print("(Y/N): ");
                    userChoice = scan.nextLine().trim();
                } while (!userChoice.toUpperCase().equals("Y") && !userChoice.toUpperCase().equals("N"));
            }
            
            // Gather credit card information from user if they would like to add a credit card
            if (userChoice.toUpperCase().equals("Y")) {
                System.out.println("Please enter a credit card to be kept on file.");
                System.out.println(" We can only accept credit cards of at most 16 digits.");
                System.out.print("New credit card: ");
                String sanitizer = scan.nextLine().trim();

                credCard = Long.parseLong(sanitizer);
                
                // Error checking the user input
                if (sanitizer.length() < 13 || sanitizer.length() > 16) {
                    do {
                        System.out.println("There seems to be an issue with your previous input.");
                        System.out.println("Please remember to only use number digits without spaces.");
                        System.out.print("New credit card: ");
                        sanitizer = scan.nextLine().trim();
                        credCard = Long.parseLong(sanitizer);
                    } while((sanitizer.length() < 13 || sanitizer.length() > 16));
                }
            }
             
            // creating the customer_id for a new customer
            long count = 7000000000L;
            do {
                //System.out.println("test");
                if(!customer_ids.contains(Long.toString(count))) {
                    customer_id = Long.toString(count);
                    //System.out.println("customer_id: " + customer_id);
                }
                count++;
            }while(count < 8000000000L && customer_ids.contains(Long.toString(count-1)));

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

                inputInfo.execute();

                
            } catch (Exception e) {
                String customerChoice = "T";
                System.out.println(e);
                e.printStackTrace();
                while (!customerChoice.toUpperCase().equals("Y") || !customerChoice.toUpperCase().equals("N")) {
                    System.out.println("There seems to have been an issue with the information you provided.");
                    System.out.println("Would you like to try updating your account once more?");
                    System.out.print("(Y/N): ");
                    customerChoice = scan.nextLine().trim();
                    System.out.println();
                }
                if (customerChoice.toUpperCase().equals("Y"))
                    processCustomer(scan, con, customer_info);
                
            }

            //System.out.println("customerIdString: " + customer_id);
            System.out.println();

            System.out.println("Congratulations on creating your account! Your new account information is as follows: ");
            try (CallableStatement confirmUpdate = con.prepareCall("{call display_customer_info(?,?,?,?,?,?,?,?,?,?)}")) {
                confirmUpdate.setString(1, customer_id); // customer_id
                confirmUpdate.registerOutParameter(2, Types.VARCHAR); // first_name
                confirmUpdate.registerOutParameter(3, Types.VARCHAR); // last_name
                confirmUpdate.registerOutParameter(4, Types.NUMERIC); // phoneNum
                confirmUpdate.registerOutParameter(5, Types.NUMERIC); // building_number
                confirmUpdate.registerOutParameter(6, Types.VARCHAR); // street_name
                confirmUpdate.registerOutParameter(7, Types.VARCHAR); // city_name
                confirmUpdate.registerOutParameter(8, Types.VARCHAR); // state_name
                confirmUpdate.registerOutParameter(9, Types.NUMERIC); // zip_code
                confirmUpdate.registerOutParameter(10, Types.NUMERIC); // credit_card

                confirmUpdate.execute();

                currPhoneNum = confirmUpdate.getLong(4);
                bldgNum = confirmUpdate.getInt(5);
                streetName = confirmUpdate.getString(6);
                cityName = confirmUpdate.getString(7);
                stateName = confirmUpdate.getString(8);
                zip = confirmUpdate.getInt(9);
                credCard = confirmUpdate.getLong(10);

                System.out.println("Name: " + uFirstName + " " + uLastName);
                System.out.println("Phone Number: " + currPhoneNum);
                if (bldgNum > 0) 
                    System.out.println("Current address: " + bldgNum + " " + streetName + " " + cityName + " " + stateName + " " + zip);
                else { System.out.println("Current address: "); }
                if (credCard > 0) {
                    System.out.println("Credit Card: " + credCard);
                    String credCardString = Long.toString(credCard);
                    customer_info.remove(3);
                    customer_info.add(customer_id);
                    customer_info.add(credCardString);
                }
                else { 
                    customer_info.remove(3);
                    customer_info.add(customer_id);
                    customer_info.add("-1");
                    System.out.println("Credit Card: "); 
                }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }
        return customer_info;
    }


    /**
     * 
     * @param scan
     * @param con
     * @return arrayList:
     * FirstName        0
     * LastName         1
     * PhoneNumber      2
     * StatusCode       3
     * customer_id      4
     * error("kill")    10
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

        // Prompting the user to enter personally identifiable information
        System.out.println("Please enter your first and last name as you would like them stored and displayed.");
        System.out.print("First Name: ");
        uFname = scan.nextLine().trim();

        System.out.print("Last Name: ");
        uLname = scan.nextLine().trim();

        System.out.println("\nAdditionally, please enter the phone number you would like to be contacted with in the event of updates to your reservation.");
        System.out.print("Phone Number: ");
        phoneNum = scan.nextLine().trim();

        Long phoneNumLong = phoneFormatChecker(scan, phoneNum);
        HashMap<Long, ArrayList<String>> customers = new HashMap<Long, ArrayList<String>>();
        // getting a list of all phone_nums to compare with
        try (CallableStatement cs = con.prepareCall("begin customer_ids(?); end;")){
            ResultSet customerPhonesSet;
            cs.registerOutParameter(1, Types.REF_CURSOR);
            cs.execute();
            customerPhonesSet = (ResultSet)cs.getObject(1);
            while (customerPhonesSet.next()) {
                customers.put(customerPhonesSet.getLong("phone_number"), new ArrayList<String>());
                customers.get(customerPhonesSet.getLong("phone_number")).add(customerPhonesSet.getString("first_name"));
                customers.get(customerPhonesSet.getLong("phone_number")).add(customerPhonesSet.getString("last_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // testing to see if the phone number entered is already associated with another account
        if (customers.containsKey(phoneNumLong) && !uFname.equals(customers.get(phoneNumLong).get(0))) { 
            String uC = "Y";
            // comparing the customer name on file associated with the phone number with the entered customer phone number
            do {
                System.out.println("Sorry that phone number is already associated with an account under a different name.");
                System.out.print("Would you like to try another phone number? (Y/N): ");
                uC = (scan.nextLine().isEmpty() ? "Y" : uC);
                System.out.print("Please enter a different phone number: ");
                phoneNum = scan.nextLine().trim();
                phoneNumLong = phoneFormatChecker(scan, phoneNum);
            } while (!uC.equals("N") || (customers.containsKey(phoneNumLong) && !uFname.equals(customers.get(phoneNumLong).get(0))));
            if (uC.equals("N")) {
                resultList.removeAll(resultList);
            }
        }

        boolean runTheSearch = false;
        if (!resultList.isEmpty()) 
            runTheSearch = true;
        
        if (runTheSearch) {
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
        }
        else {
            String client_checker = "Y"; // needs to be reset to allow for correct logical movement going forward
        
            // catching an error in the specifications given by the user if they inputted their information incorrectly
            if (client_checker.toUpperCase().equals("Y")) {
                do {
                    System.out.print("There seems to be an issue with the specified inputs you provided. Would you like to try again with different inputs? (Y or N): ");
                    client_checker = scan.nextLine().trim();
                    client_checker.toUpperCase();
                    if (client_checker.equals("Y")) {
                        resultList = knowYourCustomer(scan, con); 
                    }
                    else if (!client_checker.equals("N")){
                        client_checker = "Y";
                        System.out.print("Not a valid input (valid inputs = Y or N): ");
                    }
                } while (client_checker.equals("Y") || resultList.isEmpty());
            }
            else {
                resultList.add(10,"kill");
            }
        }
        return resultList;
    }

    /**
     * Error checking inputted phone numbers
     * @param Scanner scan
     * @param String phoneNum
     * @return Long of correctly formatted phoneNumber
     */
    public static long phoneFormatChecker(Scanner scan, String phoneNum) {
        // Error checking the users input for new phone number
        if (!phoneNum.matches("[1-9]{1}[0-9]{9}")) {
            do {
                // Prompting the user to enter their new phone number
                System.out.println("The desired format is in form ########## with area code included.");
                System.out.println("Please note that leading zero's (0#########) are not permitted");
                System.out.println("Please only enter the numbers WITHOUT any symbols ( ex: (), -)");
                System.out.print("Phone number: ");
                phoneNum = scan.nextLine().trim();

            } while (!phoneNum.matches("[1-9]{1}[0-9]{9}"));
        }
        long phoneNumLong = Long.valueOf(phoneNum);
        return phoneNumLong;
    }
    
    /**
     * Gathering all valid hotel city locations
     * @param scan
     * @param con
     * @return
     */
    public static ArrayList<String> hotelCities(Scanner scan, Connection con) {
        ArrayList<String> validArr = new ArrayList<String>();
        // calling the hotel_cities procedure to gather an arrayList of all valid cities
        try(CallableStatement gather_hotel_cities = con.prepareCall("begin hotel_cities(?); end;"))
        {
            ResultSet valid_hotel_cities;
            gather_hotel_cities.registerOutParameter(1, Types.REF_CURSOR);
            gather_hotel_cities.execute();

            valid_hotel_cities = (ResultSet)gather_hotel_cities.getObject(1);
            // populating arraylist of cities with hotels in them
            while(valid_hotel_cities.next()){
                if (!validArr.contains(valid_hotel_cities.getString(1)))
                    validArr.add(valid_hotel_cities.getString(1));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return validArr;
    }
    
    /**
     * gathers user input for desired city and arrival/departure dates.
     * @param scan
     * @param con
     * @return arraylist: contains 
     * address of hotel                                     0
     * room type the customer would like to stay in         1
     * arrival day                                          2
     * depart day                                           3
     * hotel_id and                                         4
     * desiredRoomPrice                                     5
     * error (kill)                                         10
     */
    public static ArrayList<String> chooseHotelCustomer(Scanner scan, Connection con) {
        String arrivalDate = "";
        LocalDate arrivalDateLocalDate;
        LocalDate departDateLocalDate;
        String city = "ethereum";
        ArrayList<String> validArr = hotelCities(scan, con);
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
                    if (!validArr.contains(valid_hotel_cities.getString(1)))
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
            city = scan.nextLine().trim();

            System.out.println(); // just so the command line doesn't become too crowded
            // checking if the specified city is in the set of valid hotel cities
            if (!validArr.contains(city)) {
                do {
                    System.out.println ("Please choose only from the valid cities presented again below:");

                    for (int i = 0; i < validArr.size(); i++)
                        System.out.println(validArr.get(i));
               
                    System.out.print("City: ");
                    city = scan.nextLine().trim();
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
            double desiredRoomPrice = -1;
            
            // ask the customer which hotel they would like to stay in 
            while(clientChoice < 0 || clientChoice > hotelsInCity.size()) {
                System.out.println("Enter the number associated with the hotel you would like to book a reservation in.\n");

                for (int i = 0; i < hotelsInCity.size(); i++) {
                    System.out.println(i + ":\t" + hotelsInCity.get(i));
                }
                System.out.print("\nChoice: ");
                clientChoice = Integer.parseInt(scan.nextLine().trim());
            }
            hotelIdChosen = cityHotelIds.get(clientChoice);
            hotelAddressChosen = hotelsInCity.get(clientChoice);

            System.out.println();

            // ensures that the date is correct
            arrivalDateLocalDate = arrivalDateEnforcer(scan, arrivalDate, "customer");
            departDateLocalDate = departDateEnforcer(scan, arrivalDateLocalDate, "customer");
            String arrivalDateString = arrivalDateLocalDate.toString();
            String departDateString = departDateLocalDate.toString();
            //System.out.println("departDateString after departDateLocalDate.toString(): " + departDateLocalDate.toString());
            Date arrivalDateLiteral = Date.valueOf(arrivalDateString);
            Date departDateLiteral = Date.valueOf(departDateString);

            System.out.println();

            // initializing variables to be declared in the try block below
            String desiredRoom = "";
            ArrayList<String> aRoomTypes = new ArrayList<String>();
            ArrayList<Double> roomPrices = new ArrayList<Double>();
            // calling the available_hotel procedure to gather an arrayList of all hotel rooms available in the city specified
            try(CallableStatement gather_hotel_rooms = con.prepareCall("begin available_hotels2(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?); end;"))
            {
                int userChoice = -1;
                gather_hotel_rooms.setString(1, hotelIdChosen);
                gather_hotel_rooms.setDate(2, arrivalDateLiteral);
                gather_hotel_rooms.setDate(3, departDateLiteral);
                gather_hotel_rooms.registerOutParameter(4, Types.NUMERIC); // available single rooms
                gather_hotel_rooms.registerOutParameter(5, Types.NUMERIC); // price for single rooms
                gather_hotel_rooms.registerOutParameter(6, Types.NUMERIC); // available double rooms
                gather_hotel_rooms.registerOutParameter(7, Types.NUMERIC); // price for double rooms
                gather_hotel_rooms.registerOutParameter(8, Types.NUMERIC); // available deluxe rooms
                gather_hotel_rooms.registerOutParameter(9, Types.NUMERIC); // price for deluxe rooms
                gather_hotel_rooms.registerOutParameter(10, Types.NUMERIC); // available studio rooms
                gather_hotel_rooms.registerOutParameter(11, Types.NUMERIC); // price for studio rooms
                gather_hotel_rooms.registerOutParameter(12, Types.NUMERIC); // available pres rooms
                gather_hotel_rooms.registerOutParameter(13, Types.NUMERIC); // price pres rooms
                gather_hotel_rooms.registerOutParameter(14, Types.NUMERIC); // available suite rooms
                gather_hotel_rooms.registerOutParameter(15, Types.NUMERIC); // price suite rooms
                gather_hotel_rooms.execute();

                
                // gathering available rooms for the chosen hotel
                int aSingleRooms = gather_hotel_rooms.getInt(4);
                double singlePrice = gather_hotel_rooms.getDouble(5);
                int aDoubleRooms = gather_hotel_rooms.getInt(6);
                double doublePrice = gather_hotel_rooms.getDouble(7);
                int aDeluxeRooms = gather_hotel_rooms.getInt(8);
                double deluxePrice = gather_hotel_rooms.getDouble(9);
                int aStudioRooms = gather_hotel_rooms.getInt(10);
                double studioPrice = gather_hotel_rooms.getDouble(11);
                int aPresRooms = gather_hotel_rooms.getInt(12);
                double presPrice = gather_hotel_rooms.getDouble(13);
                int aSuiteRooms = gather_hotel_rooms.getInt(14);
                double suitePrice = gather_hotel_rooms.getDouble(15);

                // asking the customer which of the rooms they would like to stay in
                if (aSingleRooms > 0) { 
                    aRoomTypes.add("Single"); 
                    roomPrices.add(singlePrice);
                }
                if (aDoubleRooms > 0) { 
                    aRoomTypes.add("Double"); 
                    roomPrices.add(doublePrice);
                }
                if (aDeluxeRooms > 0) { 
                    aRoomTypes.add("Deluxe"); 
                    roomPrices.add(deluxePrice);
                }
                if (aStudioRooms > 0) { 
                    aRoomTypes.add("Studio"); 
                    roomPrices.add(studioPrice);
                }
                if (aPresRooms > 0) { 
                    aRoomTypes.add("Presidential"); 
                    roomPrices.add(presPrice);
                }
                if (aSuiteRooms > 0) { 
                    aRoomTypes.add("Suite"); 
                    roomPrices.add(suitePrice);
                }
                
                while(userChoice < 0 || userChoice > aRoomTypes.size()){
                    System.out.println("Please enter the number associated with the room you would like to stay in.");
                    System.out.println("Only room types with rooms available for the duration of your stay are displayed.");

                    System.out.println("  \tRoom Types\t\tCost");

                    for (int i = 0; i < aRoomTypes.size(); i++) {
                        System.out.println(i + ":\t" + aRoomTypes.get(i) + "\t\t\t" + roomPrices.get(i));
                    }
                    
                    System.out.println("\nChoice: ");
                    userChoice = Integer.parseInt(scan.nextLine().trim());
                }
                desiredRoom = aRoomTypes.get(userChoice);
                desiredRoomPrice = roomPrices.get(userChoice);
            }   
            resultList.add(hotelAddressChosen);
            resultList.add(desiredRoom);
            resultList.add(arrivalDateString);
            resultList.add(departDateString);
            resultList.add(hotelIdChosen);
            resultList.add(Double.toString(desiredRoomPrice));

            String client_checker = "Y";

            // catching an error in the specifications given by the customer
            if (resultList.isEmpty() || client_checker.toUpperCase().equals("N")) {
                do {
                    System.out.println("There seems to be an issue with the specified inputs you provided. Would you like to try again with different inputs? (Y or N)");
                    client_checker = scan.nextLine().trim();
                    client_checker.toUpperCase();
                    if (client_checker.equals("Y"))
                        resultList = chooseHotelCustomer(scan, con); 
                    else if (!client_checker.equals("N")){
                        client_checker = "Y";
                        System.out.println("Not a valid input (valid inputs = Y or N)");
                    }
                } while (resultList.isEmpty() || client_checker.equals("N"));
            }

            // if the user would not like to fix their bad inputs, they probably want to just leave, so i will let them
            if (client_checker.equals("N")) {
                System.out.println("Ok, have a nice day! :)");
                resultList.add(10, "kill");
                return resultList;
            }

            // verifying that the customer is satisfied with the information they provided
            System.out.println("\nPlease verify that you entered your information regarding your reservation to your satisfaction.\n");
            System.out.println("Hotel Address: " + resultList.get(0));
            System.out.println("Room Type: " + resultList.get(1));
            System.out.println("Arrival Date (yyyy-MM-DD): " + resultList.get(2));
            System.out.println("Departure Date (yyyy-MM-DD): " + resultList.get(3));

            System.out.println("\nAre you satisfied with the above information?");
            System.out.print("(Y/N): ");
            client_checker = scan.nextLine().trim();

            System.out.println();

            // Repeating the chooseHotel() call and error checking the inputs of the user
            if (client_checker.toUpperCase().equals("N")) {
                do {
                    resultList = chooseHotelCustomer(scan, con);
                    // verifying that the customer is satisfied with the information they provided
                    System.out.println("Please verify that you entered your information regarding your reservation to your satisfaction\n.");
                    System.out.println("Hotel Address: " + resultList.get(0));
                    System.out.println("Room Type: " + resultList.get(1));
                    System.out.println("Arrival Date (yyyy-MM-DD): " + resultList.get(2));
                    System.out.println("Departure Date (yyyy-MM-DD): " + resultList.get(3));

                    System.out.println("\nAre you satisfied with the above information?");
                    System.out.println("(Y/N): ");
                    client_checker = scan.nextLine().trim();

                    System.out.println();

                    // catching an error in the specifications given by the customer
                    if (resultList.isEmpty() && client_checker.toUpperCase().equals("Y")) {
                        do {
                            System.out.print("There seems to be an issue with the specified inputs you provided. Would you like to try again with different inputs? (Y or N): ");
                            client_checker = scan.nextLine().trim();
                            client_checker.toUpperCase();
                            if (client_checker.equals("Y"))
                                resultList = chooseHotelCustomer(scan, con); 
                            else if (!client_checker.equals("N")){
                                client_checker = "Y";
                                System.out.print("Not a valid input (valid inputs = Y or N): ");
                                System.out.println();
                            }
                        } while (resultList.isEmpty() && client_checker.equals("Y"));
                    }
                } while (client_checker.equals("N")); 
            }

            if (client_checker.equals("N")) {
                System.out.println("Ok, have a nice day! :)");
                resultList.add(10, "kill");
                return resultList;
            }

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
     * 
     * @param scan
     * @param arrivalDate
     * @return valid arrvival date
     */
    static LocalDate arrivalDateEnforcer(Scanner scan, String arrivalDate, String who) {
        String s1 = "";
        String s2 = "";
        String s3 = "";
        if (who.equals("customer")){
            s1 = "you";
            s2 = "your";
            s3 = "You";
        } else if (who.equals("front desk")) {
            s1 = ("the customer");
            s2 = ("their");
            s3 = ("The customer");
        }
        LocalDate arrivalDateLiteral = LocalDate.now();
        LocalDate now = LocalDate.now();
        while (!arrivalDate.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            System.out.println("Please enter the date " + s1 + " would like to begin "+ s2 + " stay with us.");
            System.out.println("Format: (YYYY-MM-DD)");
            System.out.print("Arrival Date: ");
            arrivalDate = scan.nextLine().trim();
            System.out.println();
        }
        
        try {
            arrivalDateLiteral = LocalDate.parse(arrivalDate);

            // Error checking 
            if (arrivalDateLiteral.isBefore(now)) {
                do {
                    arrivalDate = "0";
                    while (!arrivalDate.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                        System.out.println(s2 + " cannot reserve a room from before today.");
                        System.out.println("Format: (YYYY-MM-DD)");
                        System.out.print("Arrival Date: ");
                        arrivalDate = scan.nextLine().trim();
                        arrivalDateLiteral = LocalDate.parse(arrivalDate);
                        System.out.println();
                    }
                }while (arrivalDateLiteral.isBefore(now));
            }
            return arrivalDateLiteral;
        } catch (DateTimeParseException e) {
            arrivalDate = "0";
            arrivalDateEnforcer(scan, arrivalDate, who);
        }
        return arrivalDateLiteral;
    }

    /**
     * used to make sure that the chosen departure date is valid
     * @param scan
     * @param arrivalDateLiteral
     * @return valid depart Date
     */
    static LocalDate departDateEnforcer(Scanner scan, LocalDate arrivalDateLiteral, String who) {
        String s1 = "";
        String s2 = "";

        if (who.equals("customer")){
            s1 = "you";
            s2 = "You";
        } else if (who.equals("front desk")){
            s1 = "the customer";
            s2 = "The customer";
        }
        long numNights = 0;
        LocalDate departDateLiteral = LocalDate.now();
        try {
            while(numNights < 1) {
                System.out.println("How many nights would " + s1 + " like to stay with us?");
                System.out.println("Note: " + s2+ " may not reserve more than 30 days in a single reservation.");
                System.out.println("Note: The minimum reservation length is 1 night");
                System.out.println("If " + s1 + " would like to have an extended stay, feel free to book multiple reservations");
                System.out.print("Number of nights staying with us: ");    
                numNights = Long.parseLong(scan.nextLine().trim());
                System.out.println();
            }
            departDateLiteral = arrivalDateLiteral.plusDays(numNights);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            departDateEnforcer(scan, arrivalDateLiteral, who);
        }
        

        return departDateLiteral;
    }

    /**
     * 
     * @param scan
     * @param init
     * @param upperLimit
     * @param lowerLimit
     * @param message
     * @return
     */
    public static int rangeChecker(Scanner scan, int init, int upperLimit, int lowerLimit, String message) {
        //Prompt the user to choose which interface is to be accessed
        try {
            while (init > upperLimit || init < lowerLimit) {
                System.out.print(message);
                init = Integer.parseInt(scan.nextLine().trim());
                System.out.println();
            }
        } catch (Exception e){
            System.out.println(e);
            init = upperLimit + 1;
            rangeChecker(scan, init, upperLimit, lowerLimit, message);
        }
        return init;
    }

    /**
     * 
     * @param scan
     * @param con
     * @param reservation_info
     * @param customer_info
     */
    public static void setReservations(Scanner scan, Connection con, ArrayList<String> reservation_info, ArrayList<String> customer_info) {
        ArrayList<String> reservationIds = new ArrayList<String>();
        ResultSet reservationIdsSet;
        try (CallableStatement resIds = con.prepareCall("begin get_reservation_ids(?); end;")) {
            resIds.registerOutParameter(1, Types.REF_CURSOR);
            resIds.execute();
            reservationIdsSet = (ResultSet)resIds.getObject(1);
            while (reservationIdsSet.next())
                reservationIds.add(reservationIdsSet.getString("reservation_id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // creating the reservation_id
        String reservationIdString = "T";
        long count = 6999999999L;
            do {
                count++;
                reservationIdString = Long.toString(count);
            }while(count < 8000000000L && reservationIds.contains(reservationIdString));
        
        //System.out.println("reservationIdString: " + reservationIdString);
        //System.out.println("customer_id: " + customer_info.get(4));

        // setting the new reservation
        try (CallableStatement reservation = con.prepareCall("{call set_reservation(?,?,?,?,?,?)}")) {
            Date arrDate = Date.valueOf(reservation_info.get(2));
            Date depDate = Date.valueOf(reservation_info.get(3));
            reservation.setString(1, reservationIdString);
            reservation.setString(2, customer_info.get(4));
            reservation.setString(3, reservation_info.get(4));
            reservation.setString(4, reservation_info.get(1));
            reservation.setDate(5, arrDate);
            reservation.setDate(6, depDate);
            reservation.execute();
        } catch (Exception e) {
            System.out.println("\nSorry there was an error creating your reservation. Please try again.\n");
            e.printStackTrace();
            System.out.println();
        }

        // confirming the new reservation
        ArrayList<String> reservationIds2 = new ArrayList<String>();
        ResultSet reservationIdsSet2;
        try (CallableStatement resIds = con.prepareCall("begin get_reservation_ids(?); end;")) {
            resIds.registerOutParameter(1, Types.REF_CURSOR);
            resIds.execute();
            reservationIdsSet2 = (ResultSet)resIds.getObject(1);
            while (reservationIdsSet2.next())
                reservationIds2.add(reservationIdsSet2.getString("reservation_id"));

            if (reservationIds2.contains(reservationIdString)) {
                System.out.println("Congratulations on booking your reservation at " + reservation_info.get(0) +
                    " from " + reservation_info.get(2) + " to " + reservation_info.get(3));
                System.out.println("We look forward to seeing you soon " + customer_info.get(0) + " " +
                    customer_info.get(1));
            }
            else {
                System.out.println("There were issues in creating your reservation. Please try again.");
            }
            System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    


    /**
     * 
     * @param scan
     * @param con
     */
    public static int customerInterface(Scanner scan, Connection con){
        int init = -1;
        String message = ("Enter the number associated with what you would like to do.\n0:\tMake a reservation.\n1:\tCreate/Update an account.\n2:\tView my reservations.\n3:\tReturn to main menu.\n\nChoice: ");
        int userChoice = -1;

        while (userChoice != 3) {
            userChoice = rangeChecker(scan, init, 3, 0, message);
            
            if (userChoice == 0){ // make a reservation
                ArrayList<String> reservation_info = chooseHotelCustomer(scan, con);
                // would like to enter main menu
                if (reservation_info.get(10).equals("kill")){
                    continue;
                }
                // gathering customer information
                ArrayList<String> customer_info = knowYourCustomer(scan, con);
                // would like to enter main menu
                if (customer_info.get(10).equals("kill")){
                    continue;
                }
                System.out.println();
                // determines whether the customer is returning or new and proceses them as needed
                customer_info = processCustomer(scan, con, customer_info);
                //System.out.println(customer_info.toString());
                System.out.println();
                // create the reservation
                setReservations(scan, con, reservation_info, customer_info);
            }
            if (userChoice == 1) { // create/update an account
                // gathering customer information
                ArrayList<String> customer_info = knowYourCustomer(scan, con);
                // would like to enter main menu
                if (customer_info.get(10).equals("kill")){
                    continue;
                }
                System.out.println();
                // determines whether the customer is returning or new and proceses them as needed
                customer_info = processCustomer(scan, con, customer_info);
            }
            if (userChoice == 2) {
                // in progress
            }
            if (userChoice == 3) {
                return 0;
            }
        }
        return 0;
        
    }

    /**
     * gathers user input for desired city and arrival/departure dates.
     * @param scan
     * @param con
     * @return arraylist: contains 
     * address of hotel                                     0
     * room type the customer would like to stay in         1
     * arrival day                                          2
     * depart day                                           3
     * hotel_id and                                         4
     * desiredRoomPrice                                     5
     * error (kill)                                         10
     */
    public ArrayList<String> frontDeskReservation(Scanner scan, Connection con, String hotelId) {
        
        // initializing vars to hold the address info
        int bldgNum = 0;
        String stN = "";
        String cN = "";
        String state = "";
        int zip = 0;

        // getting the address of this particular hotel
        try(PreparedStatement cs = con.prepareStatement("SELECT building_id, street, city, home_state, zip_code FROM hotels WHERE hotel_id = ?")){
            cs.setString(1, hotelId);
            try(ResultSet rs = cs.executeQuery()){
                while (rs.next()) {
                    bldgNum = rs.getInt("building_number");
                    stN = rs.getString("street_name");
                    cN = rs.getString("City");
                    state = rs.getString("home_state");
                    zip = rs.getInt("zip_code");
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String hotelAddressChosen = Integer.toString(bldgNum) + stN + cN + state + Integer.toString(zip);


        String arrivalDate = "";
        LocalDate arrivalDateLocalDate;
        LocalDate departDateLocalDate;
        ArrayList<String> resultList = new ArrayList<String>();

        double desiredRoomPrice = -1;
        
        System.out.println();

        // ensures that the date is correct
        arrivalDateLocalDate = arrivalDateEnforcer(scan, arrivalDate, "front desk");
        departDateLocalDate = departDateEnforcer(scan, arrivalDateLocalDate, "front desk");
        String arrivalDateString = arrivalDateLocalDate.toString();
        String departDateString = departDateLocalDate.toString();
        //System.out.println("departDateString after departDateLocalDate.toString(): " + departDateLocalDate.toString());
        Date arrivalDateLiteral = Date.valueOf(arrivalDateString);
        Date departDateLiteral = Date.valueOf(departDateString);

        System.out.println();

        // initializing variables to be declared in the try block below
        String desiredRoom = "";
        ArrayList<String> aRoomTypes = new ArrayList<String>();
        ArrayList<Double> roomPrices = new ArrayList<Double>();
        // calling the available_hotel procedure to gather an arrayList of all hotel rooms available in the city specified
        try(CallableStatement gather_hotel_rooms = con.prepareCall("begin available_hotels2(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?); end;"))
        {
            int userChoice = -1;
            gather_hotel_rooms.setString(1, hotelId);
            gather_hotel_rooms.setDate(2, arrivalDateLiteral);
            gather_hotel_rooms.setDate(3, departDateLiteral);
            gather_hotel_rooms.registerOutParameter(4, Types.NUMERIC); // available single rooms
            gather_hotel_rooms.registerOutParameter(5, Types.NUMERIC); // price for single rooms
            gather_hotel_rooms.registerOutParameter(6, Types.NUMERIC); // available double rooms
            gather_hotel_rooms.registerOutParameter(7, Types.NUMERIC); // price for double rooms
            gather_hotel_rooms.registerOutParameter(8, Types.NUMERIC); // available deluxe rooms
            gather_hotel_rooms.registerOutParameter(9, Types.NUMERIC); // price for deluxe rooms
            gather_hotel_rooms.registerOutParameter(10, Types.NUMERIC); // available studio rooms
            gather_hotel_rooms.registerOutParameter(11, Types.NUMERIC); // price for studio rooms
            gather_hotel_rooms.registerOutParameter(12, Types.NUMERIC); // available pres rooms
            gather_hotel_rooms.registerOutParameter(13, Types.NUMERIC); // price pres rooms
            gather_hotel_rooms.registerOutParameter(14, Types.NUMERIC); // available suite rooms
            gather_hotel_rooms.registerOutParameter(15, Types.NUMERIC); // price suite rooms
            gather_hotel_rooms.execute();

            
            // gathering available rooms for the chosen hotel
            int aSingleRooms = gather_hotel_rooms.getInt(4);
            double singlePrice = gather_hotel_rooms.getDouble(5);
            int aDoubleRooms = gather_hotel_rooms.getInt(6);
            double doublePrice = gather_hotel_rooms.getDouble(7);
            int aDeluxeRooms = gather_hotel_rooms.getInt(8);
            double deluxePrice = gather_hotel_rooms.getDouble(9);
            int aStudioRooms = gather_hotel_rooms.getInt(10);
            double studioPrice = gather_hotel_rooms.getDouble(11);
            int aPresRooms = gather_hotel_rooms.getInt(12);
            double presPrice = gather_hotel_rooms.getDouble(13);
            int aSuiteRooms = gather_hotel_rooms.getInt(14);
            double suitePrice = gather_hotel_rooms.getDouble(15);

            // asking the customer which of the rooms they would like to stay in
            if (aSingleRooms > 0) { 
                aRoomTypes.add("Single"); 
                roomPrices.add(singlePrice);
            }
            if (aDoubleRooms > 0) { 
                aRoomTypes.add("Double"); 
                roomPrices.add(doublePrice);
            }
            if (aDeluxeRooms > 0) { 
                aRoomTypes.add("Deluxe"); 
                roomPrices.add(deluxePrice);
            }
            if (aStudioRooms > 0) { 
                aRoomTypes.add("Studio"); 
                roomPrices.add(studioPrice);
            }
            if (aPresRooms > 0) { 
                aRoomTypes.add("Presidential"); 
                roomPrices.add(presPrice);
            }
            if (aSuiteRooms > 0) { 
                aRoomTypes.add("Suite"); 
                roomPrices.add(suitePrice);
            }
            
            while(userChoice < 0 || userChoice > aRoomTypes.size()){
                System.out.println("Please enter the number associated with the room the customer would like to stay in.");
                System.out.println("Only room types with rooms available for the duration of the customer's stay are displayed.");

                System.out.println("  \tRoom Types\t\tCost");

                for (int i = 0; i < aRoomTypes.size(); i++) {
                    System.out.println(i + ":\t" + aRoomTypes.get(i) + "\t\t\t" + roomPrices.get(i));
                }
                
                System.out.println("\nChoice: ");
                userChoice = Integer.parseInt(scan.nextLine().trim());
            }
            desiredRoom = aRoomTypes.get(userChoice);
            desiredRoomPrice = roomPrices.get(userChoice);
        }catch (Exception e){
            e.printStackTrace();
        }

        resultList.add(hotelAddressChosen);
        resultList.add(desiredRoom);
        resultList.add(arrivalDateString);
        resultList.add(departDateString);
        resultList.add(hotelId);
        resultList.add(Double.toString(desiredRoomPrice));

        String client_checker = "Y";

        // catching an error in the specifications given by the customer
        if (resultList.isEmpty() || client_checker.toUpperCase().equals("N")) {
            do {
                System.out.println("There seems to be an issue with the specified inputs you provided. Would you like to try again with different inputs? (Y or N)");
                client_checker = scan.nextLine().trim();
                client_checker.toUpperCase();
                if (client_checker.equals("Y"))
                    resultList = chooseHotelCustomer(scan, con); 
                else if (!client_checker.equals("N")){
                    client_checker = "Y";
                    System.out.println("Not a valid input (valid inputs = Y or N)");
                }
            } while (resultList.isEmpty() || client_checker.equals("N"));
        }

        // if the user would not like to fix their bad inputs, they probably want to just leave, so i will let them
        if (client_checker.equals("N")) {
            System.out.println("Ok, have a nice day! :)");
            resultList.add(10, "kill");
            return resultList;
        }

        // verifying that the customer is satisfied with the information they provided
        System.out.println("\nPlease verify that you entered the customer's information regarding their reservation to their satisfaction.\n");
        System.out.println("Hotel Address: " + resultList.get(0));
        System.out.println("Room Type: " + resultList.get(1));
        System.out.println("Arrival Date (yyyy-MM-DD): " + resultList.get(2));
        System.out.println("Departure Date (yyyy-MM-DD): " + resultList.get(3));

        System.out.println("\nIs the customer satisfied with the above information?");
        System.out.print("(Y/N): ");
        client_checker = scan.nextLine().trim();

        System.out.println();

        // Repeating the chooseHotel() call and error checking the inputs of the user
        if (client_checker.toUpperCase().equals("N")) {
            do {
                resultList = chooseHotelCustomer(scan, con);
                // verifying that the customer is satisfied with the information they provided
                System.out.println("Please verify that you entered the customer's information regarding their reservation to their satisfaction\n.");
                System.out.println("Hotel Address: " + resultList.get(0));
                System.out.println("Room Type: " + resultList.get(1));
                System.out.println("Arrival Date (yyyy-MM-DD): " + resultList.get(2));
                System.out.println("Departure Date (yyyy-MM-DD): " + resultList.get(3));

                System.out.println("\nIs the customer satisfied with the above information?");
                System.out.println("(Y/N): ");
                client_checker = scan.nextLine().trim();

                System.out.println();

                // catching an error in the specifications given by the customer
                if (resultList.isEmpty() && client_checker.toUpperCase().equals("Y")) {
                    do {
                        System.out.print("There seems to be an issue with the specified inputs the customer provided. Would the customer like to try again with different inputs? (Y or N): ");
                        client_checker = scan.nextLine().trim();
                        client_checker.toUpperCase();
                        if (client_checker.equals("Y"))
                            resultList = chooseHotelCustomer(scan, con); 
                        else if (!client_checker.equals("N")){
                            client_checker = "Y";
                            System.out.print("Not a valid input (valid inputs = Y or N): ");
                            System.out.println();
                        }
                    } while (resultList.isEmpty() && client_checker.equals("Y"));
                }
            } while (client_checker.equals("N")); 
        }

        if (client_checker.equals("N")) {
            System.out.println("Ok, have a nice day! :)");
            resultList.add(10, "kill");
            return resultList;
        }
        return resultList;
    }

    /**
     * Prompts the front-desk associate to pick the hotel in which they work
     * @param scan
     * @param con
     * @return
     */
    public static String frontDeskCity(Scanner scan, Connection con) {
        ArrayList<String> validArr = hotelCities(scan, con);
        String city;

        // beginning of interface interaction
        System.out.println("Hello, front-desk associate. Please enter the city in which you work.");
        System.out.println("Here are the following cities in which we have hotels:\n");

        // displaying arraylist of cities with hotels in them
        for (int i = 0; i < validArr.size(); i++)
            System.out.println(validArr.get(i));
        System.out.println("\nPlease enter the number associated with the city in which you work");
        // prompting the user to enter a city where they would like to stay
        System.out.print("City: ");
        city = scan.nextLine().trim();

        System.out.println(); // just so the command line doesn't become too crowded
        // checking if the specified city is in the set of valid hotel cities
        if (!validArr.contains(city)) {
            do {
                System.out.println ("Please choose only from the valid cities presented again below:");

                for (int i = 0; i < validArr.size(); i++)
                    System.out.println(validArr.get(i));
            
                System.out.print("City: ");
                city = scan.nextLine().trim();
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        int clientChoice = -1;
        String hotelIdChosen = "";
        
        // ask the customer which hotel they would like to stay in 
        while(clientChoice < 0 || clientChoice > hotelsInCity.size()) {
            System.out.println("Enter the number associated with the hotel you work in.\n");

            for (int i = 0; i < hotelsInCity.size(); i++) {
                System.out.println(i + ":\t" + hotelsInCity.get(i));
            }
            System.out.print("\nChoice: ");
            clientChoice = Integer.parseInt(scan.nextLine().trim());
        }
        hotelIdChosen = cityHotelIds.get(clientChoice);
        return hotelIdChosen;
    }

    public static ArrayList<ArrayList<String>> viewReservations(Scanner scan, Connection con, String phone) {
        ArrayList<ArrayList<String>> resultList = new ArrayList<ArrayList<String>>();
        ArrayList<String> addressList = new ArrayList<String>();
        ArrayList<String> roomTypeList = new ArrayList<String>();
        ArrayList<String> arrivalDateList = new ArrayList<String>();
        ArrayList<String> departDateList = new ArrayList<String>();
        ArrayList<String> roomCostList = new ArrayList<String>();
        String firstName = "";
        String lastName = "";
        long phoneNum = Long.valueOf(phone);
        ResultSet rs;

        // gathering all of the reservations associated with the phoneNumber the customer entered
        try (CallableStatement cs = con.prepareCall("begin view_reservations(?,?); end;")) {
            cs.setLong(1, phoneNum);
            cs.registerOutParameter(2, Types.REF_CURSOR);
            cs.execute();
            rs = (ResultSet)cs.getObject(2);

            if (!rs.next()){
                System.out.println("There are no reservations associated with the phone-number entered");
                return resultList;
            }
            else {
                firstName = rs.getString("first_name");
                lastName = rs.getString("last_name");
                do {
                    String bldgNum = Integer.toString(rs.getInt("building_number "));
                    String street = rs.getString("street");
                    String city = rs.getString("city");
                    String home_state = rs.getString("home_state");
                    String zip = Integer.toString(rs.getInt("zip_code"));
                    String hAddress = bldgNum + " " + street + " " + city + " " + home_state + " " + zip;
                    String room = rs.getString("room_type");
                    addressList.add(hAddress);
                    roomTypeList.add(room);
                    arrivalDateList.add(rs.getDate("arrival_date").toString());
                    departDateList.add(rs.getDate("depart_date").toString());
                } while (rs.next());
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        // gathering the price for the reservations
        for (int i = 0; i < roomTypeList.size(); i++) {
            try (CallableStatement pC = con.prepareCall("{call price_calculator(?,?,?,?)")) {
                pC.setDate(1, Date.valueOf(arrivalDateList.get(i)));
                pC.setDate(2, Date.valueOf(departDateList.get(i)));
                pC.setString(3, roomTypeList.get(i));
                pC.registerOutParameter(4, Types.NUMERIC);
                pC.execute();

                String roomCost = Long.toString(pC.getLong(4));
                roomCostList.add(roomCost);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Generate a print statement template to print a header for 3 columns in a print statement of widths 20, 10, 10 respectively
        

        System.out.println("Here are the reservations associated with phone number: " + phone);
        System.out.println();
        System.out.println("Hotel Address")

        for (int i = 0; i < roomTypeList.size(); i++) {
            
        }

    }
    
    public static int frontDeskInterface(Scanner scan, Connection con) {
        
        String hotelIdWork = frontDeskCity(scan, con);
        int init = -1;
        String message = ("Enter the number associated with what you would like to do.\n0:\tBook a reservation for a customer.\n1:\tCreate/Update a customer account.\n2:\tView customer reservations.\n3:\tCheck in a customer.\n4:\tCheck out a customer\n5:\tReturn to main menu.\n\nChoice: ");
        int userChoice = -1;

        while (userChoice != 3) {
            userChoice = rangeChecker(scan, init, 3, 0, message);
            if (userChoice == 0){ // Book a reservation
                ArrayList<String> reservation_info = chooseHotelCustomer(scan, con);
                // would like to enter main menu
                if (reservation_info.get(10).equals("kill")){
                    continue;
                }
                // gathering customer information
                ArrayList<String> customer_info = knowYourCustomer(scan, con);
                // would like to enter main menu
                if (customer_info.get(10).equals("kill")){
                    continue;
                }
                System.out.println();
                // determines whether the customer is returning or new and proceses them as needed
                customer_info = processCustomer(scan, con, customer_info);
                //System.out.println(customer_info.toString());
                System.out.println();
                // create the reservation
                setReservations(scan, con, reservation_info, customer_info);
            }
            if (userChoice == 1) { // create/update an account
                // gathering customer information
                ArrayList<String> customer_info = knowYourCustomer(scan, con);
                // would like to enter main menu
                if (customer_info.get(10).equals("kill")){
                    continue;
                }
                System.out.println();
                // determines whether the customer is returning or new and proceses them as needed
                customer_info = processCustomer(scan, con, customer_info);
            }
            if (userChoice == 2) { // view customer reservations
                
            }
            if (userChoice == 3) { // check in a customer

            }
            if (userChoice == 4) { // check out a customer

            }

        }
        return 0;
    }


}