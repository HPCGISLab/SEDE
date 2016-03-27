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
-- Name: user_mentions; Type: TABLE; Schema: public; Owner: jajayaku; Tablespace: 
--

CREATE TABLE user_mentions (
    tweetid bigint NOT NULL,
    userid bigint NOT NULL
);


ALTER TABLE user_mentions OWNER TO jajayaku;

--
-- Name: user_mentions_pkey; Type: CONSTRAINT; Schema: public; Owner: jajayaku; Tablespace: 
--

ALTER TABLE ONLY user_mentions
    ADD CONSTRAINT user_mentions_pkey PRIMARY KEY (tweetid, userid);


--
-- Name: user_mentions_tweetid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: jajayaku
--

ALTER TABLE ONLY user_mentions
    ADD CONSTRAINT user_mentions_tweetid_fkey FOREIGN KEY (tweetid) REFERENCES tweets(tweetid);


--
-- PostgreSQL database dump complete
--

