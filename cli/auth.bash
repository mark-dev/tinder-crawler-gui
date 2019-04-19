#!/usr/bin/env bash

http -v "https://api.gotinder.com/auth" <<< `cat auth.json`