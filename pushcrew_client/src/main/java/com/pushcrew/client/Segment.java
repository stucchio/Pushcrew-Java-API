package com.pushcrew.client;

class Segment {
    public final Long id;
    public final String name;

    public Segment(Long i, String n) {
        id = i;
        name = n;
    }

    public String toString() {
        return "Segment(" + id + ", " + name + ")";
    }
}
