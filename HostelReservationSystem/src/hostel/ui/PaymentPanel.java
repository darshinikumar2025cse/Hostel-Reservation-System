package hostel.ui;

import hostel.model.Payment;
import hostel.model.Reservation;
import hostel.service.DataService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Payment Management: payment details, amount and payment status. */
public class PaymentPanel extends JPanel implements Refreshable {

    private final DataService ds = DataService.get();

    private final JComboBox<String> reservationBox = Theme.combo();
    private final JTextField studentF = Theme.readOnly();
    private final JTextField totalF = Theme.readOnly();
    private final JTextField amountF = Theme.field();
    private final JComboBox<String> methodBox = Theme.combo(new String[]{"Cash", "UPI", "Card", "Net Banking"});
    private final JSpinner dateSpinner = Theme.dateSpinner(LocalDate.now());
    private final JComboBox<String> statusBox = Theme.combo(new String[]{"Paid", "Pending", "Partial"});

    private final JTextField searchField = Theme.field();
    private final JComboBox<String> filterBox = Theme.combo(new String[]{"All Status", "Paid", "Pending", "Partial"});

    private final JLabel collectedLabel = new JLabel("-");
    private final JLabel pendingLabel = new JLabel("-");
    private final JLabel countLabel = new JLabel("-");

    private final DefaultTableModel model = Theme.model(
            "Payment ID", "Reservation", "Student", "Amount", "Method", "Date", "Status");
    private final JTable table = new JTable(model);

    private List<Reservation> reservations = new ArrayList<>();
    private String selectedId = null;
    private boolean loading;

