FROM maven:3.6-jdk-11-openj9 as target
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src/ /build/src/
RUN mvn package

# Step : Package image
FROM adoptopenjdk/openjdk11-openj9:jre
CMD exec java $JAVA_OPTS -jar /app/ding2webdav.jar
COPY --from=target /build/target/*jar-with-dependencies.jar /app/ding2webdav.jar