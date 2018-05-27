#!/bin/bash
./gradlew build
docker login
docker build -t roykachouh/benchmark .
docker push roykachouh/benchmark:latest