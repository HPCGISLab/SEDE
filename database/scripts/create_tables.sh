psql -U jajayaku -d twitter_db -a -f raw_tweet.sql
psql -U jajayaku -d twitter_db -a -f users.sql
psql -U jajayaku -d twitter_db -a -f tweets.sql
psql -U jajayaku -d twitter_db -a -f user_interactions.sql
psql -U jajayaku -d twitter_db -a -f user_mentions.sql
