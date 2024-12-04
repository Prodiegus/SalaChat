# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the source code to the container
COPY . .

# Download the MongoDB driver
RUN mkdir -p lib && \
    curl -L -o lib/mongodb-driver-sync-4.3.1.jar https://repo1.maven.org/maven2/org/mongodb/mongodb-driver-sync/4.3.1/mongodb-driver-sync-4.3.1.jar && \
    curl -L -o lib/bson-4.3.1.jar https://repo1.maven.org/maven2/org/mongodb/bson/4.3.1/bson-4.3.1.jar && \
    curl -L -o lib/mongodb-driver-core-4.3.1.jar https://repo1.maven.org/maven2/org/mongodb/mongodb-driver-core/4.3.1/mongodb-driver-core-4.3.1.jar

# Compile the application
RUN javac -cp "lib/*" ServidorChat.java HiloDeCliente.java DB.java

# Set the entry point to run the application
CMD ["java", "-cp", ".:lib/*", "ServidorChat"]