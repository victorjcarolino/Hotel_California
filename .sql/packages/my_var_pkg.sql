create or replace PACKAGE MY_VAR_PKG AS 

-- set up a weakly typed cursor variable for multiple use
    TYPE my_refcur_typ IS REF CURSOR;
    type my_arr_typ is table of varchar2(10);

END MY_VAR_PKG;