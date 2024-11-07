package utalca.chatpyme;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
/**
 *
 * @author RPVZ
 */
public class ControlCliente implements ActionListener, Runnable{
    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private PanelCliente panel;
    private DB db; // Instancia de la clase DB
    
    public ControlCliente(Socket socket, PanelCliente panel, String alias){
        this.panel = panel;
        this.db = new DB(); //inicializa la instancia de DB
        try{
            dataInput = new DataInputStream(socket.getInputStream());
            dataOutput = new DataOutputStream(socket.getOutputStream());
            panel.addActionListener(this);
    
            // Enviar el alias del cliente al servidor
            dataOutput.writeUTF("/alias " + alias);
    
            // Iniciar hilo
            Thread hilo = new Thread(this);
            hilo.setName(alias);  // Asignar alias como nombre del hilo
            hilo.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void actionPerformed(ActionEvent evento){
        try{
            String mensaje = panel.getTexto();
    
            // Si el mensaje es para unirse a un grupo
            if (mensaje.startsWith("/unir ")) {
                dataOutput.writeUTF(mensaje);  // Enviar comando para unirse a un grupo
            }
            // Si el mensaje es un mensaje privado
            else if (mensaje.startsWith("/privado ")) {
                dataOutput.writeUTF(mensaje);  // Enviar mensaje privado al servidor
            }
            // Mensaje público
            else {
                dataOutput.writeUTF(mensaje);  // Enviar mensaje público al servidor
            }
            // Agregar mensaje a la base de datos
            db.agregarMensaje(mensaje);
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
