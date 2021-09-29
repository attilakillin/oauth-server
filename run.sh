#!/bin/bash

./gradlew bootJar
buildSuccess=$?

if [ $buildSuccess ]; then
    docker-compose up --build
fi
