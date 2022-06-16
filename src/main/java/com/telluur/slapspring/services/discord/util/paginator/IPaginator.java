package com.telluur.slapspring.services.discord.util.paginator;


import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.lang.NonNull;

/**
 * This interface houses the required methods for constructing a pageable MessageEmbed.
 * Implementing classes are automatically registered and paged by {@link PaginatorService}
 */
public interface IPaginator {

    /**
     * User provided ID identifying the paginator
     * <p>
     * <ul>
     *     <li>Must be unique</li>
     *     <li>Cannot contain a colon (:)</li>
     *     <li>Is limited in size (inherited from discord's limitation on button IDs)</li>
     * </ul>
     * <p>
     * Warning: these prerequisites are unchecked!
     *
     * @return paginator ID
     */
    @NonNull
    String getPaginatorId();

    /**
     * Returns the total number of pages.
     * This is used to build the next and last buttons, and their active state.
     *
     * @return number of pages
     */
    int getNumberOfTotalPages();

    /**
     * Return a MessageEmbed with the content expected for the passed index number.
     * Logic for encapsulating how many pages there should be,
     * and how many items per page is left to the implementing class.
     *
     * @param index 0 indexed number
     * @return Page content
     */
    @NonNull
    MessageEmbed getPage(int index);
}
