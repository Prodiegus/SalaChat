package utalca.chatpyme;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import java.util.List;
import java.util.Scanner;

public class DB {
    private MongoClient mongoClient;
    private MongoDatabase database;

    public DB() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("chatpyme");
    }

    public MongoCollection<Document> getUsuariosCollection() {
        return database.getCollection("usuarios");
    }

    public MongoCollection<Document> getMensajesCollection() {
        return database.getCollection("mensajes");
    }

    // Método para agregar un usuario
    public void agregarUsuario(String nombre, String clave, boolean admin) {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        Document usuario = new Document("nombre", nombre)
                                .append("clave", clave)
                                .append("admin", admin);
        usuarios.insertOne(usuario);
    }

    // Método para eliminar un usuario
    public void eliminarUsuario(String nombre) {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        usuarios.deleteOne(Filters.eq("nombre", nombre));
    }

    // Método para actualizar un usuario
    public void actualizarUsuario(String nombre, String nuevaClave, boolean nuevoAdmin) {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        usuarios.updateOne(Filters.eq("nombre", nombre),
                           Updates.combine(
                               Updates.set("clave", nuevaClave),
                               Updates.set("admin", nuevoAdmin)
                           ));
    }

    // Método para agregar un mensaje
    public void agregarMensaje(List<String> mensajes, List<String> logs) {
        MongoCollection<Document> mensajesCollection = getMensajesCollection();
        Document mensaje = new Document("mensajes", mensajes)
                                .append("logs", logs);
        mensajesCollection.insertOne(mensaje);
    }

    // Método para eliminar un mensaje
    public void eliminarMensaje(String mensajeId) {
        MongoCollection<Document> mensajesCollection = getMensajesCollection();
        mensajesCollection.deleteOne(Filters.eq("_id", mensajeId));
    }

    // Método para actualizar un mensaje
    public void actualizarMensaje(String mensajeId, List<String> nuevosMensajes, List<String> nuevosLogs) {
        MongoCollection<Document> mensajesCollection = getMensajesCollection();
        mensajesCollection.updateOne(Filters.eq("_id", mensajeId),
                                     Updates.combine(
                                         Updates.set("mensajes", nuevosMensajes),
                                         Updates.set("logs", nuevosLogs)
                                     ));
    }

    // Método para ver todos los usuarios
    public void verUsuarios() {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        for (Document usuario : usuarios.find()) {
            System.out.println(usuario.toJson());
        }
    }

    // Método para ver todos los mensajes
    public void verMensajes() {
        MongoCollection<Document> mensajes = getMensajesCollection();
        for (Document mensaje : mensajes.find()) {
            System.out.println(mensaje.toJson());
        }
    }

    // Método para verificar un usuario
    public boolean verificarUsuario(String nombre, String clave) {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        Document usuario = usuarios.find(Filters.and(Filters.eq("nombre", nombre), Filters.eq("clave", clave))).first();
        return usuario != null;
    }

    public static void main(String[] args) {
        DB db = new DB();
        Scanner scanner = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("Menú:");
            System.out.println("1. Agregar usuario");
            System.out.println("2. Eliminar usuario");
            System.out.println("3. Actualizar usuario");
            System.out.println("4. Ver usuarios");
            System.out.println("5. Verificar usuario");
            System.out.println("6. Agregar mensaje");
            System.out.println("7. Eliminar mensaje");
            System.out.println("8. Actualizar mensaje");
            System.out.println("9. Ver mensajes");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");
            opcion = scanner.nextInt();
            scanner.nextLine(); // Consumir el salto de línea

            switch (opcion) {
                case 1:
                    System.out.print("Nombre: ");
                    String nombre = scanner.nextLine();
                    System.out.print("Clave: ");
                    String clave = scanner.nextLine();
                    System.out.print("Admin (true/false): ");
                    boolean admin = scanner.nextBoolean();
                    db.agregarUsuario(nombre, clave, admin);
                    break;
                case 2:
                    System.out.print("Nombre: ");
                    nombre = scanner.nextLine();
                    db.eliminarUsuario(nombre);
                    break;
                case 3:
                    System.out.print("Nombre: ");
                    nombre = scanner.nextLine();
                    System.out.print("Nueva clave: ");
                    String nuevaClave = scanner.nextLine();
                    System.out.print("Nuevo admin (true/false): ");
                    boolean nuevoAdmin = scanner.nextBoolean();
                    db.actualizarUsuario(nombre, nuevaClave, nuevoAdmin);
                    break;
                case 4:
                    db.verUsuarios();
                    break;
                case 5:
                    System.out.print("Nombre: ");
                    nombre = scanner.nextLine();
                    System.out.print("Clave: ");
                    clave = scanner.nextLine();
                    boolean verificado = db.verificarUsuario(nombre, clave);
                    System.out.println("Usuario verificado: " + verificado);
                    break;
                case 6:
                    System.out.print("Mensajes (separados por comas): ");
                    List<String> mensajes = List.of(scanner.nextLine().split(","));
                    System.out.print("Logs (separados por comas): ");
                    List<String> logs = List.of(scanner.nextLine().split(","));
                    db.agregarMensaje(mensajes, logs);
                    break;
                case 7:
                    System.out.print("ID del mensaje: ");
                    String mensajeId = scanner.nextLine();
                    db.eliminarMensaje(mensajeId);
                    break;
                case 8:
                    System.out.print("ID del mensaje: ");
                    mensajeId = scanner.nextLine();
                    System.out.print("Nuevos mensajes (separados por comas): ");
                    List<String> nuevosMensajes = List.of(scanner.nextLine().split(","));
                    System.out.print("Nuevos logs (separados por comas): ");
                    List<String> nuevosLogs = List.of(scanner.nextLine().split(","));
                    db.actualizarMensaje(mensajeId, nuevosMensajes, nuevosLogs);
                    break;
                case 9:
                    db.verMensajes();
                    break;
                case 0:
                    System.out.println("Saliendo...");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } while (opcion != 0);

        scanner.close();
    }
}