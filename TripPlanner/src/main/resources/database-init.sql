DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS trips;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       id SERIAL PRIMARY KEY NOT NULL,
                       email TEXT UNIQUE NOT NULL,
                       enabled BOOLEAN DEFAULT TRUE NOT NULL,
                       password TEXT
);

CREATE TABLE authorities (
                             id SERIAL PRIMARY KEY NOT NULL,
                             email TEXT NOT NULL,
                             authority TEXT NOT NULL,
                             CONSTRAINT fk_user FOREIGN KEY (email) REFERENCES users (email) ON DELETE CASCADE
);

CREATE TABLE trips (
                       id SERIAL PRIMARY KEY NOT NULL,
                       user_id INTEGER NOT NULL,
                       country TEXT,
                       city TEXT,
                       state TEXT,
                       start_time DATE,
                       end_time DATE,
                       preferences TEXT,
                       trip_plan_detail jsonb,
                       CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
