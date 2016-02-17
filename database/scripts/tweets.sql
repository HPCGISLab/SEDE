--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: tweets; Type: TABLE; Schema: public; Owner: jajayaku; Tablespace: 
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


ALTER TABLE tweets OWNER TO jajayaku;

--
-- Name: tweets_pkey; Type: CONSTRAINT; Schema: public; Owner: jajayaku; Tablespace: 
--

ALTER TABLE ONLY tweets
    ADD CONSTRAINT tweets_pkey PRIMARY KEY (tweetid);


--
-- Name: tweets_raw_tweet; Type: INDEX; Schema: public; Owner: jajayaku; Tablespace: 
--

CREATE INDEX tweets_raw_tweet ON tweets USING btree (id);


--
-- Name: tweets_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: jajayaku
--

ALTER TABLE ONLY tweets
    ADD CONSTRAINT tweets_id_fkey FOREIGN KEY (id) REFERENCES raw_tweet(id);


--
-- Name: tweets_uid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: jajayaku
--

ALTER TABLE ONLY tweets
    ADD CONSTRAINT tweets_uid_fkey FOREIGN KEY (uid) REFERENCES users(uid);


--
-- PostgreSQL database dump complete
--

