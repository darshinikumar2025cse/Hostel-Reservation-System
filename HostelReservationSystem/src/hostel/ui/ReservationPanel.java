package hostel.ui;

import hostel.model.Reservation;
import hostel.model.Room;
import hostel.model.Student;
import hostel.service.DataService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Hostel Reservation: student details, room selection, dates and the Reserve button. */
public class ReservationPanel extends JPanel implements Refreshable {

    private final DataService ds = DataService.get();

    private final JComboBox<String> studentBox = Theme.combo();
    private final JTextField nameF = Theme.readOnly();
    private final JTextField emailF = Theme.readOnly();
    private final JTextField phoneF = Theme.readOnly();
    private final JTextField courseF = Theme.readOnly();

    private final JComboBox<String> typeBox = Theme.combo(new String[]{"All Types", "Single", "Double", "Triple", "Dormitory"});
    private final JComboBox<String> roomBox = Theme.combo();
    private final JTextField capacityF = Theme.readOnly();
    private final JTextField priceF = Theme.readOnly();
    private final JSpinner checkIn = Theme.dateSpinner(LocalDate.now());
    private final JSpinner checkOut = Theme.dateSpinner(LocalDate.now().plusDays(1));

    private final JLabel nightsValue = Theme.label("-", Theme.H2, Theme.NAVY);
    private final JLabel totalValue = Theme.label("-", Theme.H2, Theme.SUCCESS);

    private List<Student> students = new ArrayList<>();
    private List<Room> rooms = new ArrayList<>();
    private boolean loading;

    public ReservationPanel() {
        Theme.initPage(this);
        add(Theme.pageTitle("Hostel Reservation", "Reserve an available room for a student"), BorderLayout.NORTH);

        // ---- student card
        JPanel sForm = new JPanel(new GridBagLayout());
        sForm.setOpaque(false);
        Theme.addRow(sForm, 0, "Select Student *", studentBox);
        Theme.addRow(sForm, 1, "Student Name", nameF);
        Theme.addRow(sForm, 2, "Email", emailF);
        Theme.addRow(sForm, 3, "Phone", phoneF);
        Theme.addRow(sForm, 4, "Course", courseF);

        // ---- room card
        JPanel rForm = new JPanel(new GridBagLayout());
        rForm.setOpaque(false);
        Theme.addRow(rForm, 0, "Room Type", typeBox);
        Theme.addRow(rForm, 1, "Available Room *", roomBox);
        JPanel info = new JPanel(new GridLayout(1, 2, 12, 0));
        info.setOpaque(false);
        info.add(Theme.labeled("Capacity", capacityF));
        info.add(Theme.labeled("Price / Night", priceF));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 4; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(12, 0, 0, 0);
        rForm.add(info, g);
        JPanel dates = new JPanel(new GridLayout(1, 2, 12, 0));
        dates.setOpaque(false);
        dates.add(Theme.labeled("Check-in Date *", checkIn));
        dates.add(Theme.labeled("Check-out Date *", checkOut));
        g.gridy = 5;
        rForm.add(dates, g);

        JPanel cardsRow = new JPanel(new GridLayout(1, 2, 18, 0));
        cardsRow.setOpaque(false);
        cardsRow.add(Theme.formCard("Student Details", sForm, null));
        cardsRow.add(Theme.formCard("Room & Stay Details", rForm, null));

        // ---- summary bar
        JButton reserveBtn = Theme.primary("Reserve Room");
        reserveBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JButton clearBtn = Theme.neutral("Clear");
        reserveBtn.addActionListener(e -> reserve());
        clearBtn.addActionListener(e -> resetForm());

        JPanel summary = new JPanel(new GridLayout(1, 2, 24, 0));
        summary.setOpaque(false);
        summary.add(Theme.labeled("Number of Nights", nightsValue));
        summary.add(Theme.labeled("Total Amount", totalValue));
        JPanel btns = new JPanel(new GridLayout(1, 2, 10, 0));
        btns.setOpaque(false);
        btns.add(clearBtn);
        btns.add(reserveBtn);
        btns.setPreferredSize(new Dimension(380, 44));
        Theme.Card bottom = new Theme.Card(new BorderLayout(20, 0));
        bottom.add(summary, BorderLayout.CENTER);
        bottom.add(btns, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);
        body.add(cardsRow, BorderLayout.CENTER);
        body.add(bottom, BorderLayout.SOUTH);
        add(Theme.wrap(body, BorderLayout.CENTER), BorderLayout.CENTER);

        // ---- listeners
        studentBox.addActionListener(e -> { if (!loading) showStudent(); });
        typeBox.addActionListener(e -> { if (!loading) reloadRooms(); });
        roomBox.addActionListener(e -> { if (!loading) showRoom(); });
        checkIn.addChangeListener(e -> {
            if (!Theme.getDate(checkOut).isAfter(Theme.getDate(checkIn))) {
                Theme.setDate(checkOut, Theme.getDate(checkIn).plusDays(1));
            }
            updateSummary();
        });
        checkOut.addChangeListener(e -> updateSummary());

        refresh();
    }

