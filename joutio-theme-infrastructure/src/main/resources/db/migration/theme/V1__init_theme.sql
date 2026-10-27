-- Joutio theme — baseline schema (squashed from V1..V6)
-- Generated from a fresh PostgreSQL 16 database after applying the legacy migrations.

--
-- PostgreSQL database dump
--


-- Dumped from database version 16.13
-- Dumped by pg_dump version 16.13

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: question_answer_content; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_answer_content (
    content_id bigint NOT NULL,
    choice character varying(1) NOT NULL,
    answer_text character varying(255) NOT NULL,
    CONSTRAINT chk_question_answer_content_choice CHECK (((choice)::text = ANY ((ARRAY['A'::character varying, 'B'::character varying, 'C'::character varying, 'D'::character varying])::text[]))),
    CONSTRAINT chk_question_answer_content_text_not_blank CHECK ((char_length(TRIM(BOTH FROM answer_text)) > 0))
);


--
-- Name: TABLE question_answer_content; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.question_answer_content IS 'Reponses possibles (QCM) de chaque contenu localise';


--
-- Name: COLUMN question_answer_content.choice; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_answer_content.choice IS 'Choix de reponse (A, B, C ou D)';


--
-- Name: COLUMN question_answer_content.answer_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_answer_content.answer_text IS 'Texte de la reponse dans la langue du contenu';


--
-- Name: question_answer_record; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_answer_record (
    game_id character varying(255) NOT NULL,
    round character varying(32) NOT NULL,
    player_id character varying(255) NOT NULL,
    question_id character varying(255) NOT NULL,
    correct boolean NOT NULL,
    answered_at timestamp without time zone NOT NULL
);


--
-- Name: question_answer_stats; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_answer_stats (
    question_id character varying(255) NOT NULL,
    answer_count integer DEFAULT 0 NOT NULL,
    correct_count integer DEFAULT 0 NOT NULL,
    CONSTRAINT chk_question_answer_count_non_negative CHECK ((answer_count >= 0)),
    CONSTRAINT chk_question_correct_count_non_negative CHECK ((correct_count >= 0)),
    CONSTRAINT chk_question_correct_lte_answers CHECK ((correct_count <= answer_count))
);


--
-- Name: TABLE question_answer_stats; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.question_answer_stats IS 'Compteurs de reponses par question (base du calcul de difficulte)';


--
-- Name: COLUMN question_answer_stats.question_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_answer_stats.question_id IS 'Reference vers question_entry';


--
-- Name: COLUMN question_answer_stats.answer_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_answer_stats.answer_count IS 'Nombre total de reponses humaines (timeouts inclus)';


--
-- Name: COLUMN question_answer_stats.correct_count; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_answer_stats.correct_count IS 'Nombre de reponses humaines correctes';


--
-- Name: question_content; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_content (
    id bigint NOT NULL,
    question_id character varying(255) NOT NULL,
    language character varying(5) NOT NULL,
    text character varying(255) NOT NULL,
    CONSTRAINT chk_question_content_language CHECK (((language)::text = ANY ((ARRAY['fr'::character varying, 'en'::character varying])::text[]))),
    CONSTRAINT chk_question_content_text_not_blank CHECK ((char_length(TRIM(BOTH FROM text)) > 0))
);


--
-- Name: TABLE question_content; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.question_content IS 'Contenus localises des questions (une ligne par langue disponible : fr, en)';


--
-- Name: COLUMN question_content.question_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_content.question_id IS 'Reference vers question_entry';


--
-- Name: COLUMN question_content.language; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_content.language IS 'Code ISO 639-1 du contenu (fr, en)';


--
-- Name: COLUMN question_content.text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_content.text IS 'Texte de la question dans cette langue';


--
-- Name: question_content_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.question_content_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: question_content_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.question_content_id_seq OWNED BY public.question_content.id;


