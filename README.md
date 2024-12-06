# SalaChat

# Iniciación de aplicacion
El Servidor y la base de datos estan en una instancia de Google Cloud (GCP)
asi que para inicial el Chat solo consta de iniciar la carpeta ClienteChat.java

Al comenzar con la aplicacion preguntara cuantos usuarios desea conectar (1 ideal para usarlo de manera online y <2 ideal para trabajarlo de manera offline)

# Ingreso
Por defecto el codigo tiene incluido 2 usuarios que son:

usuario1 - clave1
usuario2 - clave2

Pero para crear un usuario de manera manual solo basta con introducir el parametro de nombre y contraseña y se genera automaticamente con el rol de medico.

# Comandos Disponibles
/privado <usuario> <mensaje>: Envía un mensaje privado al usuario especificado.

/negrita <mensaje>: Envía un mensaje en negrita.

/cursiva <mensaje>: Envía un mensaje en cursiva.

/subrayado <mensaje>: Envía un mensaje subrayado.

/all <mensaje>: Envía un mensaje a todos los clientes conectados.

/grupo <nombre>: Crea un nuevo grupo con el nombre especificado.

/unir <nombre>: Une al usuario al grupo especificado.

/alias <nuevo_alias>: Cambia el alias del usuario.

/crear <nombre_grupo>: Crea un nuevo grupo (solo para usuarios con rol de admin).