package com.telluur.slapspring.abstractions.discord.paginator;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

final class PaginatorButtonUtil {

    static final String PAGINATOR_PREFIX = "PAGINATOR";
    static final String PAGINATOR_FIRST_PAGE_INDEX = "FIRST";
    static final String PAGINATOR_LAST_PAGE_INDEX = "LAST";
    static final String ANY_EXCLUDING_COLON_REGEX = "[^:]+?";
    static final String INTEGER_REGEX = "\\d+?";

    static final Pattern FULL_BUTTON_REGEX = Pattern.compile(String.format("%s:(%s):(%s):(%s|%s|%s)",
            PAGINATOR_PREFIX, //paginator indicator
            ANY_EXCLUDING_COLON_REGEX, //[id]
            ANY_EXCLUDING_COLON_REGEX, //[data]
            INTEGER_REGEX, //[index]
            PAGINATOR_FIRST_PAGE_INDEX, //[index]
            PAGINATOR_LAST_PAGE_INDEX)); //[index]


    /*
    Utility methods
     */

    /**
     * Builds a button for a paginator with target index
     *
     * @param paginatorId corresponding paginator
     * @param index       the requested index target
     * @return formatted button id
     */
    public static String buildButton(@Nonnull String paginatorId, @Nonnull String data, int index) {
        return String.format("%s:%s:%s:%d", PAGINATOR_PREFIX, paginatorId, data, index);
    }

    /**
     * Builds a button for a paginator with target index set to first
     *
     * @param paginatorId corresponding paginator
     * @return formatted button id
     */
    public static String buildFirstButton(@Nonnull String paginatorId, @Nonnull String data) {
        return String.format("%s:%s:%s:%s", PAGINATOR_PREFIX, paginatorId, data, PAGINATOR_FIRST_PAGE_INDEX);
    }

    /**
     * Builds a button for a paginator with target index set to last
     *
     * @param paginatorId corresponding paginator
     * @return formatted button id
     */
    public static String buildLastButton(@Nonnull String paginatorId, @Nonnull String data) {
        return String.format("%s:%s:%s:%s", PAGINATOR_PREFIX, paginatorId, data, PAGINATOR_LAST_PAGE_INDEX);
    }
}
