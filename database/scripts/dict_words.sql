--
-- Name: dict_words; Type: TABLE; Schema: public; Owner: sede; Tablespace:
--

CREATE TABLE dict_words (
    categ_id integer,
    categ_pattern text,
    id integer DEFAULT nextval('dict_words_id_seq'::regclass) NOT NULL
);


ALTER TABLE dict_words OWNER TO sede;

--
-- Name: dict_words_pkey; Type: CONSTRAINT; Schema: public; Owner: sede; Tablespace:
--

ALTER TABLE ONLY dict_words
    ADD CONSTRAINT dict_words_pkey PRIMARY KEY (id);


--
-- Name: dict_words_categ_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: sede
--

ALTER TABLE ONLY dict_words
    ADD CONSTRAINT dict_words_categ_id_fkey FOREIGN KEY (categ_id) REFERENCES dict_categories(categ_id);