    public PaymentPanel() {
        Theme.initPage(this);

        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);
        stats.add(Theme.statCard("Total Collected", collectedLabel, Theme.SUCCESS));
        stats.add(Theme.statCard("Pending Amount", pendingLabel, Theme.WARNING));
        stats.add(Theme.statCard("Payment Records", countLabel, Theme.BLUE));
        for (Component c : stats.getComponents()) ((JComponent) c).setPreferredSize(new Dimension(100, 105));
        collectedLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        pendingLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(Theme.pageTitle("Payment Management", "Record payments and track payment status"), BorderLayout.NORTH);
        top.add(stats, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // ---- form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        Theme.addRow(form, 0, "Reservation *", reservationBox);
        Theme.addRow(form, 1, "Student", studentF);
        Theme.addRow(form, 2, "Reservation Total", totalF);
        Theme.addRow(form, 3, "Amount Paid (Rs.) *", amountF);
        Theme.addRow(form, 4, "Payment Method", methodBox);
        Theme.addRow(form, 5, "Payment Date", dateSpinner);
        Theme.addRow(form, 6, "Payment Status", statusBox);

        JButton recordBtn = Theme.primary("Record Payment");
        JButton updateBtn = Theme.success("Update Payment");
        JButton paidBtn = Theme.button("Mark as Paid", Theme.NAVY);
        JButton clearBtn = Theme.neutral("Clear");
        recordBtn.addActionListener(e -> recordPayment());
        updateBtn.addActionListener(e -> updatePayment());
        paidBtn.addActionListener(e -> markPaid());
        clearBtn.addActionListener(e -> clearForm());

        JScrollPane side = Theme.wrap(
                Theme.formCard("Payment Details", form, Theme.buttonGrid(2, recordBtn, updateBtn, paidBtn, clearBtn)),
                BorderLayout.NORTH);
        side.setPreferredSize(new Dimension(350, 10));

        // ---- table
        Theme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelected();
        });
        filterBox.setPreferredSize(new Dimension(150, 36));
        filterBox.addActionListener(e -> reload());
        Theme.onChange(searchField, this::reload);
        searchField.setToolTipText("Search by payment ID, reservation ID or student");

        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.add(Theme.label("Search Payment:", Theme.BOLD, Theme.TEXT), BorderLayout.WEST);
        bar.add(searchField, BorderLayout.CENTER);
        bar.add(filterBox, BorderLayout.EAST);

        Theme.Card tableCard = new Theme.Card(new BorderLayout(0, 12));
        tableCard.add(bar, BorderLayout.NORTH);
        tableCard.add(Theme.tableScroll(table), BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);
        body.add(side, BorderLayout.WEST);
        body.add(tableCard, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        reservationBox.addActionListener(e -> { if (!loading) onReservationChosen(); });
        refresh();
    }

    // --------------------------------------------------------------- actions
    private void recordPayment() {
        Reservation r = validateInput();
        if (r == null) return;
        Payment p = new Payment(ds.nextPaymentId(), r.id(), r.studentName(), parseAmount(),
                (String) methodBox.getSelectedItem(), Theme.getDate(dateSpinner), (String) statusBox.getSelectedItem());
        ds.addPayment(p);
        reload();
        clearForm();
        Theme.success(this, "Payment " + p.id() + " recorded successfully.");
    }

    private void updatePayment() {
        if (selectedId == null) { Theme.error(this, "Please select a payment from the table to update."); return; }
        Reservation r = validateInput();
        if (r == null) return;
        ds.updatePayment(new Payment(selectedId, r.id(), r.studentName(), parseAmount(),
                (String) methodBox.getSelectedItem(), Theme.getDate(dateSpinner), (String) statusBox.getSelectedItem()));
        reload();
        clearForm();
        Theme.success(this, "Payment " + selectedId + " updated successfully.");
    }

    private void markPaid() {
        if (selectedId == null) { Theme.error(this, "Please select a payment from the table first."); return; }
        int ri = reservationBox.getSelectedIndex();
        if (ri <= 0) return;
        Reservation r = reservations.get(ri - 1);
        if (!Theme.confirm(this, "Mark payment " + selectedId + " as fully paid (" + Theme.money(r.total()) + ")?")) return;
        ds.updatePayment(new Payment(selectedId, r.id(), r.studentName(), r.total(),
                (String) methodBox.getSelectedItem(), LocalDate.now(), "Paid"));
        reload();
        clearForm();
        Theme.success(this, "Payment marked as Paid.");
    }

    private double parseAmount() {
        try {
            return Double.parseDouble(amountF.getText().trim().replace(",", ""));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /** @return the chosen reservation if the form is valid, otherwise null (after showing a message). */
    private Reservation validateInput() {
        int ri = reservationBox.getSelectedIndex();
        if (ri <= 0) { Theme.error(this, "Please select a reservation."); return null; }
        if (amountF.getText().trim().isEmpty()) { Theme.error(this, "Please enter the payment amount."); return null; }
        double amt = parseAmount();
        if (amt <= 0) { Theme.error(this, "Amount must be a valid number greater than 0."); return null; }
        Reservation r = reservations.get(ri - 1);
        if (amt > r.total() + 0.001) {
            Theme.error(this, "Amount cannot exceed the reservation total (" + Theme.money(r.total()) + ").");
            return null;
        }
        return r;
    }

    // ----------------------------------------------------------------- state
    private void onReservationChosen() {
        int ri = reservationBox.getSelectedIndex();
        if (ri <= 0) {
            studentF.setText("");
            totalF.setText("");
            return;
        }
        Reservation r = reservations.get(ri - 1);
        studentF.setText(r.studentName());
        totalF.setText(Theme.money(r.total()));
        if (selectedId == null) amountF.setText(String.format("%.2f", r.total()));   // suggest full amount
    }

    private void loadReservations() {
        loading = true;
        reservations = ds.reservations();
        String[] items = new String[reservations.size() + 1];
        items[0] = "-- Select Reservation --";
        for (int i = 0; i < reservations.size(); i++) {
            Reservation r = reservations.get(i);
            items[i + 1] = r.id() + " - " + r.studentName() + (r.status().equals("Cancelled") ? " [Cancelled]" : "");
        }
        reservationBox.setModel(new DefaultComboBoxModel<>(items));
        loading = false;
    }

    private void loadSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String payId = (String) model.getValueAt(row, 0);
        for (Payment p : ds.payments()) {
            if (!p.id().equals(payId)) continue;
            selectedId = p.id();
            loading = true;
            for (int i = 0; i < reservations.size(); i++) {
                if (reservations.get(i).id().equals(p.reservationId())) reservationBox.setSelectedIndex(i + 1);
            }
            loading = false;
            onReservationChosen();
            amountF.setText(String.format("%.2f", p.amount()));
            methodBox.setSelectedItem(p.method());
            Theme.setDate(dateSpinner, p.date());
            statusBox.setSelectedItem(p.status());
            return;
        }
    }

    private void clearForm() {
        selectedId = null;
        table.clearSelection();
        loading = true;
        reservationBox.setSelectedIndex(0);
        loading = false;
        studentF.setText("");
        totalF.setText("");
        amountF.setText("");
        methodBox.setSelectedIndex(0);
        Theme.setDate(dateSpinner, LocalDate.now());
        statusBox.setSelectedIndex(0);
    }

    private void reload() {
        model.setRowCount(0);
        for (Payment p : ds.searchPayments(searchField.getText(), (String) filterBox.getSelectedItem())) {
            model.addRow(new Object[]{p.id(), p.reservationId(), p.studentName(), Theme.money(p.amount()),
                    p.method(), p.date().format(Theme.DATE_FMT), p.status()});
        }
        double collected = 0, pending = 0;
        for (Payment p : ds.payments()) {
            if ("Pending".equals(p.status())) pending += p.amount();
            else collected += p.amount();
        }
        collectedLabel.setText(Theme.money(collected));
        pendingLabel.setText(Theme.money(pending));
        countLabel.setText(String.valueOf(ds.payments().size()));
    }

    @Override public void refresh() {
        loadReservations();
        reload();
        if (selectedId == null) clearForm();
    }
}
