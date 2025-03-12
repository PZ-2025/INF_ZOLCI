#!/usr/bin/env bash

# Start the backend
cd ../backend

./gradlew bootRun &

# Start the frontend 
cd frontend

npm install

npm run start:prod &