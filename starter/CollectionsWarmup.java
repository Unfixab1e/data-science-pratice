import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Optional bridge before T02: unrelated book examples, not dataset analysis.
 * Run from the repository root: java starter/CollectionsWarmup.java
 * The page counts are illustrative values for this exercise.
 */
public class CollectionsWarmup {
    public static void main(String[] args) {
        // TODO 1: Change the title. Predict which printed lines will change.
        String currentBookTitle = "The Small Garden";
        int pageCount = 96;
        boolean available = true;

        System.out.println("Title: " + currentBookTitle);
        System.out.println("Pages: " + pageCount);
        System.out.println("Available: " + available);

        List<String> readingList = new ArrayList<>();
        readingList.add(currentBookTitle);
        readingList.add("A Long Journey");
        // TODO 2: Add one more readingList.add(...) statement here.

        System.out.println("Reading-list titles:");
        for (String title : readingList) {
            System.out.println("  " + title);
        }

        Map<String, String> firstBook = new LinkedHashMap<>();
        firstBook.put("title", currentBookTitle);
        firstBook.put("pages", Integer.toString(pageCount));

        Map<String, String> secondBook = new LinkedHashMap<>();
        secondBook.put("title", "A Long Journey");
        secondBook.put("pages", "312");

        List<Map<String, String>> books = new ArrayList<>();
        books.add(firstBook);
        books.add(secondBook);

        System.out.println("Books matching the page filter:");
        for (Map<String, String> book : books) {
            String title = book.get("title");
            int pages = Integer.parseInt(book.get("pages"));
            if (isShortBook(pages)) {
                System.out.println("  " + title + " (" + pages + " pages)");
            }
        }
    }

    static boolean isShortBook(int pages) {
        // TODO 3: Change 250 to 400. Predict the extra matching book.
        return pages < 250;
    }
}
