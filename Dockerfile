FROM openjdk:11

COPY target/wsdlToTemplate-0.0.1-SNAPSHOT.jar app.jar
CMD [ "java", "-jar", "app.jar" ]


#FROM node:14
#WORKDIR src/js
#COPY package*.json ./
#RUN npm install -g
#COPY . .

#EXPOSE 8080

#CMD ["node", "server.js", "cd .." , "cd ..", "java", "-jar", "app.jar"  ]

# docker build -f src/main/docker/Dockerfile -t ${tag_name} --build-arg jar_name=${jar_name} .
# docker run -p 8080:8080 ${tag_name}