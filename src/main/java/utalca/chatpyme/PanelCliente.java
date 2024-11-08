package utalca.chatpyme;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.io.IOException;

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
    private JTextField textField;
    private JButton boton;

    public PanelCliente(Container contenedor){
        contenedor.setLayout(new BorderLayout());
        textPane = new JTextPane();
        textPane.setContentType("text/html"); // Permite interpretar HTML
        textPane.setEditable(false);
        scroll = new JScrollPane(textPane);

        JPanel panel = new JPanel(new BorderLayout());
        textField = new JTextField(50);
        boton = new JButton("Enviar");
        panel.add(textField, BorderLayout.NORTH);
        panel.add(boton, BorderLayout.SOUTH);

        contenedor.add(scroll, BorderLayout.CENTER);
        contenedor.add(panel, BorderLayout.SOUTH);
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
    }

    public String getTexto(){
        String texto = textField.getText();
        textField.setText("");
        return texto;
    }

    // Método para limpiar la ventana del chat
    public void limpiarTexto() {
        textPane.setText(""); // Usar textPane en lugar de textArea
    }
}
