package gui;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
    
public class CompressionGUI extends JFrame {

    private JTextArea inputArea;
    private JTable table;
    private DefaultTableModel model;

    public CompressionGUI() {
        setTitle("Compression Comparison");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main container with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mainPanel);

        // Top panel for input
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        JLabel inputLabel = new JLabel("Enter Text for Compression:");
        inputLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(inputLabel, BorderLayout.NORTH);

        inputArea = new JTextArea(5, 50);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(inputArea);
        topPanel.add(scrollPane, BorderLayout.CENTER);

        JButton compareButton = new JButton("Compare Compression");
        topPanel.add(compareButton, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Table panel
        String[] columns = {"Algorithm", "Original Size", "Compressed Size", "Ratio"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane tableScroll = new JScrollPane(table);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        // Action listener
        compareButton.addActionListener(e -> compareCompression());
    }

    private void compareCompression() {
        String text = inputArea.getText();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter some text!");
            return;
        }

        int originalSize = text.length() * 8; // assume 1 char = 8 bits
        model.setRowCount(0);

        String rle = compressRLE(text);
        int rleSize = rle.length() * 8;
        model.addRow(new Object[]{"RLE", originalSize + " bits", rleSize + " bits",
                (rleSize * 100 / originalSize) + "%"});

        int huffSize = compressHuffman(text);
        model.addRow(new Object[]{"Huffman", originalSize + " bits", huffSize + " bits",
                (huffSize * 100 / originalSize) + "%"});

        int lzwSize = compressLZW(text);
        model.addRow(new Object[]{"LZW", originalSize + " bits", lzwSize + " bits",
                (lzwSize * 100 / originalSize) + "%"});
    }

    private String compressRLE(String text) {
        StringBuilder sb = new StringBuilder();
        int count = 1;
        for (int i = 1; i <= text.length(); i++) {
            if (i < text.length() && text.charAt(i) == text.charAt(i - 1)) {
                count++;
            } else {
                sb.append(text.charAt(i - 1)).append(count);
                count = 1;
            }
        }
        return sb.toString();
    }

    private int compressHuffman(String text) {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : text.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.freq));
        for (var entry : freq.entrySet()) {
            pq.add(new Node(entry.getKey(), entry.getValue()));
        }

        while (pq.size() > 1) {
            Node a = pq.poll(), b = pq.poll();
            pq.add(new Node('\0', a.freq + b.freq, a, b));
        }

        Map<Character, String> codes = new HashMap<>();
        buildCodes(pq.peek(), "", codes);

        int bits = 0;
        for (char c : text.toCharArray()) {
            bits += codes.get(c).length();
        }
        return bits;
    }

    private void buildCodes(Node node, String code, Map<Character, String> codes) {
        if (node == null) return;
        if (node.left == null && node.right == null) {
            codes.put(node.ch, code.length() > 0 ? code : "0");
        }
        buildCodes(node.left, code + "0", codes);
        buildCodes(node.right, code + "1", codes);
    }

    static class Node {
        char ch;
        int freq;
        Node left, right;
        Node(char ch, int freq) { this.ch = ch; this.freq = freq; }
        Node(char ch, int freq, Node left, Node right) {
            this.ch = ch; this.freq = freq; this.left = left; this.right = right;
        }
    }

    private int compressLZW(String text) {
        Map<String, Integer> dict = new HashMap<>();
        for (int i = 0; i < 256; i++) dict.put("" + (char)i, i);

        String w = "";
        java.util.List<Integer> result = new ArrayList<>();
        int dictSize = 256;

        for (char c : text.toCharArray()) {
            String wc = w + c;
            if (dict.containsKey(wc)) {
                w = wc;
            } else {
                result.add(dict.get(w));
                dict.put(wc, dictSize++);
                w = "" + c;
            }
        }
        if (!w.isEmpty()) result.add(dict.get(w));

        return result.size() * 12; // assume fixed 12-bit codes
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CompressionGUI app = new CompressionGUI();
            app.setVisible(true);
        });
    }
}