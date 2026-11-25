package hostel.ui;

import hostel.service.DataService;

import javax.swing.*;
import java.awt.*;

/** Admin login screen. Demo credentials: admin / admin123 */
public class LoginFrame extends JFrame {

    private final JTextField userField = Theme.field();
    private final JPasswordField passField = new JPasswordField();
    private final JCheckBox showPass = new JCheckBox("Show password");
    private final JLabel errorLabel = Theme.label(" ", Theme.SMALL_BOLD, Theme.DANGER);
    private final JButton loginBtn = Theme.primary("Login");

    public LoginFrame() {
        super("Hostel Reservation System - Admin Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 560));
        setSize(1000, 620);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildBrandPanel());
        root.add(buildFormPanel());
        setContentPane(root);
        getRootPane().setDefaultButton(loginBtn);   // Enter key = Login
    }

    private JPanel buildBrandPanel() {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Theme.NAVY_DARK, getWidth(), getHeight(), Theme.BLUE));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillOval(-90, getHeight() - 230, 380, 380);
                g2.fillOval(getWidth() - 170, -110, 320, 320);
                g2.dispose();
            }
        };
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(left(Theme.label("HOSTEL", new Font("Segoe UI", Font.BOLD, 44), Color.WHITE)));
        text.add(left(Theme.label("Reservation System", new Font("Segoe UI", Font.PLAIN, 24), new Color(0xA9D2FF))));
        text.add(Box.createVerticalStrut(28));
        text.add(left(Theme.label("Manage students, rooms, bookings", Theme.FONT, new Color(0xDCEBFF))));
        text.add(left(Theme.label("and payments from one place.", Theme.FONT, new Color(0xDCEBFF))));
        text.add(Box.createVerticalStrut(22));
        String[] features = {"Student & room management", "Quick room reservations", "Payment tracking"};
        for (String f : features) {
            text.add(left(Theme.label("\u2022  " + f, Theme.FONT, Color.WHITE)));
            text.add(Box.createVerticalStrut(6));
        }
        p.add(text);
        return p;
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Theme.WHITE);

        JPanel f = new JPanel();
        f.setOpaque(false);
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));
        f.setPreferredSize(new Dimension(340, 450));

        f.add(left(Theme.label("Admin Login", Theme.TITLE, Theme.NAVY)));
        f.add(left(Theme.label("Sign in to manage the hostel", Theme.FONT, Theme.MUTED)));
        f.add(Box.createVerticalStrut(28));

        f.add(left(Theme.label("Username", Theme.SMALL_BOLD, Theme.MUTED)));
        f.add(Box.createVerticalStrut(4));
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.add(left(userField));
        f.add(Box.createVerticalStrut(14));

        f.add(left(Theme.label("Password", Theme.SMALL_BOLD, Theme.MUTED)));
        f.add(Box.createVerticalStrut(4));
        Theme.styleInput(passField);
        passField.setEchoChar('\u2022');
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.add(left(passField));
        f.add(Box.createVerticalStrut(8));

        showPass.setOpaque(false);
        showPass.setFont(Theme.FONT);
        showPass.setForeground(Theme.MUTED);
        showPass.addActionListener(e -> passField.setEchoChar(showPass.isSelected() ? (char) 0 : '\u2022'));
        f.add(left(showPass));
        f.add(Box.createVerticalStrut(8));
        f.add(left(errorLabel));
        f.add(Box.createVerticalStrut(8));

        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.addActionListener(e -> doLogin());
        f.add(left(loginBtn));
        f.add(Box.createVerticalStrut(18));
        f.add(left(Theme.label("Demo login:  admin / admin123", Theme.FONT, Theme.MUTED)));

        outer.add(f);
        return outer;
    }

    private void doLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }
        if (!DataService.get().authenticate(user, pass)) {
            errorLabel.setText("Invalid username or password.");
            passField.setText("");
            passField.requestFocusInWindow();
            return;
        }
        dispose();
        new MainFrame(user).setVisible(true);
    }

    private static <T extends JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }
}
