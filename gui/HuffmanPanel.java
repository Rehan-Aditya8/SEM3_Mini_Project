package gui;

import java.util.List;
import algorithms.Huffman.Step;
import algorithms.Huffman.CompressResult;
import algorithms.Huffman;
import algorithms.HuffmanNode;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

@SuppressWarnings("unchecked")
public class HuffmanPanel extends JPanel {

    private HuffmanNode root;
    private Map<Character, String> codes;
    private String encoded;
    private String decoded;
    private String inputText;

    private JTextArea inputArea;
    private JButton compressButton;
    private JButton decompressButton;
    private JCheckBox stepModeCheck;
    private JButton prevStepButton, nextStepButton;
    private JTextArea resultsArea;
    private JTextArea stepExplainArea;
    private TreePanel treePanel;
    private JTable freqTable;
    private CompressResult compressResult;
    private int stepIndex;

    // Store frequency map to keep table visible during all steps
    private Map<Character, Integer> freqMap = null;

    public HuffmanPanel() {
        setLayout(new BorderLayout(8,8));

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputArea = new JTextArea(3, 30);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputPanel.add(new JLabel("Enter text to compress:"), BorderLayout.NORTH);
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        compressButton = new JButton("Compress");
        decompressButton = new JButton("Decompress");
        stepModeCheck = new JCheckBox("Step Mode");

        JPanel topButtons = new JPanel();
        topButtons.add(compressButton);
        topButtons.add(decompressButton);
        topButtons.add(stepModeCheck);
        inputPanel.add(topButtons, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.NORTH);

        // Tree visualization & frequency table area
        treePanel = new TreePanel();
        treePanel.setPreferredSize(new Dimension(800, 380));
        freqTable = new JTable();
        freqTable.setPreferredScrollableViewportSize(new Dimension(320, 120));
        JPanel visualizePanel = new JPanel(new BorderLayout());
        visualizePanel.add(new JLabel("Tree Visualization:"), BorderLayout.NORTH);
        visualizePanel.add(treePanel, BorderLayout.CENTER);

        JPanel freqTablePanel = new JPanel(new BorderLayout());
        freqTablePanel.add(new JLabel("Frequency Table:"), BorderLayout.NORTH);
        freqTablePanel.add(new JScrollPane(freqTable), BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new GridLayout(1,2));
        centerPanel.add(visualizePanel);
        centerPanel.add(freqTablePanel);

        add(centerPanel, BorderLayout.CENTER);

        // Results + step explain
        resultsArea = new JTextArea(7, 50);
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane resultsScroll = new JScrollPane(resultsArea);

        stepExplainArea = new JTextArea(3, 50);
        stepExplainArea.setEditable(false);
        stepExplainArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        stepExplainArea.setForeground(Color.BLUE);

        JPanel explainPanel = new JPanel(new BorderLayout());
        explainPanel.add(new JLabel("Step Explanation:"), BorderLayout.NORTH);
        explainPanel.add(stepExplainArea, BorderLayout.CENTER);

        prevStepButton = new JButton("Previous Step");
        nextStepButton = new JButton("Next Step");
        prevStepButton.setEnabled(false);
        nextStepButton.setEnabled(false);

        JPanel navigationPanel = new JPanel();
        navigationPanel.add(prevStepButton);
        navigationPanel.add(nextStepButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(explainPanel, BorderLayout.NORTH);
        southPanel.add(resultsScroll, BorderLayout.CENTER);
        southPanel.add(navigationPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        compressButton.addActionListener(e -> performCompression());
        decompressButton.addActionListener(e -> performDecompression());
        stepModeCheck.addActionListener(e -> toggleStepMode());
        prevStepButton.addActionListener(e -> showStep(stepIndex-1));
        nextStepButton.addActionListener(e -> showStep(stepIndex+1));
    }

    private void performCompression() {
        inputText = inputArea.getText();
        if (inputText == null || inputText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter text to compress.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        compressResult = Huffman.compressWithSteps(inputText);
        root = compressResult.root;
        codes = compressResult.codes;
        encoded = compressResult.encoded;
        decoded = Huffman.decompress(encoded, root);

        resultsArea.setText("Input: " + inputText + "\n\n"
                + "Encoded: " + encoded + "\n"
                + "Decoded: " + decoded + "\n"
                + "Correct Decode: " + (inputText.equals(decoded) ? "Yes" : "No") + "\n");

        if (stepModeCheck.isSelected()) {
            stepIndex = 0;
            prevStepButton.setEnabled(true);
            nextStepButton.setEnabled(true);
            showStep(0);
        } else {
            prevStepButton.setEnabled(false);
            nextStepButton.setEnabled(false);
            showFinalCodesAndTree();
        }
    }

    private void performDecompression() {
        if (encoded == null || encoded.isEmpty() || root == null) {
            resultsArea.setText("Nothing to decompress. Please compress some text first.");
            return;
        }
        String decompressedText = Huffman.decompress(encoded, root);
        resultsArea.setText("Encoded: " + encoded + "\n" +
                            "Decoded: " + decompressedText + "\n" +
                            "Correct Decode: " + (inputText != null && inputText.equals(decompressedText) ? "Yes" : "No") + "\n");
    }

    private void toggleStepMode() {
        boolean stepMode = stepModeCheck.isSelected();
        prevStepButton.setEnabled(stepMode);
        nextStepButton.setEnabled(stepMode && compressResult != null);
        if (stepMode && compressResult != null) {
            showStep(stepIndex);
        }
    }

    private void showStep(int idx) {
        if (compressResult == null) return;
        List<Step> steps = compressResult.steps;
        if (idx < 0 || idx >= steps.size()) return;

        Step step = steps.get(idx);
        stepIndex = idx;
        stepExplainArea.setText(step.title + "\n" + step.explanation);

        // Frequency table: Keep freqMap from Step 1, and reuse always
        if (step.visual instanceof Map) {
            if (step.title.contains("Frequency Table")) {
                freqMap = (Map<Character, Integer>) step.visual;
            }
        }

        if (freqMap != null) {
            String[] cols = {"Character", "Frequency"};
            Object[][] data = new Object[freqMap.size()][2];
            int i = 0;
            for (Map.Entry<Character, Integer> e : freqMap.entrySet()) {
                data[i][0] = (e.getKey() == ' ') ? "' '" : "" + e.getKey();
                data[i][1] = e.getValue();
                i++;
            }
            freqTable.setModel(new javax.swing.table.DefaultTableModel(data, cols));
        } else {
            freqTable.setModel(new javax.swing.table.DefaultTableModel());
        }

        // Tree Visualization
        if (step.visual instanceof HuffmanNode) {
            treePanel.setRoot((HuffmanNode) step.visual);
        } else if (step.visual instanceof Map) {
            treePanel.setRoot(null);
        } else {
            treePanel.setRoot(step.visual instanceof HuffmanNode ? (HuffmanNode) step.visual : null);
        }
        treePanel.repaint();

        // Results area update
        resultsArea.setText("");
        if (step.visual instanceof Map && step.title.contains("Codes")) {
            @SuppressWarnings("unchecked")
            Map<Character, String> codeMap = (Map<Character, String>) step.visual;
            StringBuilder sb = new StringBuilder();
            sb.append("Codes:\n");
            for (Map.Entry<Character, String> entry : codeMap.entrySet()) {
                sb.append("  " + (entry.getKey() == ' ' ? "' '" : entry.getKey()) + " : " + entry.getValue() + "\n");
            }
            resultsArea.setText(sb.toString());
        }
        if (step.visual instanceof String && step.title.contains("Encoded")) {
            resultsArea.setText("Encoded Text: " + (String) step.visual);
        }

        prevStepButton.setEnabled(idx > 0);
        nextStepButton.setEnabled(idx < compressResult.steps.size() - 1);
    }

    private void showFinalCodesAndTree() {
        if (compressResult == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("Codes:\n");
        for (Map.Entry<Character, String> entry : codes.entrySet()) {
            sb.append("  " + (entry.getKey() == ' ' ? "' '" : entry.getKey()) + " : " + entry.getValue() + "\n");
        }
        sb.append("\nEncoded: " + encoded + "\n");
        sb.append("Decoded: " + decoded + "\n");
        sb.append("Correct Decode: " + (inputText.equals(decoded) ? "Yes" : "No") + "\n");
        resultsArea.setText(sb.toString());
        stepExplainArea.setText("");
        treePanel.setRoot(root);
        treePanel.repaint();
    }

    // --- Tree Visualization ---
    private static class TreePanel extends JPanel {
        private HuffmanNode root;
        public void setRoot(HuffmanNode root) { this.root = root; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (root != null) {
                drawTree(g, root, getWidth() / 2, 30, getWidth() / 4);
            }
        }
        private void drawTree(Graphics g, HuffmanNode node, int x, int y, int gap) {
            if (node == null) return;
            String text = (node.ch == '\0') ? String.valueOf(node.freq) : node.ch + ":" + node.freq;
            g.setColor(new Color(240, 240, 240));
            g.fillOval(x - 20, y - 18, 42, 36);
            g.setColor(Color.BLACK);
            g.drawOval(x - 20, y - 18, 42, 36);
            g.drawString(text, x - 16, y + 2);

            if (node.left != null) {
                g.drawLine(x, y + 18, x - gap, y + 80 - 18);
                drawTree(g, node.left, x - gap, y + 80, gap / 2);
            }
            if (node.right != null) {
                g.drawLine(x, y + 18, x + gap, y + 80 - 18);
                drawTree(g, node.right, x + gap, y + 80, gap / 2);
            }
        }
    }

    // --- Main method to run ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Huffman Coding Visualizer (Teaching Mode)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new HuffmanPanel());
            frame.setMinimumSize(new Dimension(940, 720));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
