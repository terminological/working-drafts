package uk.co.terminological.nlptools.words;

import com.koloboke.compile.KolobokeMap;

@KolobokeMap
abstract class IntIntMap {
    
	static IntIntMap withExpectedSize(int expectedSize) {
        return new KolobokeIntIntMap(expectedSize);
    }
    
    abstract void justPut(int key, int value);

    abstract int getInt(int key);

    
}
