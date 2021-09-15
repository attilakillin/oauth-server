#!/bin/bash

authorization-server/gradlew bootJar
docker-compose up --build
