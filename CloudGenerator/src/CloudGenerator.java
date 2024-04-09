import java.util.Comparator;

import components.map.Map;
import components.map.Map.Pair;
import components.map.Map1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.sortingmachine.SortingMachine;
import components.sortingmachine.SortingMachine1L;

/**
 * Put a short phrase describing the program here.
 *
 * @author Ben Walls, Matt Chandran
 *
 */
public final class CloudGenerator {

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private CloudGenerator() {
    }

    /**
     * Return the first word or separator in the String starting at a defined
     * index. Separators include spaces, tabs, and punctuation.
     *
     * @param startingIndex
     *            First index on the string that is checked.
     * @param s
     *            String that represents one line of the input file.
     * @param seperators
     *            Set of strings that are defined as "separators", not to be
     *            counted as part of words.
     * @return first word or separator occurrence starting at the startingIndex.
     */
    private static String nextWordOrSeperator(int startingIndex, String s,
            Set<String> seperators) {
        StringBuilder sb = new StringBuilder();

        // get the character at the starting index
        sb.append(s.charAt(startingIndex));
        int index = startingIndex + 1;

        /*
         * if the first string is not a seperator, add following characters
         * until a seperator is found
         */
        if (!seperators.contains(String.valueOf(s.charAt(startingIndex)))) {
            while (index < s.length()
                    && !seperators.contains(String.valueOf(s.charAt(index)))) {
                sb.append(s.charAt(index));
                index++;
            }
        }

        return sb.toString();
    }

    /**
     * Compare {@code Integer}s in decreasing order.
     */
    public static class ValueComparator
            implements Comparator<Map.Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> obj1,
                Map.Pair<String, Integer> obj2) {
            return Integer.compare(obj2.value(), obj1.value());
        }
    }

    /**
     * Compare {@code String}s in alphabetical order, ignoring case.
     */
    public static class KeyComparator
            implements Comparator<Pair<String, Integer>> {
        @Override
        public int compare(Map.Pair<String, Integer> obj1,
                Map.Pair<String, Integer> obj2) {
            return obj1.key().compareToIgnoreCase(obj2.key());
        }
    }

    /**
     * Print HTML of a word cloud. The n most frequent words are sorted
     * alphabetically and displayed with a size corresponding to their frequency
     * in the input text file.
     *
     * @param wordSorter
     *            Pairs of words to their counts in an alphabetical sorting
     *            machine.
     * @param out
     *            Writer to the output text file.
     * @param fileName
     *            Name of the output file.
     * @param maxCount
     *            Count of the most frequent word in the input file.
     * @param minCount
     *            Count of the least frequent word in the sorting machine
     */
    private static void makeOutputFile(
            SortingMachine<Map.Pair<String, Integer>> wordSorter,
            SimpleWriter out, String fileName, int maxCount, int minCount) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Top " + wordSorter.size() + " words in " + fileName
                + "</title>");
        out.println(
                "<link href=\"https://web.cse.ohio-state.edu/software/2231/web-sw2/assignments/projects/tag-cloud-generator/data/tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println(
                "<link href=\"styles.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println("</head>");
        out.println("<body>");

        out.println("<h2>Top " + wordSorter.size() + " words in " + fileName
                + "</h2>");
        out.println("<hr>");
        out.println("<div class=\"cdiv\">");
        out.println("<p class=\"cbox\">");

        while (wordSorter.size() > 0) {
            Pair<String, Integer> pair = wordSorter.removeFirst();
            int fSize = (int) Math.ceil(11
                    + 37 * (pair.value() - minCount) / (maxCount - minCount));
            out.println("<span style=\"cursor:default\" class=\"f" + fSize
                    + "\" title=\"count: " + pair.value() + "\">" + pair.key()
                    + "</span>");
        }

        out.println("</p>");
        out.println("</div>");

        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        // get file names and initialize readers / writers
        out.println("enter an input file name: ");
        String inFileName = in.nextLine();
        SimpleReader fileReader = new SimpleReader1L(inFileName);
        out.println("enter an output file name: ");
        String outFileName = in.nextLine();
        SimpleWriter fileWriter = new SimpleWriter1L(outFileName);

        // get positive number of words to show in the cloud
        int nWords = 0;
        try {
            out.println("enter the amount of words you want shown: ");
            nWords = Integer.parseInt(in.nextLine());
            if (nWords < 0) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            out.println("Number of words cannot be negative");
        }

        // initialize some variables
        Map<String, Integer> wordsToCounts = new Map1L<>();
        Set<String> seperators = new Set1L<>();
        seperators.add(" ");
        seperators.add(".");
        seperators.add(",");
        seperators.add("-");
        seperators.add("\t");

        // iterate on each line until the stream ends
        while (!fileReader.atEOS()) {
            String line = fileReader.nextLine();
            int index = 0;
            while (index < line.length()) {
                String wordOrSeperator = nextWordOrSeperator(index, line,
                        seperators);

                if (!seperators.contains(wordOrSeperator)) {
                    wordOrSeperator = wordOrSeperator.toLowerCase();
                    if (!wordsToCounts.hasKey(wordOrSeperator)) {
                        // create new map pair if word doesn't exist
                        wordsToCounts.add(wordOrSeperator, 1);
                    } else {
                        // increment word counter if word exists
                        int wordCount = wordsToCounts.value(wordOrSeperator);
                        wordsToCounts.replaceValue(wordOrSeperator,
                                wordCount + 1);
                    }
                }
                // update the index
                index += wordOrSeperator.length();
            }
        }

        // set up sorting machines
        Comparator<Map.Pair<String, Integer>> valueOrder = new ValueComparator();
        Comparator<Map.Pair<String, Integer>> keyOrder = new KeyComparator();
        SortingMachine<Map.Pair<String, Integer>> countSorter = new SortingMachine1L<Map.Pair<String, Integer>>(
                valueOrder);
        SortingMachine<Map.Pair<String, Integer>> wordSorter = new SortingMachine1L<Map.Pair<String, Integer>>(
                keyOrder);
        for (Map.Pair<String, Integer> pair : wordsToCounts) {
            countSorter.add(pair);
        }
        countSorter.changeToExtractionMode();

        // hold the minimum and maximum word count for later use
        Map.Pair<String, Integer> maxPair = countSorter.removeFirst();
        int maxCount = maxPair.value();
        int minCount = 0;
        wordSorter.add(maxPair);

        // remove n words to be included in the cloud
        for (int i = 0; i < Math.min(nWords, wordsToCounts.size()) - 1; i++) {
            Map.Pair<String, Integer> pair = countSorter.removeFirst();
            wordSorter.add(pair);
            minCount = pair.value();
        }

        // print HTML text to output file
        wordSorter.changeToExtractionMode();
        makeOutputFile(wordSorter, fileWriter, inFileName, maxCount, minCount);

        fileReader.close();
        fileWriter.close();
        in.close();
        out.close();

    }
}
