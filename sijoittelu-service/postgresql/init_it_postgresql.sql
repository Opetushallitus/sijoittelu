CREATE ROLE oph;
ALTER ROLE oph WITH login;
ALTER SCHEMA public OWNER TO oph;
GRANT ALL ON SCHEMA public TO oph;
