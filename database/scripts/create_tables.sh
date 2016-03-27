psql -U sede -d twitter_db -a -f raw_tweet.sql
psql -U sede -d twitter_db -a -f users.sql
psql -U sede -d twitter_db -a -f tweets.sql
psql -U sede -d twitter_db -a -f user_interactions.sql
psql -U sede -d twitter_db -a -f user_mentions.sql
psql -U sede -d twitter_db -a -f survey.sql
psql -U sede -d twitter_db -a -f surveyresponse.sql
