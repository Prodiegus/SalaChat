db = db.getSiblingDB('chatpyme');

db.createCollection("usuarios");

db.usuarios.createIndex({ nombre: 1 }, { unique: true });

db.usuarios.insertMany([
  { nombre: "usuario1", clave: "clave1", admin: true, mensajes: ["mensaje1", "mensaje2"], grupo: "admin"},
  { nombre: "usuario2", clave: "clave2", admin: false, mensajes: ["mensaje3", "mensaje4"], grupo: "medico"}
]);