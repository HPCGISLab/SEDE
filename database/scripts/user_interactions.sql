--
-- Name: user_interactions; Type: TABLE; Schema: public; Owner: sede; Tablespace:
--

CREATE TABLE user_interactions (
    reference_tweetid bigint NOT NULL,
    originating_tweet_id bigint,
    originating_user_id bigint
);


ALTER TABLE user_interactions OWNER TO sede;

--
-- Name: user_interactions_pkey; Type: CONSTRAINT; Schema: public; Owner: sede; Tablespace:
--

ALTER TABLE ONLY user_interactions
    ADD CONSTRAINT user_interactions_pkey PRIMARY KEY (reference_tweetid);


--
-- Name: user_interactions_reference_tweetid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: sede
--

ALTER TABLE ONLY user_interactions
    ADD CONSTRAINT user_interactions_reference_tweetid_fkey FOREIGN KEY (reference_tweetid) REFERENCES tweets(tweetid);
