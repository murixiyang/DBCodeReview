#!/usr/bin/env bash

echo "Stopping Postgres container…"
docker stop code-review-db

echo "Removing container…"
docker rm code-review-db

echo "Done."
