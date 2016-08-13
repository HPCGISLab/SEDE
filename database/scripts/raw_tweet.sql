
CREATE TABLE raw_tweet (
    id integer NOT NULL,
    body jsonb,
    created_at timestamp without time zone
);


ALTER TABLE raw_tweet OWNER TO sede;

--
-- Name: raw_tweet_id_seq; Type: SEQUENCE; Schema: public; Owner: sede
--

CREATE SEQUENCE raw_tweet_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE raw_tweet_id_seq OWNER TO sede;

--
-- Name: raw_tweet_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: sede
--

ALTER SEQUENCE raw_tweet_id_seq OWNED BY raw_tweet.id;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: sede
--ALTER TABLE ONLY raw_tweet
    ADD CONSTRAINT raw_tweet_pkey PRIMARY KEY (id);

                                                                                                                                                                                                                                             ALTER TABLE ONLY raw_tweet ALTER COLUMN id SET DEFAULT nextval('raw_tweet_id_seq'::regclass);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          --                                                                                                                                                                                                                                           -- Name: raw_tweet_pkey; Type: CONSTRAINT; Schema: public; Owner: sede; Tablespace:                                                                                                                                                          --                                                                                                
