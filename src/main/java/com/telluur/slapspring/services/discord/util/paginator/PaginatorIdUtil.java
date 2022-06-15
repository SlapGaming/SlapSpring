package com.telluur.slapspring.services.discord.util.paginator;

import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaginatorIdUtil {

    private static final String PAGINATOR_PREFIX = "PAGINATOR";
    private static final String ID_REGEX_PART = "[a-zA-Z]";
    private static final String INDEX_REGEX_PART = "[0-9]+";
    private static final Pattern ID_REGEX = Pattern.compile(String.format("%s:%s:%s", PAGINATOR_PREFIX, ID_REGEX_PART, INDEX_REGEX_PART));
    private static final Pattern CAPTURE_ID_REGEX = Pattern.compile(String.format("%s:(%s):%s", PAGINATOR_PREFIX, ID_REGEX_PART, INDEX_REGEX_PART));
    private static final Pattern CAPTURE_INDEX_REGEX = Pattern.compile(String.format("%s:%s:(%s)", PAGINATOR_PREFIX, ID_REGEX_PART, INDEX_REGEX_PART));


    public static String buildButtonId(@Nonnull String paginatorID, int index) {
        return String.format("%s:%s:%d", PAGINATOR_PREFIX, paginatorID, index);
    }


    @Nullable
    public static String pasrsePaginatorIdFromButtonId(String buttonId) {
        final Matcher matcher = CAPTURE_ID_REGEX.matcher(buttonId);
        if (matcher.find()) {
            return matcher.group(1); //regex groups are 1 indexed
        }
    }


    public static int parseIndexFromButtonId(String buttonId) {
        final Matcher matcher = CAPTURE_INDEX_REGEX.matcher(buttonId);
        if (matcher.find()) {
            String indexString = matcher.group(1); //regex groups are 1 indexed
            try {
                return Integer.parseInt(indexString);
            } catch (NumberFormatException ignored) {
                //Simply return 0 index.
            }
        }
        return 0;
    }


    public static boolean isValidPaginatorButtonId(String buttonId) {
        final Matcher matcher = ID_REGEX.matcher(buttonId);
        return matcher.find();
    }
}
