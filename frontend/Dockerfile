FROM node:18 AS build

WORKDIR /app

COPY . .

RUN npm install && npm run build

FROM node:18
WORKDIR /app
COPY --from=build /app/dist dist

CMD ["node", "dist/main.js"]
