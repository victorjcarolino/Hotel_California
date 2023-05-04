create or replace PROCEDURE HOTELS_PER_CITY(
    uCity in hotels.hotel_id%type,
    h_cursor out my_var_pkg.my_refcur_typ
    ) AS
BEGIN
    open h_cursor for
        select hotel_id, building_number, street, city, home_state, zip_code
        from hotels
        where city = uCity;
exception
    when no_data_found then raise;
    close h_cursor;
END;