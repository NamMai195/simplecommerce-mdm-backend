#!/bin/bash
set -e

# Perform all actions as the 'postgres' user
export PGUSER="$POSTGRES_USER"

# Create the database if it doesn't exist
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
  SELECT 'CREATE DATABASE simplecommerce_mdm'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'simplecommerce_mdm')\gexec
EOSQL 