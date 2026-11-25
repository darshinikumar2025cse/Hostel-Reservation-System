package hostel.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Main window: dark-blue sidebar + top bar + CardLayout content area. */
public class MainFrame extends JFrame {

    public static final String DASHBOARD = "dashboard";
    public static final String STUDENTS = "students";
    public static final String ROOMS = "rooms";
    public static final String RESERVE = "reserve";
    public static final String RESERVATIONS = "reservations";
    public static final String PAYMENTS = "payments";

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, JPanel> screens = new LinkedHashMap<>();
    private final Map<String, String> titles = new HashMap<>();
    private final Map<String, NavButton> navButtons = new HashMap<>();
    private final JLabel pageLabel = Theme.label("", new Font("Segoe UI", Font.BOLD, 20), Theme.NAVY);

    public MainFrame(String username) {
        super("Hostel Reservation System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1150, 720));
        setSize(1300, 780);
        setLocationRelativeTo(null);

        screens.put(DASHBOARD, new DashboardPanel(this::showScreen));
        screens.put(STUDENTS, new StudentPanel());
        screens.put(ROOMS, new RoomPanel());
        screens.put(RESERVE, new ReservationPanel());
        screens.put(RESERVATIONS, new ReservationManagementPanel());
        screens.put(PAYMENTS, new PaymentPanel());
        screens.forEach((key, panel) -> content.add(panel, key));

        titles.put(DASHBOARD, "Dashboard");
        titles.put(STUDENTS, "Student Management");
        titles.put(ROOMS, "Room Management");
        titles.put(RESERVE, "Hostel Reservation");
        titles.put(RESERVATIONS, "Reservation Management");
        titles.put(PAYMENTS, "Payment Management");

        JPanel right = new JPanel(new BorderLayout());
        right.add(buildTopBar(username), BorderLayout.NORTH);
        content.setBackground(Theme.BG);
        right.add(content, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(right, BorderLayout.CENTER);

        showScreen(DASHBOARD);
    }

    /** Switch screen (also refreshes it so it always shows the latest data). */
    public void showScreen(String key) {
        JPanel p = screens.get(key);
        if (p instanceof Refreshable) ((Refreshable) p).refresh();
        cards.show(content, key);
        pageLabel.setText(titles.get(key));
        navButtons.get(key).setSelected(true);
    }

    // --------------------------------------------------------------- sidebar
    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Theme.NAVY);
        side.setPreferredSize(new Dimension(240, 10));

        JPanel brand = new JPanel(new GridLayout(2, 1));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(26, 24, 22, 16));
        brand.add(Theme.label("HOSTEL", new Font("Segoe UI", Font.BOLD, 26), Color.WHITE));
        brand.add(Theme.label("Reservation System", Theme.FONT, Theme.LIGHT_BLUE));
        side.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(6, 14, 6, 14));
        ButtonGroup group = new ButtonGroup();
        String[][] items = {
                {DASHBOARD, "Dashboard"}, {STUDENTS, "Student Management"}, {ROOMS, "Room Management"},
                {RESERVE, "New Reservation"}, {RESERVATIONS, "Reservations"}, {PAYMENTS, "Payments"}
        };
        for (String[] it : items) {
            NavButton b = new NavButton(it[1]);
            b.addActionListener(e -> showScreen(it[0]));
            group.add(b);
            nav.add(b);
            nav.add(Box.createVerticalStrut(6));
            navButtons.put(it[0], b);
        }
        side.add(nav, BorderLayout.CENTER);

        JButton logout = Theme.button("Logout", new Color(0x1B4A8C));
        logout.addActionListener(e -> logout());
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 14, 22, 14));
        bottom.add(logout, BorderLayout.CENTER);
        side.add(bottom, BorderLayout.SOUTH);
        return side;
    }

    private JPanel buildTopBar(String username) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER), new EmptyBorder(12, 26, 12, 26)));
        bar.add(pageLabel, BorderLayout.WEST);

        JPanel user = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        user.setOpaque(false);
        user.add(Theme.label("Welcome, " + username, Theme.BOLD, Theme.TEXT));
        user.add(avatar(username));
        bar.add(user, BorderLayout.EAST);
        return bar;
    }

    private void logout() {
        if (Theme.confirm(this, "Are you sure you want to log out?")) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private static JComponent avatar(String name) {
        final String initial = name.isEmpty() ? "A" : name.substring(0, 1).toUpperCase();
        JComponent c = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BLUE);
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(Color.WHITE);
                g2.setFont(Theme.BOLD);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (36 - fm.stringWidth(initial)) / 2, (36 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        c.setPreferredSize(new Dimension(36, 36));
        return c;
    }

    /** Sidebar toggle button with hover + selected highlight. */
    private static class NavButton extends JToggleButton {
        private boolean hover;

        NavButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.PLAIN, 15));
            setHorizontalAlignment(SwingConstants.LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(12, 18, 12, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected()) {
                g2.setColor(Theme.BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Theme.LIGHT_BLUE);
                g2.fillRoundRect(0, 8, 4, getHeight() - 16, 4, 4);
            } else if (hover) {
                g2.setColor(new Color(255, 255, 255, 28));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
            g2.dispose();
            setForeground(isSelected() ? Color.WHITE : new Color(0xC9DDF7));
            super.paintComponent(g);
        }
    }
}