--
-- Name: question_entry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_entry (
    question_id character varying(255) NOT NULL,
    topic_id character varying(255) NOT NULL,
    image_url character varying(1024),
    difficulty character varying(255),
    correct_answer character varying(1) NOT NULL,
    status character varying(255) NOT NULL,
    creator_id character varying(255) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_by character varying(255),
    updated_at timestamp without time zone,
    suspended boolean DEFAULT false NOT NULL,
    suspension_reason character varying(40),
    suspension_note character varying(500),
    suspended_at timestamp without time zone,
    suspended_by character varying(255),
    reinstated_at timestamp without time zone,
    archived boolean DEFAULT false NOT NULL,
    archive_note character varying(500),
    archived_at timestamp without time zone,
    archived_by character varying(255),
    restored_at timestamp without time zone,
    CONSTRAINT chk_question_choice CHECK (((correct_answer)::text = ANY ((ARRAY['A'::character varying, 'B'::character varying, 'C'::character varying, 'D'::character varying])::text[]))),
    CONSTRAINT chk_question_difficulty CHECK (((difficulty IS NULL) OR ((difficulty)::text = ANY ((ARRAY['EASY'::character varying, 'MEDIUM'::character varying, 'HARD'::character varying, 'EXPERT'::character varying])::text[])))),
    CONSTRAINT chk_question_status CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


--
-- Name: TABLE question_entry; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.question_entry IS 'Table des questions - projection read-only mise a jour via Event Handlers';


--
-- Name: COLUMN question_entry.question_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_entry.question_id IS 'Identifiant unique de la question';


--
-- Name: COLUMN question_entry.topic_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_entry.topic_id IS 'Reference vers topic_entry';


--
-- Name: COLUMN question_entry.image_url; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_entry.image_url IS 'URL externe optionnelle de l''illustration de la question';


--
-- Name: COLUMN question_entry.difficulty; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_entry.difficulty IS 'Difficulte deduite du taux de bonnes reponses (EASY, MEDIUM, HARD, EXPERT), NULL tant que l''echantillon est insuffisant';


--
-- Name: COLUMN question_entry.correct_answer; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_entry.correct_answer IS 'Bonne reponse (A, B, C ou D)';


--
-- Name: COLUMN question_entry.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_entry.status IS 'Etat de moderation (PENDING, APPROVED, REJECTED)';


--
-- Name: COLUMN question_entry.creator_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_entry.creator_id IS 'Identifiant du createur de la question';


--
-- Name: COLUMN question_entry.created_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_entry.created_at IS 'Date de creation de la question';


--
-- Name: COLUMN question_entry.updated_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_entry.updated_by IS 'Identifiant du dernier acteur ayant modifie la question';


--
-- Name: COLUMN question_entry.updated_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_entry.updated_at IS 'Date de derniere mise a jour de la question';


--
-- Name: COLUMN question_entry.suspended; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_entry.suspended IS 'Question temporairement retirée du tirage (signalements ou action admin)';


--
-- Name: COLUMN question_entry.archived; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_entry.archived IS 'Question archivée (soft delete) : conservée pour l’historique, plus jouée';


--
-- Name: question_report; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_report (
    report_id character varying(255) NOT NULL,
    thread_id character varying(255) NOT NULL,
    question_id character varying(255) NOT NULL,
    topic_id character varying(255) NOT NULL,
    reporter_id character varying(255) NOT NULL,
    game_id character varying(255),
    comment character varying(500),
    language character varying(5),
    snapshot_json text,
    snapshot_source character varying(20),
    status character varying(20) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    reviewed_by character varying(255),
    reviewed_at timestamp without time zone,
    resolution_note character varying(500),
    CONSTRAINT chk_question_report_snapshot_source CHECK (((snapshot_source IS NULL) OR ((snapshot_source)::text = ANY ((ARRAY['PLAYED'::character varying, 'CURRENT'::character varying])::text[])))),
    CONSTRAINT chk_question_report_status CHECK (((status)::text = ANY ((ARRAY['OPEN'::character varying, 'RESOLVED'::character varying, 'DISMISSED'::character varying])::text[])))
);


--
-- Name: TABLE question_report; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.question_report IS 'Signalements de questions - projection read-only';


--
-- Name: COLUMN question_report.thread_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_report.thread_id IS 'Thread déterministe (question × joueur)';


--
-- Name: COLUMN question_report.snapshot_json; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_report.snapshot_json IS 'Contenu de la question tel que joué au moment du signalement';


--
-- Name: COLUMN question_report.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.question_report.status IS 'OPEN, RESOLVED ou DISMISSED';


--
-- Name: question_report_reason; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.question_report_reason (
    report_id character varying(255) NOT NULL,
    reason character varying(32) NOT NULL,
    CONSTRAINT chk_question_report_reason CHECK (((reason)::text = ANY ((ARRAY['QUESTION'::character varying, 'IMAGE'::character varying, 'ANSWERS'::character varying])::text[])))
);


--
-- Name: topic_entry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.topic_entry (
    topic_id character varying(255) NOT NULL,
    description character varying(500),
    category character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    creator_id character varying(255) NOT NULL,
    followers_counter integer DEFAULT 0,
    emoji character varying(16),
    color character varying(16),
    image_url character varying(1024),
    title_names text,
    created_at timestamp without time zone NOT NULL,
    updated_by character varying(255),
    updated_at timestamp without time zone,
    name_fr character varying(255) NOT NULL,
    name_en character varying(255),
    name_fr_normalized character varying(255),
    name_en_normalized character varying(255),
    banner_url character varying(1024),
    games_counter integer DEFAULT 0 NOT NULL,
    CONSTRAINT chk_topic_category CHECK (((category)::text = ANY ((ARRAY['ARTS'::character varying, 'BUSINESS'::character varying, 'EDUCATION'::character varying, 'ENTERTAINMENT'::character varying, 'FOOD_AND_DRINK'::character varying, 'GAMES'::character varying, 'GENERAL'::character varying, 'HISTORY'::character varying, 'LITERATURE'::character varying, 'MOVIES'::character varying, 'MUSIC'::character varying, 'NATURE'::character varying, 'RELIGION'::character varying, 'SCIENCE'::character varying, 'SPORTS'::character varying, 'TELEVISION'::character varying, 'TECHNOLOGY'::character varying, 'WORLD'::character varying])::text[]))),
    CONSTRAINT chk_topic_name_fr_not_blank CHECK ((char_length(TRIM(BOTH FROM name_fr)) > 0)),
    CONSTRAINT chk_topic_status CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PUBLISHED'::character varying, 'ARCHIVED'::character varying])::text[])))
);


--
-- Name: TABLE topic_entry; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.topic_entry IS 'Table des themes - projection read-only mise a jour via Event Handlers';


--
-- Name: COLUMN topic_entry.topic_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.topic_id IS 'Identifiant unique du theme';


--
-- Name: COLUMN topic_entry.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.description IS 'Description du theme (max 500 caracteres)';


--
-- Name: COLUMN topic_entry.category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.category IS 'Categorie fonctionnelle du theme';


--
-- Name: COLUMN topic_entry.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.status IS 'Etat du theme (DRAFT, PUBLISHED, ARCHIVED)';


--
-- Name: COLUMN topic_entry.creator_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.creator_id IS 'Identifiant du createur du theme';


--
-- Name: COLUMN topic_entry.followers_counter; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.followers_counter IS 'Utilisateurs suivant ce theme';


--
-- Name: COLUMN topic_entry.created_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.created_at IS 'Date de creation du theme';


--
-- Name: COLUMN topic_entry.updated_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.updated_by IS 'Identifiant du dernier acteur ayant modifie le theme';


--
-- Name: COLUMN topic_entry.updated_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.updated_at IS 'Date de derniere mise a jour du theme';


--
-- Name: COLUMN topic_entry.name_fr; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.name_fr IS 'Nom du theme (francais, reference, max 255)';


--
-- Name: COLUMN topic_entry.name_en; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.name_en IS 'Nom du theme (anglais, optionnel, max 255)';


--
-- Name: COLUMN topic_entry.name_fr_normalized; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.name_fr_normalized IS 'name_fr normalise (recherche insensible accents/casse)';


--
-- Name: COLUMN topic_entry.name_en_normalized; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.name_en_normalized IS 'name_en normalise (recherche insensible accents/casse)';


--
-- Name: COLUMN topic_entry.banner_url; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.banner_url IS 'URL externe optionnelle de la bannière de la fiche sujet (distincte de la couverture)';


--
-- Name: COLUMN topic_entry.games_counter; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_entry.games_counter IS 'Parties créées sur ce thème (recalculé depuis topic_played_game_ref)';


--
-- Name: topic_follower_ref; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.topic_follower_ref (
    topic_id character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL
);


--
-- Name: topic_played_game_ref; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.topic_played_game_ref (
    game_id character varying(255) NOT NULL,
    topic_id character varying(255) NOT NULL
);


--
-- Name: topic_questions_counter; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.topic_questions_counter (
    topic_id character varying(255) NOT NULL,
    question_status character varying(50) NOT NULL,
    counter integer DEFAULT 0 NOT NULL,
    CONSTRAINT chk_topic_questions_counter_non_negative CHECK ((counter >= 0)),
    CONSTRAINT chk_topic_questions_status CHECK (((question_status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


--
-- Name: TABLE topic_questions_counter; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.topic_questions_counter IS 'Compteurs de questions par statut pour chaque theme';


--
-- Name: COLUMN topic_questions_counter.topic_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_questions_counter.topic_id IS 'Identifiant du theme';


--
-- Name: COLUMN topic_questions_counter.question_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_questions_counter.question_status IS 'Statut de la question (PENDING, APPROVED, REJECTED)';


--
-- Name: COLUMN topic_questions_counter.counter; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.topic_questions_counter.counter IS 'Nombre de questions avec ce statut';


--
-- Name: question_content id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_content ALTER COLUMN id SET DEFAULT nextval('public.question_content_id_seq'::regclass);


--
-- Name: question_answer_content pk_question_answer_content; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_answer_content
    ADD CONSTRAINT pk_question_answer_content PRIMARY KEY (content_id, choice);


--
-- Name: question_report_reason pk_question_report_reason; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_report_reason
    ADD CONSTRAINT pk_question_report_reason PRIMARY KEY (report_id, reason);


--
-- Name: question_answer_record question_answer_record_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_answer_record
    ADD CONSTRAINT question_answer_record_pkey PRIMARY KEY (game_id, round, player_id);


--
-- Name: question_answer_stats question_answer_stats_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_answer_stats
    ADD CONSTRAINT question_answer_stats_pkey PRIMARY KEY (question_id);


--
-- Name: question_content question_content_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_content
    ADD CONSTRAINT question_content_pkey PRIMARY KEY (id);


--
-- Name: question_entry question_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_entry
    ADD CONSTRAINT question_entry_pkey PRIMARY KEY (question_id);


--
-- Name: question_report question_report_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_report
    ADD CONSTRAINT question_report_pkey PRIMARY KEY (report_id);


--
-- Name: topic_entry topic_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.topic_entry
    ADD CONSTRAINT topic_entry_pkey PRIMARY KEY (topic_id);


--
-- Name: topic_follower_ref topic_follower_ref_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.topic_follower_ref
    ADD CONSTRAINT topic_follower_ref_pkey PRIMARY KEY (topic_id, user_id);


--
-- Name: topic_played_game_ref topic_played_game_ref_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.topic_played_game_ref
    ADD CONSTRAINT topic_played_game_ref_pkey PRIMARY KEY (game_id);


--
-- Name: topic_questions_counter topic_questions_counter_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.topic_questions_counter
    ADD CONSTRAINT topic_questions_counter_pkey PRIMARY KEY (topic_id, question_status);


--
-- Name: question_content uq_question_content; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_content
    ADD CONSTRAINT uq_question_content UNIQUE (question_id, language);


--
-- Name: idx_question_answer_content_content; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_answer_content_content ON public.question_answer_content USING btree (content_id);


--
-- Name: idx_question_answer_record_question; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_answer_record_question ON public.question_answer_record USING btree (question_id);


--
-- Name: idx_question_content_language; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_content_language ON public.question_content USING btree (language);


--
-- Name: idx_question_content_question; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_content_question ON public.question_content USING btree (question_id);


--
-- Name: idx_question_entry_archived; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_entry_archived ON public.question_entry USING btree (archived);


--
-- Name: idx_question_entry_creator; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_entry_creator ON public.question_entry USING btree (creator_id);


--
-- Name: idx_question_entry_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_entry_status ON public.question_entry USING btree (status);


--
-- Name: idx_question_entry_suspended; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_entry_suspended ON public.question_entry USING btree (suspended);


--
-- Name: idx_question_entry_topic; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_entry_topic ON public.question_entry USING btree (topic_id);


--
-- Name: idx_question_report_question; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_report_question ON public.question_report USING btree (question_id, status);


--
-- Name: idx_question_report_reason_report; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_report_reason_report ON public.question_report_reason USING btree (report_id);


--
-- Name: idx_question_report_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_report_status ON public.question_report USING btree (status, created_at);


--
-- Name: idx_question_report_thread; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_question_report_thread ON public.question_report USING btree (thread_id);


--
-- Name: idx_topic_entry_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_topic_entry_category ON public.topic_entry USING btree (category);


--
-- Name: idx_topic_entry_creator; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_topic_entry_creator ON public.topic_entry USING btree (creator_id);


--
-- Name: idx_topic_entry_name_en_normalized; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_topic_entry_name_en_normalized ON public.topic_entry USING btree (name_en_normalized);


--
-- Name: idx_topic_entry_name_fr_normalized; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_topic_entry_name_fr_normalized ON public.topic_entry USING btree (name_fr_normalized);


--
-- Name: idx_topic_entry_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_topic_entry_status ON public.topic_entry USING btree (status);


--
-- Name: idx_topic_follower_ref_topic; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_topic_follower_ref_topic ON public.topic_follower_ref USING btree (topic_id);


--
-- Name: idx_topic_played_game_ref_topic; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_topic_played_game_ref_topic ON public.topic_played_game_ref USING btree (topic_id);


--
-- Name: idx_topic_questions_counter_topic; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_topic_questions_counter_topic ON public.topic_questions_counter USING btree (topic_id);


--
-- Name: uq_question_report_open_thread; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_question_report_open_thread ON public.question_report USING btree (thread_id) WHERE ((status)::text = 'OPEN'::text);


--
-- Name: question_answer_content fk_question_answer_content_content; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_answer_content
    ADD CONSTRAINT fk_question_answer_content_content FOREIGN KEY (content_id) REFERENCES public.question_content(id) ON DELETE CASCADE;


--
-- Name: question_answer_record fk_question_answer_record_question; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_answer_record
    ADD CONSTRAINT fk_question_answer_record_question FOREIGN KEY (question_id) REFERENCES public.question_entry(question_id) ON DELETE CASCADE;


--
-- Name: question_answer_stats fk_question_answer_stats_question; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_answer_stats
    ADD CONSTRAINT fk_question_answer_stats_question FOREIGN KEY (question_id) REFERENCES public.question_entry(question_id) ON DELETE CASCADE;


--
-- Name: question_content fk_question_content_question; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_content
    ADD CONSTRAINT fk_question_content_question FOREIGN KEY (question_id) REFERENCES public.question_entry(question_id) ON DELETE CASCADE;


--
-- Name: question_entry fk_question_entry_topic; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_entry
    ADD CONSTRAINT fk_question_entry_topic FOREIGN KEY (topic_id) REFERENCES public.topic_entry(topic_id) ON DELETE CASCADE;


--
-- Name: question_report fk_question_report_question; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_report
    ADD CONSTRAINT fk_question_report_question FOREIGN KEY (question_id) REFERENCES public.question_entry(question_id) ON DELETE CASCADE;


--
-- Name: question_report_reason fk_question_report_reason_report; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.question_report_reason
    ADD CONSTRAINT fk_question_report_reason_report FOREIGN KEY (report_id) REFERENCES public.question_report(report_id) ON DELETE CASCADE;


--
-- Name: topic_follower_ref fk_topic_follower_ref_topic; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.topic_follower_ref
    ADD CONSTRAINT fk_topic_follower_ref_topic FOREIGN KEY (topic_id) REFERENCES public.topic_entry(topic_id) ON DELETE CASCADE;


--
-- Name: topic_questions_counter fk_topic_questions_counter_topic; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.topic_questions_counter
    ADD CONSTRAINT fk_topic_questions_counter_topic FOREIGN KEY (topic_id) REFERENCES public.topic_entry(topic_id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--


