Name: Victor Carolino

Hotel California

Where I got my data:
    - Mockaroo (majority of data population) + Oraclec Data Import Wizard
    - Individual testing

Changes to ER Diagram and Relational Design
    - I added reservation_id to table payments in order to refernce the two together, and I removed customer_id from payments as it can be inferred from reservation_id
        - this change is reflected by the updated er-diagram pdf included in submission

Considerations with testing
    - This project was initially compiled with all documents in a single directory
    - When testing to make sure that all user inputs are validated, following inputs may need to be entered twice in order for the while loop to catch that the control flow change condition has been met.
    - In the event that an exception is caught, a message stating that an unexpected power outage has occured will be printed and the program will exit, prompting the user to start the program once more.
    - Time is handled by java.time.*
        - all logical constraints with time are with respect to EST
    - I use stored procedures heavily in my implementation, and a special variable type has been created in a package called my_var_pkg in the db.
        - this special variable is necessary to run my implementation
    - There is a Makefile in the vjc225 directory

Interfaces that I implemented
    - Customer
    - Front Desk Associate
    - Housekeeper

Functionality that each interface can complete
    - Customer
        - create reservations
        - update phone number, address, credit card
        - create account
        - view reservations
            - cancel reservations
    - Front Desk Associate
        - create reservations (at specific hotel location of work)
        - update phone number, address, credit card, and create account for customer
        - view customer reservations (at any hotel)
            - cancel reservations (at any hotel)
        - check in a customer
        - check out a customer
    - Housekeeper
        - mark dirty rooms clean after cleaning them (at specific location of work)

Check-in logic:
    - customer will pay at check-in
    - in order to recieve frequent guest points for a stay, a customer must select the frequent guest points option when paying regardless if they have a frequent guest account
    - earliest check-in is 12pm on arrival day
    - cannot upgrade rooms
    - will get frequent guest points (5% of reservation cost) at check-out

Check-out logic:
    - can check out late, but it will be kept on record and front desk associate will be told to tell the customer that punitive action could be taken if the behavior continues
    - customer will be asked which room they stayed in (I understand this is not good business practice, but for the sake of this assignment, Hotel California a la Victor hopes customers will be honest)
        - regardless, the number of available/dirty/occupied rooms will be correct
    - after a customer checks out, the room will be marked dirty at which point someone needs to go to the housekeeping module in order to mark the room as clean

Cancel reservations:
    - users cannot cancel reservations whose departure date has passed or reservations which have already been checked in for
    - to cancel a reservation, a user must first view the reservations to access the delete reservation option

Reservation considerations:
    - the maximum reservation length is 30 days and the minimum is a day 
        - when doing data population, this was not taken into consideration, but when looking at the legality of hotel stays, a hotel must kick out a customer after 30 days of staying, so I later implemented this constraint to be reflected for future reservations
    - these reservations will remain in the database and remain valid reservations until their departure dates have passed
    - if a user checks in late, they will be charged the full amount
    - if a user checks out late, they will be charged the full amount and no more
    - if a user checks out early, they will be charged the full amount and no more