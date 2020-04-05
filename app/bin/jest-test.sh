#!/bin/bash
cd "$(dirname "$0")"
cd ..

docker-compose -f docker-compose.yml -f docker-compose.test.yml down -v --remove-orphans
docker-compose -f docker-compose.yml -f docker-compose.test.yml up --renew-anon-volumes --build --abort-on-container-exit