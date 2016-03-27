-- Table: public.survey

-- DROP TABLE public.survey;

CREATE TABLE public.survey
(
  survey_id bigint NOT NULL DEFAULT nextval('survey_survey_id_seq'::regclass),
  created_at timestamp without time zone,
  created_email character varying(100) NOT NULL,
  categ_1 character varying(100),
  categ_2 character varying(100),
  categ_3 character varying(100),
  categ_4 character varying(100),
  categ_5 character varying(100),
  desc_1 text,
  desc_2 text,
  desc_3 text,
  desc_4 text,
  desc_5 text,
  survey_hash bigint,
  dataquery text,
  CONSTRAINT survey_pkey PRIMARY KEY (survey_id),
  CONSTRAINT survey_survey_hash_key UNIQUE (survey_hash)
)
WITH (
  OIDS=FALSE
);

