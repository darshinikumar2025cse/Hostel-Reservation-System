package hostel.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * Central place for colours, fonts and reusable styled components,
 * so every screen has the same professional look.
 */
public final class Theme {
    private Theme() { }

    // ------------------------------------------------------------- palette
    public static final Color NAVY       = new Color(0x0B2A5B);   // dark blue
    public static final Color NAVY_DARK  = new Color(0x071B3D);
    public static final Color BLUE       = new Color(0x1F6FD1);   // primary actions
    public static final Color LIGHT_BLUE = new Color(0x5AA9F5);   // accents
    public static final Color PALE_BLUE  = new Color(0xE8F1FD);
    public static final Color BG         = new Color(0xF3F7FC);   // page background
    public static final Color WHITE      = Color.WHITE;
    public static final Color TEXT       = new Color(0x1C2B45);
    public static final Color MUTED      = new Color(0x6A7A92);
    public static final Color BORDER     = new Color(0xD5E0EF);
    public static final Color SELECTION  = new Color(0xCFE3FB);
    public static final Color STRIPE     = new Color(0xF6F9FE);
    public static final Color SUCCESS    = new Color(0x1E9E68);
    public static final Color DANGER     = new Color(0xD64545);
    public static final Color WARNING    = new Color(0xE29A1F);

    // --------------------------------------------------------------- fonts
    public static final Font FONT       = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BOLD       = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font H2         = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font TITLE      = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font BIG        = new Font("Segoe UI", Font.BOLD, 32);

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static String money(double v) { return String.format("Rs. %,.2f", v); }

    // --------------------------------------------------------------- labels
    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static JLabel sectionTitle(String text) { return label(text, H2, NAVY); }

