import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    // Fields remain unchanged
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField inputField;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private String playerName = null;
    private int score = 0;

    public Client(String serverAddress) {
        try {
            socket = new Socket(serverAddress, 12345);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            SwingUtilities.invokeLater(() -> initUI());
            new Thread(new ServerListener()).start();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to connect to the server.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initUI() {
        frame = new JFrame("Word Hunt Battle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(18, 18, 18));

        // Use system default font with emoji fallback
        Font titleFont = new Font("Segoe UI", Font.BOLD, 24);
        Font mainFont = new Font("Segoe UI", Font.PLAIN, 16);
        Color accentColor = new Color(0, 150, 200);
        Color darkBg = new Color(30, 30, 30);
        Color lightText = new Color(240, 240, 240);

        // Header Panel with text-based symbols
        JPanel headerPanel = new JPanel(new GridLayout(1, 2));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        headerPanel.setBackground(darkBg);

        // Using standard Unicode characters with HTML formatting for proper rendering
        timerLabel = new JLabel("<html>⌛ Time Left: 2:00</html>", SwingConstants.CENTER);
        styleLabel(timerLabel, titleFont, accentColor);

        scoreLabel = new JLabel("<html>★ Score:</html>"+ score, SwingConstants.CENTER);
        styleLabel(scoreLabel, titleFont, accentColor);

        headerPanel.add(timerLabel);
        headerPanel.add(scoreLabel);

        // Main Message Area
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(mainFont);
        messageArea.setForeground(lightText);
        messageArea.setBackground(darkBg);
        messageArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));

        // Input Panel (modified)
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        inputPanel.setBackground(darkBg);

        inputField = new JTextField();
        inputField.setFont(mainFont);
        inputField.setForeground(lightText);
        inputField.setBackground(new Color(50, 50, 50));
        inputField.setCaretColor(accentColor);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        inputField.addActionListener(e -> processInput());

        inputPanel.add(inputField, BorderLayout.CENTER);

        // Initial Message with ASCII art
        appendWelcomeMessage();

        // Assemble frame
        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        inputField.requestFocusInWindow();
    }

    private void styleLabel(JLabel label, Font font, Color color) {
        label.setFont(font);
        label.setForeground(color);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void appendWelcomeMessage() {
        messageArea.append("=== Word Hunt Battle ===\n\n");
        messageArea.append("Please enter your name to start playing:\n");
    }

    private void processInput() {
        String text = inputField.getText().trim();
        if (!text.isEmpty()) {
            if (playerName == null) {
                playerName = text;
                output.println("PLAYER_NAME:" + playerName);
                messageArea.append("\nWelcome, " + playerName + "!\n");
                messageArea.append("You have 2 minutes to guess words!\n");
                messageArea.append("Enter words using the input below:\n\n");
            } else {
                output.println("WORD:" + text);
            }
            inputField.setText("");
        }
    }

    private class ServerListener implements Runnable {
        public void run() {
            try {
                String message;
                while ((message = input.readLine()) != null) {
                    final String finalMessage = message;
                    SwingUtilities.invokeLater(() -> {
                        if (finalMessage.matches("\\d+:\\d{2}")) {
                            timerLabel.setText("<html>⌛ Time Left: " + finalMessage + "</html>");
                        } else if (finalMessage.contains("Total score:")) {
                            String[] parts = finalMessage.split("Total score: ");
                            if (parts.length > 1) {
                                score = Integer.parseInt(parts[1].trim());
                                scoreLabel.setText("<html>★ Score: " + score + "</html>");
                            }
                            messageArea.append("\n" + finalMessage + "\n");
                        } else {
                            messageArea.append(finalMessage + "\n");
                        }
                        // Auto-scroll to bottom
                        messageArea.setCaretPosition(messageArea.getDocument().getLength());
                    });
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    messageArea.append("\n🔌 Connection to server lost\n");
                    inputField.setEnabled(false);
                });
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client("127.0.0.1"));
    }
}
