package gui;

import algorithms.LZW;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class LZWPanel extends JPanel {

    private JTextArea inputArea;
    private JTextArea compressedArea;
    private JTextArea decompressedArea;

    private JCheckBox teachingModeCheck;
    private JButton nextStepButton, prevStepButton;
    private JTextArea teachingExplainArea;
    private JTable dictTable;
    private java.util.List<LZW.TeachingStep> teachingSteps;
    private int teachingStepIdx;
    private String teachingInput;
    private DefaultTableModel dictTableModel;
    private String teachingCompressed;

    public LZWPanel() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(1000, 600));

        JPanel inputPanel = new JPanel(new BorderLayout());
        JLabel inputLabel = new JLabel("Enter text to compress/decompress:");
        inputArea = new JTextArea(4, 70);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane inputScroll = new JScrollPane(inputArea);

        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton compressButton = new JButton("Compress");
        JButton decompressButton = new JButton("Decompress");
        teachingModeCheck = new JCheckBox("Teaching Mode");
        prevStepButton = new JButton("Previous Step");
        nextStepButton = new JButton("Next Step");
        nextStepButton.setEnabled(false);
        prevStepButton.setEnabled(false);

        buttonPanel.add(compressButton);
        buttonPanel.add(decompressButton);
        buttonPanel.add(teachingModeCheck);
        buttonPanel.add(prevStepButton);
        buttonPanel.add(nextStepButton);

        // Teaching step explanation area
        teachingExplainArea = new JTextArea(2, 60);
        teachingExplainArea.setEditable(false);
        teachingExplainArea.setLineWrap(true);
        teachingExplainArea.setWrapStyleWord(true);
        teachingExplainArea.setFont(new Font("Arial", Font.BOLD, 13));
        teachingExplainArea.setBorder(BorderFactory.createTitledBorder("Teaching Step Explanation"));
        teachingExplainArea.setBackground(new Color(245, 250, 255));
        teachingExplainArea.setPreferredSize(new Dimension(600, 55));
        teachingExplainArea.setMaximumSize(new Dimension(Short.MAX_VALUE, 55));

        // Compressed Codes area
        compressedArea = new JTextArea(2, 60);
        compressedArea.setEditable(false);
        compressedArea.setBorder(BorderFactory.createTitledBorder("Compressed Codes"));

        // Decompressed Text area
        decompressedArea = new JTextArea(2, 60);
        decompressedArea.setEditable(false);
        decompressedArea.setBorder(BorderFactory.createTitledBorder("Decompressed Text"));

        // Dictionary Table with improved columns
        dictTableModel = new DefaultTableModel(new Object[]{"String", "Code"}, 0);
        dictTable = new JTable(dictTableModel);
        dictTable.setRowHeight(24);
        dictTable.setEnabled(false);
        dictTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane dictScroll = new JScrollPane(dictTable);
        dictScroll.setPreferredSize(new Dimension(320, 370));
        dictScroll.setBorder(BorderFactory.createTitledBorder("Current Dictionary (added entries)"));

        // Set column widths
        TableColumnModel colModel = dictTable.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(180);
        colModel.getColumn(1).setPreferredWidth(60);

        dictTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer(){
            @Override
            protected void setValue(Object value) {
                if (value == null) setText("");
                else {
                    String s = (String)value;
                    if (s.length() == 1) {
                        char c = s.charAt(0);
                        if (c == 32) setText("[SPACE]");
                        else if (c == 9) setText("[TAB]");
                        else if (c == 10) setText("[LF]");
                        else if (c < 32) setText("[CTRL-" + ((int)c) + "]");
                        else setText("'" + c + "'");
                    } else if (s.equals("")) {
                        setText("(empty)");
                    } else setText(s);
                }
            }
        });

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(buttonPanel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(teachingExplainArea);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(new JScrollPane(compressedArea));
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(new JScrollPane(decompressedArea));

        add(inputPanel, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.CENTER);
        add(dictScroll, BorderLayout.EAST);

        // Listeners
        compressButton.addActionListener(e -> handleCompress());
        decompressButton.addActionListener(e -> handleDecompress());
        teachingModeCheck.addActionListener(e -> {
            boolean teaching = teachingModeCheck.isSelected();
            nextStepButton.setEnabled(teaching && teachingSteps != null && teachingSteps.size() > 0);
            prevStepButton.setEnabled(teaching && teachingStepIdx > 0 && teachingSteps != null && teachingSteps.size() > 0);
            teachingExplainArea.setVisible(teaching);
            dictScroll.setVisible(teaching);
            if (!teaching) {
                compressedArea.setText("");
                decompressedArea.setText("");
                dictTableModel.setRowCount(0);
                teachingExplainArea.setText("");
            }
        });
        nextStepButton.addActionListener(e -> showTeachingStep(teachingStepIdx + 1));
        prevStepButton.addActionListener(e -> showTeachingStep(teachingStepIdx - 1));

        // Hide teaching stuff by default
        teachingExplainArea.setVisible(false);
        dictScroll.setVisible(false);
    }

    private void handleCompress() {
        String input = inputArea.getText();
        if (input == null || input.isEmpty()) {
            JOptionPane.showMessageDialog(LZWPanel.this, "Please enter text to compress.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (teachingModeCheck.isSelected()) {
            LZW.TeachingResult result = LZW.compressWithTeachingSteps(input);
            teachingSteps = result.steps;
            teachingCompressed = result.compressed.toString().replaceAll("[\\[\\] ]", "");
            teachingInput = input;
            teachingStepIdx = 0;
            showTeachingStep(0);
            nextStepButton.setEnabled(teachingSteps.size() > 1);
            prevStepButton.setEnabled(false);
        } else {
            String compressedStr = LZW.compressToString(input);
            compressedArea.setText("[" + compressedStr + "]");
            decompressedArea.setText("");
            teachingExplainArea.setText("");
            dictTableModel.setRowCount(0);
        }
    }

    private void handleDecompress() {
        String compressedStr = compressedArea.getText();
        if (compressedStr == null || compressedStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(LZWPanel.this, "No compressed codes to decompress.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        compressedStr = compressedStr.trim();
        if (compressedStr.startsWith("[") && compressedStr.endsWith("]")) {
            compressedStr = compressedStr.substring(1, compressedStr.length() - 1);
        }
        List<Integer> codes = LZW.decompressFromString(compressedStr);
        String decompressed = LZW.decompress(codes);
        decompressedArea.setText(decompressed);
    }

    private void showTeachingStep(int idx) {
        if (teachingSteps == null || idx < 0 || idx >= teachingSteps.size()) return;

        teachingStepIdx = idx;
        LZW.TeachingStep step = teachingSteps.get(idx);

        teachingExplainArea.setText("Step " + (idx + 1) + "/" + teachingSteps.size() + ": " + step.explanation);

        StringBuilder inputExplain = new StringBuilder(teachingInput);
        int pos = step.inputPos;
        if (pos >= 0 && pos < teachingInput.length()) {
            inputExplain.insert(pos + 1, "]");
            inputExplain.insert(pos, "[");
        }
        inputArea.setText(inputExplain.toString());

        dictTableModel.setRowCount(0);
        int shown = 0, highlightRow = -1;
        for (Map.Entry<String, Integer> entry : step.addedEntries.entrySet()) {
            if (++shown > 25) break;
            dictTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
            if (step.newEntry != null && entry.getKey().equals(step.newEntry)) {
                highlightRow = shown - 1;
            }
        }
        if (highlightRow >= 0) {
            dictTable.setRowSelectionInterval(highlightRow, highlightRow);
            dictTable.scrollRectToVisible(dictTable.getCellRect(highlightRow, 0, true));
        } else {
            dictTable.clearSelection();
        }

        String outputStr = step.outputCodes == null ? "" : step.outputCodes.toString().replaceAll("[\\[\\] ]", "");
        compressedArea.setText("[" + outputStr + "]");
        decompressedArea.setText("");

        prevStepButton.setEnabled(teachingStepIdx > 0);
        nextStepButton.setEnabled(teachingStepIdx < teachingSteps.size() - 1);

        if (teachingStepIdx == teachingSteps.size() - 1) {
            compressedArea.setText("[" + teachingCompressed + "]");
            List<Integer> codes = LZW.decompressFromString(teachingCompressed);
            decompressedArea.setText(LZW.decompress(codes));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("LZW Compression Visualizer (Teaching Mode)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new LZWPanel());
            frame.setSize(1050, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
