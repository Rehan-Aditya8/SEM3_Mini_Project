package algorithms;

import java.util.*;

public class LZW {

    // Teaching step class
    public static class TeachingStep {
        public int inputPos;            // Current position in input
        public String processedPart;    // Text already output (coded)
        public String currentW;         // Current matched substring w
        public String currentC;         // Current next char c
        public String newEntry;         // New entry added to dictionary (if any)
        public int codeOutput;          // Code output this step, -1 no output
        public LinkedHashMap<String,Integer> addedEntries; // Entries added this step
        public List<Integer> outputCodes;  // Output codes so far
        public String explanation;      // Text explanation for this step
    }

    public static class TeachingResult {
        public List<TeachingStep> steps;
        public List<Integer> compressed;
        public String input;
    }

    // Teaching mode compression - step-wise
    public static TeachingResult compressWithTeachingSteps(String input) {
        TeachingResult result = new TeachingResult();
        result.steps = new ArrayList<>();
        result.compressed = new ArrayList<>();
        result.input = input;

        // Start dictionary with ASCII printed characters only (32..126)
        LinkedHashMap<String,Integer> dictionary = new LinkedHashMap<>();
        int dictSize = 0;
        for (int i = 32; i <= 126; i++) {
            dictionary.put("" + (char) i, dictSize++);
        }

        String w = "";
        List<Integer> output = new ArrayList<>();

        int pos = 0;
        while (pos < input.length()) {
            char c = input.charAt(pos);
            String wc = w + c;
            TeachingStep step = new TeachingStep();
            step.inputPos = pos;
            step.processedPart = input.substring(0, pos - w.length());
            step.currentW = w;
            step.currentC = String.valueOf(c);
            step.outputCodes = new ArrayList<>(output);
            step.addedEntries = new LinkedHashMap<>();

            if (dictionary.containsKey(wc)) {
                w = wc;
                step.codeOutput = -1;
                step.explanation = "Found substring '" + wc + "' in dictionary, extend search.";
            } else {
                // Output code for w
                if (!w.isEmpty()) {
                    int code = dictionary.get(w);
                    output.add(code);
                    step.codeOutput = code;
                    step.explanation = "Longest match: '" + w + "' → output code " + code + ". ";
                } else {
                    step.codeOutput = -1;
                    step.explanation = "No match for current substring; this shouldn't happen.";
                }
                // Add new entry wc
                dictionary.put(wc, dictSize++);
                step.newEntry = wc;
                step.addedEntries.put(wc, dictionary.get(wc));
                step.explanation += "New dictionary entry added: '" + wc + "' with code " + dictionary.get(wc) + ".";

                w = "" + c;
            }
            result.steps.add(step);
            pos++;
        }

        // Output code for last w
        if (!w.isEmpty()) {
            TeachingStep lastStep = new TeachingStep();
            lastStep.inputPos = pos;
            lastStep.processedPart = input.substring(0, input.length() - w.length());
            lastStep.currentW = w;
            lastStep.currentC = "";
            lastStep.outputCodes = new ArrayList<>(output);
            lastStep.addedEntries = new LinkedHashMap<>();
            int finalCode = dictionary.get(w);
            output.add(finalCode);
            lastStep.codeOutput = finalCode;
            lastStep.explanation = "End of input. Output code " + finalCode + " for '" + w + "'. No new dictionary entry added.";
            result.steps.add(lastStep);
        }

        result.compressed = output;
        return result;
    }

    // Normal LZW compression (no teaching steps)
    public static List<Integer> compress(String input) {
        if (input == null || input.isEmpty()) return Collections.emptyList();

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
        if (!w.equals("")) {
            result.add(dictionary.get(w));
        }
        return result;
    }

    public static String decompress(List<Integer> compressed) {
        if (compressed == null || compressed.isEmpty()) return "";

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

            dictionary.put(dictSize++, w + entry.charAt(0));
            w = entry;
        }
        return result.toString();
    }

    public static String compressToString(String input) {
        List<Integer> compressed = compress(input);
        if (compressed.isEmpty()) return "";
        return compressed.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b).get();
    }

    public static List<Integer> decompressFromString(String compressedString) {
        if (compressedString == null || compressedString.trim().isEmpty()) return Collections.emptyList();
        String[] tokens = compressedString.split(",");
        List<Integer> codes = new ArrayList<>();
        for (String token : tokens) {
            codes.add(Integer.parseInt(token.trim()));
        }
        return codes;
    }
}
