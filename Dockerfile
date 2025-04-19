
#con este dockerfile se ejecuta el wrapper de maven sin como en el lab.
FROM maven:3.8.4-openjdk-17 AS build

FROM openjdk:17-jdk-slim

RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copiar todos los archivos del proyecto al contenedor
COPY . .

# Dar permisos para que se ejecute en kernel Linux
RUN chmod +x mvnw

# Instalar Maven Wrapper y compilar el proyecto
#RUN ./mvnw clean package -DskipTests
RUN ["./mvnw", "clean", "package", "-DskipTests"]
RUN mv /app/target/novarentacares-1.0-SNAPSHOT.jar /app/app.jar
# Exponer el puerto en el que se ejecutará tu aplicación (por ejemplo, 8080)
#EXPOSE 8080
EXPOSE 9000

WORKDIR /app

#CMD ["java", "-jar", "target/RECUVA-0.0.1-SNAPSHOT.jar"]
CMD ["java", "-jar", "/app/app.jar"]