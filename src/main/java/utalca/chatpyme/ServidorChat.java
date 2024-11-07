package utalca.chatpyme;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
/**
 *
 * @author RPVZ
 */
public class ServidorChat {
    private static Map<String, DefaultListModel<String>> grupos = new HashMap<>(); // Mapa de grupos
    private static Map<String, HiloDeCliente> clientesConectados = new HashMap<>(); // Mapa de alias a hilos

    public static void main(String[] args) {
        new ServidorChat();
    }

    public ServidorChat() {
        try {
            ServerSocket socketServidor = new ServerSocket(5000);
            while (true) {
                Socket cliente = socketServidor.accept();
                Runnable nuevoCliente = new HiloDeCliente(grupos, cliente,clientesConectados);
                Thread hilo = new Thread(nuevoCliente);
                hilo.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
