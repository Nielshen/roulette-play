-- create_tables.sql
CREATE TABLE players (
                         player_index SERIAL PRIMARY KEY,
                         available_money INT
);

CREATE TABLE bets (
                      bet_type VARCHAR,
                      player_index INT,
                      bet_number INTEGER,
                      bet_odd_or_even VARCHAR,
                      bet_color VARCHAR,
                      bet_amount INTEGER,
                      random_number INTEGER,
                      FOREIGN KEY (player_index) REFERENCES players(player_index)
);
