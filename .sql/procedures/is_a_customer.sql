create or replace PROCEDURE IS_A_CUSTOMER 
(
    uFirstName IN OUT customers.first_name%type, -- in out not b/c i actually need this to be outputted in java but i need to be able to fetch into it
    uLastName IN OUT customers.last_name%type, -- ^
    uPhoneNum IN OUT customers.phone_number%type, -- ^
    uFrequentGuestNum IN customers.phone_number%type,
    status_code OUT number, -- will be used to determine if the user is a customer or not
    u_id OUT customers.customer_id%type -- will only be used to maintain hold of the customer we are servicing
) AS 
    customer_cursor SYS_REFCURSOR;
BEGIN
    -- if customer enters a phone number, search will be done with phone number
    if uPhoneNum is not null and uFrequentGuestNum is null then
        open customer_cursor for
        select first_name, last_name, phone_number, customer_id
        from customers
        where phone_number = uPhoneNum;
        loop
            exit when customer_cursor%notfound;
            fetch customer_cursor 
            into uFirstName, uLastName, uPhoneNum, u_id;
        end loop;
    end if;
    
    -- if customer enters a frequent guest number, search will be done with frequent guest number
    if uFrequentGuestNum is not null and uPhoneNum is null then
        open customer_cursor for 
        select first_name, last_name, phone_number, customer_id
        from customers
        where customer_id = uFrequentGuestNum;
        loop
            exit when customer_cursor%notfound;
            fetch customer_cursor
            into uFirstName, uLastName, uPhoneNum, u_id;
        end loop;
    end if;
    
    -- if either are null, a search is done with the first and last name
    if uFrequentGuestNum is null and uPhoneNum is null then
        open customer_cursor for
        select customer_id, first_name, last_name, phone_number
        from customers
        where first_name = uFirstName
            and last_name = uLastName;
        
        loop
            exit when customer_cursor%notfound;
            fetch customer_cursor
            into uFirstName, uLastName, uPhoneNum, u_id;
        end loop;
    end if;
    
    status_code := 0;
    
    select count(phone_number)
    into status_code -- should be 1 since there is a UNIQUE constraint on phone_number if customer exists
    from customers 
    where phone_number = uPhoneNum;
    
exception
    when no_data_found then raise;
END;