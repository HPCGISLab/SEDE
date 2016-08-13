--
-- Name: survey_response; Type: TABLE; Schema: public; Owner: sede; Tablespace:
--

CREATE TABLE survey_response (
    response_id bigint NOT NULL,
    survey_id bigint,
    email character varying(100) NOT NULL,
    resp_1 smallint,
    resp_2 smallint,
    resp_3 smallint,
    resp_4 smallint,
    resp_5 smallint,
    tweetid bigint
);


ALTER TABLE survey_response OWNER TO sede;

--
-- Name: survey_response_response_id_seq; Type: SEQUENCE; Schema: public; Owner: sede
--

CREATE SEQUENCE survey_response_response_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE survey_response_response_id_seq OWNER TO sede;

--
-- Name: survey_response_response_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: sede
--

ALTER SEQUENCE survey_response_response_id_seq OWNED BY survey_response.response_id;


--
-- Name: response_id; Type: DEFAULT; Schema: public; Owner: sede
--

ALTER TABLE ONLY survey_response ALTER COLUMN response_id SET DEFAULT nextval('survey_response_response_id_seq'::regclass);

--
-- Name: survey_response_pkey; Type: CONSTRAINT; Schema: public; Owner: sede; Tablespace:
--

ALTER TABLE ONLY survey_response
    ADD CONSTRAINT survey_response_pkey PRIMARY KEY (response_id);


--
-- Name: survey_response_survey_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: sede
--

ALTER TABLE ONLY survey_response
    ADD CONSTRAINT survey_response_survey_id_fkey FOREIGN KEY (survey_id) REFERENCES survey(survey_id);


--
-- Name: survey_response_tweetid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: sede
--

ALTER TABLE ONLY survey_response
    ADD CONSTRAINT survey_response_tweetid_fkey FOREIGN KEY (tweetid) REFERENCES tweets(tweetid);

