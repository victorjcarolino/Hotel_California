-- Workspace
select building_number, street, city, home_state, zip_code, hotel_rooms.room_type
        from hotels full outer join reservations 
        on hotels.hotel_id = reservations.hotel_id
                    full outer join hotel_rooms
        on hotel_rooms.hotel_id = hotels.hotel_id
        and city = 'Erie'
        and city is not NULL
        and reservation_id is NULL;

------------------------------------------------
create table hotels
    (hotel_id               varchar(10) not null,
     building_number        numeric(4,0) not null,
     street                 varchar(15) not null,
     city                   varchar(15) not null,
     home_state             varchar(2) not null,
     zip_code               numeric(5,0) not null,
     primary key (hotel_id));
    
-- has attributes dependant on table hotels
create table amenities
    (hotel_id               varchar(10) not null,
     free_wifi              numeric(1) not null,
     gym                    numeric(1) not null,
     free_spa               numeric(1) not null,
     parking                numeric(1) not null,
     swimming_pool          numeric(1) not null,
     business_center        numeric(1) not null,
     shuttle_service        numeric(1) not null,
     free_breakfast         numeric(1) not null,
     room_service           numeric(1) not null,
     primary key (hotel_id, room_type),
     foreign key (hotel_id) references hotels
        on delete cascade);

-- has attributes dependant on table customers
create table reservations
    (reservation_id         varchar(10) not null, 
     customer_id            varchar(10) not null,
     hotel_id               varchar(10) not null,
     room_type              varchar(10) not null,
     arrival_date           date not null,
     depart_date            date not null,
     primary key (reservation_id),
     foreign key (hotel_id) references hotel
        on delete set null);

create table customers
    (customer_id            varchar(10) not null,
     first_name             varchar(15) not null,
     last_name              varchar(15) not null,
     phone_number           numeric(10,0) not null,
     building_number        numeric(4,0),
     street                 varchar(10),
     city                   varchar(15),
     home_state             varchar(2),
     zip_code               numeric(5,0),
     credit_card            numeric(16,0),     
     primary key (customer_id));

-- has attributes dependant on customers
create table frequent_guests
    (customer_id            varchar(10) not null,
     frequent_guest_points  numeric(7,2),
     primary key (customer_id),
     foreign key (customer_id) references customers
        on delete cascade);
    
-- has attributes dependant on customers
create table payments 
    (payment_id             varchar(10) not null,
     customer_id            varchar(10) not null,
     payment_method         varchar(3) not null,
     payment_cost           numeric(7,2) not null,
     credit_card            numeric(16,0),
     check (payment_method in ('usd', 'fgp')),
     primary key (payment_id),
     foreign key (customer_id) references customers
        on delete set null);
    
-- has attributes dependant on hotels
create table hotel_rooms
    (hotel_id               varchar(10) not null, 
     room_type              varchar(10) not null, 
     room_number            numeric(4,0) not null, 
     status                 varchar(5) not null check (status in ('clean', 'dirty')),
     primary key (hotel_id, room_number),
     foreign key (hotel_id) references hotels
        on delete cascade);

-- has attributes dependant on hotels and customers
create table occupied_rooms
    (hotel_id               varchar(10) not null,
     room_type              varchar(10) not null,
     room_number            numeric(4,0) not null,
     customer_id            varchar(10) not null,
     primary key (hotel_id, room_type, customer_id),
     foreign key (hotel_id) references hotels
        on delete cascade,
     foreign key (customer_id) references customers
        on delete set null,
     foreign key (hotel_id, room_number) references hotel_rooms
        on delete set null);

-- has attributes dependant on hotels and hotel_rooms
create table rates_info
    (hotel_id               varchar(10) not null,
     room_type              varchar(10) not null,
     room_cost              numeric(7,2),
     primary key (hotel_id, room_type),
     foreign key (hotel_id) references hotel
        on delete cascade,
     foreign key (hotel_id, room_type) references hotel_rooms
        on delete cascade);
    
-- has attributes dependant on hotels and customers 
create table check_in_out_info
    (check_in_id            varchar(10) not null,
     arrival_date           timestamp not null,
     depart_date            timestamp,
     reservation_id         varchar(10) not null,
     primary key (check_in_id),
     foreign key (reservation_id) references reservations
        on delete set null);
        
CREATE OR REPLACE PROCEDURE AVAILABLE_HOTELS(
    city in hotels.city%type,
    designated_arrival_date in reservations.arrival_date%type,
    designated_depart_date in reservations.depart_date%type,
    hotel_bldg_num out hotels.building_number%type,
    hotel_street out hotels.street%type,
    hotel_city out hotels.city%type,
    hotel_state out hotels.home_state%type,
    hotel_zip out hotels.zip_code%type
    ) AS 
    does_exist number(1) := 0;
    
    utilCursor sys_refcursor;
    reservation_ids reservations.reservation_id%type;
    arrival_dates reservations.arrival_date%type;
    depart_dates reservations.depart_date%type;
BEGIN
    select count(*)
    into does_exist
    from hotels inner join reservations
    on hotels.hotel_id = reservations.hotel_id
    where hotels.city = city;
    
    if does_exist = 0 then
        return;
    end if;
    
    select count(distinct hotels.hotel_id)
    into does_exist
    from hotels inner join reservations 
    on hotels.hotel_id = reservations.hotel_id
        and hotels.city = hotel_city
        and ((designated_arrival_date > arrival_date
            and designated_depart_date > depart_date
            and designated_depart_date > arrival_date
            and designated_arrival_date > depart_date)
        or (designated_arrival_date < arrival_date
            and designated_depart_date < depart_date
            and designated_arrival_date < depart_date
            and designated_depart_date < arrival_date));
    
    if does_exist = 0 then
        return;
    end if;
    
    open utilCursor for
        select building_number, street, city, home_state, zip_code
        from hotels inner join reservations 
        on hotels.hotel_id = reservations.hotel_id
            and hotels.city = hotel_city
            and (designated_arrival_date > arrival_date
                and designated_depart_date > depart_date
                and designated_depart_date > arrival_date
                and designated_arrival_date > depart_date)
            or (designated_arrival_date < arrival_date
                and designated_depart_date < depart_date
                and designated_arrival_date < depart_date
                and designated_depart_date < arrival_date);
    loop
        fetch utilCursor 
        into hotel_bldg_num, hotel_street, hotel_city, hotel_state, hotel_zip;
        exit when utilCursor%notfound;
    end loop;
    close utilCursor;
exception
    when no_data_found then raise;
END;


     