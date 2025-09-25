import gui.HuffmanPanel;
import gui.CompressionGUI;
import gui.RLEPanel;
import gui.LZWPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame {

    public Main() {
        super("Data Compression Visualizer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Heading label
        JLabel heading = new JLabel("Data Compression Visualizer", SwingConstants.CENTER);
        heading.setFont(new Font("Serif", Font.BOLD, 24));
        add(heading, BorderLayout.NORTH);

        // Panel with buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton rleButton = new JButton("RLE");
        JButton huffmanButton = new JButton("Huffman");
        JButton lzwButton = new JButton("LZW");
        JButton compareAllButton = new JButton("Compare All");

        buttonPanel.add(rleButton);
        buttonPanel.add(huffmanButton);
        buttonPanel.add(lzwButton);
        buttonPanel.add(compareAllButton);

        add(buttonPanel, BorderLayout.CENTER);

        // Button actions
        rleButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            JFrame rleFrame = new JFrame("Run Length Encoding Visualizer");
            rleFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            rleFrame.setContentPane(new gui.RLEPanel());
            rleFrame.pack();
            rleFrame.setLocationRelativeTo(null);
            rleFrame.setMinimumSize(new Dimension(600, 400));
            rleFrame.setVisible(true);
        }));

        lzwButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            JFrame lzwFrame = new JFrame("LZW Compression Visualizer");
            lzwFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            lzwFrame.setContentPane(new gui.LZWPanel());
            lzwFrame.pack();
            lzwFrame.setLocationRelativeTo(null);
            lzwFrame.setMinimumSize(new Dimension(700, 450));
            lzwFrame.setVisible(true);
        }));


        // Huffman opens HuffmanPanel window
        huffmanButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            JFrame huffFrame = new JFrame("Huffman Coding Visualizer");
            huffFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            huffFrame.setContentPane(new HuffmanPanel());
            huffFrame.pack();
            huffFrame.setLocationRelativeTo(null);
            huffFrame.setMinimumSize(new Dimension(800, 700));
            huffFrame.setVisible(true);
        }));

        // Compare All opens your CompressionGUI window
        compareAllButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            try {
                JFrame compareFrame = new CompressionGUI();
                compareFrame.setLocationRelativeTo(null);
                compareFrame.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error opening Compare All window:\n" + ex.getMessage());
            }
        }));

        setSize(500, 300);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main mainWindow = new Main();
            mainWindow.setVisible(true);
        });
    }
}
