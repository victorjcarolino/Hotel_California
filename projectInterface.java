import java.io.*;
import java.sql.*;
import java.util.ArrayList;
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

            String message = "Please enter the number associated with the interface you would like to access.\n0:\tCustomer\n1:\tFront Desk Agent\n2:\tHousekeeping\n\n\nChoice: ";

            int userC = rangeChecker(scan, 5, 2, 0, message);

            if (userC == 0){ //customer_interface
                customerInterface(scan, con);
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

    /**
     * 
     * @param scan
     * @param con
     * @param mode (1 => returning customer) (0 => new customer)
     * @throws SQLException
     * @return arrayList:
     * First Name
     * Last Name
     * Phone Number
     * customer_id
     * cred_card
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

                System.out.println("customer_id: " + customer_info.get(4));
                System.out.println("building_number: " + bldgNum);
                System.out.println("street_name: " + streetName);
                System.out.println("city_name: " + cityName);
                System.out.println("stateName: " + stateName);
                System.out.println("zip_code: " + zip);
                System.out.println("credit_card: " + credCard);

                System.out.println();

                // Display customer information 
                System.out.println("We were able to find an account you had with us in the past.\n");
                System.out.println("Name: " + customer_info.get(0) + customer_info.get(1));
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
                    addAddress = scan.nextLine();

                    // error checking in case the user entered anything other than Y or N
                    if (!addAddress.toUpperCase().equals("Y") && !addAddress.toUpperCase().equals("N")){
                        do{
                            System.out.println("Currently you have no address on file.");
                            System.out.print("Would you like to add an address to recieve junk mail for the rest of time? (Y/N): ");
                            addAddress = scan.nextLine();
                        }while (!addAddress.toUpperCase().equals("Y") && !addAddress.toUpperCase().equals("N"));
                    }
                } else {
                    System.out.println("Would you like to update your address on file?");
                    System.out.println("Current address: " + bldgNum + " " + streetName + " " + cityName + " " + stateName + " " + zip);
                    System.out.print("(Y/N): ");
                    addAddress = scan.nextLine();

                    // error checking in case the user entered anything other than Y or N
                    if (!addAddress.toUpperCase().equals("Y") && !addAddress.toUpperCase().equals("N")){
                        do{
                            System.out.print("Would you like to update your address on file? (Y/N): ");
                            System.out.println("Current address: " + bldgNum + " " + streetName + " " + cityName + " " + stateName + " " + zip);
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
                    if (bldgNum > 999999 || bldgNum < 1){
                        do {
                            System.out.println("That number was far too large to be a building number.");
                            System.out.println("Please enter your building number in the correct format (< 6 chars long): ");
                            bldgNum = Integer.parseInt(scan.nextLine()); 
                        } while (bldgNum > 999999 || bldgNum < 1);
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
                    if (zip > 99999 || zip < 0){
                        do {
                            System.out.println("I'm sorry, we cannot store a city name that large/small. Consider truncating if too large.");
                            System.out.println("Please enter the city associated with your address (< 15 chars long): ");
                            cityName = scan.nextLine(); 
                        } while (zip > 99999 || zip < 0);
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
                    if (sanitizer.length() > 10 || sanitizer.length() < 10 || sanitizer == null || sanitizer.substring(0,1).equals("0")) {
                        do {
                            // Prompting the user to enter their new phone number
                            System.out.println("The desired format is in form ########## with area code included.");
                            System.out.println("Please note that leading zero's (0#########) are not permitted");
                            System.out.println("Please only enter the numbers WITHOUT any symbols ( ex: (), -)");
                            System.out.print("New phone number: ");
                            sanitizer = scan.nextLine();
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
                            exitFlag = scan.nextLine();
                            System.out.print("New phone number: ");
                            sanitizer = scan.nextLine();
                        } while((customer_phones.contains(newPhoneNum)) || (sanitizer.length() > 9 || sanitizer.length() < 9) || (!exitFlag.toUpperCase().equals("EXIT")) || sanitizer.substring(0,1).equals("0") );
                    }

                    newPhoneNum = Long.parseLong(sanitizer);
                    currPhoneNum = newPhoneNum;

                }

                // If the user does not have a credit card on file, ask if they'd like to enter a credit card to be kept on file
                if (credCard == -1) {
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
                    if (sanitizer.length() < 13 || sanitizer.length() > 16) {
                        do {
                            System.out.println("There seems to be an issue with your previous input.");
                            System.out.println("Please remember to only use number digits without spaces.");
                            System.out.println("New credit card: ");
                            sanitizer = scan.nextLine();
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
                    customer_info.remove(customer_info.get(3));
                    customer_info.add(credCardString);
                }
                else { 
                    customer_info.remove(customer_info.get(3));
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
                if (bldgNum > 999999 || bldgNum < 1){
                    do {
                        System.out.println("That number was far too large to be a building number.");
                        System.out.println("Please enter your building number in the correct format (< 6 chars long): ");
                        bldgNum = Integer.parseInt(scan.nextLine()); 
                    } while (bldgNum > 999999 || bldgNum < 1);
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
                if (zip > 99999 || zip < 0){
                    do {
                        System.out.println("I'm sorry, we cannot store a city name that large/small. Consider truncating if too large.");
                        System.out.println("Please enter the city associated with your address (< 15 chars long): ");
                        cityName = scan.nextLine(); 
                    } while (zip > 99999 || zip < 0);
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
                if (sanitizer.length() < 13 || sanitizer.length() > 16) {
                    do {
                        System.out.println("There seems to be an issue with your previous input.");
                        System.out.println("Please remember to only use number digits without spaces.");
                        System.out.println("New credit card: ");
                        sanitizer = scan.nextLine();
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
                    customerChoice = scan.nextLine();
                    System.out.println();
                }
                if (customerChoice.toUpperCase().equals("Y"))
                    processCustomer(scan, con, customer_info);
                
            }

            System.out.println("Congratulations on creating your account! Your new account information is as follows: ");
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

                currPhoneNum = confirmUpdate.getLong("phone_number");
                bldgNum = confirmUpdate.getInt("building_number");
                streetName = confirmUpdate.getString("street");
                cityName = confirmUpdate.getString("city");
                stateName = confirmUpdate.getString("home_state");
                zip = confirmUpdate.getInt("zip_code");
                credCard = confirmUpdate.getLong("credit_card");

                System.out.println("Name: " + uFirstName + " " + uLastName);
                System.out.println("Phone Number: " + currPhoneNum);
                if (bldgNum > 0) 
                    System.out.println("Current address: " + bldgNum + " " + streetName + " " + cityName + " " + stateName + " " + zip);
                else { System.out.println("Current address: "); }
                if (credCard > 0) {
                    System.out.println("Credit Card: " + credCard);
                    String credCardString = Long.toString(credCard);
                    customer_info.remove(customer_info.get(3));
                    customer_info.add(credCardString);
                }
                else { 
                    customer_info.remove(customer_info.get(3));
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
     * FirstName
     * LastName
     * PhoneNumber
     * StatusCode
     * customer_id
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
        if (phoneNum.length() > 10 || phoneNum.length() < 10 || phoneNum == null || phoneNum.substring(0,1).equals("0")) {
            do {
                // Prompting the user to enter their new phone number
                System.out.println("The desired format is in form ########## with area code included.");
                System.out.println("Please note that leading zero's (0#########) are not permitted");
                System.out.println("Please only enter the numbers WITHOUT any symbols ( ex: (), -)");
                System.out.print("Phone number: ");
                phoneNum = scan.nextLine();
            } while ((phoneNum.length() > 10 || phoneNum.length() < 10 || phoneNum == null || phoneNum.substring(0,1).equals("0")));
        }

        Long phoneNumLong = Long.parseLong(phoneNum);

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
     * @return arraylist: contains 
     * address of hotel 
     * room type the customer would like to stay in 
     * arrival day 
     * depart day 
     * hotel_id and 
     * desiredRoomPrice
     */
    public static ArrayList<String> chooseHotel(Scanner scan, Connection con) {
        String arrivalDate = "";
        LocalDate arrivalDateLocalDate;
        LocalDate departDateLocalDate;
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
            double desiredRoomPrice = -1;
            
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

            System.out.println();

            // ensures that the date is correct
            arrivalDateLocalDate = arrivalDateEnforcer(scan, arrivalDate);
            departDateLocalDate = departDateEnforcer(scan, arrivalDateLocalDate);
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
                        
                    userChoice = Integer.parseInt(scan.nextLine());
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
     * @return
     */
    static LocalDate arrivalDateEnforcer(Scanner scan, String arrivalDate) {
        LocalDate arrivalDateLiteral = LocalDate.now();
        LocalDate now = LocalDate.now();
        while (!arrivalDate.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            System.out.println("Please enter the date you would like to begin your stay with us.");
            System.out.println("Format: (YYYY-MM-DD)");
            System.out.println("Arrival Date: ");
            arrivalDate = scan.nextLine();
            System.out.println();
        }
        
        try {
            arrivalDateLiteral = LocalDate.parse(arrivalDate);

            // Error checking 
            if (arrivalDateLiteral.isBefore(now)) {
                do {
                    arrivalDate = "0";
                    while (!arrivalDate.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                        System.out.println("You cannot reserve a room from before today.");
                        System.out.println("Format: (YYYY-MM-DD)");
                        System.out.println("Arrival Date: ");
                        arrivalDate = scan.nextLine();
                        System.out.println();
                    }
                }while (arrivalDateLiteral.isBefore(now));
                arrivalDateLiteral = LocalDate.parse(arrivalDate);
            }
            return arrivalDateLiteral;
        } catch (DateTimeParseException e) {
            arrivalDate = "0";
            arrivalDateEnforcer(scan, arrivalDate);
        }
        return arrivalDateLiteral;
    }

    /**
     * used to make sure that the chosen departure date is valid
     * @param scan
     * @param arrivalDateLiteral
     * @return
     */
    static LocalDate departDateEnforcer(Scanner scan, LocalDate arrivalDateLiteral) {
        long numNights = 0;
        LocalDate departDateLiteral = LocalDate.now();
        try {
            while(numNights < 1 || numNights > 30) {
                System.out.println("How many nights would you like to stay with us?");
                System.out.println("Note: You may not reserve more than 30 days in a single reservation.");
                System.out.println("Note: The minimum reservation length is 1 night");
                System.out.println("If you would like to have an extended stay, feel free to book multiple reservations");
                System.out.print("Number of nights staying with us: ");    
                numNights = Long.parseLong(scan.nextLine());
                System.out.println();
            }
            departDateLiteral = arrivalDateLiteral.plusDays(numNights);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            departDateEnforcer(scan, arrivalDateLiteral);
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
                init = Integer.parseInt(scan.nextLine());
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
    
        String reservationIdString = "T";
        long count = 8000000000L;
            do {
                //System.out.println("test");
                if(!reservationIds.contains(Long.toString(count))) {
                    reservationIdString = Long.toString(count);
                    //System.out.println("customer_id: " + customer_id);
                }
                count++;
            }while(count < 8000000000L && reservationIds.contains(Long.toString(count-1)));
        
        try (CallableStatement reservation = con.prepareCall("{call set_reservation(?,?,?,?,?,?)}")) {
            reservation.setString(1, reservationIdString);
            reservation.setString(2, customer_info.get(3));
            reservation.setString(3, reservation_info.get(4));
            reservation.setString(4, reservation_info.get(1));
            reservation.setString(5, reservation_info.get(2));
            reservation.setString(6, reservation_info.get(3));
            System.out.println("Congratulations on booking your reservation at " + reservation_info.get(0) + " from " + reservation_info.get(2) + " to " + reservation_info.get(3));
            System.out.println("We look forward to seeing you then!");
        } catch (Exception e) {
            System.out.println("\nSorry there was an error creating your reservation. Please try again.\n");
            e.printStackTrace();
            System.out.println();
        }
    }
    
     
    /**
     * 
     * @param scan
     * @param con
     * @param reservation_info
     * @param customer_info
     */
    /* 
    public static void setPayments(Scanner scan, Connection con, ArrayList<String> reservation_info, ArrayList<String> customer_info) {
        int freqG = 0;
        double freqP = 0;
        int uC = 0;
        int init = 4;
        
        try (CallableStatement isAFrequentGuest = con.prepareCall("{call is_a_frequent_guest(?,?,?)}")) {
            isAFrequentGuest.setString(1, customer_info.get(3));
            isAFrequentGuest.registerOutParameter(2, Types.NUMERIC);
            isAFrequentGuest.registerOutParameter(3, Types.NUMERIC);
            
            freqG = isAFrequentGuest.getInt(2);
            freqP = isAFrequentGuest.getDouble(3);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Asking the user if they would like to join fgp
        if (freqG < 1) {
            uC = rangeChecker(scan, freqG, 1, 0, "Would you like to enter our frequent guest points program?\nEvery dollar you spend will recieve 5% back on frequent guest points to be used on future reservations!\nPlease enter 1 if you'd like to join. Otherwise, enter 0\n(0/1)");
        } else {
            uC = rangeChecker(scan, init, 1, 0, "Would you like to use a portion of your frequent guest points to pay for your reservation?\nFrequent Guest Points: " + Double.toString(freqP) + " \nIf you'd like to use your frequent guest points, enter 1, otherwise enter 0.\n(0/1): ");
        }

        if (uC == 1) {

        } else {

        }

        System.out.println("We will need to take ")
    }

        */

    /**
     * 
     * @param scan
     * @param con
     */
    public static void customerInterface(Scanner scan, Connection con){
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
                        System.out.println("\nThere seems to be an issue with the specified inputs you provided. Would you like to try again with different inputs? (Y or N)");
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
                System.out.println("\nThere seems to be an issue with the specified inputs you provided. Would you like to try again with different inputs? (Y or N)");
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

        // determines whether the customer is returning or new and proceses them as needed
        customer_info = processCustomer(scan, con, customer_info);

        // create the reservation
        setReservations(scan, con, reservation_info, customer_info);


    }

}