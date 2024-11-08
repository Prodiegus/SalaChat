package utalca.chatpyme;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

import javax.swing.*;

/**
 * @author RPVZ
 */
public class ClienteChat{
    private Socket socket;
    private PanelCliente panel;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Cuantos clientes quieres crear conectar: ");
        int n = sc.nextInt();
        for (int i = 0; i < n; i++) {
            new ClienteChat();
        }
    }

    public ClienteChat() {
        try {
            DB db = new DB();
            mostrarDialogoLogin(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDialogoLogin(DB db) {
        boolean autenticado = false;

        while (!autenticado) {
            // Crear el panel de inicio de sesión
            JPanel loginPanel = new JPanel();
            GroupLayout layout = new GroupLayout(loginPanel);
            loginPanel.setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            JLabel aliasLabel = new JLabel("Introduce tu alias:");
            JTextField aliasField = new JTextField();
            JLabel passwordLabel = new JLabel("Introduce tu contraseña:");
            JPasswordField passwordField = new JPasswordField();
            JLabel errorLabel = new JLabel(""); // Etiqueta para mostrar mensajes de error

            layout.setHorizontalGroup(
                layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(aliasLabel)
                        .addComponent(passwordLabel)
                        .addComponent(errorLabel))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(aliasField)
                        .addComponent(passwordField))
            );

            layout.setVerticalGroup(
                layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(aliasLabel)
                        .addComponent(aliasField))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(passwordLabel)
                        .addComponent(passwordField))
                    .addComponent(errorLabel)
            );

            // Mostrar el panel en un cuadro de diálogo
            int option = JOptionPane.showConfirmDialog(null, loginPanel, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                String alias = aliasField.getText();
                String password = new String(passwordField.getPassword());

                // Si el usuario no introduce nada, asignar un alias por defecto
                if (alias == null || alias.trim().isEmpty()) {
                    alias = "Cliente" + (int) (Math.random() * 1000); // Alias por defecto
                }

                try {
                    // Conectar al servidor
                    socket = new Socket("localhost", 5000);
                    DataOutputStream dataOutput = new DataOutputStream(socket.getOutputStream());

                    dataOutput.writeUTF(alias);
                    dataOutput.writeUTF(password);
                    List<String> res = db.verificarUsuario(alias, password);
                    if (res.get(0).equals("Invalido")) { // Verifica si el usuario está en la base de datos
                        try{
                            db.agregarUsuario(alias, password, false, "medico");  // Agrega usuario a la base de datos

                            // Pasar el alias al ControlCliente
                            // ControlCliente control = new ControlCliente(socket, panel, alias);

                        } catch (Exception e){
                            JOptionPane.showMessageDialog(null, "Usuario o contraseña erronea");
                            return;
                        }
                    } else {
                        autenticado = true;  // Usuario autenticado
                        creaYVisualizaVentana(alias); // Crear la ventana del chat
                        ControlCliente control = new ControlCliente(socket, panel, alias, res.get(1)); // Crear el control cliente
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                break;  // Si el usuario cancela, salir del bucle
            }
        }
    }

    // Modificación para recibir el alias como parámetro y usarlo en el título de la ventana
    private void creaYVisualizaVentana(String alias) {
        SwingUtilities.invokeLater(() -> {
            JFrame v = new JFrame(alias);  // Establece el alias como título de la ventana
            panel = new PanelCliente(v.getContentPane());
            panel.setAlias(alias);
            try {
                DB db = new DB();
                List<String> mensajes = db.verMensajes(alias);
                for (String mensaje : mensajes) {
                    panel.iniciarText(mensaje);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            v.pack();
            v.setVisible(true);
            v.setSize(800, 300);
            v.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            String tipo = "medico";
            try {
                DB db = new DB();
                tipo = db.verUsuario(alias).get(3);
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ControlCliente(socket, panel, alias, tipo);
        });
    }
}
