#!/bin/sh

PGDUMP=pg_dump

$PGDUMP --file "scripts/tcrawler_backup.sql" \
        --host "localhost" \
        --port "5432" \
        --username "postgres" \
        --verbose \
        --format=p \
        --no-owner \
        --inserts \
        --column-inserts \
        --create \
        --encoding "UTF8" \
        "tcrawler"