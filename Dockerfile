# syntax=docker/dockerfile:experimental
FROM maven:3.9.3-eclipse-temurin-20 AS build
COPY ./src /tmp/src
COPY ./pom.xml /tmp/pom.xml
WORKDIR /tmp
RUN --mount=type=cache,target=/root/.m2,source=/root/.m2,from=smartcommunitylab/playful-edu-standbyme:cache \ 
    mvn package -DskipTests

FROM eclipse-temurin:20-jdk-alpine
ENV APP=playful.component.standbyme-1.0.jar
ARG USER=playfuledu
ARG USER_ID=10005
ARG USER_GROUP=playfuledu
ARG USER_GROUP_ID=10005
ARG USER_HOME=/home/${USER}

RUN  addgroup -g ${USER_GROUP_ID} ${USER_GROUP}; \
     adduser -u ${USER_ID} -D -g '' -h ${USER_HOME} -G ${USER_GROUP} ${USER} ;

WORKDIR  ${USER_HOME}
COPY --chown=playfuledu:playfuledu --from=build /tmp/target/${APP} ${USER_HOME}/${APP}
USER 10005
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar ${APP}"]
