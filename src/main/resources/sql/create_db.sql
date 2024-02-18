CREATE TABLE IF NOT EXISTS groups (id SERIAL PRIMARY KEY, name VARCHAR(32) UNIQUE NOT NULL, graduation_date DATE NOT NULL);
CREATE TABLE IF NOT EXISTS curators (id SERIAL PRIMARY KEY, name VARCHAR(32) NOT NULL, email VARCHAR(320) NOT NULL, experience INTEGER NOT NULL, group_id INTEGER REFERENCES groups(id));
CREATE TABLE IF NOT EXISTS students (id SERIAL PRIMARY KEY, name VARCHAR(32) NOT NULL, date_of_birth DATE NOT NULL, group_id INTEGER REFERENCES groups(id));