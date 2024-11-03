package utalca.chatpyme;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PanelCliente extends JPanel {
    private JScrollPane scroll;
    private JTextArea textArea;
    private JTextField textField;
    private JButton boton;

    public PanelCliente(Container contenedor){
        contenedor.setLayout(new BorderLayout());
        textArea = new JTextArea();
        scroll = new JScrollPane(textArea);

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

    public void addTexto(String texto){
        textArea.append(texto);
    }

    public String getTexto(){
        String texto = textField.getText();
        textField.setText("");
        return texto;
    }

    // Método para limpiar la ventana del chat
    public void limpiarTexto() {
        textArea.setText("");
    }
}
