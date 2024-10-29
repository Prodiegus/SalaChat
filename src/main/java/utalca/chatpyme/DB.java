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
import java.util.ArrayList;

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

    // Método para agregar un usuario
    public void agregarUsuario(String nombre, String clave, boolean admin, String grupo) {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        Document usuario = new Document("nombre", nombre)
                                .append("clave", clave)
                                .append("admin", admin)
                                .append("grupo", grupo)
                                .append("mensajes", new ArrayList<String>());
        usuarios.insertOne(usuario);
    }

    // Método para eliminar un usuario
    public void eliminarUsuario(String nombre) {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        usuarios.deleteOne(Filters.eq("nombre", nombre));
    }

    // Método para actualizar un usuario
    public void actualizarClave(String nombre, String nuevaClave) {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        usuarios.updateOne(Filters.eq("nombre", nombre),
                           Updates.combine(
                               Updates.set("clave", nuevaClave)
                           ));
    }

    public void actualizarUsuario(String nombre, boolean nuevoAdmin, String nuevoGrupo) {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        usuarios.updateOne(Filters.eq("nombre", nombre),
                Updates.combine(
                        Updates.set("admin", nuevoAdmin),
                        Updates.set("grupo", nuevoGrupo)
                ));
    }

    // Método para ver los mensajes de un usuario
    public List<String> verMensajes(String nombreUsuario) {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        Document usuario = usuarios.find(Filters.eq("nombre", nombreUsuario)).first();
        if (usuario != null) {
            return (List<String>) usuario.get("mensajes");
        }
        return new ArrayList<>();
    }

    // Método para verificar un usuario
    public List<String> verificarUsuario(String nombre, String clave) {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        Document usuario = usuarios.find(Filters.and(Filters.eq("nombre", nombre), Filters.eq("clave", clave))).first();
        if (usuario != null) {
            boolean admin = usuario.getBoolean("admin");
            String grupo = usuario.getString("grupo");
            return List.of("Valido", String.valueOf(admin), grupo);
        }
        return List.of("Invalido", "", "");
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
                    scanner.nextLine(); // Consumir el salto de línea
                    System.out.print("Grupo: ");
                    String grupo = scanner.nextLine();
                    db.agregarUsuario(nombre, clave, admin, grupo);
                    break;
                case 2:
                    System.out.print("Nombre: ");
                    nombre = scanner.nextLine();
                    db.eliminarUsuario(nombre);
                    break;
                case 3:
                    // Implementar método actualizarUsuario si es necesario
                    break;
                case 4:
                    System.out.println("Usuarios:");
                    db.getUsuariosCollection().find().forEach(System.out::println);

                    break;
                case 5:
                    System.out.print("Nombre: ");
                    nombre = scanner.nextLine();
                    System.out.print("Clave: ");
                    clave = scanner.nextLine();
                    List<String> verificado = db.verificarUsuario(nombre, clave);
                    System.out.println("Usuario verificado: " + verificado);
                    break;
                case 6:
                    // Implementar método agregarMensaje si es necesario
                    break;
                case 7:
                    // Implementar método eliminarMensaje si es necesario
                    break;
                case 8:
                    // Implementar método actualizarMensaje si es necesario
                    break;
                case 9:
                    System.out.print("Nombre del usuario: ");
                    nombre = scanner.nextLine();
                    List<String> mensajesUsuario = db.verMensajes(nombre);
                    System.out.println("Mensajes del usuario: " + mensajesUsuario);
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