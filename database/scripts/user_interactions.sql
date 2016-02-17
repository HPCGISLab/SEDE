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
-- Name: user_interactions; Type: TABLE; Schema: public; Owner: jajayaku; Tablespace: 
--

CREATE TABLE user_interactions (
    reference_tweetid bigint NOT NULL,
    originating_tweet_id bigint,
    originating_user_id bigint
);


ALTER TABLE user_interactions OWNER TO jajayaku;

--
-- Name: user_interactions_pkey; Type: CONSTRAINT; Schema: public; Owner: jajayaku; Tablespace: 
--

ALTER TABLE ONLY user_interactions
    ADD CONSTRAINT user_interactions_pkey PRIMARY KEY (reference_tweetid);


--
-- Name: user_interactions_reference_tweetid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: jajayaku
--

ALTER TABLE ONLY user_interactions
    ADD CONSTRAINT user_interactions_reference_tweetid_fkey FOREIGN KEY (reference_tweetid) REFERENCES tweets(tweetid);


--
-- PostgreSQL database dump complete
--

