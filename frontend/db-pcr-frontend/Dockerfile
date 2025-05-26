# frontend/Dockerfile
# Stage 1: build
FROM node:18 AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=development

# Stage 2: serve

FROM nginx:1.25-alpine
# remove default welcome page
RUN rm -rf /usr/share/nginx/html/* /etc/nginx/conf.d/default.conf

# copy our custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# copy the Angular build output
COPY --from=builder /app/dist/db-cpr-frontend/browser /usr/share/nginx/html

EXPOSE 80
CMD ["nginx","-g","daemon off;"]