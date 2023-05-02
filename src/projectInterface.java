import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
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
            ArrayList<String> desired_room = chooseHotel(scan, con);
            String client_checker = "Y";
            while (desired_room.isEmpty() && client_checker.equals("Y")) {
                System.out.println("There seems to be an issue with the specified inputs you provided. Would you like to try again with different inputs? (Y or N)");
                client_checker = scan.nextLine();
                client_checker.toUpperCase();
                if (client_checker.equals("Y"))
                    desired_room = chooseHotel(scan, con); 
                else if (!client_checker.equals("N")){
                    client_checker = "Y";
                    System.out.println("Not a valid input (valid inputs = Y or N)");
                }
            }

            // asking the user to select an option and gathering user information
            con.close();
        }
        scan.close();
    }

    // public static void create_reservation(Scanner scan, Connection con) {
        
    // }
 
    /**
     * gathers user input for desired city and arrival/departure dates.
     * @param scan
     * @param con
     * @return resultList: contains address of hotel and respective room type the customer would like to stay in
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
                } ;
            }
            
            // beginning of interface interaction
            System.out.println("Hello, valued customer. Please enter the city you would like to book a stay in.");
            System.out.println("Here are the following cities in which we have hotels. Please only select a valid city!");

            // displaying arraylist of cities with hotels in them
            for (int i = 0; i < validArr.size(); i++)
                System.out.println(validArr.get(i));
           
            // prompting the user to enter a city where they would like to stay
            System.out.print("City: ");
            city = scan.nextLine();

            // checking if the specified city is in the set of valid hotel cities
            if (!validArr.contains(city)) {
                do {
                    System.out.println ("Please choose only from the valid cities presented again below:");

                    for (int i = 0; i < validArr.size(); i++)
                        System.out.println(validArr.get(i));
               
                    System.out.print("City: ");
                    city = scan.nextLine();
                } while (!validArr.contains(city));
            }

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
            String desiredAddress = "";
            String desiredRoom = "";

            // calling the available_hotel procedure to gather an arrayList of all hotel rooms available in the city specified
            try(CallableStatement gather_hotel_rooms = con.prepareCall("begin available_hotels(?,?,?,?); end;"))
            {
                ResultSet available_hotel_rooms;
                gather_hotel_rooms.setString(1, city);
                gather_hotel_rooms.setDate(2, arrivalDateLiteral);
                gather_hotel_rooms.setDate(3, departDateLiteral);
                gather_hotel_rooms.registerOutParameter(4, Types.REF_CURSOR);
                gather_hotel_rooms.execute();
                available_hotel_rooms = (ResultSet)gather_hotel_rooms.getObject(4);
                if (!available_hotel_rooms.next()) {
                    System.out.println("There seems to be no hotels. Hmmmmm...");
                }
                else {
                    // keeping track of all distinct hotels and the room types available in them
                    String oneBanger = ""; // going to be used in case only one (1) hotel address exists for a given city
                    ArrayList<String> recordKeeper = new ArrayList<String>();
                    HashMap<String,ArrayList<String>> hotels_in_area = new HashMap<String,ArrayList<String>>();

                    while (available_hotel_rooms.next()) {
                        String hotel_address = Integer.toString(available_hotel_rooms.getInt(1)) + " " + available_hotel_rooms.getString(2)
                            + " " + available_hotel_rooms.getString(3) + " " + available_hotel_rooms.getString(4) 
                            + " " + Integer.toString(available_hotel_rooms.getInt(5));
                        if (!hotels_in_area.containsKey(hotel_address)) {
                            oneBanger = hotel_address; // will only be relevant when only 1 hotel exists in the given city
                            recordKeeper.add(hotel_address);
                            hotels_in_area.put(hotel_address, new ArrayList<String>());
                        }
                        // placing all distinct room types in an arrayList mapped to a key of the hotel_address
                        if (!hotels_in_area.get(hotel_address).contains(available_hotel_rooms.getString(6))) {
                            hotels_in_area.get(hotel_address).add(available_hotel_rooms.getString(6));
                        }
                    } 
                    // Displaying all of the available hotel rooms in the available hotels in the specified city
                    int counter = 0;
                    while (!recordKeeper.isEmpty()){
                        System.out.println("Here are the room_types that can be reserved at the hotel located at " + recordKeeper.get(counter) + ":");
                        System.out.println(hotels_in_area.get(recordKeeper.get(counter)));
                        recordKeeper.remove(counter);
                        counter++;
                    }

                    System.out.println(); // Just so the console doesn't become too crowded

                    // Prompting the user to choose a hotel address in their given city
                    if (hotels_in_area.size() > 1){
                        System.out.println("Please choose the address and room type that you would like to stay in (EXACTLY as displayed in the options above)");
                        System.out.println("Desired Address: ");
                        desiredAddress = scan.nextLine();

                        // input validation for desiredAddress
                        if (!hotels_in_area.containsKey(desiredAddress)) {
                            while (!hotels_in_area.containsKey(desiredAddress)) {
                                System.out.println(); // Just so the console doesn't become too crowded
                                System.out.println("There seems to be an issue with your last entry.");
                                System.out.println("It is imperitive that you please enter the address EXACTLY as displayed in the options above: ");
                                System.out.print("Desired Address: ");
                                desiredAddress = scan.nextLine();
                            }
                        }
                        resultList.add(desiredAddress);
                        System.out.println(); // Just so the console doesn't become too crowded
                    }
                    else
                        desiredAddress = oneBanger;
                    

                    System.out.println("Please select the room type that you would like to stay in.");
                    System.out.println("Remember, you can only choose from the rooms that are available in your desired hotel.");
                    System.out.println("To refresh your memory, the available hotel rooms that you can choose from are: " + hotels_in_area.get(desiredAddress));
                    System.out.println("Again, please only enter your choice as it is exactly displayed (no funny business with alternate capitalization >:( ");
                    System.out.print("Desired Room Type: ");
                    desiredRoom = scan.nextLine();


                    while(!hotels_in_area.get(desiredAddress).contains(desiredRoom)) {
                        System.out.println("There seems to be an issue with your last entry.");
                        System.out.println("It is imperitive that you please enter the room type EXACTLY as displayed in the options above: ");
                        System.out.println("Again, your options are: "+ hotels_in_area.get(desiredAddress));
                        System.out.print("Desired Room Type: ");
                        desiredAddress = scan.nextLine();
                    }
                    resultList.add(desiredAddress);
                }
            }
            return resultList;
        }
        catch (InputMismatchException e){
            System.out.println("Input mismatch exception triggered in gather_available_hotels()");
            System.out.println(e);
            return resultList;
        } catch (SQLException e) {
            System.out.println("SQL exception triggered in gather_available_hotels()");
            e.printStackTrace();
            return resultList;
        } catch (Exception e) {
            System.out.println("Exception triggered in gather_available_hotels");
            System.out.println(e);
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
     * @return
     */
    public static int date_checker(Scanner scan, int mode, int input, int auxYear, int auxMonth, int auxDay, int year_diff, int month_diff){
        if (mode == 1) {
           // checking if the arrival year is valid
           if (input < 2023) {
               do{
                   System.out.println("Enter your arrival year below in the format YYYY");
                   System.out.println("Please select a valid arrival year (arrival_year >= 2023)");
                   System.out.print("Arrival Year: ");
                   input = scan.nextInt();
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
                    input = scan.nextInt();
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
                input = scan.nextInt();
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
                    input = scan.nextInt();
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
                input = scan.nextInt();
            } while (nonValidMonths.contains(input) || input < 1);   
            return input;          
        } 
        else if (mode == 6) {
            ArrayList<Integer> nonValidDays = new ArrayList<Integer>();
            // checking if the departure day is valid
            if (year_diff < 1 && month_diff != 0){ // arrival and depart on same month of same year
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
                    System.out.println("Enter your arrival day below in the format DD.");
                    System.out.println("Please select a valid arrival day");
                    System.out.println("For reference your arrival date is (in format DD/MM/YYYY): " + auxDay + "/" + auxMonth
                    + "/" + auxYear);
                    System.out.print("Arrival Day: ");
                    input = scan.nextInt();
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
                    input = scan.nextInt();
                } while (input > greatestDay || input < 1);
                return input;
            }
        }
        return input;
    }
}