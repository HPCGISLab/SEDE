--
-- Name: tweets; Type: TABLE; Schema: public; Owner: sede; Tablespace:
--

CREATE TABLE tweets (
    tweetid bigint NOT NULL,
    id integer,
    uid bigint,
    body character varying(2000),
    created_at timestamp without time zone,
    coordinates geography(Point,4326),
    coordinate_class smallint,
    tweet_type smallint,
    rt_enabled boolean,
    retweet_count integer,
    likes integer
);


ALTER TABLE tweets OWNER TO sede;

--
-- Name: tweets_pkey; Type: CONSTRAINT; Schema: public; Owner: sede; Tablespace:
--

ALTER TABLE ONLY tweets
    ADD CONSTRAINT tweets_pkey PRIMARY KEY (tweetid);


--
-- Name: created_at_index; Type: INDEX; Schema: public; Owner: sede; Tablespace:
--

CREATE INDEX created_at_index ON tweets USING btree (created_at);


--
-- Name: tweets_raw_tweet; Type: INDEX; Schema: public; Owner: sede; Tablespace:
--

CREATE INDEX tweets_raw_tweet ON tweets USING btree (id);


--
-- Name: tweets_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: sede
--

ALTER TABLE ONLY tweets
    ADD CONSTRAINT tweets_id_fkey FOREIGN KEY (id) REFERENCES raw_tweet(id);


--
-- Name: tweets_uid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: sede
--

ALTER TABLE ONLY tweets
    ADD CONSTRAINT tweets_uid_fkey FOREIGN KEY (uid) REFERENCES users(uid);

