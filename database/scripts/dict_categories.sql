--
-- Name: dict_categories; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--

CREATE TABLE dict_categories (
    categ_id integer NOT NULL,
    category character varying(200)
);


ALTER TABLE dict_categories OWNER TO postgres;

--
-- Name: dict_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace:
--

ALTER TABLE ONLY dict_categories
    ADD CONSTRAINT dict_categories_pkey PRIMARY KEY (categ_id);
