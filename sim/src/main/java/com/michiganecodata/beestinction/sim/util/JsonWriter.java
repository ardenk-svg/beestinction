package com.michiganecodata.beestinction.sim.util;

import java.util.ArrayList;
import java.util.List;

/**
 * OVERVIEW:      A minimal, hand-written JSON builder (objects, arrays, strings with escaping, numbers,
 *                booleans, null). Used for game logs and summaries instead of reflection-based serialization
 *                (CLAUDE.md A.5 rule 6).
 *                Example: new JsonWriter().beginObject().name("seed").value(42).endObject().toString()
 *                produces {"seed":42}.
 * INVARIANTS:    The output is always a prefix of valid JSON; once every begin has a matching end, toString()
 *                is valid JSON. Inside an object, every value is preceded by exactly one name().
 *                Misuse (a value without a name in an object, unbalanced ends) throws IllegalStateException.
 * COLLABORATORS: GameLog, RunSummary, Order.writeParams.
 */
public final class JsonWriter {
    private static final int IN_OBJECT = 1;
    private static final int IN_ARRAY = 2;

    private final StringBuilder out = new StringBuilder();
    /** One entry per open container: IN_OBJECT or IN_ARRAY. */
    private final List<Integer> stack = new ArrayList<>();
    /** One entry per open container: whether it already holds an element (so the next one needs a comma). */
    private final List<Boolean> hasElements = new ArrayList<>();
    private boolean nameWritten;
    private boolean rootWritten;

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Opens a JSON object as the next value. Returns this.
     */
    public JsonWriter beginObject() {
        beforeValue();
        out.append('{');
        push(IN_OBJECT);
        return this;
    }

    /**
     * REQUIRES: the innermost open container is an object, and no name() is waiting for its value
     * MODIFIES: this
     * EFFECTS:  Closes the innermost object. Returns this.
     */
    public JsonWriter endObject() {
        pop(IN_OBJECT);
        out.append('}');
        return this;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Opens a JSON array as the next value. Returns this.
     */
    public JsonWriter beginArray() {
        beforeValue();
        out.append('[');
        push(IN_ARRAY);
        return this;
    }

    /**
     * REQUIRES: the innermost open container is an array
     * MODIFIES: this
     * EFFECTS:  Closes the innermost array. Returns this.
     */
    public JsonWriter endArray() {
        pop(IN_ARRAY);
        out.append(']');
        return this;
    }

    /**
     * REQUIRES: the innermost open container is an object, name != null, and the previous name has a value
     * MODIFIES: this
     * EFFECTS:  Writes the member name for the next value. Returns this.
     */
    public JsonWriter name(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (stack.isEmpty() || top() != IN_OBJECT) {
            throw new IllegalStateException("name() is only allowed inside an object");
        }
        if (nameWritten) {
            throw new IllegalStateException("name() called twice without a value");
        }
        if (hasElements.get(hasElements.size() - 1)) {
            out.append(',');
        }
        hasElements.set(hasElements.size() - 1, Boolean.TRUE);
        writeString(name);
        out.append(':');
        nameWritten = true;
        return this;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Writes a string value with JSON escaping, or null if value is null. Returns this.
     */
    public JsonWriter value(String value) {
        if (value == null) {
            return nullValue();
        }
        beforeValue();
        writeString(value);
        return this;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Writes an integer number value. Returns this.
     */
    public JsonWriter value(long value) {
        beforeValue();
        out.append(value);
        return this;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Writes a boolean value. Returns this.
     */
    public JsonWriter value(boolean value) {
        beforeValue();
        out.append(value ? "true" : "false");
        return this;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: this
     * EFFECTS:  Writes a JSON null. Returns this.
     */
    public JsonWriter nullValue() {
        beforeValue();
        out.append("null");
        return this;
    }

    /**
     * REQUIRES: json is a complete, valid JSON value (for example produced by another JsonWriter)
     * MODIFIES: this
     * EFFECTS:  Writes json verbatim as the next value. Returns this.
     */
    public JsonWriter rawValue(String json) {
        if (json == null) {
            return nullValue();
        }
        beforeValue();
        out.append(json);
        return this;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns true if every opened object and array has been closed.
     */
    public boolean isComplete() {
        return stack.isEmpty() && rootWritten && !nameWritten;
    }

    /**
     * REQUIRES: nothing
     * MODIFIES: nothing
     * EFFECTS:  Returns the JSON written so far.
     */
    @Override
    public String toString() {
        return out.toString();
    }

    private void beforeValue() {
        if (stack.isEmpty()) {
            if (rootWritten) {
                throw new IllegalStateException("only one root value is allowed");
            }
            rootWritten = true;
            return;
        }
        if (top() == IN_OBJECT) {
            if (!nameWritten) {
                throw new IllegalStateException("a value inside an object needs a name() first");
            }
            nameWritten = false;
            return;
        }
        // In an array.
        if (hasElements.get(hasElements.size() - 1)) {
            out.append(',');
        }
        hasElements.set(hasElements.size() - 1, Boolean.TRUE);
    }

    private void push(int kind) {
        stack.add(kind);
        hasElements.add(Boolean.FALSE);
    }

    private void pop(int kind) {
        if (stack.isEmpty() || top() != kind) {
            throw new IllegalStateException("unbalanced " + (kind == IN_OBJECT ? "endObject()" : "endArray()"));
        }
        if (nameWritten) {
            throw new IllegalStateException("name() is missing its value");
        }
        stack.remove(stack.size() - 1);
        hasElements.remove(hasElements.size() - 1);
    }

    private int top() {
        return stack.get(stack.size() - 1);
    }

    private void writeString(String s) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                case '\b':
                    out.append("\\b");
                    break;
                case '\f':
                    out.append("\\f");
                    break;
                default:
                    if (c < 0x20 || c == ' ' || c == ' ') {
                        out.append("\\u");
                        String hex = Integer.toHexString(c);
                        for (int pad = hex.length(); pad < 4; pad++) {
                            out.append('0');
                        }
                        out.append(hex);
                    } else {
                        out.append(c);
                    }
            }
        }
        out.append('"');
    }
}
