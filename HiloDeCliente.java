import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class HiloDeCliente implements Runnable, ListDataListener {
    private DefaultListModel mensajes;
    private Socket socket;
    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private int idCliente; // Identificador del cliente
    private String grupoActual = null; // Grupo al que pertenece el cliente, null si está en el chat global
    private static HashMap<String, ArrayList<HiloDeCliente>> grupos = new HashMap<>(); // Grupos y sus miembros
    private static HashMap<Integer, DataOutputStream> clientesConectados = new HashMap<>(); // Lista de clientes conectados

    public HiloDeCliente(DefaultListModel mensajes, Socket socket, int idCliente) {
        this.mensajes = mensajes;
        this.socket = socket;
        this.idCliente = idCliente;
        try {
            dataInput = new DataInputStream(socket.getInputStream());
            dataOutput = new DataOutputStream(socket.getOutputStream());

            // Almacenar el stream de este cliente en el HashMap
            synchronized (clientesConectados) {
                clientesConectados.put(idCliente, dataOutput);
            }

            // Enviar la ID al cliente
            dataOutput.writeUTF("Tu ID es: Cliente " + idCliente);

            mensajes.addListDataListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String texto = dataInput.readUTF();

                // Manejo de comandos de grupos
                if (texto.startsWith("/grupo ")) {
                    String nombreGrupo = texto.split(" ", 2)[1];
                    if (!grupos.containsKey(nombreGrupo)) {
                        grupos.put(nombreGrupo, new ArrayList<>());
                        dataOutput.writeUTF("Grupo " + nombreGrupo + " creado.");
                    } else {
                        dataOutput.writeUTF("El grupo " + nombreGrupo + " ya existe.");
                    }
                } else if (texto.startsWith("/unir ")) {
                    String nombreGrupo = texto.split(" ", 2)[1];
                    if (grupos.containsKey(nombreGrupo)) {
                        if (grupoActual != null) {
                            grupos.get(grupoActual).remove(this); // Salir del grupo actual
                        }
                        grupoActual = nombreGrupo;
                        grupos.get(grupoActual).add(this); // Unirse al nuevo grupo
                        dataOutput.writeUTF("Te has unido al grupo " + nombreGrupo);
                        mensajes.addElement("Cliente " + idCliente + " se ha unido al grupo " + nombreGrupo);
                    } else {
                        dataOutput.writeUTF("El grupo " + nombreGrupo + " no existe.");
                    }
                } else if (texto.equals("/salir")) {
                    if (grupoActual != null) {
                        grupos.get(grupoActual).remove(this); // Salir del grupo
                        dataOutput.writeUTF("Has salido del grupo y estás de vuelta en el chat global.");
                        mensajes.addElement("Cliente " + idCliente + " ha salido del grupo " + grupoActual);
                        grupoActual = null; // Volver al chat global
                    }
                } else if (texto.startsWith("@")) {
                    // Enviar mensaje privado
                    String[] partes = texto.split(":", 2);
                    String idDestinoStr = partes[0].substring(1).trim();
                    String mensajePrivado = partes[1].trim();

                    try {
                        int idDestino = Integer.parseInt(idDestinoStr);
                        if (clientesConectados.containsKey(idDestino)) {
                            DataOutputStream clienteDestino = clientesConectados.get(idDestino);
                            clienteDestino.writeUTF("(Privado de Cliente " + idCliente + "): " + mensajePrivado);
                            dataOutput.writeUTF("(Privado a Cliente " + idDestino + "): " + mensajePrivado);
                        } else {
                            dataOutput.writeUTF("El cliente con ID " + idDestino + " no está conectado.");
                        }
                    } catch (NumberFormatException e) {
                        dataOutput.writeUTF("ID de cliente no válido.");
                    }
                } else {
                    // Enviar mensaje al grupo o al chat global
                    if (grupoActual != null) {
                        // Mensaje al grupo
                        ArrayList<HiloDeCliente> miembros = grupos.get(grupoActual);
                        for (HiloDeCliente miembro : miembros) {
                            miembro.dataOutput.writeUTF("(Grupo " + grupoActual + ") Cliente " + idCliente + ": " + texto);
                        }
                    } else {
                        // Mensaje al chat global
                        synchronized (mensajes) {
                            mensajes.addElement("Cliente " + idCliente + ": " + texto);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        String texto = (String) mensajes.getElementAt(e.getIndex0());

        try {
            // Enviar solo mensajes del chat global a los clientes que no estén en un grupo
            if (grupoActual == null && texto.startsWith("Cliente ")) {
                dataOutput.writeUTF(texto);
            }
        } catch (Exception excepcion) {
            excepcion.printStackTrace();
        }
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {}

    @Override
    public void contentsChanged(ListDataEvent e) {}
}
