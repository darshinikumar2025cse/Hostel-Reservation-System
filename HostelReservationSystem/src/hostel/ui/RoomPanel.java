package hostel.ui;

import hostel.model.Room;
import hostel.service.DataService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Room Management: add, update, delete rooms; filter available rooms; room type and price. */
public class RoomPanel extends JPanel implements Refreshable {

    private final DataService ds = DataService.get();

    private final JTextField roomNoField = Theme.field();
    private final JComboBox<String> typeBox = Theme.combo(new String[]{"Single", "Double", "Triple", "Dormitory"});
    private final JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
    private final JTextField priceField = Theme.field();
    private final JComboBox<String> statusBox = Theme.combo(new String[]{"Available", "Occupied", "Maintenance"});

    private final JComboBox<String> filterBox = Theme.combo(new String[]{"All Rooms", "Available", "Occupied", "Maintenance"});
    private final JTextField searchField = Theme.field();

    private final DefaultTableModel model = Theme.model("Room No", "Type", "Capacity", "Price / Night", "Status");
    private final JTable table = new JTable(model);
    private String selectedNo = null;

    public RoomPanel() {
        Theme.initPage(this);
        add(Theme.pageTitle("Room Management", "Add, update, delete rooms and view availability"), BorderLayout.NORTH);

        Theme.styleSpinner(capacitySpinner);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        Theme.addRow(form, 0, "Room Number *", roomNoField);
        Theme.addRow(form, 1, "Room Type", typeBox);
        Theme.addRow(form, 2, "Capacity (persons)", capacitySpinner);
        Theme.addRow(form, 3, "Price per Night (Rs.) *", priceField);
        Theme.addRow(form, 4, "Status", statusBox);

        typeBox.addActionListener(e -> { if (selectedNo == null) applyTypeDefaults(); });

        JButton addBtn = Theme.primary("Add Room");
        JButton updateBtn = Theme.success("Update");
        JButton deleteBtn = Theme.danger("Delete");
        JButton clearBtn = Theme.neutral("Clear");
        addBtn.addActionListener(e -> addRoom());
        updateBtn.addActionListener(e -> updateRoom());
        deleteBtn.addActionListener(e -> deleteRoom());
        clearBtn.addActionListener(e -> clearForm());

        JScrollPane side = Theme.wrap(
                Theme.formCard("Room Details", form, Theme.buttonGrid(2, addBtn, updateBtn, deleteBtn, clearBtn)),
                BorderLayout.NORTH);
        side.setPreferredSize(new Dimension(350, 10));

        // ---- right: filters + table
        Theme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelected();
        });
        filterBox.setPreferredSize(new Dimension(150, 36));
        filterBox.addActionListener(e -> reload());
        Theme.onChange(searchField, this::reload);
        searchField.setToolTipText("Search by room number or type");

        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.add(Theme.label("View:", Theme.BOLD, Theme.TEXT), BorderLayout.WEST);
        JPanel mid = new JPanel(new BorderLayout(10, 0));
        mid.setOpaque(false);
        mid.add(filterBox, BorderLayout.WEST);
        mid.add(searchField, BorderLayout.CENTER);
        bar.add(mid, BorderLayout.CENTER);

        Theme.Card tableCard = new Theme.Card(new BorderLayout(0, 12));
        tableCard.add(bar, BorderLayout.NORTH);
        tableCard.add(Theme.tableScroll(table), BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);
        body.add(side, BorderLayout.WEST);
        body.add(tableCard, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        clearForm();
        reload();
    }

    // ------------------------------------------------------------ actions
    private void addRoom() {
        String err = validateInput(true);
        if (err != null) { Theme.error(this, err); return; }
        ds.addRoom(buildRoom());
        reload();
        clearForm();
        Theme.success(this, "Room added successfully.");
    }

    private void updateRoom() {
        if (selectedNo == null) { Theme.error(this, "Please select a room from the table to update."); return; }
        String err = validateInput(false);
        if (err != null) { Theme.error(this, err); return; }
        ds.updateRoom(buildRoom());
        reload();
        clearForm();
        Theme.success(this, "Room updated successfully.");
    }

    private void deleteRoom() {
        if (selectedNo == null) { Theme.error(this, "Please select a room from the table to delete."); return; }
        if (!Theme.confirm(this, "Delete room " + selectedNo + "?")) return;
        if (!ds.deleteRoom(selectedNo)) {
            Theme.error(this, "Room " + selectedNo + " is currently occupied and cannot be deleted.");
            return;
        }
        reload();
        clearForm();
        Theme.success(this, "Room deleted successfully.");
    }

    private Room buildRoom() {
        return new Room(roomNoField.getText().trim().toUpperCase(), (String) typeBox.getSelectedItem(),
                (Integer) capacitySpinner.getValue(), parsePrice(), (String) statusBox.getSelectedItem());
    }

    private double parsePrice() {
        try {
            return Double.parseDouble(priceField.getText().trim().replace(",", ""));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private String validateInput(boolean isNew) {
        String no = roomNoField.getText().trim().toUpperCase();
        if (no.isEmpty()) return "Room number is required.";
        if (!no.matches("[A-Z0-9-]{1,8}")) return "Room number may contain letters, digits and '-' only (max 8 characters).";
        if (isNew && ds.roomExists(no)) return "Room " + no + " already exists.";
        if (priceField.getText().trim().isEmpty()) return "Price per night is required.";
        if (parsePrice() <= 0) return "Price must be a valid number greater than 0.";
        return null;
    }

    private void applyTypeDefaults() {
        String t = (String) typeBox.getSelectedItem();
        if (t == null) return;
        switch (t) {
            case "Single":    capacitySpinner.setValue(1); priceField.setText("1200"); break;
            case "Double":    capacitySpinner.setValue(2); priceField.setText("2000"); break;
            case "Triple":    capacitySpinner.setValue(3); priceField.setText("2700"); break;
            default:          capacitySpinner.setValue(6); priceField.setText("3600"); break;
        }
    }

    // --------------------------------------------------------------- state
    private void loadSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Room r = ds.findRoom((String) model.getValueAt(row, 0));
        if (r == null) return;
        selectedNo = r.roomNo();
        roomNoField.setText(r.roomNo());
        roomNoField.setEditable(false);          // room number is the key - not editable when updating
        typeBox.setSelectedItem(r.type());
        capacitySpinner.setValue(r.capacity());
        priceField.setText(String.valueOf((long) r.price()));
        statusBox.setSelectedItem(r.status());
    }

    private void clearForm() {
        selectedNo = null;
        table.clearSelection();
        roomNoField.setText("");
        roomNoField.setEditable(true);
        typeBox.setSelectedIndex(0);
        applyTypeDefaults();
        statusBox.setSelectedIndex(0);
    }

    private void reload() {
        model.setRowCount(0);
        for (Room r : ds.searchRooms(searchField.getText(), (String) filterBox.getSelectedItem())) {
            model.addRow(new Object[]{r.roomNo(), r.type(), r.capacity(), String.format("Rs. %,.0f", r.price()), r.status()});
        }
    }

    @Override public void refresh() { reload(); }
}
