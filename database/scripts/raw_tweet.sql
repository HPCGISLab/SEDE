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
-- Name: raw_tweet; Type: TABLE; Schema: public; Owner: jajayaku; Tablespace: 
--

CREATE TABLE raw_tweet (
    id integer NOT NULL,
    body jsonb,
    created_at timestamp without time zone
);


ALTER TABLE raw_tweet OWNER TO jajayaku;

--
-- Name: raw_tweet_id_seq; Type: SEQUENCE; Schema: public; Owner: jajayaku
--

CREATE SEQUENCE raw_tweet_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE raw_tweet_id_seq OWNER TO jajayaku;

--
-- Name: raw_tweet_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: jajayaku
--

ALTER SEQUENCE raw_tweet_id_seq OWNED BY raw_tweet.id;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: jajayaku
--

ALTER TABLE ONLY raw_tweet ALTER COLUMN id SET DEFAULT nextval('raw_tweet_id_seq'::regclass);


--
-- Name: raw_tweet_pkey; Type: CONSTRAINT; Schema: public; Owner: jajayaku; Tablespace: 
--

ALTER TABLE ONLY raw_tweet
    ADD CONSTRAINT raw_tweet_pkey PRIMARY KEY (id);


--
-- Name: raw_tweet_index; Type: INDEX; Schema: public; Owner: jajayaku; Tablespace: 
--

CREATE INDEX raw_tweet_index ON raw_tweet USING btree (created_at);


--
-- PostgreSQL database dump complete
--

