package hostel;

import hostel.ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Application entry point - run this file. */
public class Main {
    public static void main(String[] args) {
        System.setProperty("swing.boldMetal", "false");
        try {
            // Cross-platform look & feel keeps custom colours identical on Windows / macOS / Linux.
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
