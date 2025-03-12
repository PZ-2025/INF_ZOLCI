# Setting up environments

## Dev

Go into `backend/` folder and run server.

```shell
./gradlew bootRun
```

Go into `frontend/` folder and install dependencies and run app.

```shell
npm install

npm run dev
```

## Prod

Go into `backend/` folder and run server.

```shell
./gradlew bootRun
```

Go into `frontend/` folder and install dependencies and run app. This will build package for given system into `dist/`.

```shell
npm install

npm run start:prod
```
