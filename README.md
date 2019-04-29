# Tinder crawler

![](etc/screenv2.png)

This project allow to sort your tinder recomendations based on [simple keyword scoring model](/src/main/java/ru/gotinder/crawler/scoring/ScoringModelService.java)

No more recomendations with empty bio!

### How it works: 

1. Pull tinder recomendations (+ store in [postgresql database](/sql/schema.sql))

2. Apply scoring model

3. Show results via web gui (top by rating,latest data,search,possible likes)

4. You can like/pass/superlike from gui and sync results with tinder backend.

I inspired to wrote this code after thinking about best strategy of use tinder's free superlike per day. 
Look for similar math problem - [Secretary problem](https://en.wikipedia.org/wiki/Secretary_problem)

### Dependencies
[tinder-api](https://github.com/mark-dev/tinder-api)