--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: users; Type: TABLE; Schema: public; Owner: jajayaku; Tablespace: 
--

CREATE TABLE users (
    uid bigint NOT NULL,
    url character varying(1000),
    name character varying(250),
    screen_name character varying(250),
    description character varying(250),
    created_at timestamp without time zone,
    verified boolean,
    user_location character varying(250),
    user_location_coordinates geography(Point,4326),
    favourites_count integer,
    followers_count integer
);


ALTER TABLE users OWNER TO jajayaku;

--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: jajayaku; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (uid);


--
-- PostgreSQL database dump complete
--

