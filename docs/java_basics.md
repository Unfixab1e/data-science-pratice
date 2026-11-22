# A small Java bridge before T02

Use this optional 30-60 minute exercise if `List`, `Map`, loops or methods are unfamiliar. If you already know them, continue with T02. The example uses an invented book collection, not the project dataset. Its page counts are illustrative. You are learning the syntax needed to write your own analysis later.

## 1. Run before changing

From the repository root, with JDK 21:

```text
java starter/CollectionsWarmup.java
```

The program prints one book's properties, two reading-list titles and one book matching a page filter. Read the code from the start of `main`. Do not edit the supplied CSV parser in `Practice.java`.

## 2. Values have types

`String currentBookTitle` holds text. String literals are between double quotes. `int pageCount` holds a whole number. `boolean available` holds `true` or `false`.

The number `96` and the text `"96"` have different types. Java can compare two numbers numerically, but text must first be interpreted as a number. `Integer.toString(pageCount)` produces text; `Integer.parseInt("312")` produces an integer. This warm-up has known valid values. The project will later teach what to do when supplied numbers are missing or invalid.

`System.out.println(...)` prints a line. Inside a text expression, `+` joins the pieces into a message. Printing a value is a useful way to see what your program currently knows.

## 3. Lists hold several items

`List<String>` means a list whose items are strings. `new ArrayList<>()` creates a list. `readingList.add(...)` appends an item.

The ordinary `for (String title : readingList)` loop reads one item at a time. On each turn, `title` refers to that item. The statements between the following braces run once for it. This loop has no special knowledge of books; the variable name makes your intention readable.

## 4. Maps connect keys and values

`Map<String, String>` connects text keys to text values. Think of `firstBook` as one row with named fields. `put("title", currentBookTitle)` stores a title, and `get("title")` retrieves it. Key spelling matters: `"title"` and `"Title"` are different keys.

`List<Map<String, String>>` means a list of those row-like maps. Read the type from the outside inward: a list, containing maps, with string keys and string values. The `books` loop selects one map at a time, retrieves its fields, and uses them.

The two lists are separate. Adding a title to `readingList` does not create a detailed record in `books`. Programs change the collection you actually tell them to change.

## 5. Conditions and a method

`if (isShortBook(pages))` prints a book only when that method returns `true`. The method receives one integer, evaluates `pages < 250`, and returns a boolean. The filter is an invented rule for this example, not a definition of a short book.

The method lives inside the class but outside `main`. Its `static` modifier lets `main` call it directly here. Keep early helper methods inside your class; you do not need additional files or classes for this warm-up.

## 6. Three tiny changes

For each change, predict the output, edit one thing, save and run again:

1. Change `currentBookTitle`. Which printed lines reuse that variable?
2. Add a third title with `readingList.add(...)`. Which list changes?
3. Change the method's filter from `250` to `400`. Which extra book now matches?

If Java reports a compile error, read the first message and line number. Check quotes, semicolons, braces and variable spelling. Restore the last working edit if necessary, then try one smaller change. Record the command, prediction and result in your learning journal.

You are ready for T02 when you can point to one string, one number, one map lookup, one loop and the method call, and explain each in your own words. Use the same concepts on the project data; this warm-up does not contain the analysis solution.
