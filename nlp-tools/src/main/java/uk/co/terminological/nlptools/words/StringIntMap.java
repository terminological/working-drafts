package uk.co.terminological.nlptools.words;

@KolobokeMap
abstract class StringIntMap {
    static StringIntMap withExpectedSize(int expectedSize) {
        return new KolobokeStringIntMap(expectedSize);
    }
    abstract int put(String key, int value);

    abstract int getInt(String key);

    abstract boolean containsKey(String key);
    
}