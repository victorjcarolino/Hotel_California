package customer;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Scanner;

public class processCustomer {
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
                if (credCard > 0)
                    System.out.println("Credit Card: " + credCard);
                else { System.out.println("Credit Card: "); }
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
                System.out.println("test");
                if(!customer_ids.contains(Long.toString(count))) {
                    customer_id = Long.toString(count);
                    System.out.println("customer_id: " + customer_id);
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
                if (credCard > 0)
                    System.out.println("Credit Card: " + credCard);
                else { System.out.println("Credit Card: "); }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }
}
