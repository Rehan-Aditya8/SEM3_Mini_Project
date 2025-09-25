package algorithms;

public class HuffmanNode {
    public char ch;
    public int freq;
    public HuffmanNode left, right;

    public HuffmanNode(char ch, int freq) {
        this.ch = ch;
        this.freq = freq;
    }

    public HuffmanNode(int freq, HuffmanNode left, HuffmanNode right) {
        this.ch = '\0';
        this.freq = freq;
        this.left = left;
        this.right = right;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }
}
