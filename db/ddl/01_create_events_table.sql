-- =============================================================================
-- DDL: the `events` table used by eventservice (MySQL 8, InnoDB), in db_example.
-- Identical to what bankingservices' db/ddl/01_create_tables.sql created. The table
-- already exists in db_example; eventservice runs with ddl-auto: none and never changes it.
-- Usage:   mysql -u <user> -p < db/ddl/01_create_events_table.sql
-- =============================================================================

USE db_example;

CREATE TABLE IF NOT EXISTS events (
    timestamp datetime(6),
    email varchar(255),
    event_id varchar(255) not null,
    user_id varchar(255),
    version varchar(255),
    primary key (event_id)
) engine=InnoDB;
