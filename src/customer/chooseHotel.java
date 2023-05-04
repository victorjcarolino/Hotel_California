package customer;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class chooseHotel{

    /**
     * gathers user input for desired city and arrival/departure dates.
     * @param scan
     * @param con
     * @return arraylist: contains address of hotel and respective room type the customer would like to stay in w/ arrival and depart days and hotel_id 
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

     // used to make sure that the chosen departure date is valid
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
            }
            departDateLiteral = arrivalDateLiteral.plusDays(numNights);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            departDateEnforcer(scan, arrivalDateLiteral);
        }
        

        return departDateLiteral;
    }
}
