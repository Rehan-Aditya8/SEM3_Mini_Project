package algorithms;

import java.util.Scanner;

public class RLE {

    // Compress input string using Run Length Encoding
    public static String compress(String input) {
        if (input == null || input.isEmpty()) return "";

        StringBuilder result = new StringBuilder();

        int count = 1;
        char prev = input.charAt(0);

        for (int i = 1; i < input.length(); i++) {
            char curr = input.charAt(i);
            if (curr == prev) {
                count++;
            } else {
                // Append previous char and its count
                result.append(prev).append(count);
                prev = curr;
                count = 1;
            }
        }
        // Append last character
        result.append(prev).append(count);

        return result.toString();
    }

    // Decompress RLE string
    // Format: <char><count><char><count>...
    public static String decompress(String input) {
        if (input == null || input.isEmpty()) return "";

        StringBuilder result = new StringBuilder();

        int i = 0;
        while (i < input.length()) {
            char ch = input.charAt(i);
            i++;

            // Read number after character (can be multi-digit)
            StringBuilder countStr = new StringBuilder();
            while (i < input.length() && Character.isDigit(input.charAt(i))) {
                countStr.append(input.charAt(i));
                i++;
            }

            int count = countStr.length() > 0 ? Integer.parseInt(countStr.toString()) : 1;

            // Append character count times
            for (int j = 0; j < count; j++) {
                result.append(ch);
            }
        }
        return result.toString();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter text to compress using RLE: ");
        String input = scanner.nextLine();

        String compressed = compress(input);
        String decompressed = decompress(compressed);

        System.out.println("Compressed: " + compressed);
        System.out.println("Decompressed: " + decompressed);
        System.out.println("Correct decompression: " + input.equals(decompressed));
    }
}
