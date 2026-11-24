
# Build React frontend
FROM node:25-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

ARG VITE_API_BASE_URL=https://sneha.prod.page
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

RUN npm run build

# Serve frontend using Nginx
FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
