-- Description de l'image d'une question, par langue (lecteurs d'écran) ; NULL sans image ou non renseignée.
ALTER TABLE public.question_content ADD COLUMN image_alt character varying(125);

COMMENT ON COLUMN public.question_content.image_alt IS 'Description de l''image de la question dans cette langue (accessibilité)';
