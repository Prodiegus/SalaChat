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
    private String tipo;
    private String alias;
    
    public ControlCliente(Socket socket, PanelCliente panel, String alias, String tipo){
        this.tipo = tipo;
        this.panel = panel;
        this.alias = alias;
        this.db = new DB(); //inicializa la instancia de DB
        try{
            dataInput = new DataInputStream(socket.getInputStream());
            dataOutput = new DataOutputStream(socket.getOutputStream());
            panel.addActionListener(this);
            panel.addTexto("Conectado como " + alias + "\n");
            panel.addTexto("Tipo de usuario: " + tipo + "\n");

            dataOutput.writeUTF("/tipo " + tipo);
            Thread.sleep(1000);
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
                String nuevoGrupo = mensaje.split(" ")[1];
                dataOutput.writeUTF(mensaje);
                panel.addTexto("Uniendo al grupo " + nuevoGrupo + ".\n");
                db.actualizarUsuario(alias, tipo.equals("admin") ,nuevoGrupo);
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
            //db.agregarMensaje(mensaje);
            db.guardarMensaje(alias, alias+": "+mensaje);
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
