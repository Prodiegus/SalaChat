import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ControlCliente implements ActionListener, Runnable{
    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private PanelCliente panel;
    private int idCliente; // ID del cliente

    public ControlCliente(Socket socket, PanelCliente panel){
        this.panel = panel;
        try{
            dataInput = new DataInputStream(socket.getInputStream());
            dataOutput = new DataOutputStream(socket.getOutputStream());
            panel.addActionListener(this);

            // Recibir la ID del cliente al iniciar la conexión
            this.idCliente = Integer.parseInt(dataInput.readUTF().replaceAll("[^0-9]", ""));

            Thread hilo = new Thread(this);
            hilo.start();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent evento){
        try{
            String texto = panel.getTexto();
            if (texto.startsWith("/")) {
                // Comandos de usuario
                if (texto.startsWith("/grupo ")) {
                    String nombreGrupo = texto.split(" ", 2)[1];
                    dataOutput.writeUTF("/grupo " + nombreGrupo);
                } else if (texto.startsWith("/unir ")) {
                    String nombreGrupo = texto.split(" ", 2)[1];
                    dataOutput.writeUTF("/unir " + nombreGrupo);
                    panel.limpiarTexto(); // Limpia la ventana al unirse a un grupo
                } else if (texto.equals("/salir")) {
                    dataOutput.writeUTF("/salir");
                    panel.limpiarTexto(); // Limpia la ventana al salir del grupo
                }
            } else {
                dataOutput.writeUTF(texto);
            }
        } catch (Exception excepcion){
            excepcion.printStackTrace();
        }
    }

    @Override
    public void run(){
        try{
            while (true){
                String texto = dataInput.readUTF();
                panel.addTexto(texto);
                panel.addTexto("\n");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
