#!/bin/bash
set -e
(cd client && ./gradlew test)
(cd server && ./gradlew test)
