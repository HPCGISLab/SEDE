--
-- Name: states; Type: TABLE; Schema: public; Owner: sede; Tablespace:
--

CREATE TABLE states (
    gid integer NOT NULL,
    state_name character varying(25),
    drawseq smallint,
    state_fips character varying(2),
    sub_region character varying(20),
    state_abbr character varying(2),
    geom geometry(MultiPolygon,4269)
);


ALTER TABLE states OWNER TO sede;

--
-- Name: states_gid_seq; Type: SEQUENCE; Schema: public; Owner: sede
--

CREATE SEQUENCE states_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE states_gid_seq OWNER TO sede;

--
-- Name: states_gid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: sede
--

ALTER SEQUENCE states_gid_seq OWNED BY states.gid;


--
-- Name: gid; Type: DEFAULT; Schema: public; Owner: sede
--

ALTER TABLE ONLY states ALTER COLUMN gid SET DEFAULT nextval('states_gid_seq'::regclass);


--
-- Name: states_pkey; Type: CONSTRAINT; Schema: public; Owner: sede; Tablespace:
--

ALTER TABLE ONLY states
    ADD CONSTRAINT states_pkey PRIMARY KEY (gid);


--
-- Name: states_geom_idx; Type: INDEX; Schema: public; Owner: sede; Tablespace:
--

CREATE INDEX states_geom_idx ON states USING gist (geom);
