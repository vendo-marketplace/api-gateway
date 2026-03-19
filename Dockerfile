FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /build

COPY pom.xml ./

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

RUN ls -lh target

FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /build/target/api-gateway*.jar api-gateway.jar

EXPOSE 8030

CMD ["java", "-jar", "api-gateway.jar"]
