# Use an official Maven image to build the application
FROM maven:3.8.7-eclipse-temurin-17 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml file and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code to the container
COPY src ./src

# Package the application
RUN mvn package

# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the packaged jar file from the build stage
COPY --from=build /app/target/chatpyme-1.0-SNAPSHOT-shaded.jar app.jar

# Set the entry point to run the application
CMD ["java", "-jar", "app.jar"]