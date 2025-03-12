# BuildTask

## Project Description

BuildTask is an application consisting of frontend and backend parts. The goal of the project is to manage tasks within teams in a construction company.

The purpose of creating the application is to improve teamwork, practice TDD, automate deployment, and implement SCRUM methodologies with a touch of DevOps, and learn about spreaded applications.

## Architecture

The application is divided into two main parts:

- **Frontend**: Built with Electron, providing a desktop application interface.

- **Backend**: Implemented using Spring Boot, offering RESTful APIs to handle business logic and data management.

## Setup

### Requirements

- Node.js
- npm
- Java 21
- Gradle

### Installation

#### Frontend

Client side is ran locally via installing package provided with `npm build` (`dist/` folder) or via CLI operations:

NOTE: Provide environment variable for server location: `API_URL='http://localhost:8080'` or make and source `.env` file.

1. Navigate to the `frontend` folder:

    ```shell
    cd frontend
    ```

2. Install dependencies:

    ```shell
    npm install
    ```

3. Run the application:

    ```shell
    npm start:prod
    ```

#### Backend

RESTful API server is ran via Backend service.

1. Navigate to the `backend` folder:

    ```shell
    cd backend
    ```

2. Run the server:

    ```shell
    ./gradlew bootRun
    ```

## Authors

- [Mateusz Bocak](https://github.com/JustFiesta)
- [Jakub Jakubowski](https://github.com/Rolaski)
- [Gabriela Bieniek](https://github.com/gubbl3bum)
- [Kacper Bułaś](https://github.com/bolson1313)
- [Karol Dąbrowski](https://github.com/Poicalee)
