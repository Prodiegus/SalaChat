package utalca.chatpyme;
import java.io.DataOutputStream;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

/**
 * @author RPVZ
 */
public class ClienteChat{
    private Socket socket;
    private PanelCliente panel;

    public static void main(String[] args){
        new ClienteChat();
        new ClienteChat();
        new ClienteChat();
    }

    public ClienteChat(){
        try{
            DB db = new DB();
            // Solicitar el alias al usuario
            String alias = JOptionPane.showInputDialog(null, "Introduce tu alias:");
            String password = JOptionPane.showInputDialog(null, "Introduce tu contraseña:");

            // Si el usuario no introduce nada, asignar un alias por defecto
            if (alias == null || alias.trim().isEmpty()) {
                alias = "Cliente" + (int)(Math.random() * 1000);  // Alias por defecto
            }
            creaYVisualizaVentana(alias);  // Modificación para recibir el alias como parámetro

            socket = new Socket("localhost", 5000);
            DataOutputStream dataOutput = new DataOutputStream(socket.getOutputStream());

            dataOutput.writeUTF(alias);
            dataOutput.writeUTF(password);

            db.agregarUsuario(alias, password, false, "medico"); // Agregar usuario a la base de datos

            // Pasar el alias al ControlCliente
            ControlCliente control = new ControlCliente(socket, panel, alias);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // Modificación para recibir el alias como parámetro y usarlo en el título de la ventana
    private void creaYVisualizaVentana(String alias) {
        JFrame v = new JFrame(alias);  // Establece el alias como título de la ventana
        panel = new PanelCliente(v.getContentPane());
        v.pack();
        v.setVisible(true);
        v.setSize(600, 300);
        v.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
