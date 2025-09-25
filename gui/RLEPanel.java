package gui;

import algorithms.RLE;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RLEPanel extends JPanel {

    private JTextArea inputArea;
    private JTextArea encodedArea;
    private JTextArea decodedArea;

    public RLEPanel() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(600, 400));
        
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
        buttonPanel.add(compressButton);
        buttonPanel.add(decompressButton);

        // Output Panel
        JPanel outputPanel = new JPanel(new GridLayout(2,1,5,5));
        encodedArea = new JTextArea(3, 40);
        encodedArea.setEditable(false);
        encodedArea.setBorder(BorderFactory.createTitledBorder("Encoded (Compressed) String"));

        decodedArea = new JTextArea(3, 40);
        decodedArea.setEditable(false);
        decodedArea.setBorder(BorderFactory.createTitledBorder("Decoded (Decompressed) String"));

        outputPanel.add(new JScrollPane(encodedArea));
        outputPanel.add(new JScrollPane(decodedArea));

        // Add all parts to main panel
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(outputPanel, BorderLayout.SOUTH);

        // Button listeners
        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = inputArea.getText();
                if (input == null || input.isEmpty()) {
                    JOptionPane.showMessageDialog(RLEPanel.this, "Please enter text to compress.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String compressed = RLE.compress(input);
                encodedArea.setText(compressed);
                decodedArea.setText(""); // Clear decoded on compress
            }
        });

        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String encoded = encodedArea.getText();
                if (encoded == null || encoded.isEmpty()) {
                    JOptionPane.showMessageDialog(RLEPanel.this, "No encoded text to decompress.", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String decoded = RLE.decompress(encoded);
                decodedArea.setText(decoded);
            }
        });
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
