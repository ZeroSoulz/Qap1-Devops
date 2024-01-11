package com.keyin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SuggestionEngineTest {
    private SuggestionsDatabase wordSuggestionDB;

    private Stream<String> wordEdits(final String word) {
        Stream<String> deletes = IntStream.range(0, word.length()).mapToObj((i) -> word.substring(0, i) + word.substring(i + 1));
        Stream<String> replaces = IntStream.range(0, word.length()).mapToObj((i) -> i).flatMap((i) -> "abcdefghijklmnopqrstuvwxyz".chars().mapToObj((c) ->
                word.substring(0, i) + (char) c + word.substring(i + 1)));
        Stream<String> inserts = IntStream.range(0, word.length() + 1).mapToObj((i) -> i).flatMap((i) -> "abcdefghijklmnopqrstuvwxyz".chars().mapToObj((c) ->
                word.substring(0, i) + (char) c + word.substring(i)));
        Stream<String> transposes = IntStream.range(0, word.length() - 1).mapToObj((i) -> word.substring(0, i) + word.substring(i + 1, i + 2) + word.charAt(i) + word.substring(i + 2));
        return Stream.of(deletes, replaces, inserts, transposes).flatMap((x) -> x);
    }


    private Stream<String> known(Stream<String> words) {
        return words.filter((word) -> getWordSuggestionDB().containsKey(word));
    }


    public void loadDictionaryData(Path dictionaryFile) throws IOException {
        Stream.of(new String(Files.readAllBytes(dictionaryFile)).toLowerCase().split("\\n")).forEach((word) -> {
            getWordSuggestionDB().compute(word, (k, v) -> v == null ? 1 : v + 1);
        });
    }


    private <V> Map<String, Integer> getWordSuggestionDB() {
        return getWordSuggestionDB();
    }


    public String generateSuggestions(String word) {
        if (getWordSuggestionDBTest().containsKey(word)) {
            return "";
        }

        Stream<String> e1 = known(wordEdits(word));
        Stream<String> e2 = known(wordEdits(word).map((w2) -> wordEdits(w2)).flatMap((x) -> x));

        Stream<String> suggestions = Stream.concat(e1, e2);

        Map<String, Long> collectedSuggestions = suggestions
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return collectedSuggestions.keySet().stream()
                .sorted(Comparator
                        .comparing(collectedSuggestions::get)
                        .reversed()
                        .thenComparing(Collator.getInstance()))
                .limit(10) // limit to top 10 suggestions to keep list consumable
                .collect(Collectors.joining("\n"));
    }

    public Map<String, Integer> getWordSuggestionDBTest() {
        if (wordSuggestionDB == null) {
            wordSuggestionDB = new SuggestionsDatabase();
        }

        return wordSuggestionDB.getWordMap();
    }

    public void setWordSuggestionDB(SuggestionsDatabase wordSuggestionDB) {
        this.wordSuggestionDB = wordSuggestionDB;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("USAGE: " + SuggestionEngine.class.getName() + " <word to generateSuggestions>");
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("?")) {
            System.out.println("USAGE: " + SuggestionEngine.class.getName() + " <word to generateSuggestions>");
            System.out.println("Output: A list of suggestions OR empty string if word is generateSuggestions");
        }

        SuggestionEngine suggestionEngine = new SuggestionEngine();
        suggestionEngine.loadDictionaryData(Paths.get(ClassLoader.getSystemResource("words.txt").getPath()));

        System.out.println(suggestionEngine.generateSuggestions(args[0]));
    }
}
