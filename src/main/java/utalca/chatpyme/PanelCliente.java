package utalca.chatpyme;

import utalca.chatpyme.DB;

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
    private JButton limpiarBoton;

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
        limpiarBoton = new JButton("Limpiar");

        panel2.add(usuarios, BorderLayout.NORTH);
        panel2.add(new JScrollPane(lista), BorderLayout.CENTER);
        panel2.add(limpiarBoton, BorderLayout.SOUTH);

        panelMensajes.add(scroll, BorderLayout.CENTER);
        panelMensajes.add(panel, BorderLayout.SOUTH);

        panel.add(textField, BorderLayout.CENTER);
        panel.add(boton, BorderLayout.EAST);

        contenedor.add(panelMensajes, BorderLayout.CENTER);
        contenedor.add(panel2, BorderLayout.EAST);

        limpiarBoton.addActionListener(e -> limpiarTexto());
    }

    public void addActionListener(ActionListener accion){
        textField.addActionListener(accion);
        boton.addActionListener(accion);
    }

    public void addTexto(String texto){
        SwingUtilities.invokeLater(() -> {
            textArea.append(texto.trim() + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
        try {
            DB db = new DB();
            db.guardarMensaje(alias, texto.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void iniciarText(String texto){
        SwingUtilities.invokeLater(() -> {
            textArea.append(texto.trim() + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    public String getTexto(){
        String texto = textField.getText().trim();
        textField.setText("");
        return texto;
    }

    public void setAlias(String alias){
        this.alias = alias;
    }

    public void setUsuariosConectados(String[] usuarios){
        lista.setListData(usuarios);
    }

    public void limpiarTexto() {
        textArea.setText("");
        try {
            DB db = new DB();
            db.vaciarMensaje(alias);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}