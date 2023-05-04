create or replace PROCEDURE CUSTOMER_PROCESSING(
    u_id in out customers.customer_id%type,
    fName in out customers.first_name%type,
    lName in out customers.last_name%type,
    uPhone in out customers.phone_number%type, 
    uBldgNum in out customers.building_number%type,
    uStreet in out customers.street%type,
    uCity in out customers.city%type,
    uState in out customers.home_state%type,
    uZip in out customers.zip_code%type,
    uCred in out customers.credit_card%type,
    customer_status in number -- 0 for new, 1 for returning
)AS 
    utilCursor sys_refcursor;
BEGIN

    if customer_status = 0 then -- new customer
        insert into customers values (u_id, fName, lName, uPhone, uBldgNum, uStreet, uCity, uState, uZip, uCred);
    end if;
    if customer_status = 1 then -- returning customer
        update customers set phone_number = uPhone,
                             building_number = uBldgNum,
                             street = uStreet,
                             city = uCity,
                             home_state = uState,
                             zip_code = uZip,
                             credit_card = uCred
                         where customer_id = u_id;
    end if;
END;