create or replace PROCEDURE AVAILABLE_HOTELS2 (
    hotelId in hotels.hotel_id%type,
    designated_arrival_date in reservations.arrival_date%type,
    designated_depart_date in reservations.depart_date%type,
    
    aSingleRooms out number,
    aDoubleRooms out number,
    aDeluxeRooms out number,
    aStudioRooms out number,
    aPresRooms out number,
    aSuiteRooms out number
    ) AS

    hotel_num number := 0;
    
    totSingleRooms number := 0;
    totDoubleRooms number := 0;
    totDeluxeRooms number := 0;
    totStudioRooms number := 0;
    totPresRooms number := 0;
    totSuiteRooms number := 0;
    
    resSingleRooms number := 0;
    resDoubleRooms number := 0;
    resDeluxeRooms number := 0;
    resStudioRooms number := 0;
    resPresRooms number := 0;
    resSuiteRooms number := 0;
    
BEGIN
    -- gathering the total number of single rooms for a given hotel_id
    select count(room_number) into totSingleRooms from hotel_rooms where hotel_id = hotelID and room_type = 'Single';
        
    -- gathering the total number of double rooms for a given hotel_id
    select count(room_number) into totDoubleRooms from hotel_rooms where hotel_id = hotelID and room_type = 'Double';
    
    -- gathering the total number of deluxe rooms for a given hotel_id
    select count(room_number) into totDeluxeRooms from hotel_rooms where hotel_id = hotelID and room_type = 'Deluxe';
        
    -- gathering the total number of studio rooms for a given hotel_id
    select count(room_number) into totStudioRooms from hotel_rooms where hotel_id = hotelID and room_type = 'Studio';
    
    -- gathering the total number of suite rooms for a given hotel_id
    select count(room_number) into totSuiteRooms from hotel_rooms where hotel_id = hotelID and room_type = 'Suite';
        
    -- gathering the total number of presidential rooms for a given hotel_id
    select count(room_number) into totPresRooms from hotel_rooms where hotel_id = hotel_ID and room_type = 'Presidential';
        
    -- gathering the total number of rooms of type single that are reserved from arrivaldate to departdate
    select count(room_type)
    into resSingleRooms
    from reservations
    where hotel_id = hotelID
        and room_type = 'Single'
        and (not(designated_arrival_date > arrival_date 
                and designated_depart_date > depart_date
                and designated_depart_date > arrival_date
                and designated_arrival_date > depart_date)
            or (not(designated_arrival_date < arrival_date
                and designated_depart_date < depart_date
                and designated_arrival_date < depart_date
                and designated_depart_date < arrival_date)));
                
    -- gathering the total number of rooms of type double that are reserved from arrivaldate to departdate
    select count(room_type)
    into resDoubleRooms
    from reservations
    where hotel_id = hotelID
        and room_type = 'Double'
        and (not(designated_arrival_date > arrival_date 
                and designated_depart_date > depart_date
                and designated_depart_date > arrival_date
                and designated_arrival_date > depart_date)
            or (not(designated_arrival_date < arrival_date
                and designated_depart_date < depart_date
                and designated_arrival_date < depart_date
                and designated_depart_date < arrival_date)));
                
    -- gathering the total number of rooms of type deluxe that are reserved from arrivaldate to departdate
    select count(room_type)
    into resDeluxeRooms
    from reservations
    where hotel_id = hotelID
        and room_type = 'Deluxe'
        and (not(designated_arrival_date > arrival_date 
                and designated_depart_date > depart_date
                and designated_depart_date > arrival_date
                and designated_arrival_date > depart_date)
            or (not(designated_arrival_date < arrival_date
                and designated_depart_date < depart_date
                and designated_arrival_date < depart_date
                and designated_depart_date < arrival_date)));
                
    -- gathering the total number of rooms of type studio that are reserved from arrivaldate to departdate
    select count(room_type)
    into resStudioRooms
    from reservations
    where hotel_id = hotelID
        and room_type = 'Studio'
        and (not(designated_arrival_date > arrival_date 
                and designated_depart_date > depart_date
                and designated_depart_date > arrival_date
                and designated_arrival_date > depart_date)
            or (not(designated_arrival_date < arrival_date
                and designated_depart_date < depart_date
                and designated_arrival_date < depart_date
                and designated_depart_date < arrival_date)));
    
    -- gathering the total number of rooms of type suite that are reserved from arrivaldate to departdate
    select count(room_type)
    into resSuiteRooms
    from reservations
    where hotel_id = hotelID
        and room_type = 'Suite'
        and (not(designated_arrival_date > arrival_date 
                and designated_depart_date > depart_date
                and designated_depart_date > arrival_date
                and designated_arrival_date > depart_date)
            or (not(designated_arrival_date < arrival_date
                and designated_depart_date < depart_date
                and designated_arrival_date < depart_date
                and designated_depart_date < arrival_date)));
                
    -- gathering the total number of rooms of type presidential that are reserved from arrivaldate to departdate
    select count(room_type)
    into resPresRooms
    from reservations
    where hotel_id = hotelID
        and room_type = 'Presidential'
        and (not(designated_arrival_date > arrival_date 
                and designated_depart_date > depart_date
                and designated_depart_date > arrival_date
                and designated_arrival_date > depart_date)
            or (not(designated_arrival_date < arrival_date
                and designated_depart_date < depart_date
                and designated_arrival_date < depart_date
                and designated_depart_date < arrival_date)));
                
    -- setting the number of available rooms for each room type
    aSingleRooms := totSingleRooms - resSingleRooms;
    aDoubleRooms := totDoubleRooms - resDoubleRooms;
    aStudioRooms := totStudioRooms - resStudioRooms;
    aDeluxeRooms := totDeluxeRooms - resDeluxeRooms;
    aSuiteRooms := totSuiteRooms - resSuiteRooms;
    aPresRooms := totPresRooms - resSuiteRooms;
END AVAILABLE_HOTELS2;