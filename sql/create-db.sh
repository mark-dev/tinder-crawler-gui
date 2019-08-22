#!/bin/bash

export PG_USER='postgres'
export PG_PORT=5432
export DB_NAME='tcrawler'
export PGPASSWORD='postgres'

PSQL=psql

run() {
  $*
  if [ $? -ne 0 ]
  then
    echo "$* failed with exit code $?"
    exit 1
  else
    return 0
  fi
}

if [ -z "$1" ]
then
    echo "Create database on localhost"
    export PG_HOST=localhost
else
    export PG_HOST=$1
fi

echo "Create database"
${PSQL} -f scripts/create-db.sql -h $PG_HOST -p $PG_PORT -U $PG_USER -W

echo "Create schema"
${PSQL}  -f scripts/trawler_backup.sql -h $PG_HOST -p $PG_PORT -U $PG_USER -d $DB_NAME

${PSQL}  -h $PG_HOST -p $PG_PORT -U $PG_USER -d $DB_NAME -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO tcrawler;"
${PSQL}  -h $PG_HOST -p $PG_PORT -U $PG_USER -d $DB_NAME -c "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO tcrawler;"
${PSQL}  -h $PG_HOST -p $PG_PORT -U $PG_USER -d $DB_NAME -c "GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO tcrawler;"
