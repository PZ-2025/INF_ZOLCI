FROM gradle:8-jdk17 AS build

WORKDIR /app

COPY . .

RUN gradle clean build -x test

FROM openjdk:17-jdk
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
