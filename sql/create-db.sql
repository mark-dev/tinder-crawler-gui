DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT
      FROM   pg_catalog.pg_roles
      WHERE  rolname = 'tcrawler'
   ) THEN
      CREATE ROLE tcrawler LOGIN PASSWORD 'tcrawler';
   END IF;
END
$do$;

DROP DATABASE IF EXISTS tcrawler;
CREATE DATABASE tcrawler WITH OWNER tcrawler;
