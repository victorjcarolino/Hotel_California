create or replace PROCEDURE customer_ids(
    c_cursor out my_var_pkg.my_refcur_typ
    )AS 
BEGIN
    open c_cursor for 
        select customer_id, phone_number
        from customers;
EXCEPTION
    when no_data_found then raise;
    close c_cursor;
END;