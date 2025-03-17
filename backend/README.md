# Setting up environments

Go into `backend/` folder

## Dev

```shell
./gradlew bootRun
```

## Prod

```shell
./gradlew clean build -x test

java -jar *.jar
```
