CREATE FUNCTION match_percent(in x bigint, in y bigint, out p real)
AS
$$
DECLARE
    counter bigint = 0;
    temp    bigint;
BEGIN
    temp := x # y;

    if (temp < 0) then
        temp := ~temp;
    end if;

    while (temp > 0)
        loop
            counter := counter + (temp & 1);
            temp := temp >> 1;
        end loop;

    p := 1 - cast(counter as double precision) / 64;

END;

$$ LANGUAGE plpgsql;