    public static JPanel pageTitle(String title, String subtitle) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        p.add(label(title, TITLE, NAVY));
        p.add(label(subtitle, FONT, MUTED));
        return p;
    }

    /** Standard page setup: background colour, padding and a BorderLayout. */
    public static void initPage(JPanel p) {
        p.setLayout(new BorderLayout(0, 18));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(22, 26, 22, 26));
    }

    public static JPanel labeled(String text, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(label(text, SMALL_BOLD, MUTED), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    // ---------------------------------------------------------- input fields
    public static void styleInput(JComponent c) {
        c.setFont(FONT);
        c.setForeground(TEXT);
        c.setBorder(new javax.swing.border.CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(6, 10, 6, 10)));
        c.setPreferredSize(new Dimension(200, 36));
    }

    public static JTextField field() {
        JTextField f = new JTextField();
        styleInput(f);
        return f;
    }

    public static JTextField readOnly() {
        JTextField f = field();
        f.setEditable(false);
        f.setBackground(PALE_BLUE);
        return f;
    }

    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        styleCombo(c);
        return c;
    }

    public static JComboBox<String> combo() {
        JComboBox<String> c = new JComboBox<>();
        styleCombo(c);
        return c;
    }

    private static void styleCombo(JComboBox<?> c) {
        c.setFont(FONT);
        c.setBackground(WHITE);
        c.setForeground(TEXT);
        c.setPreferredSize(new Dimension(200, 36));
    }

    public static void styleSpinner(JSpinner s) {
        s.setFont(FONT);
        s.setPreferredSize(new Dimension(150, 36));
        if (s.getEditor() instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setFont(FONT);
        }
    }

    // ------------------------------------------------------------ date helpers
    private static Date toDate(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static JSpinner dateSpinner(LocalDate initial) {
        JSpinner sp = new JSpinner(new SpinnerDateModel(toDate(initial), null, null, Calendar.DAY_OF_MONTH));
        sp.setEditor(new JSpinner.DateEditor(sp, "dd-MM-yyyy"));
        styleSpinner(sp);
        return sp;
    }

    public static LocalDate getDate(JSpinner sp) {
        return ((Date) sp.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static void setDate(JSpinner sp, LocalDate d) { sp.setValue(toDate(d)); }

    // ------------------------------------------------------------ form helpers
    /** Adds a "label above field" row to a GridBagLayout form. */
    public static void addRow(JPanel form, int row, String label, JComponent comp) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = row * 2;
        g.weightx = 1;
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(row == 0 ? 0 : 12, 0, 4, 0);
        form.add(label(label, SMALL_BOLD, MUTED), g);
        g.gridy = row * 2 + 1;
        g.insets = new Insets(0, 0, 0, 0);
        form.add(comp, g);
    }

    public static JPanel buttonGrid(int cols, JButton... buttons) {
        JPanel p = new JPanel(new GridLayout(0, cols, 8, 8));
        p.setOpaque(false);
        for (JButton b : buttons) p.add(b);
        return p;
    }

    /** White rounded card with a title, a form and an optional button area. */
    public static Card formCard(String title, JComponent form, JComponent buttons) {
        Card card = new Card(new BorderLayout());
        JPanel north = new JPanel(new BorderLayout(0, 14));
        north.setOpaque(false);
        north.add(sectionTitle(title), BorderLayout.NORTH);
        north.add(form, BorderLayout.CENTER);
        if (buttons != null) {
            JPanel b = new JPanel(new BorderLayout());
            b.setOpaque(false);
            b.setBorder(new EmptyBorder(16, 0, 0, 0));
            b.add(buttons, BorderLayout.CENTER);
            north.add(b, BorderLayout.SOUTH);
        }
        card.add(north, BorderLayout.NORTH);
        return card;
    }

    public static void onChange(JTextComponent c, Runnable r) {
        c.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { r.run(); }
            public void removeUpdate(DocumentEvent e) { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        });
    }

    // --------------------------------------------------------------- buttons
    public static JButton button(String text, Color color) { return new RoundedButton(text, color); }
    public static JButton primary(String text) { return button(text, BLUE); }
    public static JButton success(String text) { return button(text, SUCCESS); }
    public static JButton danger(String text)  { return button(text, DANGER); }
    public static JButton neutral(String text) { return button(text, MUTED); }

    public static class RoundedButton extends JButton {
        private final Color base;
        private boolean hover;

        public RoundedButton(String text, Color base) {
            super(text);
            this.base = base;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(BOLD);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 16, 9, 16));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = !isEnabled() ? new Color(0xB8C4D6)
                    : getModel().isPressed() ? base.darker().darker()
                    : hover ? base.darker() : base;
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------------------------------------------------------------- panels
    public static class Card extends JPanel {
        public Card(LayoutManager lm) {
            super(lm);
            setOpaque(false);
            setBorder(new EmptyBorder(18, 18, 18, 18));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Small KPI card used on the dashboard and payment screens. */
    public static Card statCard(String title, JLabel value, Color accent) {
        Card c = new Card(new BorderLayout(0, 6));
        value.setFont(BIG);
        value.setForeground(accent);
        c.add(label(title, BOLD, MUTED), BorderLayout.NORTH);
        c.add(value, BorderLayout.CENTER);
        return c;
    }

    /** Panel that follows the viewport width so scroll panes never clip content sideways. */
    public static class ScrollBody extends JPanel implements Scrollable {
        public ScrollBody() { super(new BorderLayout()); setOpaque(false); }
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 80; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() {
            Container p = getParent();
            return p instanceof JViewport && p.getHeight() > getPreferredSize().height;
        }
    }

    /** Wraps content in a transparent scroll pane (scrollbar only appears on small windows). */
    public static JScrollPane wrap(JComponent content, String borderPos) {
        ScrollBody body = new ScrollBody();
        body.add(content, borderPos);
        JScrollPane sp = new JScrollPane(body, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    // ---------------------------------------------------------------- tables
    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static JScrollPane tableScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(BORDER));
        sp.getViewport().setBackground(WHITE);
        return sp;
    }

    public static void styleTable(JTable t) {
        t.setFont(FONT);
        t.setForeground(TEXT);
        t.setRowHeight(34);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xE6EDF7));
        t.setSelectionBackground(SELECTION);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(tb, v, s, f, r, c);
                setOpaque(true);
                setBackground(NAVY);
                setForeground(WHITE);
                setFont(BOLD);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tb, v, sel, false, r, c);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setFont(FONT);
                if (!sel) setBackground(r % 2 == 0 ? WHITE : STRIPE);
                setForeground(TEXT);
                if ("Status".equals(tb.getColumnName(c)) && v instanceof String) {
                    Color col = statusColor((String) v);
                    if (col != null) { setForeground(col); setFont(BOLD); }
                }
                return this;
            }
        });
    }

    private static Color statusColor(String s) {
        switch (s) {
            case "Available": case "Paid": case "Confirmed": case "Completed": return SUCCESS;
            case "Pending": case "Partial": case "Maintenance": return WARNING;
            case "Cancelled": case "Failed": return DANGER;
            case "Occupied": case "Checked-In": return BLUE;
            default: return null;
        }
    }

    // --------------------------------------------------------------- dialogs
    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    public static void success(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Please Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
