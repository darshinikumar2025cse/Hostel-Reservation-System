package hostel.ui;

import hostel.model.Reservation;
import hostel.model.Room;
import hostel.service.DataService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Reservation Management: view, search, update and cancel reservations. */
public class ReservationManagementPanel extends JPanel implements Refreshable {

    private final DataService ds = DataService.get();

    private final JTextField searchField = Theme.field();
    private final JComboBox<String> statusFilter =
            Theme.combo(new String[]{"All Status", "Confirmed", "Checked-In", "Completed", "Cancelled"});

    private final DefaultTableModel model = Theme.model(
            "ID", "Student ID", "Student", "Room", "Check-in", "Check-out", "Total", "Status");
    private final JTable table = new JTable(model);

    private final JLabel selectedLabel = Theme.label("Select a reservation from the table to update or cancel it.", Theme.BOLD, Theme.MUTED);
    private final JSpinner checkIn = Theme.dateSpinner(LocalDate.now());
    private final JSpinner checkOut = Theme.dateSpinner(LocalDate.now().plusDays(1));
    private final JComboBox<String> statusBox =
            Theme.combo(new String[]{"Confirmed", "Checked-In", "Completed", "Cancelled"});
    private String selectedId = null;

    public ReservationManagementPanel() {
        Theme.initPage(this);
        add(Theme.pageTitle("Reservation Management", "View, search, update and cancel reservations"), BorderLayout.NORTH);

        // ---- table card with toolbar
        Theme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelected();
        });
        statusFilter.setPreferredSize(new Dimension(150, 36));
        statusFilter.addActionListener(e -> reload());
        Theme.onChange(searchField, this::reload);
        searchField.setToolTipText("Search by reservation ID, student, or room");

        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.add(Theme.label("Search Reservation:", Theme.BOLD, Theme.TEXT), BorderLayout.WEST);
        bar.add(searchField, BorderLayout.CENTER);
        bar.add(statusFilter, BorderLayout.EAST);

        Theme.Card tableCard = new Theme.Card(new BorderLayout(0, 12));
        tableCard.add(bar, BorderLayout.NORTH);
        tableCard.add(Theme.tableScroll(table), BorderLayout.CENTER);

        // ---- edit card
        JButton updateBtn = Theme.success("Update Reservation");
        JButton cancelBtn = Theme.danger("Cancel Reservation");
        JButton clearBtn = Theme.neutral("Clear");
        updateBtn.addActionListener(e -> updateReservation());
        cancelBtn.addActionListener(e -> cancelReservation());
        clearBtn.addActionListener(e -> clearForm());

        JPanel row = new JPanel(new GridLayout(1, 0, 14, 0));
        row.setOpaque(false);
        row.add(Theme.labeled("Check-in Date", checkIn));
        row.add(Theme.labeled("Check-out Date", checkOut));
        row.add(Theme.labeled("Status", statusBox));
        row.add(Theme.labeled(" ", updateBtn));
        row.add(Theme.labeled(" ", cancelBtn));
        row.add(Theme.labeled(" ", clearBtn));

        Theme.Card editCard = new Theme.Card(new BorderLayout(0, 12));
        editCard.add(selectedLabel, BorderLayout.NORTH);
        editCard.add(row, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(tableCard, BorderLayout.CENTER);
        body.add(editCard, BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);

        reload();
    }

    // --------------------------------------------------------------- actions
    private void updateReservation() {
        if (selectedId == null) { Theme.error(this, "Please select a reservation from the table first."); return; }
        Reservation old = ds.findReservation(selectedId);
        if (old == null) return;
        if ("Cancelled".equals(old.status())) { Theme.error(this, "A cancelled reservation cannot be updated."); return; }

        LocalDate in = Theme.getDate(checkIn);
        LocalDate out = Theme.getDate(checkOut);
        if (!out.isAfter(in)) { Theme.error(this, "Check-out date must be after the check-in date."); return; }

        Room room = ds.findRoom(old.roomNo());
        double total = room == null ? old.total() : ChronoUnit.DAYS.between(in, out) * room.price();
        ds.updateReservation(new Reservation(old.id(), old.studentId(), old.studentName(), old.roomNo(),
                in, out, total, (String) statusBox.getSelectedItem()));
        reload();
        clearForm();
        Theme.success(this, "Reservation " + old.id() + " updated successfully.\nNew total: " + Theme.money(total));
    }

    private void cancelReservation() {
        if (selectedId == null) { Theme.error(this, "Please select a reservation from the table first."); return; }
        Reservation r = ds.findReservation(selectedId);
        if (r == null) return;
        if (!"Confirmed".equals(r.status()) && !"Checked-In".equals(r.status())) {
            Theme.error(this, "Only Confirmed or Checked-In reservations can be cancelled.");
            return;
        }
        if (!Theme.confirm(this, "Cancel reservation " + r.id() + " for " + r.studentName() + "?")) return;
        ds.cancelReservation(r.id());
        reload();
        clearForm();
        Theme.success(this, "Reservation " + r.id() + " has been cancelled and room " + r.roomNo() + " is available again.");
    }

    // ----------------------------------------------------------------- state
    private void loadSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Reservation r = ds.findReservation((String) model.getValueAt(row, 0));
        if (r == null) return;
        selectedId = r.id();
        selectedLabel.setText("Selected: " + r.id() + "  -  " + r.studentName() + "  (Room " + r.roomNo() + ")");
        selectedLabel.setForeground(Theme.NAVY);
        Theme.setDate(checkIn, r.checkIn());
        Theme.setDate(checkOut, r.checkOut());
        statusBox.setSelectedItem(r.status());
    }

    private void clearForm() {
        selectedId = null;
        table.clearSelection();
        selectedLabel.setText("Select a reservation from the table to update or cancel it.");
        selectedLabel.setForeground(Theme.MUTED);
        Theme.setDate(checkIn, LocalDate.now());
        Theme.setDate(checkOut, LocalDate.now().plusDays(1));
        statusBox.setSelectedIndex(0);
    }

    private void reload() {
        model.setRowCount(0);
        for (Reservation r : ds.searchReservations(searchField.getText(), (String) statusFilter.getSelectedItem())) {
            model.addRow(new Object[]{r.id(), r.studentId(), r.studentName(), r.roomNo(),
                    r.checkIn().format(Theme.DATE_FMT), r.checkOut().format(Theme.DATE_FMT),
                    Theme.money(r.total()), r.status()});
        }
    }

    @Override public void refresh() { reload(); }
}
