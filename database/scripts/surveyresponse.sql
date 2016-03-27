-- Table: public.survey_response

-- DROP TABLE public.survey_response;

CREATE TABLE public.survey_response
(
  response_id bigint NOT NULL DEFAULT nextval('survey_response_response_id_seq'::regclass),
  survey_id bigint,
  email character varying(100) NOT NULL,
  resp_1 smallint,
  resp_2 smallint,
  resp_3 smallint,
  resp_4 smallint,
  resp_5 smallint,
  tweetid bigint,
  CONSTRAINT survey_response_pkey PRIMARY KEY (response_id),
  CONSTRAINT survey_response_survey_id_fkey FOREIGN KEY (survey_id)
      REFERENCES public.survey (survey_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT survey_response_tweetid_fkey FOREIGN KEY (tweetid)
      REFERENCES public.tweets (tweetid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);

