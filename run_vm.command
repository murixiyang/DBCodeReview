#!/bin/bash

# SSH into the VM and run the commands in one go
ssh -t murixiyang@20.77.48.174 << 'REMOTE'
cd DBCodeReview/
docker compose up -d
exit
REMOTE
