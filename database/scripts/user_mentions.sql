--
-- Name: user_mentions; Type: TABLE; Schema: public; Owner: sede; Tablespace:
--

CREATE TABLE user_mentions (
    tweetid bigint NOT NULL,
    userid bigint NOT NULL
);


ALTER TABLE user_mentions OWNER TO sede;

--
-- Name: user_mentions_pkey; Type: CONSTRAINT; Schema: public; Owner: sede; Tablespace:
--

ALTER TABLE ONLY user_mentions
    ADD CONSTRAINT user_mentions_pkey PRIMARY KEY (tweetid, userid);


--
-- Name: user_mentions_tweetid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: sede
--

ALTER TABLE ONLY user_mentions
    ADD CONSTRAINT user_mentions_tweetid_fkey FOREIGN KEY (tweetid) REFERENCES tweets(tweetid);

