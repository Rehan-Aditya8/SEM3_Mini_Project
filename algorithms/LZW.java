package algorithms;

import java.util.*;

public class LZW {

    // Compress input string using LZW
    public static List<Integer> compress(String input) {
        if (input == null || input.isEmpty()) return Collections.emptyList();

        // Initialize dictionary with 256 ASCII chars
        Map<String, Integer> dictionary = new HashMap<>();
        int dictSize = 256;
        for (int i = 0; i < 256; i++) {
            dictionary.put("" + (char) i, i);
        }

        List<Integer> result = new ArrayList<>();
        String w = "";

        for (char c : input.toCharArray()) {
            String wc = w + c;
            if (dictionary.containsKey(wc)) {
                w = wc;
            } else {
                result.add(dictionary.get(w));
                dictionary.put(wc, dictSize++);
                w = "" + c;
            }
        }
        // Output code for w.
        if (!w.equals("")) {
            result.add(dictionary.get(w));
        }
        return result;
    }

    // Decompress list of codes using LZW
    public static String decompress(List<Integer> compressed) {
        if (compressed == null || compressed.isEmpty()) return "";

        // Initialize dictionary with 256 ASCII chars
        Map<Integer, String> dictionary = new HashMap<>();
        int dictSize = 256;
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, "" + (char) i);
        }

        String w = "" + (char)(int)compressed.get(0);
        StringBuilder result = new StringBuilder(w);

        for (int i = 1; i < compressed.size(); i++) {
            int k = compressed.get(i);
            String entry;

            if (dictionary.containsKey(k)) {
                entry = dictionary.get(k);
            } else if (k == dictSize) {
                entry = w + w.charAt(0);
            } else {
                throw new IllegalArgumentException("Bad compressed k: " + k);
            }
            result.append(entry);

            // Add w+entry[0] to dictionary
            dictionary.put(dictSize++, w + entry.charAt(0));
            w = entry;
        }
        return result.toString();
    }

    // Utility method for displaying compressed codes as comma-separated string
    public static String compressToString(String input) {
        List<Integer> compressed = compress(input);
        if (compressed.isEmpty()) return "";
        return compressed.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b).get();
    }

    // Utility method for parsing compressed string (comma-separated codes) back to list integers
    public static List<Integer> decompressFromString(String compressedString) {
        if (compressedString == null || compressedString.trim().isEmpty()) return Collections.emptyList();
        String[] tokens = compressedString.split(",");
        List<Integer> codes = new ArrayList<>();
        for (String token : tokens) {
            codes.add(Integer.parseInt(token.trim()));
        }
        return codes;
    }

    // For simple manual testing
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter text to compress (LZW): ");
        String input = scanner.nextLine();

        String compressedStr = compressToString(input);
        System.out.println("Compressed Codes: " + compressedStr);

        List<Integer> codes = decompressFromString(compressedStr);
        String decompressedStr = decompress(codes);
        System.out.println("Decompressed String: " + decompressedStr);

        System.out.println("Correct decompression: " + input.equals(decompressedStr));
    }
}
