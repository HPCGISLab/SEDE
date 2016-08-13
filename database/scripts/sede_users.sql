--
-- Name: sede_users; Type: TABLE; Schema: public; Owner: sede; Tablespace:
--

CREATE TABLE sede_users (
    user_name character varying(100) NOT NULL,
    password character varying(30),
    email character varying(100)
);


ALTER TABLE sede_users OWNER TO sede;

--
-- Name: sede_users_email_key; Type: CONSTRAINT; Schema: public; Owner: sede; Tablespace:
--

ALTER TABLE ONLY sede_users
    ADD CONSTRAINT sede_users_email_key UNIQUE (email);


--
-- Name: sede_users_pkey; Type: CONSTRAINT; Schema: public; Owner: sede; Tablespace:
--

ALTER TABLE ONLY sede_users
    ADD CONSTRAINT sede_users_pkey PRIMARY KEY (user_name);
