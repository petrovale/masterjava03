DROP TABLE IF EXISTS user_groups;
DROP TABLE IF EXISTS users;
DROP SEQUENCE IF EXISTS user_seq;
DROP TYPE IF EXISTS user_flag;
DROP TABLE IF EXISTS cities;
DROP SEQUENCE IF EXISTS city_seq;
DROP TABLE IF EXISTS groups;
DROP TYPE IF EXISTS group_type;
DROP TABLE IF EXISTS projects;
DROP SEQUENCE IF EXISTS project_seq;

CREATE TYPE USER_FLAG AS ENUM ('active', 'deleted', 'superuser');

CREATE SEQUENCE user_seq START 100000;
CREATE SEQUENCE city_seq START 100000;
CREATE SEQUENCE project_seq START 100000;

CREATE TABLE cities (
  id          INTEGER PRIMARY KEY DEFAULT nextval('city_seq'),
  name        TEXT NOT NULL,
  middle_name TEXT NOT NULL
);

CREATE UNIQUE INDEX city_idx
  ON cities (name);

CREATE TABLE users (
  id        INTEGER PRIMARY KEY DEFAULT nextval('user_seq'),
  full_name TEXT      NOT NULL,
  email     TEXT      NOT NULL,
  flag      USER_FLAG NOT NULL,
  city_id   INTEGER   NOT NULL,
  FOREIGN KEY (city_id) REFERENCES cities (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX email_idx
  ON users (email);

CREATE TABLE projects (
  id          INTEGER PRIMARY KEY DEFAULT nextval('project_seq'),
  name        TEXT NOT NULL,
  description TEXT NOT NULL
);

CREATE UNIQUE INDEX project_idx
  ON projects (name);

CREATE TYPE GROUP_TYPE AS ENUM ('REGISTERING', 'CURRENT', 'FINISHED');

CREATE TABLE groups (
  id       INTEGER PRIMARY KEY DEFAULT nextval('project_seq'),
  name     TEXT       NOT NULL,
  type     GROUP_TYPE NOT NULL,
  project_id INTEGER,
  FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX group_idx
  ON groups (name);

CREATE TABLE user_groups
(
  user_id  INTEGER NOT NULL,
  group_id INTEGER NOT NULL,
  CONSTRAINT user_groups_idx UNIQUE (user_id, group_id),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (group_id) REFERENCES groups (id)
);

