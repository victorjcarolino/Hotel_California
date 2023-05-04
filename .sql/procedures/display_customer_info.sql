create or replace PROCEDURE DISPLAY_CUSTOMER_INFO(
    u_id in customers.customer_id%type,
    fName out customers.first_name%type,
    lName out customers.last_name%type,
    phoneNum out customers.phone_number%type,
    bldgNum out customers.building_number%type,
    uStreet out customers.street%type,
    uCity out customers.city%type,
    uState out customers.home_state%type,
    uZip out customers.zip_code%type,
    uCred out customers.credit_card%type
) AS 
    utilCursor sys_refcursor;
BEGIN
    open utilCursor for 
    select first_name, last_name, phone_number, building_number, street, city, home_state, zip_code, credit_card
    from customers
    where customer_id = u_id;
    loop
        fetch utilCursor 
        into fName, lName, phoneNum, bldgNum, uStreet, uCity, uState, uZip, uCred;
        exit when utilCursor%notfound;
    end loop;
exception
    when no_data_found then raise;
END;