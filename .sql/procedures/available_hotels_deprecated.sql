create or replace PROCEDURE AVAILABLE_HOTELS_DEPRECATED(
    uCity in hotels.city%type,
    designated_arrival_date in reservations.arrival_date%type,
    designated_depart_date in reservations.depart_date%type,
    hotel_cursor out my_var_pkg.my_refcur_typ
    ) AS 
    does_exist number(2) := 0;
    
    utilCursor sys_refcursor;
BEGIN
    open hotel_cursor for
        select building_number, street, city, home_state, zip_code, hotel_rooms.room_type
        from hotels full join hotel_rooms 
        on hotels.hotel_id = hotel_rooms.hotel_id
                    full join reservations
        on hotel_rooms.hotel_id = reservations.hotel_id
        and hotel_rooms.room_type = reservations.room_type
        where city = uCity and city is not NULL
        and ((designated_arrival_date > arrival_date
            and designated_depart_date > depart_date
            and designated_depart_date > arrival_date
            and designated_arrival_date > depart_date)
            or (designated_arrival_date < arrival_date
                and designated_depart_date < depart_date
                and designated_arrival_date < depart_date
                and designated_depart_date < arrival_date)
            or reservation_id is NULL);
exception
    when no_data_found then raise;
END;