#!/bin/bash

pushd authorization-server

./gradlew bootJar
buildSuccess=$?

popd

if [ $buildSuccess ]; then
    docker-compose up --build
fi
