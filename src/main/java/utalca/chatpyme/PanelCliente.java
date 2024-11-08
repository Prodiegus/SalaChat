package utalca.chatpyme;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionListener;

import javax.swing.*;

public class PanelCliente extends JPanel {
    private JScrollPane scroll;
    private JList<String> lista;
    private JLabel usuarios;
    private JTextArea textArea;
    private JTextField textField;
    private JButton boton;

    private String alias;

    public PanelCliente(Container contenedor){
        contenedor.setLayout(new BorderLayout());
        lista = new JList<>();
        usuarios = new JLabel("Usuarios conectados");
        textArea = new JTextArea();
        scroll = new JScrollPane(textArea);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel panel2 = new JPanel(new BorderLayout());
        JPanel panelMensajes = new JPanel(new BorderLayout());

        textField = new JTextField(50);
        boton = new JButton("Enviar");

        panel2.add(usuarios, BorderLayout.NORTH);
        panel2.add(new JScrollPane(lista), BorderLayout.CENTER);

        panelMensajes.add(scroll, BorderLayout.CENTER);
        panelMensajes.add(panel, BorderLayout.SOUTH);

        panel.add(textField, BorderLayout.CENTER);
        panel.add(boton, BorderLayout.EAST);

        contenedor.add(panelMensajes, BorderLayout.CENTER);
        contenedor.add(panel2, BorderLayout.EAST);
    }

    public void addActionListener(ActionListener accion){
        textField.addActionListener(accion);
        boton.addActionListener(accion);
    }

    public void addTexto(String texto){
        SwingUtilities.invokeLater(() -> {
            textArea.append(texto + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
        try {
            DB db = new DB();
            db.guardarMensaje(alias, texto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void iniciarText(String texto){
        SwingUtilities.invokeLater(() -> {
            textArea.append(texto + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    public String getTexto(){
        String texto = textField.getText();
        textField.setText("");
        return texto;
    }

    public void setAlias(String alias){
        this.alias = alias;
    }

    public void setUsuariosConectados(String[] usuarios){
        lista.setListData(usuarios);
    }


    // Método para limpiar la ventana del chat
    public void limpiarTexto() {
        textArea.setText("");
    }
}
