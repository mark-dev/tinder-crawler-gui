#!/usr/bin/env bash

TinderToken="1c75a31e-123b-4dc6-ba0f-ae3a3423e534"

http -v "https://api.gotinder.com/user/recs" "X-Auth-Token: $TinderToken" "Content-type: application/json" "User-agent: Tinder/7.5.3 (iPhone; iOS 10.3.2; Scale/2.00)"