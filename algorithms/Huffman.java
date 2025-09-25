package algorithms;

import java.util.*;
import algorithms.HuffmanNode;

public class Huffman {

    // Build frequency map
    private static Map<Character, Integer> buildFrequency(String text) {
        Map<Character, Integer> freqMap = new HashMap<>();
        for (char c : text.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }
        return freqMap;
    }

    // Build Huffman tree
    private static HuffmanNode buildTree(Map<Character, Integer> freqMap) {
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.freq));
        for (Map.Entry<Character, Integer> e : freqMap.entrySet()) {
            pq.add(new HuffmanNode(e.getKey(), e.getValue()));
        }
        // Edge case: single unique character
        if (pq.size() == 1) {
            pq.add(new HuffmanNode('\0', 1)); // dummy node
        }
        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();
            HuffmanNode parent = new HuffmanNode(left.freq + right.freq, left, right);
            pq.add(parent);
        }
        return pq.poll(); // root
    }

    // Build codes from tree
    private static void buildCodes(HuffmanNode node, String code, Map<Character, String> codes) {
        if (node == null) return;
        if (node.isLeaf()) {
            codes.put(node.ch, code.length() > 0 ? code : "0"); // handle single char case
        }
        buildCodes(node.left, code + "0", codes);
        buildCodes(node.right, code + "1", codes);
    }

    // Compress: return encoded string + codes + root
    public static Map<String, Object> compress(String text) {
        Map<String, Object> result = new HashMap<>();
        if (text == null || text.isEmpty()) {
            result.put("encoded", "");
            result.put("codes", new HashMap<Character, String>());
            result.put("root", null);
            return result;
        }
        Map<Character, Integer> freqMap = buildFrequency(text);
        HuffmanNode root = buildTree(freqMap);
        Map<Character, String> codes = new HashMap<>();
        buildCodes(root, "", codes);
        StringBuilder encoded = new StringBuilder();
        for (char c : text.toCharArray()) {
            encoded.append(codes.get(c));
        }
        result.put("encoded", encoded.toString());
        result.put("codes", codes);
        result.put("root", root);
        return result;
    }

    // Decompress
    public static String decompress(String encoded, HuffmanNode root) {
        if (encoded == null || encoded.isEmpty() || root == null) return "";
        StringBuilder sb = new StringBuilder();
        HuffmanNode current = root;
        for (char bit : encoded.toCharArray()) {
            current = (bit == '0') ? current.left : current.right;
            if (current.isLeaf()) {
                sb.append(current.ch);
                current = root; // reset
            }
        }
        return sb.toString();
    }
}
