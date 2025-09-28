package gui;

import algorithms.RLE;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RLEPanel extends JPanel {

    private JTextArea inputArea;
    private JTextArea encodedArea;
    private JTextArea decodedArea;

    // Teaching mode components
    private JLabel teachingLabel;
    private JTable stepTable;
    private DefaultTableModel tableModel;
    private JButton nextStepButton;

    // State for teaching mode
    private String teachingInput;
    private int index;
    private StringBuilder compressedBuilder;

    public RLEPanel() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(700, 500));

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        JLabel inputLabel = new JLabel("Enter text:");
        inputArea = new JTextArea(5, 40);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane inputScroll = new JScrollPane(inputArea);

        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton compressButton = new JButton("Compress");
        JButton decompressButton = new JButton("Decompress");
        JButton teachButton = new JButton("Teach Mode");
        buttonPanel.add(compressButton);
        buttonPanel.add(decompressButton);
        buttonPanel.add(teachButton);

        // Output Panel
        JPanel outputPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        encodedArea = new JTextArea(3, 40);
        encodedArea.setEditable(false);
        encodedArea.setBorder(BorderFactory.createTitledBorder("Encoded (Compressed) String"));

        decodedArea = new JTextArea(3, 40);
        decodedArea.setEditable(false);
        decodedArea.setBorder(BorderFactory.createTitledBorder("Decoded (Decompressed) String"));

        outputPanel.add(new JScrollPane(encodedArea));
        outputPanel.add(new JScrollPane(decodedArea));

        // Teaching Mode Panel
        JPanel teachingPanel = new JPanel(new BorderLayout(5, 5));
        teachingLabel = new JLabel("Currently reading: ");
        teachingLabel.setFont(new Font("Arial", Font.BOLD, 14));
        teachingLabel.setForeground(Color.BLUE);

        tableModel = new DefaultTableModel(new Object[]{"Character", "Count"}, 0);
        stepTable = new JTable(tableModel);

        nextStepButton = new JButton("Next Step");
        nextStepButton.setEnabled(false);

        teachingPanel.add(teachingLabel, BorderLayout.NORTH);
        teachingPanel.add(new JScrollPane(stepTable), BorderLayout.CENTER);
        teachingPanel.add(nextStepButton, BorderLayout.SOUTH);
        teachingPanel.setBorder(BorderFactory.createTitledBorder("Teaching Mode"));

        // Add all parts to main panel
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(outputPanel, BorderLayout.SOUTH);
        add(teachingPanel, BorderLayout.EAST);

        // Button listeners
        compressButton.addActionListener(e -> {
            String input = inputArea.getText();
            if (input == null || input.isEmpty()) {
                JOptionPane.showMessageDialog(RLEPanel.this, "Please enter text to compress.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String compressed = RLE.compress(input);
            encodedArea.setText(compressed);
            decodedArea.setText(""); // Clear decoded on compress
        });

        decompressButton.addActionListener(e -> {
            String encoded = encodedArea.getText();
            if (encoded == null || encoded.isEmpty()) {
                JOptionPane.showMessageDialog(RLEPanel.this, "No encoded text to decompress.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String decoded = RLE.decompress(encoded);
            decodedArea.setText(decoded);
        });

        teachButton.addActionListener(e -> startTeachingMode());
        nextStepButton.addActionListener(e -> performNextStep());
    }

    private void startTeachingMode() {
        teachingInput = inputArea.getText();
        if (teachingInput == null || teachingInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter text to teach compress.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        index = 0;
        compressedBuilder = new StringBuilder();
        tableModel.setRowCount(0);
        encodedArea.setText("");
        teachingLabel.setText("Currently reading: " + teachingInput);
        nextStepButton.setEnabled(true);
    }

    private void performNextStep() {
        if (index >= teachingInput.length()) {
            encodedArea.setText(compressedBuilder.toString());
            teachingLabel.setText("Teaching finished!");
            nextStepButton.setEnabled(false);
            return;
        }

        char current = teachingInput.charAt(index);
        int count = 1;
        int j = index + 1;
        while (j < teachingInput.length() && teachingInput.charAt(j) == current) {
            count++;
            j++;
        }

        // Update teaching label with highlight
        StringBuilder highlighted = new StringBuilder("<html>Currently reading: ");
        for (int k = 0; k < teachingInput.length(); k++) {
            if (k == index) {
                highlighted.append("<span style='color:red; font-weight:bold;'>").append(teachingInput.charAt(k)).append("</span>");
            } else {
                highlighted.append(teachingInput.charAt(k));
            }
        }
        highlighted.append("</html>");
        teachingLabel.setText(highlighted.toString());

        // Add to table
        tableModel.addRow(new Object[]{current, count});
        compressedBuilder.append(current).append(count);

        index = j;
    }

    // Optional main method for standalone testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Run Length Encoding Visualizer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new RLEPanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
