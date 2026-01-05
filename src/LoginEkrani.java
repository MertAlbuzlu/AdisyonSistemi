import javax.swing.*;
import java.awt.*;

public class LoginEkrani {

    public static void goster() {
        JFrame loginFrame = new JFrame("Zümrüt POS - Güvenli Giriş");
        loginFrame.setSize(400, 320);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setResizable(false);

        JPanel panel = new JPanel(null);
        panel.setBackground(Tema.COL_BG_MAIN);

        JLabel lblTitle = new JLabel("SİSTEM GİRİŞİ");
        lblTitle.setForeground(Tema.COL_ACCENT);
        lblTitle.setFont(Tema.FONT_TITLE);
        lblTitle.setBounds(100, 20, 200, 40);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblUser = new JLabel("Kullanıcı ID:");
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(Tema.FONT_NORMAL);
        lblUser.setBounds(50, 80, 100, 30);
        JTextField txtUser = new JTextField();
        txtUser.setBounds(150, 80, 180, 30);

        JLabel lblPass = new JLabel("Şifre:");
        lblPass.setForeground(Color.WHITE);
        lblPass.setFont(Tema.FONT_NORMAL);
        lblPass.setBounds(50, 130, 100, 30);
        JPasswordField txtPass = new JPasswordField();
        txtPass.setBounds(150, 130, 180, 30);

        JButton btnLogin = new JButton("GİRİŞ YAP");
        btnLogin.setBounds(50, 190, 280, 45);
        btnLogin.setBackground(Tema.COL_ACCENT);
        btnLogin.setForeground(Color.WHITE);

        btnLogin.addActionListener(e -> {
            // Şifre kontrolü
            if(txtUser.getText().equals("Adisyon") && new String(txtPass.getPassword()).equals("Premium")) {
                loginFrame.dispose();
                // Ana ekranı başlat
                new AdisyonSistemi().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Hatalı ID veya Şifre!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(lblTitle); panel.add(lblUser);
        panel.add(txtUser); panel.add(lblPass); panel.add(txtPass);
        panel.add(btnLogin);
        loginFrame.add(panel);
        loginFrame.setVisible(true);
    }
}