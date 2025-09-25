package gui;

import algorithms.Huffman;
import algorithms.HuffmanNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.stream.Collectors;

public class HuffmanPanel extends JPanel {

    private HuffmanNode root;
    private Map<Character, String> codes;
    private String encoded;
    private String decoded;
    private String inputText;

    private JTextArea inputArea;
    private JButton compressButton;
    private TreePanel treePanel;
    private JTextArea resultsArea;

    public HuffmanPanel() {
        setLayout(new BorderLayout());

        // Input Panel (Top)
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputArea = new JTextArea(3, 30);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputPanel.add(new JLabel("Enter text to compress:"), BorderLayout.NORTH);
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        compressButton = new JButton("Compress");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(compressButton);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.NORTH);

        // Tree Panel (Center)
        treePanel = new TreePanel();
        treePanel.setPreferredSize(new Dimension(800, 400));
        JScrollPane treeScroll = new JScrollPane(treePanel);
        add(treeScroll, BorderLayout.CENTER);

        // Results Panel (Bottom)
        resultsArea = new JTextArea(8, 50);
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane resultsScroll = new JScrollPane(resultsArea);

        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.add(new JLabel("Results:"), BorderLayout.NORTH);
        resultsPanel.add(resultsScroll, BorderLayout.CENTER);
        resultsPanel.setPreferredSize(new Dimension(800, 200));

        add(resultsPanel, BorderLayout.SOUTH);

        // Button Action Listener
        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performCompression();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void performCompression() {
        inputText = inputArea.getText();
        if (inputText == null || inputText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter text to compress.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<String, Object> compressed = Huffman.compress(inputText);
        root = (HuffmanNode) compressed.get("root");
        codes = (Map<Character, String>) compressed.get("codes");
        encoded = (String) compressed.get("encoded");
        decoded = Huffman.decompress(encoded, root);

        // Update results area
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("Input: ").append(inputText).append("\n\n");
        resultBuilder.append("Codes:\n");
        for (Map.Entry<Character, String> entry : codes.entrySet()) {
            String charDisplay = entry.getKey() == ' ' ? "' '" : String.valueOf(entry.getKey());
            resultBuilder.append("  ").append(charDisplay).append(" : ").append(entry.getValue()).append("\n");
        }
        resultBuilder.append("\nEncoded: ").append(encoded).append("\n\n");
        resultBuilder.append("Decoded: ").append(decoded).append("\n\n");
        resultBuilder.append("Correct Decode: ").append(inputText.equals(decoded) ? "Yes" : "No").append("\n");

        resultsArea.setText(resultBuilder.toString());

        // Repaint tree panel
        treePanel.setRoot(root);
        treePanel.repaint();
    }

    // Inner class for drawing the Huffman tree
    private static class TreePanel extends JPanel {
        private HuffmanNode root;

        public void setRoot(HuffmanNode root) {
            this.root = root;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (root != null) {
                drawTree(g, root, getWidth() / 2, 40, getWidth() / 4);
            }
        }

        private void drawTree(Graphics g, HuffmanNode node, int x, int y, int gap) {
            if (node == null) return;

            String text = (node.ch == '\0') ? String.valueOf(node.freq) : node.ch + ":" + node.freq;

            g.setColor(Color.WHITE);
            g.fillOval(x - 20, y - 20, 40, 40);
            g.setColor(Color.BLACK);
            g.drawOval(x - 20, y - 20, 40, 40);
            g.drawString(text, x - 10, y + 5);

            if (node.left != null) {
                g.drawLine(x, y + 20, x - gap, y + 80 - 20);
                drawTree(g, node.left, x - gap, y + 80, gap / 2);
            }
            if (node.right != null) {
                g.drawLine(x, y + 20, x + gap, y + 80 - 20);
                drawTree(g, node.right, x + gap, y + 80, gap / 2);
            }
        }
    }

    // Main method to launch GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Huffman Coding Visualizer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            HuffmanPanel panel = new HuffmanPanel();
            frame.setContentPane(panel);
            frame.pack();
            frame.setMinimumSize(new Dimension(800, 700));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
