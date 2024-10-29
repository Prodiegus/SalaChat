
import java.net.Socket;
import javax.swing.JFrame;
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
            creaYVisualizaVentana();
            socket = new Socket("localhost", 5000);
            ControlCliente control = new ControlCliente(socket, panel);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private void creaYVisualizaVentana(){
        JFrame v = new JFrame();
        panel = new PanelCliente(v.getContentPane());
        v.pack();
        v.setVisible(true);
        v.setSize(600, 300);
        v.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}