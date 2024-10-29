db = db.getSiblingDB('chatpyme');

db.createCollection("usuarios");
db.createCollection("mensajes");

db.usuarios.createIndex({ nombre: 1 }, { unique: true });

db.usuarios.insertMany([
  { nombre: "usuario1", clave: "clave1", admin: true },
  { nombre: "usuario2", clave: "clave2", admin: false }
]);

db.mensajes.insertMany([
  { mensajes: ["mensaje1", "mensaje2"], logs: ["log1", "log2"] },
  { mensajes: ["mensaje3", "mensaje4"], logs: ["log3", "log4"] }
]);