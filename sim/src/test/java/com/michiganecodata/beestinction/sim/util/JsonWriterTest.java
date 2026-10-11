package com.michiganecodata.beestinction.sim.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonWriterTest {

    @Test
    void writesNestedObjectsAndArrays() {
        JsonWriter w = new JsonWriter();
        w.beginObject()
                .name("a").value(1)
                .name("b").beginArray().value("x").value(true).nullValue().beginObject().endObject().endArray()
                .name("c").beginObject().name("d").value(-5L).endObject()
                .endObject();
        assertEquals("{\"a\":1,\"b\":[\"x\",true,null,{}],\"c\":{\"d\":-5}}", w.toString());
        assertTrue(w.isComplete());
    }

    @Test
    void escapesStrings() {
        JsonWriter w = new JsonWriter();
        w.value("quote\" backslash\\ newline\n tab\t ctrl\u0001");
        assertEquals("\"quote\\\" backslash\\\\ newline\\n tab\\t ctrl\\u0001\"", w.toString());
    }

    @Test
    void nullStringIsWrittenAsNull() {
        JsonWriter w = new JsonWriter();
        w.beginObject().name("s").value((String) null).endObject();
        assertEquals("{\"s\":null}", w.toString());
    }

    @Test
    void rawValueIsEmbeddedVerbatim() {
        JsonWriter w = new JsonWriter();
        w.beginArray().rawValue("{\"k\":1}").value(2).endArray();
        assertEquals("[{\"k\":1},2]", w.toString());
    }

    @Test
    void rejectsMisuse() {
        assertThrows(IllegalStateException.class, () -> new JsonWriter().beginObject().value(1));
        assertThrows(IllegalStateException.class, () -> new JsonWriter().beginArray().endObject());
        assertThrows(IllegalStateException.class, () -> new JsonWriter().name("x"));
        assertThrows(IllegalStateException.class, () -> new JsonWriter().value(1).value(2));
        assertThrows(IllegalStateException.class, () -> new JsonWriter().beginObject().name("a").endObject());
        assertFalse(new JsonWriter().beginObject().isComplete());
    }
}
