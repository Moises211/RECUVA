package com.gt11.RECUVA.Login;

import javax.swing.*;
import java.awt.*;

public class LoginUI extends JFrame {

    private JTextField usuarioField;
    private JPasswordField contrasenaField;
    private JButton iniciarSesionBtn;
    private JLabel recuperarContrasenaLabel;

    public LoginUI() {
        initComponents();
    }

    private void initComponents() {
        // Título de la ventana
        setTitle("Login - Recuva");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null); // Centra la ventana

        // Panel principal
        JPanel panel = new JPanel();
        panel.setBackground(new Color(250, 245, 235)); // Color similar al fondo de la imagen
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Logo (opcional)
        JLabel logo = new JLabel("🔒 RECUVA");
        logo.setFont(new Font("Arial", Font.BOLD, 24));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Texto de bienvenida
        JLabel bienvenida = new JLabel("Bienvenid@");
        bienvenida.setFont(new Font("Arial", Font.BOLD, 20));
        bienvenida.setAlignmentX(Component.CENTER_ALIGNMENT);
        bienvenida.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Campo Usuario
        usuarioField = new JTextField();
        usuarioField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usuarioField.setFont(new Font("Arial", Font.PLAIN, 14));
        usuarioField.setBorder(BorderFactory.createTitledBorder("Usuario"));

        // Campo Contraseña
        contrasenaField = new JPasswordField();
        contrasenaField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        contrasenaField.setFont(new Font("Arial", Font.PLAIN, 14));
        contrasenaField.setBorder(BorderFactory.createTitledBorder("Contraseña"));

        // Botón de inicio
        iniciarSesionBtn = new JButton("Iniciar Sesión");
        iniciarSesionBtn.setBackground(new Color(0, 153, 255));
        iniciarSesionBtn.setForeground(Color.white);
        iniciarSesionBtn.setFont(new Font("Arial", Font.BOLD, 16));
        iniciarSesionBtn.setFocusPainted(false);
        iniciarSesionBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        iniciarSesionBtn.addActionListener(e -> login());

        // Etiqueta "¿Olvidaste tu contraseña?"
        recuperarContrasenaLabel = new JLabel("¿Olvidaste tu contraseña?");
        recuperarContrasenaLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        recuperarContrasenaLabel.setForeground(Color.GRAY);
        recuperarContrasenaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Agregar elementos al panel
        panel.add(Box.createVerticalStrut(20));
        panel.add(logo);
        panel.add(bienvenida);
        panel.add(usuarioField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(contrasenaField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(iniciarSesionBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(recuperarContrasenaLabel);

        // Agregar panel al frame
        add(panel);
    }

    private void login() {
        String usuario = usuarioField.getText();
        String contrasena = new String(contrasenaField.getPassword());

        if (usuario.equals("admin") && contrasena.equals("1234")) {
            JOptionPane.showMessageDialog(this, "¡Inicio de sesión exitoso!");
            // Aquí puedes redirigir a otra pantalla
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginUI().setVisible(true);
        });
    }
}
