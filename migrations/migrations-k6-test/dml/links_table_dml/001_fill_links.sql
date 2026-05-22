CREATE OR REPLACE FUNCTION fill_links()
RETURNS void
LANGUAGE sql
AS $$
    INSERT INTO links (url, resource_type)
    SELECT
        'https://github.com/user/repo' || gs,
        'GITHUB_REPOSITORY'
    FROM generate_series(1, 100000) AS gs;
$$;

SELECT fill_links();
