#!/bin/bash

pushd authorization-server
./gradlew bootJar
popd

docker-compose up --build
