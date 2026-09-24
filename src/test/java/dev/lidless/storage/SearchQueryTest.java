package dev.lidless.storage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class SearchQueryTest {
    @Test
    void blankQueryIsEmpty() {
        assertTrue(SearchQuery.parse("   ").isEmpty());
        assertTrue(SearchQuery.parse("@").isEmpty());
    }

    @Test
    void everyWordMustMatchSomeText() {
        SearchQuery query = SearchQuery.parse("iron PICK");
        assertTrue(query.matches("minecraft", List.of("Iron Pickaxe")));
        assertFalse(query.matches("minecraft", List.of("Iron Ingot")));
        assertTrue(query.matches("minecraft", List.of("Enchanted Book", "Iron", "pickaxe")));
    }

    @Test
    void modFilterChecksNamespacePrefix() {
        SearchQuery query = SearchQuery.parse("@create gear");
        assertTrue(query.matches("create", List.of("Cogwheel", "large_gear")));
        assertFalse(query.matches("minecraft", List.of("gear")));
        assertTrue(SearchQuery.parse("@mine").matches("minecraft", List.of("Stone")));
    }
}
