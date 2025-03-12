# How to run

`package.json` has two ways of running the frontend app

## Dev frontend

For local development testing

```shell
cd frontend
npm install
npm run dev
```

NOTE: Provide environment variable for server location: `API_URL='http://localhost:8080'` or make and source `.env` file.

## Prod frontend

For the production run

```shell
cd frontend
npm install
npm run build && npm run start:prod
```

## How to package app for given system

```shell
npm run build
```
