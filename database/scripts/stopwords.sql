--
-- Name: stopwords; Type: TABLE; Schema: public; Owner: sede; Tablespace:
--

CREATE TABLE stopwords (
    word character varying(1000) NOT NULL
);


ALTER TABLE stopwords OWNER TO sede;

--
-- Name: stopwords_pkey; Type: CONSTRAINT; Schema: public; Owner: sede; Tablespace:
--

ALTER TABLE ONLY stopwords
    ADD CONSTRAINT stopwords_pkey PRIMARY KEY (word);
