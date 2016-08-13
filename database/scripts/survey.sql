--
-- Name: survey; Type: TABLE; Schema: public; Owner: sede; Tablespace:
--

CREATE TABLE survey (
    survey_id bigint NOT NULL,
    created_at timestamp without time zone,
    created_email character varying(100) NOT NULL,
    categ_1 character varying(100),
    categ_2 character varying(100),
    categ_3 character varying(100),
    categ_4 character varying(100),
    categ_5 character varying(100),
    desc_1 text,
    desc_2 text,
    desc_3 text,
    desc_4 text,
    desc_5 text,
    survey_hash bigint,
    datainfo jsonb,
    survey_name character varying(30)
);


ALTER TABLE survey OWNER TO sede;

--
-- Name: survey_survey_id_seq; Type: SEQUENCE; Schema: public; Owner: sede
--

CREATE SEQUENCE survey_survey_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE survey_survey_id_seq OWNER TO sede;

--
-- Name: survey_survey_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: sede
--

ALTER SEQUENCE survey_survey_id_seq OWNED BY survey.survey_id;


--
-- Name: survey_id; Type: DEFAULT; Schema: public; Owner: sede
--

ALTER TABLE ONLY survey ALTER COLUMN survey_id SET DEFAULT nextval('survey_survey_id_seq'::regclass);


--
-- Name: survey_pkey; Type: CONSTRAINT; Schema: public; Owner: sede; Tablespace:
--

ALTER TABLE ONLY survey
    ADD CONSTRAINT survey_pkey PRIMARY KEY (survey_id);
--
-- Name: survey_survey_hash_key; Type: CONSTRAINT; Schema: public; Owner: sede; Tablespace:
--

ALTER TABLE ONLY survey
    ADD CONSTRAINT survey_survey_hash_key UNIQUE (survey_hash);
