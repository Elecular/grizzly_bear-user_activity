FROM node:12.16

COPY . code
WORKDIR /code

ENV NODE_ENV=production
RUN npm ci

CMD ["npm", "start"]