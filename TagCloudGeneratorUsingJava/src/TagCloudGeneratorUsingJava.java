import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

/**
 * Displays a cloud of the words contained in a text file. Allows the user to
 * specify the input text file and number of words shown on the cloud. Font size
 * of the words is proportional to their frequency in the text file.
 *
 * @author Ben Walls, Matt Chandran
 *
 */
public final class TagCloudGeneratorUsingJava {

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private TagCloudGeneratorUsingJava() {
    }

    /**
     * Maximum font size.
     */
    static final int FONT_MAX = 37;
    /**
     * Minimum font size.
     */
    static final int FONT_MIN = 11;
    /**
     * String of separators.
     */
    static final String SEPARATORS = " \t,.-:;/\"!?_@#$%&*[]()";

    /**
     * Compare {@code Integer}s values of the map in decreasing order.
     */
    private static class ValueComparator
            implements Comparator<Map.Entry<String, Integer>> {

        @Override
        public int compare(Map.Entry<String, Integer> pair1,
                Map.Entry<String, Integer> pair2) {

            int returnVal = Integer.compare(pair2.getValue(), pair1.getValue());
            if (returnVal == 0) {
                returnVal = pair1.getKey().compareTo(pair2.getKey());
            }

            return returnVal;
        }
    }

    /**
     * Return the first word or separator in the String starting at a defined
     * index. Separators include spaces, tabs, and punctuation.
     *
     * @param startingIndex
     *            First index on the string that is checked.
     * @param s
     *            String that represents one line of the input file.
     * @return first word or separator occurrence starting at the startingIndex.
     */
    private static String nextWordOrSeperator(int startingIndex, String s) {
        StringBuilder sb = new StringBuilder();

        // get the character at the starting index
        sb.append(s.charAt(startingIndex));
        int index = startingIndex + 1;

        /*
         * if the first string is not a separator, add following characters
         * until a separator is found
         */
        if (!SEPARATORS.contains(Character.toString(s.charAt(startingIndex)))) {
            while (index < s.length() && !SEPARATORS
                    .contains(Character.toString(s.charAt(index)))) {
                sb.append(s.charAt(index));
                index++;
            }
        }

        return sb.toString();
    }

    /**
     * counts the frequency of each unique word in the input file.
     *
     * @param fileReader
     *            input stream of the text file.
     * @return map of words to their count
     */
    private static Map<String, Integer> getWordCounts(
            BufferedReader fileReader) {
        Map<String, Integer> wordsToCounts = new HashMap<String, Integer>();

        // iterate on each line until the stream ends
        String line = null;
        try {
            line = fileReader.readLine();
        } catch (IOException e) {
            System.out.println("error reading the input stream");
        }
        while (line != null) {
            try {
                line = fileReader.readLine();
            } catch (IOException e) {
                System.out.println("error reading the input stream");
            }
            int index = 0;
            while (line != null && index < line.length()) {
                String wordOrSeperator = nextWordOrSeperator(index, line);

                if (!SEPARATORS.contains(wordOrSeperator)) {
                    wordOrSeperator = wordOrSeperator.toLowerCase();
                    if (!wordsToCounts.containsKey(wordOrSeperator)) {
                        // create new map pair if word doesn't exist
                        wordsToCounts.put(wordOrSeperator, 1);
                    } else {
                        // increment word counter if word exists
                        int wordCount = wordsToCounts.get(wordOrSeperator);
                        wordsToCounts.replace(wordOrSeperator, wordCount + 1);
                    }
                }
                // update the index
                index += wordOrSeperator.length();
            }
        }

        return wordsToCounts;
    }

    /**
     * Sort the map of words to counts into a list with the entries of the top n
     * values. Place those entries into a map that sorts alphabetically by key.
     *
     * @param wordsToCountsSorted
     *            Map of the top entries in alphabetical order by key.
     * @param wordsToCounts
     *            Unique words found in the input file mapped to their
     *            frequency.
     * @param nWords
     *            Number of words to sort.
     * @return List of the map entries in order of their integer values.
     */
    private static List<String> sortWordCount(
            Map<String, Integer> wordsToCountsSorted,
            Map<String, Integer> wordsToCounts, int nWords) {

        // Add entries to a priority queue
        PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(
                new ValueComparator());
        Set<Map.Entry<String, Integer>> entries = wordsToCounts.entrySet();
        for (Map.Entry<String, Integer> entry : entries) {
            pq.add(entry);
        }

        // create a list to hold the chosen words in order of their counts
        List<String> topWords = new LinkedList<String>();

        // add chosen entries to an alphabetically sorted tree map
        for (int i = 0; i < Math.min(nWords, wordsToCounts.size()); i++) {
            Map.Entry<String, Integer> entry = pq.remove();
            wordsToCountsSorted.put(entry.getKey(), entry.getValue());
            topWords.add(entry.getKey());
        }

        return topWords;
    }

