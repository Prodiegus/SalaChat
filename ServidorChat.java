import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.DefaultListModel;

public class ServidorChat {
    private DefaultListModel mensajes = new DefaultListModel();
    private int idCliente = 1;

    public static void main(String[] args) {
        new ServidorChat();
    }

    public ServidorChat() {
        try {
            ServerSocket socketServidor = new ServerSocket(5000);
            while (true) {
                Socket cliente = socketServidor.accept();
                Runnable nuevoCliente = new HiloDeCliente(mensajes, cliente, idCliente);
                Thread hilo = new Thread(nuevoCliente);
                hilo.start();
                idCliente++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
