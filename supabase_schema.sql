-- Users table (links to Auth users)
CREATE TABLE public.users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    email TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'STUDENT',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at BIGINT NOT NULL
);

-- Events table
CREATE TABLE public.events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    date TEXT NOT NULL,
    venue TEXT NOT NULL,
    description TEXT NOT NULL,
    category TEXT NOT NULL,
    image_url TEXT,
    reaction_counts JSONB DEFAULT '{}'::jsonb,
    updated_at BIGINT NOT NULL
);

-- Event Reactions table (Tracks who reacted with what)
CREATE TABLE public.event_reactions (
    event_id UUID REFERENCES public.events(id) ON DELETE CASCADE,
    user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    reaction_type TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    PRIMARY KEY (event_id, user_id)
);

-- Function to update reaction_counts on events table
CREATE OR REPLACE FUNCTION update_event_reaction_counts()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        UPDATE public.events
        SET reaction_counts = (
            SELECT COALESCE(jsonb_object_agg(reaction_type, count), '{}'::jsonb)
            FROM (
                SELECT reaction_type, count(*) as count
                FROM public.event_reactions
                WHERE event_id = NEW.event_id
                GROUP BY reaction_type
            ) s
        )
        WHERE id = NEW.event_id;
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE public.events
        SET reaction_counts = (
            SELECT COALESCE(jsonb_object_agg(reaction_type, count), '{}'::jsonb)
            FROM (
                SELECT reaction_type, count(*) as count
                FROM public.event_reactions
                WHERE event_id = OLD.event_id
                GROUP BY reaction_type
            ) s
        )
        WHERE id = OLD.event_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger for reactions
CREATE TRIGGER on_reaction_change
AFTER INSERT OR UPDATE OR DELETE ON public.event_reactions
FOR EACH ROW EXECUTE FUNCTION update_event_reaction_counts();

-- Announcements table
CREATE TABLE public.announcements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    priority INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);

-- Media table
CREATE TABLE public.media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES public.events(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    file_name TEXT NOT NULL,
    date TEXT NOT NULL,
    size_mb INTEGER NOT NULL,
    duration TEXT NOT NULL,
    saves INTEGER NOT NULL DEFAULT 0,
    cover_url TEXT NOT NULL,
    updated_at BIGINT NOT NULL
);

-- Enable Realtime for all tables
-- Note: If the publication 'supabase_realtime' doesn't exist, you may need to create it first.
-- Usually it exists by default in new projects.
ALTER PUBLICATION supabase_realtime ADD TABLE public.users;
ALTER PUBLICATION supabase_realtime ADD TABLE public.events;
ALTER PUBLICATION supabase_realtime ADD TABLE public.event_reactions;
ALTER PUBLICATION supabase_realtime ADD TABLE public.announcements;
ALTER PUBLICATION supabase_realtime ADD TABLE public.media;

-- RLS Policies (Basic - Allow all for authenticated users for now)
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.event_reactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.announcements ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.media ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow all for authenticated users" ON public.users FOR ALL TO authenticated USING (true);
CREATE POLICY "Allow all for authenticated users" ON public.events FOR ALL TO authenticated USING (true);
CREATE POLICY "Allow all for authenticated users" ON public.event_reactions FOR ALL TO authenticated USING (true);
CREATE POLICY "Allow all for authenticated users" ON public.announcements FOR ALL TO authenticated USING (true);
CREATE POLICY "Allow all for authenticated users" ON public.media FOR ALL TO authenticated USING (true);

-- Ensure reaction_counts is always accurate even if client sends null
CREATE OR REPLACE FUNCTION ensure_event_reaction_counts()
RETURNS TRIGGER AS $$
BEGIN
    NEW.reaction_counts = (
        SELECT COALESCE(jsonb_object_agg(reaction_type, count), '{}'::jsonb)
        FROM (
            SELECT reaction_type, count(*) as count
            FROM public.event_reactions
            WHERE event_id = NEW.id
            GROUP BY reaction_type
        ) s
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER on_event_upsert
BEFORE INSERT OR UPDATE ON public.events
FOR EACH ROW EXECUTE FUNCTION ensure_event_reaction_counts();
