package dev.benpetrillo.elixir.types;

import java.util.List;

public final class LaudiolinSearchData {
    public static class Track {
        public String url;
        public String id;
    }

    public Track top;
    public List<Track> results;
}
