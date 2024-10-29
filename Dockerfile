# Usa la imagen oficial de MongoDB
FROM mongo:latest

# Copia el script de inicialización a la imagen del contenedor
COPY init-mongo.js /docker-entrypoint-initdb.d/

# Exponer el puerto de MongoDB
EXPOSE 27017

# Definir un volumen para almacenar los datos de MongoDB
VOLUME /mongo