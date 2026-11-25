package hostel.ui;

import hostel.model.Reservation;
import hostel.service.DataService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/** Dashboard: KPI cards, recent reservations, occupancy and quick actions. */
public class DashboardPanel extends JPanel implements Refreshable {

    private final DataService ds = DataService.get();
    private final JLabel totalRooms = new JLabel("0");
    private final JLabel availRooms = new JLabel("0");
    private final JLabel totalStudents = new JLabel("0");
    private final JLabel totalReservations = new JLabel("0");
    private final DefaultTableModel model = Theme.model("ID", "Student", "Room", "Check-in", "Check-out", "Status");
    private final JProgressBar occupancy = new JProgressBar(0, 100);
    private final JLabel occupancyText = Theme.label(" ", Theme.FONT, Theme.MUTED);

    public DashboardPanel(Consumer<String> navigator) {
        Theme.initPage(this);
        add(Theme.pageTitle("Dashboard", "Overview of hostel occupancy and recent activity"), BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(Theme.statCard("Total Rooms", totalRooms, Theme.NAVY));
        stats.add(Theme.statCard("Available Rooms", availRooms, Theme.SUCCESS));
        stats.add(Theme.statCard("Total Students", totalStudents, Theme.BLUE));
        stats.add(Theme.statCard("Total Reservations", totalReservations, Theme.WARNING));

        JTable table = new JTable(model);
        Theme.styleTable(table);
        Theme.Card recent = new Theme.Card(new BorderLayout(0, 12));
        recent.add(Theme.sectionTitle("Recent Reservations"), BorderLayout.NORTH);
        recent.add(Theme.tableScroll(table), BorderLayout.CENTER);

        // right column: occupancy + quick actions
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.add(left(Theme.sectionTitle("Room Occupancy")));
        box.add(Box.createVerticalStrut(12));
        occupancy.setForeground(Theme.BLUE);
        occupancy.setBackground(Theme.PALE_BLUE);
        occupancy.setBorderPainted(false);
        occupancy.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        box.add(left(occupancy));
        box.add(Box.createVerticalStrut(8));
        box.add(left(occupancyText));
        box.add(Box.createVerticalStrut(28));
        box.add(left(Theme.sectionTitle("Quick Actions")));
        box.add(Box.createVerticalStrut(12));
        box.add(quick("New Reservation", MainFrame.RESERVE, navigator));
        box.add(Box.createVerticalStrut(10));
        box.add(quick("Add Student", MainFrame.STUDENTS, navigator));
        box.add(Box.createVerticalStrut(10));
        box.add(quick("Manage Rooms", MainFrame.ROOMS, navigator));
        box.add(Box.createVerticalStrut(10));
        box.add(quick("Record Payment", MainFrame.PAYMENTS, navigator));

        Theme.Card side = new Theme.Card(new BorderLayout());
        side.add(box, BorderLayout.NORTH);
        side.setPreferredSize(new Dimension(290, 10));

        JPanel center = new JPanel(new BorderLayout(16, 16));
        center.setOpaque(false);
        center.add(stats, BorderLayout.NORTH);
        center.add(recent, BorderLayout.CENTER);
        center.add(side, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);
        refresh();
    }

    private JButton quick(String text, String target, Consumer<String> nav) {
        JButton b = Theme.primary(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        b.addActionListener(e -> nav.accept(target));
        return b;
    }

    private static <T extends JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    @Override public void refresh() {
        totalRooms.setText(String.valueOf(ds.totalRooms()));
        availRooms.setText(String.valueOf(ds.availableRoomCount()));
        totalStudents.setText(String.valueOf(ds.totalStudents()));
        totalReservations.setText(String.valueOf(ds.totalReservations()));

        int total = ds.totalRooms(), occ = ds.occupiedRoomCount();
        int pct = total == 0 ? 0 : occ * 100 / total;
        occupancy.setValue(pct);
        occupancyText.setText(occ + " of " + total + " rooms occupied (" + pct + "%)");

        model.setRowCount(0);
        List<Reservation> list = ds.reservations();
        Collections.reverse(list);
        for (int i = 0; i < Math.min(8, list.size()); i++) {
            Reservation r = list.get(i);
            model.addRow(new Object[]{r.id(), r.studentName(), r.roomNo(),
                    r.checkIn().format(Theme.DATE_FMT), r.checkOut().format(Theme.DATE_FMT), r.status()});
        }
    }
}
