package gui;


import java.awt.BorderLayout;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
// import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class CompressionGUI extends JFrame {
    private JTextArea inputArea;
    private JTable table;
    private DefaultTableModel model;
    private JLabel teachingLabel;

    public CompressionGUI() {
        setTitle("Compression Comparison");
        setSize(850, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mainPanel);

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

        String[] columns = {"Algorithm", "Original Size (bytes)", "Compressed Size (bytes)", "Compression Ratio (%)"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        JScrollPane tableScroll = new JScrollPane(table);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        table.setDefaultRenderer(Object.class, new TableCellRendererImpl());

        teachingLabel = new JLabel();
        teachingLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        teachingLabel.setBorder(BorderFactory.createEmptyBorder(12,0,0,0));
        mainPanel.add(teachingLabel, BorderLayout.SOUTH);

        compareButton.addActionListener(e -> compareCompression());
    }

    private void compareCompression() {
        String text = inputArea.getText();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter some text!");
            return;
        }

        int originalSize = text.getBytes().length; // bytes
        model.setRowCount(0);

        // RLE
        String rle = compressRLE(text);
        int rleSize = rle.getBytes().length;
        double rleRatio = originalSize == 0 ? 0 : (100.0 * rleSize / originalSize);
        model.addRow(new Object[]{"RLE", originalSize, rleSize, String.format("%.1f", rleRatio)});

        // Huffman
        int huffBits = compressHuffman(text);
        int huffSize = (huffBits + 7) / 8;
        double huffRatio = originalSize == 0 ? 0 : (100.0 * huffSize / originalSize);
        model.addRow(new Object[]{"Huffman", originalSize, huffSize, String.format("%.1f", huffRatio)});

        // LZW
        int lzwBits = compressLZW(text);
        int lzwSize = (lzwBits + 7) / 8;
        double lzwRatio = originalSize == 0 ? 0 : (100.0 * lzwSize / originalSize);
        model.addRow(new Object[]{"LZW", originalSize, lzwSize, String.format("%.1f", lzwRatio)});

        int bestRow = 0, worstRow = 0;
        double best = Double.MAX_VALUE, worst = -1;
        for (int i = 0; i < 3; i++) {
            double ratio = Double.parseDouble((String)model.getValueAt(i, 3));
            if (ratio < best) { best = ratio; bestRow = i; }
            if (ratio > worst) { worst = ratio; worstRow = i; }
        }
        TableCellRendererImpl.bestRow = bestRow;
        TableCellRendererImpl.worstRow = worstRow;
        table.repaint();

        teachingLabel.setText(
            "<html>"
            + "<b>RLE:</b> Efficient for text with repeated characters. &nbsp; &nbsp; "
            + "<b>Huffman:</b> Best when character frequencies vary. &nbsp; &nbsp; "
            + "<b>LZW:</b> Suited for text with repeated patterns or dictionary words."
            + "</html>");
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
        for (char c : text.toCharArray())
            freq.put(c, freq.getOrDefault(c, 0) + 1);

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.freq));
        for (var entry : freq.entrySet())
            pq.add(new Node(entry.getKey(), entry.getValue()));
        if (pq.size() == 1) pq.add(new Node('\0', 1));

        while (pq.size() > 1) {
            Node a = pq.poll(), b = pq.poll();
            pq.add(new Node('\0', a.freq + b.freq, a, b));
        }

        Map<Character, String> codes = new HashMap<>();
        buildCodes(pq.peek(), "", codes);

        int bits = 0;
        for (char c : text.toCharArray())
            bits += codes.get(c).length();

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
        for (int i = 0; i < 256; i++)
            dict.put("" + (char) i, i);

        String w = "";
        java.util.List<Integer> result = new java.util.ArrayList<>();
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

        return result.size() * 12;
    }

    static class TableCellRendererImpl extends DefaultTableCellRenderer {
        static int bestRow = 0;
        static int worstRow = 0;
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (row == bestRow) {
                c.setBackground(new Color(198, 239, 206)); // green
            } else if (row == worstRow) {
                c.setBackground(new Color(255, 199, 206)); // red
            } else {
                c.setBackground(Color.white);
            }
            setHorizontalAlignment(CENTER);
            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CompressionGUI app = new CompressionGUI();
            app.setVisible(true);
        });
    }
}
