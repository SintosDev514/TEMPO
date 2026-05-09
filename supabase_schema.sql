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
    updated_at BIGINT NOT NULL
);

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
ALTER PUBLICATION supabase_realtime ADD TABLE public.announcements;
ALTER PUBLICATION supabase_realtime ADD TABLE public.media;

-- RLS Policies (Basic - Allow all for authenticated users for now)
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.announcements ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.media ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow all for authenticated users" ON public.users FOR ALL TO authenticated USING (true);
CREATE POLICY "Allow all for authenticated users" ON public.events FOR ALL TO authenticated USING (true);
CREATE POLICY "Allow all for authenticated users" ON public.announcements FOR ALL TO authenticated USING (true);
CREATE POLICY "Allow all for authenticated users" ON public.media FOR ALL TO authenticated USING (true);
