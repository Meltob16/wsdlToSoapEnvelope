FROM openjdk:11

COPY target/wsdlToTemplate-0.0.1-SNAPSHOT.jar app.jar
CMD [ "java", "-jar", "app.jar" ]

WORKDIR src/js
FROM node:14
COPY package-lock.json ./
RUN npm install

ENV PATH = "./node_modules/.bin:$PATH"

COPY . ./
RUN npm run build

# docker build -f src/main/docker/Dockerfile -t ${tag_name} --build-arg jar_name=${jar_name} .
# docker run -p 8080:8080 ${tag_name}