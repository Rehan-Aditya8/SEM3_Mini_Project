package gui;

import algorithms.LZW;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class LZWPanel extends JPanel {

    private JTextArea inputArea;
    private JTextArea compressedArea;
    private JTextArea decompressedArea;
    private JLabel lengthLabel;

    public LZWPanel() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(700, 450));

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        JLabel inputLabel = new JLabel("Enter text to compress/decompress:");
        inputArea = new JTextArea(6, 50);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane inputScroll = new JScrollPane(inputArea);

        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton compressButton = new JButton("Compress");
        JButton decompressButton = new JButton("Decompress");
        buttonPanel.add(compressButton);
        buttonPanel.add(decompressButton);

        // Output Panel
        JPanel outputPanel = new JPanel(new GridLayout(3, 1, 5,5));

        compressedArea = new JTextArea(3, 50);
        compressedArea.setEditable(false);
        compressedArea.setBorder(BorderFactory.createTitledBorder("Compressed Codes"));

        decompressedArea = new JTextArea(3, 50);
        decompressedArea.setEditable(false);
        decompressedArea.setBorder(BorderFactory.createTitledBorder("Decompressed Text"));

        lengthLabel = new JLabel("Original length: 0, Compressed length: 0");
        lengthLabel.setHorizontalAlignment(SwingConstants.CENTER);

        outputPanel.add(new JScrollPane(compressedArea));
        outputPanel.add(new JScrollPane(decompressedArea));
        outputPanel.add(lengthLabel);

        // Assemble main panel
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(outputPanel, BorderLayout.SOUTH);

        // Button Listeners
        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = inputArea.getText();
                if (input == null || input.isEmpty()) {
                    JOptionPane.showMessageDialog(LZWPanel.this, "Please enter text to compress.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String compressedStr = LZW.compressToString(input);
                compressedArea.setText("[" + compressedStr + "]");

                // Clear decompressed area on compression
                decompressedArea.setText("");
                lengthLabel.setText(String.format("Original length: %d, Compressed length: %d", input.length(), compressedStr.replaceAll("[^\\d]", "").length()/2 + 1));
            }
        });

        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String compressedStr = compressedArea.getText();
                if (compressedStr == null || compressedStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(LZWPanel.this, "No compressed codes to decompress.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // Remove brackets [] from string
                compressedStr = compressedStr.trim();
                if (compressedStr.startsWith("[") && compressedStr.endsWith("]")) {
                    compressedStr = compressedStr.substring(1, compressedStr.length() - 1);
                }
                List<Integer> codes = LZW.decompressFromString(compressedStr);
                String decompressed = LZW.decompress(codes);
                decompressedArea.setText(decompressed);
            }
        });
    }

    // Optional standalone test main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("LZW Compression Visualizer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new LZWPanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
