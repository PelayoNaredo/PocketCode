-- PocketCode Database Schema
-- Provider: Supabase (PostgreSQL)

-- Phase 1: User and Profile Schema
-- This script assumes the existence of the `auth.users` table created by Supabase.

-- Create an ENUM type for user subscription status.
CREATE TYPE public.subscription_status AS ENUM (
    'free',
    'premium',
    'trial'
);

COMMENT ON TYPE public.subscription_status IS 'Represents the subscription status of a user.';

-- Create the profiles table to store public user data and settings.
CREATE TABLE public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    username TEXT UNIQUE,
    editor_settings JSONB DEFAULT '{}'::jsonb,
    encrypted_api_key TEXT,
    subscription_status public.subscription_status DEFAULT 'free',
    subscription_expires_at TIMESTAMPTZ,

    CONSTRAINT username_length CHECK (char_length(username) >= 3)
);

COMMENT ON TABLE public.profiles IS 'Stores public user profile information and application-specific settings.';
COMMENT ON COLUMN public.profiles.id IS 'References the user in the auth.users table.';
COMMENT ON COLUMN public.profiles.username IS 'Public username, must be unique.';
COMMENT ON COLUMN public.profiles.editor_settings IS 'Stores user-specific editor settings, like theme, font size, etc.';
COMMENT ON COLUMN public.profiles.encrypted_api_key IS 'Stores the user''s encrypted "Bring Your Own Key" for third-party services.';
COMMENT ON COLUMN public.profiles.subscription_status IS 'The user''s current subscription status.';
COMMENT ON COLUMN public.profiles.subscription_expires_at IS 'The date when the current subscription or trial expires.';

-- Add a trigger to automatically update the `updated_at` timestamp on any change.
CREATE OR REPLACE FUNCTION public.trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_profiles_timestamp
BEFORE UPDATE ON public.profiles
FOR EACH ROW
EXECUTE PROCEDURE public.trigger_set_timestamp();

-- Function to automatically create a profile for a new user.
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    -- Inserts a new row into public.profiles, setting the username to the user's email by default.
    -- The user can change their username later in the application settings.
    INSERT INTO public.profiles (id, username)
    VALUES (new.id, new.email);
    RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to call the function when a new user is added to auth.users.
-- This ensures that every user in `auth.users` has a corresponding `profiles` entry.
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();


-- Phase 2: Project Schema

CREATE TABLE public.projects (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    source_code_bucket_path TEXT NOT NULL UNIQUE,
    git_repository_url TEXT UNIQUE
);

COMMENT ON TABLE public.projects IS 'Stores metadata for each user project.';
COMMENT ON COLUMN public.projects.user_id IS 'The user who owns the project.';
COMMENT ON COLUMN public.projects.name IS 'The name of the project.';
COMMENT ON COLUMN public.projects.source_code_bucket_path IS 'The path to the project''s source code in Supabase Storage.';
COMMENT ON COLUMN public.projects.git_repository_url IS 'The URL of the private GitHub repository used for CI/CD.';

-- Add index on user_id for faster lookups of a user's projects.
CREATE INDEX ON public.projects (user_id);

-- Apply the timestamp trigger to the projects table.
CREATE TRIGGER set_projects_timestamp
BEFORE UPDATE ON public.projects
FOR EACH ROW
EXECUTE PROCEDURE public.trigger_set_timestamp();


-- Phase 3: Build and Artifacts Schema

-- Create an ENUM type for build status to ensure data consistency.
CREATE TYPE public.build_status AS ENUM (
    'pending',
    'running',
    'success',
    'failed'
);

COMMENT ON TYPE public.build_status IS 'Represents the possible states of a CI/CD build job.';

CREATE TABLE public.builds (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    project_id BIGINT NOT NULL REFERENCES public.projects(id) ON DELETE CASCADE,
    status public.build_status NOT NULL DEFAULT 'pending',
    build_log TEXT,
    artifact_bucket_path TEXT
);

COMMENT ON TABLE public.builds IS 'Logs all build jobs, their status, and links to artifacts.';
COMMENT ON COLUMN public.builds.project_id IS 'The project that this build belongs to.';
COMMENT ON COLUMN public.builds.status IS 'The current status of the build.';
COMMENT ON COLUMN public.builds.build_log IS 'Stores the log output from the build process.';
COMMENT ON COLUMN public.builds.artifact_bucket_path IS 'The path to the build artifact in Supabase Storage (null if build failed or not completed).';

-- Add index on project_id for faster lookups of a project's builds.
CREATE INDEX ON public.builds (project_id);

-- Apply the timestamp trigger to the builds table.
CREATE TRIGGER set_builds_timestamp
BEFORE UPDATE ON public.builds
FOR EACH ROW
EXECUTE PROCEDURE public.trigger_set_timestamp();


-- Phase 4: Row-Level Security (RLS) Policies
-- These policies ensure that users can only access their own data.

-- Enable RLS for all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.builds ENABLE ROW LEVEL SECURITY;

-- RLS Policies for `profiles` table
-- Users can view their own profile.
CREATE POLICY "Users can view their own profile"
ON public.profiles FOR SELECT
USING (auth.uid() = id);

-- Users can update their own profile.
CREATE POLICY "Users can update their own profile"
ON public.profiles FOR UPDATE
USING (auth.uid() = id)
WITH CHECK (auth.uid() = id);

-- RLS Policies for `projects` table
-- Users can view their own projects.
CREATE POLICY "Users can view their own projects"
ON public.projects FOR SELECT
USING (auth.uid() = user_id);

-- Users can insert new projects for themselves.
CREATE POLICY "Users can create their own projects"
ON public.projects FOR INSERT
WITH CHECK (auth.uid() = user_id);

-- Users can update their own projects.
CREATE POLICY "Users can update their own projects"
ON public.projects FOR UPDATE
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- Users can delete their own projects.
CREATE POLICY "Users can delete their own projects"
ON public.projects FOR DELETE
USING (auth.uid() = user_id);

-- RLS Policies for `builds` table
-- Users can view builds for their own projects.
CREATE POLICY "Users can view builds for their own projects"
ON public.builds FOR SELECT
-- The `USING` clause checks if the user trying to access the build row is the owner of the associated project.
USING (EXISTS (
    SELECT 1
    FROM public.projects
    WHERE projects.id = builds.project_id AND projects.user_id = auth.uid()
));

-- Users can insert new builds for their own projects.
CREATE POLICY "Users can create builds for their own projects"
ON public.builds FOR INSERT
-- The `WITH CHECK` clause ensures that a user cannot create a build for a project they do not own.
WITH CHECK (EXISTS (
    SELECT 1
    FROM public.projects
    WHERE projects.id = builds.project_id AND projects.user_id = auth.uid()
));

-- Note: UPDATE and DELETE policies for builds are intentionally omitted.
-- These operations should be handled by backend services or trusted roles, not directly by users.
