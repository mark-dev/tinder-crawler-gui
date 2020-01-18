# Tinder Crawler

### Motivation
I inspired to create this project after thinking about best strategy of use tinder's free superlike per day. 

If we thinking about best candidate for today - how choose girl for superlike? 

I found similar math problem - [Secretary problem.](https://en.wikipedia.org/wiki/Secretary_problem)

Ok, boomer! Wiki tells us, that first of all, we need to skip some candidates, but how do it via mobile tinder client?

This is impossible. Let write own tinder client with black-jack and hookers(a lot of). 

### About

This project allow to range your tinder recomendations based on [simple keyword scoring model](/src/main/java/ru/gotinder/crawler/enrich/ScoringModelService.java) and show it via web-gui.

This mean that your likes will be targeted on people, that's you really like (based on theirs description, not on attractiveness only)

You will no longer see recommendations with empty description!

<details><summary>Screenshot</summary>
<p>

 ![](etc/screenv2.png)

</p>
</details>

### How it works: 

1. Pull tinder recomendations (+ store in [postgresql database](/sql/scripts/schema.sql))

2. Apply scoring model

3. Show results via web gui (top by rating,latest data,search by text,possible likes)

4. You can like/pass/superlike from gui and sync your verdicts with tinder backend.


### Dependencies

[postgresql](https://www.postgresql.org/)
[chromedriver](https://chromedriver.chromium.org/)
[my fork of java tinder-api](https://github.com/mark-dev/tinder-api)

### How to run
1. Install PostgreSQL and deploy [sql schema](/sql/create-db.sh)
2. Move `_application-facebook.yaml` to`application-facebook.yaml` and specify your facebook login/password (for tinder auth)
3. Install `chromedriver` and `google-chrome` or `chromium`. Application assuming that `chromedriver` in PATH, also you must specify browser binary location via `tinder.crawler.fb.chrome-path` property
4. Tune [scoring model](/src/main/java/ru/gotinder/crawler/enrich/ScoringModelService.java). Also you can extend code, write your own model with NLP, image recognition ...
5. Start application: `./gradlew bootRun`
