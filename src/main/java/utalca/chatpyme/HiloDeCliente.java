package utalca.chatpyme;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.DefaultListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class HiloDeCliente implements Runnable, ListDataListener {
    private Socket socket;
    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private String alias;
    private String tipo;
    private String grupoActual; // Grupo actual al que pertenece el cliente
    private Map<String, DefaultListModel<String>> grupos;
    private static Map<String, HiloDeCliente> clientesConectados = new HashMap<>(); // Mapa de alias a hilos

    public HiloDeCliente(Map<String, DefaultListModel<String>> grupos, Socket socket, Map<String, HiloDeCliente> clientesConectados) {
        this.socket = socket;
        this.grupos = grupos; // Mapa de grupos
        try {
            dataInput = new DataInputStream(socket.getInputStream());
            dataOutput = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        try {
            DB db = new DB();
            while (true) {
                String alias = dataInput.readUTF();
                String password = dataInput.readUTF();

                while (true) {
                    String texto = dataInput.readUTF();
                    String mensajeCompleto = alias + ": " + texto;

                    // Si el cliente envía su alias
                    if (texto.startsWith("/alias ")) {
                        this.alias = texto.split(" ")[1];
                        clientesConectados.put(alias, this); // Agregar cliente al mapa

                        // Inicializar grupos si es necesario
                        grupos.putIfAbsent("medico", new DefaultListModel<>());
                        grupos.putIfAbsent("admin", new DefaultListModel<>());
                        grupos.putIfAbsent("auxiliar", new DefaultListModel<>());
                        grupos.putIfAbsent("admicion", new DefaultListModel<>());
                        grupos.putIfAbsent("pabellon", new DefaultListModel<>());
                        grupos.putIfAbsent("examenes", new DefaultListModel<>());
                        // Asignar el grupo por defecto
                        this.grupoActual = db.verUsuario(alias).get(3);
                        this.tipo = db.verUsuario(alias).get(2);
                        grupos.get(grupoActual).addElement(alias + " se ha conectado.");
                        // Notificar a todos los miembros del grupo que el nuevo cliente se ha unido
                        enviarUsuariosConectadosATodos();
                        notificarGrupo("¡" + alias + " se ha unido al grupo " + grupoActual + "!");
                    }

                    // Comando para unirse a un grupo
                    else if (texto.startsWith("/unir ")) {
                        String nuevoGrupo = texto.split(" ")[1];
                        if (Objects.equals(nuevoGrupo, "admin") && !Objects.equals(tipo, "admin")) {
                            dataOutput.writeUTF("No tienes permiso para unirte al grupo admin.");
                        } else if (nuevoGrupo.equals(grupoActual)) {
                            dataOutput.writeUTF("Ya estás en el grupo " + grupoActual);
                        } else if (grupos.containsKey(nuevoGrupo)) {
                            grupos.get(grupoActual).removeElement(alias + " se ha desconectado de " + grupoActual);
                            grupoActual = nuevoGrupo;
                            grupos.get(grupoActual).addElement(alias + " se ha unido a " + grupoActual);
                            // Notificar a todos los miembros del nuevo grupo
                            db.actualizarUsuario(alias, tipo.equals("admin"), grupoActual);
                            notificarGrupo("¡" + alias + " se ha unido al grupo " + grupoActual + "!");
                            dataOutput.writeUTF("Te has unido al grupo: " + grupoActual);
                        } else {
                            dataOutput.writeUTF("El grupo " + nuevoGrupo + " no existe.");
                        }
                    }
                    // Mensaje privado
                    else if (texto.startsWith("/privado ")) {
                        String[] partes = texto.split(" ", 3); // Parte 1: /privado, Parte 2: destinatario, Parte 3: mensaje
                        String destinatario = partes[1];
                        String mensajePrivado = partes[2];
                    
                        // Procesar el mensaje para añadir formato si es necesario
                        if (mensajePrivado.startsWith("/negrita ")) {
                            mensajePrivado = "<b>" + mensajePrivado.substring(9) + "</b>"; // Elimina "/negrita " y aplica formato
                        } else if (mensajePrivado.startsWith("/cursiva ")) {
                            mensajePrivado = "<i>" + mensajePrivado.substring(9) + "</i>"; // Elimina "/cursiva " y aplica formato
                        } else if (mensajePrivado.startsWith("/subrayado ")) {
                            mensajePrivado = "<u>" + mensajePrivado.substring(12) + "</u>"; // Elimina "/subrayado " y aplica formato
                        }
                        this.dataOutput.writeUTF("[Privado a " + destinatario + "]: " + mensajePrivado);
                        // Buscar el destinatario en el mapa de clientes conectados
                        HiloDeCliente clienteDestino = clientesConectados.get(destinatario);
                        if (clienteDestino != null) {
                            clienteDestino.dataOutput.writeUTF("Mensaje privado de " + alias + ": " + mensajePrivado);
                        } else {
                            db.guardarMensaje(alias, "Mensaje privado a " + destinatario + ": " + mensajePrivado+"\n");
                            db.guardarMensaje(destinatario, "Mensaje privado de " + alias + ": " + mensajePrivado+"\n");
                        }
                    }
                    else if (texto.startsWith("/negrita ")) {
                        String mensaje = texto.substring(9);
                        mensaje = "<b>" + mensaje + "</b>";
                        dataOutput.writeUTF(mensaje);
                    } else if (texto.startsWith("/cursiva ")) {
                        String mensaje = texto.substring(9);
                        mensaje = "<i>" + mensaje + "</i>";
                        dataOutput.writeUTF(mensaje);
                    } else if (texto.startsWith("/subrayado ")) {
                        String mensaje = texto.substring(12);
                        mensaje = "<u>" + mensaje + "</u>";
                        dataOutput.writeUTF(mensaje);
                    } else if (texto.startsWith("/crear ")) {
                        if (tipo.equals("admin")) {
                            String nombre = texto.split(" ")[1];
                            String clave = texto.split(" ")[2];
                            Boolean admin = texto.split(" ")[3].equals("admin");
                            String grupo = texto.split(" ")[4];
                            db.agregarUsuario(nombre, clave, admin, grupo);
                            dataOutput.writeUTF("Usuario creado con éxito.");
                        } else {
                            dataOutput.writeUTF("No tienes permiso para crear usuarios.");
                        }
                    } else if (texto.startsWith("/grupo ")) {
                        String[] partes = texto.split(" ", 3); // Parte 1: /grupo, Parte 2: grupo, Parte 3: mensaje
                        String destinatario = partes[1];
                        String mensajePrivado = partes[2];
                    
                        // Procesar el mensaje para añadir formato si es necesario
                        if (mensajePrivado.startsWith("/negrita ")) {
                            mensajePrivado = "<b>" + mensajePrivado.substring(9) + "</b>";
                        } else if (mensajePrivado.startsWith("/cursiva ")) {
                            mensajePrivado = "<i>" + mensajePrivado.substring(9) + "</i>";
                        } else if (mensajePrivado.startsWith("/subrayado ")) {
                            mensajePrivado = "<u>" + mensajePrivado.substring(12) + "</u>";
                        }
                    
                        // Verificar si el destinatario es un grupo
                        if (grupos.containsKey(destinatario)) {
                            // Enviar el mensaje solo a los miembros del grupo
                            DefaultListModel<String> mensajesGrupo = grupos.get(destinatario);
                            synchronized (mensajesGrupo) {
                                mensajesGrupo.addElement(alias + ": " + mensajePrivado);
                                // Notificar a todos los clientes en el grupo
                                for (HiloDeCliente cliente : clientesConectados.values()) {
                                    if (cliente.grupoActual.equals(destinatario)) {
                                        db.guardarMensaje(alias, "[" + grupoActual + "]" + alias + ": " + mensajePrivado);
                                        cliente.dataOutput.writeUTF("[" + grupoActual + "]" + alias + ": " + mensajePrivado);
                                    }
                                }
                            }
                        } else {
                            dataOutput.writeUTF("El grupo " + destinatario + " no existe.");
                        }
                    }
                    
                    // Mensaje a todos los clientes
                    else if (texto.startsWith("/all ")) {
                        String mensaje = texto.substring(5); // Extrae el mensaje después del comando
                        for (HiloDeCliente cliente : clientesConectados.values()) {
                            cliente.dataOutput.writeUTF("[Todos]" + alias + ": " + mensaje);
                        }
                    } else if (texto.startsWith("/tipo ")) {
                        this.tipo = texto.split(" ")[1];
                    }
                    // Mensaje de grupo
                    else {
                        // Enviar el mensaje solo a los miembros del grupo actual
                        DefaultListModel<String> mensajesGrupo = grupos.get(grupoActual);
                        synchronized (mensajesGrupo) {
                            mensajesGrupo.addElement(alias + ": " + texto);
                            // Notificar a todos los clientes en el grupo
                            for (HiloDeCliente cliente : clientesConectados.values()) {
                                if (cliente.grupoActual.equals(grupoActual)) {
                                    cliente.dataOutput.writeUTF("["+grupoActual+"]"+alias + ": " + texto);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notificarGrupo(String mensaje) {
        for (HiloDeCliente cliente : clientesConectados.values()) {
            if (cliente.grupoActual.equals(grupoActual)) {
                try {
                    cliente.dataOutput.writeUTF(mensaje);
                } catch (IOException e) {
                    e.printStackTrace(); // Manejo de la excepción
                }
            }
        }
    }

    private void enviarUsuariosConectadosATodos() {
        for (HiloDeCliente cliente : clientesConectados.values()) {
            cliente.enviarUsuariosConectados();
        }
    }

    private void enviarUsuariosConectados() {
        try {
            StringBuilder usuariosConectados = new StringBuilder();
            for (Map.Entry<String, HiloDeCliente> entry : clientesConectados.entrySet()) {
                String usuario = entry.getKey();
                String grupo = entry.getValue().grupoActual;
                usuariosConectados.append(usuario).append(" (").append(grupo).append(")\n");
            }
            dataOutput.writeUTF("/usuarios " + usuariosConectados.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Implementación de ListDataListener
    @Override
    public void intervalAdded(ListDataEvent e) { }

    @Override
    public void intervalRemoved(ListDataEvent e) { }

    @Override
    public void contentsChanged(ListDataEvent e) { }
}
