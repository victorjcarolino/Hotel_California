create or replace PROCEDURE HOTEL_CITIES(
    hotel_cursor in out my_var_pkg.my_refcur_typ
    )AS 
BEGIN
    open hotel_cursor for 
        select hotels.city
        from hotels;
EXCEPTION
    when no_data_found then raise;
    close hotel_cursor;
END;