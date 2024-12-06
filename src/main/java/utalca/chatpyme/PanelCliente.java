package utalca.chatpyme;

import utalca.chatpyme.DB;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.*;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

public class PanelCliente extends JPanel {
    private JScrollPane scroll;
    private JTextPane textPane; // Cambiar JTextArea por JTextPane
    private JList<String> lista;
    private JLabel usuarios;
    private JTextField textField;
    private JButton boton;
    private JButton limpiarBoton;

    private String alias;

    public PanelCliente(Container contenedor){
        contenedor.setLayout(new BorderLayout());
        textPane = new JTextPane();
        textPane.setContentType("text/html"); // Permite interpretar HTML
        textPane.setEditable(false);
        scroll = new JScrollPane(textPane);
        lista = new JList<>();
        usuarios = new JLabel("Usuarios conectados");

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

    // Método addTexto para agregar texto con HTML
    public void addTexto(String texto){
        try {
            // Obtenemos el documento y agregamos el texto HTML al final
            HTMLDocument doc = (HTMLDocument) textPane.getDocument();
            doc.insertAfterEnd(doc.getCharacterElement(doc.getLength()), texto + "<br>");
        } catch (BadLocationException | IOException e) {
            e.printStackTrace();
        }
        try {
            DB db = new DB();
            db.guardarMensaje(alias, texto.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void iniciarText(String texto){
        try {
            // Obtenemos el documento y agregamos el texto HTML al final
            HTMLDocument doc = (HTMLDocument) textPane.getDocument();
            doc.insertAfterEnd(doc.getCharacterElement(doc.getLength()), texto + "<br>");
        } catch (BadLocationException | IOException e) {
            e.printStackTrace();
        }
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
        textPane.setText(""); // Usar textPane en lugar de textArea
        try {
            DB db = new DB();
            db.vaciarMensaje(alias);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}