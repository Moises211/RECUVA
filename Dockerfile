
#con este dockerfile se ejecuta el wrapper de maven sin como en el lab.
FROM maven:3.8.4-openjdk-17 AS build

FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar todos los archivos del proyecto al contenedor
COPY . .

# Dar permisos para que se ejecute en kernel Linux
RUN chmod +x mvnw

# Instalar Maven Wrapper y compilar el proyecto
RUN ./mvnw clean package -DskipTests

# Exponer el puerto en el que se ejecutará tu aplicación (por ejemplo, 8080)
EXPOSE 8080

WORKDIR /app

CMD ["java", "-jar", "target/RECUVA-0.0.1-SNAPSHOT.jar"]
