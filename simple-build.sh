#!/bin/bash
./gradlew build
docker build -t roykachouh/benchmark .
docker push roykachouh/benchmark:latest