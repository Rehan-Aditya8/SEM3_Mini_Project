package algorithms;

import java.util.*;

public class Huffman {

    public static class Step {
        public String title;
        public String explanation;
        public Object visual;
        public Step(String title, String explanation, Object visual) {
            this.title = title;
            this.explanation = explanation;
            this.visual = visual;
        }
    }

    public static class CompressResult {
        public HuffmanNode root;
        public Map<Character, String> codes;
        public String encoded;
        public List<Step> steps;
    }

    public static CompressResult compressWithSteps(String text) {
        CompressResult result = new CompressResult();
        result.steps = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            result.root = null;
            result.codes = new HashMap<>();
            result.encoded = "";
            result.steps.add(new Step(
                "Empty Input",
                "No input text provided.",
                null
            ));
            return result;
        }

        // Step 1: Frequency Table
        Map<Character, Integer> freqMap = new HashMap<>();
        for (char c : text.toCharArray())
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);

        result.steps.add(new Step(
            "Step 1: Frequency Table Built",
            "Calculated frequency of each character in input.",
            new HashMap<>(freqMap)
        ));

        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.freq));
        for (Map.Entry<Character, Integer> e : freqMap.entrySet()) {
            pq.add(new HuffmanNode(e.getKey(), e.getValue()));
        }
        if (pq.size() == 1) {
            pq.add(new HuffmanNode('\0', 1));
        }
        int stepNum = 2;
        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();
            HuffmanNode parent = new HuffmanNode(left.freq + right.freq, left, right);
            pq.add(parent);
            result.steps.add(new Step(
                "Step " + stepNum + ": Combined Nodes",
                "Combined nodes (" +
                    (left.ch == '\0' ? left.freq : left.ch + ":" + left.freq) + ") and (" +
                    (right.ch == '\0' ? right.freq : right.ch + ":" + right.freq) +
                    ") → new node (" + parent.freq + ")",
                parent
            ));
            stepNum++;
        }

        HuffmanNode root = pq.poll();
        result.root = root;
        Map<Character, String> codes = new HashMap<>();
        buildCodes(root, "", codes);
        result.codes = codes;
        result.steps.add(new Step(
            "Step " + stepNum + ": Codes Assigned",
            "Assigned binary codes to each character.",
            new HashMap<>(codes)
        ));
        stepNum++;
        StringBuilder encoded = new StringBuilder();
        for (char c : text.toCharArray()) {
            encoded.append(codes.get(c));
        }
        result.encoded = encoded.toString();
        result.steps.add(new Step(
            "Step " + stepNum + ": Text Encoded",
            "Encoded text using binary Huffman codes.",
            result.encoded
        ));
        return result;
    }

    private static void buildCodes(HuffmanNode node, String code, Map<Character, String> codes) {
        if (node == null) return;
        if (node.isLeaf()) {
            codes.put(node.ch, code.length() > 0 ? code : "0");
        }
        buildCodes(node.left, code + "0", codes);
        buildCodes(node.right, code + "1", codes);
    }

    public static String decompress(String encoded, HuffmanNode root) {
        if (encoded == null || encoded.isEmpty() || root == null) return "";
        StringBuilder sb = new StringBuilder();
        HuffmanNode current = root;
        for (char bit : encoded.toCharArray()) {
            current = (bit == '0') ? current.left : current.right;
            if (current.isLeaf()) {
                sb.append(current.ch);
                current = root;
            }
        }
        return sb.toString();
    }
}
