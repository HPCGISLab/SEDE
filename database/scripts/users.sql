--
-- Name: users; Type: TABLE; Schema: public; Owner: sede; Tablespace:
--

CREATE TABLE users (
    uid bigint NOT NULL,
    url character varying(1000),
    name character varying(250),
    screen_name character varying(250),
    description character varying(1000),
    created_at timestamp without time zone,
    verified boolean,
    user_location character varying(250),
    user_location_coordinates geography(Point,4326),
    favourites_count integer,
    followers_count integer
);


ALTER TABLE users OWNER TO sede;

--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: sede; Tablespace:
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (uid);
