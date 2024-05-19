#!/usr/bin/env bash

echo "Building application... "

./gradlew clean build -x test

echo "Clearing docker..."

./cleanup_docker.sh

echo "Building docker-compose..."

docker-compose build

echo "Build has been finished!"