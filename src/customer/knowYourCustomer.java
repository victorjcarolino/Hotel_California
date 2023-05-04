package customer;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Scanner;

public class knowYourCustomer {
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
}