    /**
     * Print HTML of a word cloud. The n most frequent words are sorted
     * alphabetically and displayed with a size corresponding to their frequency
     * in the input text file.
     *
     * @param wordsToCounts
     *            Top frequency words mapped to their frequency
     * @param out
     *            Writer to the output text file.
     * @param fileName
     *            Name of the output file.
     * @param maxCount
     *            Count of the most frequent word in the input file.
     * @param minCount
     *            Count of the least frequent word in the sorting machine
     */
    private static void makeOutputFile(Map<String, Integer> wordsToCounts,
            PrintWriter out, String fileName, int maxCount, int minCount) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Top " + wordsToCounts.size() + " words in "
                + fileName + "</title>");
        out.println(
                "<link href=\"https://web.cse.ohio-state.edu/software/2231/web-sw2/assignments/projects/tag-cloud-generator/data/tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println(
                "<link href=\"styles.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println("</head>");
        out.println("<body>");

        out.println("<h2>Top " + wordsToCounts.size() + " words in " + fileName
                + "</h2>");
        out.println("<hr>");
        out.println("<div class=\"cdiv\">");
        out.println("<p class=\"cbox\">");

        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(
                wordsToCounts.entrySet());
        if (minCount != maxCount) {
            for (int i = 0; i < entryList.size(); i++) {
                Map.Entry<String, Integer> entry = entryList.get(i);
                int fSize = (int) Math.ceil(
                        FONT_MIN + FONT_MAX * (entry.getValue() - minCount)
                                / (maxCount - minCount));
                out.println("<span style=\"cursor:default\" class=\"f" + fSize
                        + "\" title=\"count: " + entry.getValue() + "\">"
                        + entry.getKey() + "</span>");
            }
        } else {
            for (int i = 0; i < entryList.size(); i++) {
                Map.Entry<String, Integer> entry = entryList.get(i);
                int fSize = (FONT_MAX + FONT_MIN) / 2;
                out.println("<span style=\"cursor:default\" class=\"f" + fSize
                        + "\" title=\"count: " + entry.getValue() + "\">"
                        + entry.getKey() + "</span>");
            }
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
        // acess input file
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));
        System.out.println("Enter an input file name: ");
        String inFileName = "";
        BufferedReader fileReader = null;
        try {
            inFileName = in.readLine();
            fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(inFileName)));
        } catch (IOException e) {
            System.out.println("Error opening the input file " + e);
        }

        // create output file and printer
        System.out.println("Enter an output file name: ");
        PrintWriter fileWriter = null;
        try {
            String outFileName = in.readLine();
            fileWriter = new PrintWriter(
                    new BufferedWriter(new FileWriter(outFileName)));
        } catch (IOException e) {
            System.out.println("Error opening the output printer " + e);
        }

        // get positive number of words to show in the cloud
        int nWords = 0;
        System.out.println("Enter the amount of words to be in the cloud: ");
        try {
            nWords = Integer.parseInt(in.readLine());
        } catch (IOException e) {
            System.err.println("Error reading system input " + e);
            return;
        }

        // get the counts of all unique words in the file
        Map<String, Integer> wordsToCounts = getWordCounts(fileReader);

        // get top n words based on their counts and sort alphabetically
        Map<String, Integer> wordsToCountsSorted = new TreeMap<String, Integer>();
        List<String> topWords = sortWordCount(wordsToCountsSorted,
                wordsToCounts, nWords);

        // hold the minimum and maximum word count for later use
        int maxCount = 0;
        int minCount = 0;
        if (topWords.size() > 0) {
            maxCount = wordsToCountsSorted.get(topWords.get(0));
            minCount = wordsToCountsSorted
                    .get(topWords.get(topWords.size() - 1));
        }
        if (topWords.size() >= nWords) {
            minCount = wordsToCountsSorted
                    .get(topWords.get(topWords.size() - 1));
        }

        // print HTML text to output file
        makeOutputFile(wordsToCountsSorted, fileWriter, inFileName, maxCount,
                minCount);

        try {
            fileReader.close();
            fileWriter.close();
            in.close();
        } catch (IOException e) {
            System.out.println("Error closing the input/output streams " + e);
        }
    }
}