    // --------------------------------------------------------------- loading
    private void reloadStudents() {
        loading = true;
        students = ds.students();
        String[] items = new String[students.size() + 1];
        items[0] = "-- Select Student --";
        for (int i = 0; i < students.size(); i++) items[i + 1] = students.get(i).id() + " - " + students.get(i).name();
        studentBox.setModel(new DefaultComboBoxModel<>(items));
        loading = false;
        showStudent();
    }

    private void reloadRooms() {
        loading = true;
        String type = (String) typeBox.getSelectedItem();
        rooms = ds.availableRooms("All Types".equals(type) ? null : type);
        String[] items = new String[rooms.size() + 1];
        items[0] = "-- Select Room --";
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            items[i + 1] = r.roomNo() + "  |  " + r.type() + "  |  Rs. " + String.format("%,.0f", r.price()) + " / night";
        }
        roomBox.setModel(new DefaultComboBoxModel<>(items));
        loading = false;
        showRoom();
    }

    private void showStudent() {
        int i = studentBox.getSelectedIndex();
        if (i <= 0) {
            nameF.setText(""); emailF.setText(""); phoneF.setText(""); courseF.setText("");
            return;
        }
        Student s = students.get(i - 1);
        nameF.setText(s.name());
        emailF.setText(s.email());
        phoneF.setText(s.phone());
        courseF.setText(s.course());
    }

    private void showRoom() {
        int i = roomBox.getSelectedIndex();
        if (i <= 0) {
            capacityF.setText("");
            priceF.setText("");
        } else {
            Room r = rooms.get(i - 1);
            capacityF.setText(r.capacity() + " person(s)");
            priceF.setText(Theme.money(r.price()));
        }
        updateSummary();
    }

    private void updateSummary() {
        long nights = ChronoUnit.DAYS.between(Theme.getDate(checkIn), Theme.getDate(checkOut));
        int ri = roomBox.getSelectedIndex();
        nightsValue.setText(nights > 0 ? String.valueOf(nights) : "-");
        totalValue.setText(nights > 0 && ri > 0 ? Theme.money(nights * rooms.get(ri - 1).price()) : "-");
    }

    // --------------------------------------------------------------- actions
    private void reserve() {
        int si = studentBox.getSelectedIndex();
        int ri = roomBox.getSelectedIndex();
        LocalDate in = Theme.getDate(checkIn);
        LocalDate out = Theme.getDate(checkOut);

        if (si <= 0) { Theme.error(this, "Please select a student."); return; }
        if (ri <= 0) { Theme.error(this, "Please select an available room."); return; }
        if (in.isBefore(LocalDate.now())) { Theme.error(this, "Check-in date cannot be in the past."); return; }
        if (!out.isAfter(in)) { Theme.error(this, "Check-out date must be after the check-in date."); return; }

        Student s = students.get(si - 1);
        Room r = rooms.get(ri - 1);
        double total = ChronoUnit.DAYS.between(in, out) * r.price();
        Reservation res = new Reservation(ds.nextReservationId(), s.id(), s.name(), r.roomNo(), in, out, total, "Confirmed");
        ds.addReservation(res);

        Theme.success(this, "Reservation " + res.id() + " created for " + s.name() + "\nRoom: " + r.roomNo()
                + "\nTotal amount: " + Theme.money(total) + "\n\nYou can record the payment in Payment Management.");
        resetForm();
    }

    private void resetForm() {
        studentBox.setSelectedIndex(0);
        loading = true;
        typeBox.setSelectedIndex(0);
        loading = false;
        Theme.setDate(checkIn, LocalDate.now());
        Theme.setDate(checkOut, LocalDate.now().plusDays(1));
        reloadRooms();
    }

    @Override public void refresh() {
        reloadStudents();
        reloadRooms();
    }
}
