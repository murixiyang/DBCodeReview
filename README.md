# DBCodeReview

Individual Project - Double-blind Code Review

# Azure

ssh murixiyang@20.77.48.174
username: murixiyang
password: uVb@9aNsZdPdx3m

# Docker

docker compose down
docker compose build
docker compose up -d

docker compose logs backend

## Live log

docker compose logs --tail=100 -f backend

## only rebuild this stage

docker compose up -d --build backend

docker compose restart frontend

# Frontend(Angular)

Run: `cd ./frontend/db-pcr-frontend` --> `ng serve` --> localhost:4200

# Backend(Java)

Run: `cd ./backend/db-pcr-backend` --> `mvn spring-boot:run` --> loclahost:8081

API Documentation: http://localhost:8081/swagger-ui.html

# Gerrit

--> localhost:8080

git remote add gerrit: `ssh://admin@gerrit.myserver.com:29418/BlindTestExample.git`

Push to Gerrit: `git push origin HEAD:refs/for/master`

Basic testing of REST API functionality can be done with curl:
GET: `curl http://localhost:8080/path/to/api/`
PUT: `curl -X PUT http://localhost:8080/path/to/api/`
POST: `curl -X POST http://localhost:8080/path/to/api/`
DELETE: `curl -X DELETE http://localhost:8080/path/to/api/`

## Database

DELETE FROM review_statuses;
