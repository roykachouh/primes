#!/bin/bash
./gradlew build
docker login
docker build -t roykachouh/benchmark .
docker tag roykachouh/benchmark:latest 002957041265.dkr.ecr.us-west-2.amazonaws.com/roykachouh/benchmark:latest
docker push roykachouh/benchmark:latest