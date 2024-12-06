package utalca.chatpyme;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import javax.swing.*;

public class DB {
    private MongoClient mongoClient;
    private MongoDatabase database;

    public DB() {
        mongoClient = MongoClients.create(
                "mongodb+srv://tvalenzuela20:contrasenaSD@clustersd.iezyv.mongodb.net/?retryWrites=true&w=majority&appName=ClusterSD"
        );
        database = mongoClient.getDatabase("chatpyme");
    }

    public MongoCollection<Document> getUsuariosCollection() {
        return database.getCollection("usuarios");
    }

    public MongoCollection<Document> getMensajesCollection() {
        return database.getCollection("mensajes");
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

    public List<String> verUsuario(String nombre) {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        Document usuario = usuarios.find(Filters.eq("nombre", nombre)).first();
        if (usuario != null) {
            return List.of(usuario.getString("nombre"), usuario.getString("clave"), usuario.getBoolean("admin") ? "admin" : "medico", usuario.getString("grupo"));
        }
        return List.of("", "", "medico", "");
    }

    public List<String> getCantidadDeMensajesQueEnviaCadaUsuario() {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        List<String> mensajes = new ArrayList<>();
        usuarios.find().forEach(usuario -> {
            String nombre = usuario.getString("nombre");
            List<String> mensajesUsuario = (List<String>) usuario.get("mensajes");
            int cantidadMensajes = 0;
            for (String mensaje : mensajesUsuario) {
                String autor = mensaje.split(":")[0];
                if (autor.contains(nombre)) {
                    cantidadMensajes++;
                }
            }
            mensajes.add(nombre + ": " + cantidadMensajes);
        });
        return mensajes;
    }

    //Método para agregar un mensaje
    public void agregarMensaje(String contenido) {
        MongoCollection<Document> mensajes = getMensajesCollection();
        Document mensajeDoc = new Document("_id", Instant.now().toEpochMilli())
                                .append("contenido", contenido);
        mensajes.insertOne(mensajeDoc);
    }

    public void guardarMensaje(String nombre, String contenido) {
        try {
            MongoCollection<Document> usuarios = getUsuariosCollection();
            Document usuario = usuarios.find(Filters.eq("nombre", nombre)).first();
            if (usuario != null) {
                List<String> mensajes = (List<String>) usuario.get("mensajes");
                mensajes.add(contenido);
                usuarios.updateOne(Filters.eq("nombre", nombre),
                        Updates.combine(
                                Updates.set("mensajes", mensajes)
                        ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Serialize the query to a file
            serializeQuery(nombre, contenido);
            // Start a thread to retry the operation
            startRetryThread();
        }
    }

    private void serializeQuery(String nombre, String contenido) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("query.ser"))) {
            oos.writeObject(new Query(nombre, contenido));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startRetryThread() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("query.ser"))) {
                Query query = (Query) ois.readObject();
                guardarMensaje(query.nombre, query.contenido);
                new File("query.ser").delete(); // Delete the file after successful execution
                executor.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS); // Retry every 5 seconds
    }

    private static class Query implements Serializable {
        String nombre;
        String contenido;

        Query(String nombre, String contenido) {
            this.nombre = nombre;
            this.contenido = contenido;
        }
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

    public void agregarMensaje(String nombreUsuario, List<String> mensajes) {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        usuarios.updateOne(Filters.eq("nombre", nombreUsuario),
                Updates.combine(
                        Updates.set("mensajes", mensajes)
                ));
    }

    // Método para verificar un usuario
    public List<String> verificarUsuario(String nombre, String clave) {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        Document usuario = usuarios.find(Filters.and(Filters.eq("nombre", nombre), Filters.eq("clave", clave))).first();
        if (usuario != null) {
            boolean admin = usuario.getBoolean("admin");
            return List.of("Usuario Valido", admin ? "admin" : "medico", usuario.getString("grupo"));
        }
        return List.of("Invalido", "", "");
    }

    public void vaciarMensaje(String nombreUsuario) {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        usuarios.updateOne(Filters.eq("nombre", nombreUsuario),
                Updates.combine(
                        Updates.set("mensajes", new ArrayList<String>())
                ));
    }

    public List<String> cantidadDeMensajesEnCadaGrupo() {
        MongoCollection<Document> usuarios = getUsuariosCollection();
        List<String> mensajes = new ArrayList<>();
        usuarios.find().forEach(usuario -> {
            String grupo = usuario.getString("grupo");
            List<String> mensajesUsuario = (List<String>) usuario.get("mensajes");
            int cantidadMensajes = 0;
            for (String mensaje : mensajesUsuario) {
                String grup = mensaje.split(":")[0];
                if (grup.contains(grupo)) {
                    cantidadMensajes++;
                }
            }
            mensajes.add(grupo + ": " + cantidadMensajes);
        });
        return mensajes;
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
            System.out.println("10. Estadisticas mensajes entre usuarios");
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
                    //System.out.println("Usuario verificado: " + verificado);
                    System.out.println(verificado);
                    break;
                case 6:
                    System.out.print("Nombre del usuario: ");
                    nombre = scanner.nextLine();
                    System.out.print("Mensaje: ");
                    String mensaje = scanner.nextLine();
                    List<String> mensajes = db.verMensajes(nombre);
                    mensajes.add(mensaje);
                    mensajes.add(mensaje);
                    break;
                case 7:
                    System.out.print("Nombre del usuario: ");
                    nombre = scanner.nextLine();
                    db.vaciarMensaje(nombre);
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
                case 10:
                    System.out.println("Cantidad de mensajes que envía cada usuario:");
                    List<String> mensajesUsuarios = db.getCantidadDeMensajesQueEnviaCadaUsuario();
                    for (String mensajeUsuario : mensajesUsuarios) {
                        System.out.println(mensajeUsuario);
                    }
                    System.out.print("presione enter para continuar...");
                    scanner.nextLine();
                    System.out.println("Cantidad de mensajes en cada grupo:");
                    List<String> mensajesGrupos = db.cantidadDeMensajesEnCadaGrupo();
                    for (String mensajeGrupo : mensajesGrupos) {
                        System.out.println(mensajeGrupo);
                    }
                    System.out.print("presione enter para continuar...");
                    scanner.nextLine();
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