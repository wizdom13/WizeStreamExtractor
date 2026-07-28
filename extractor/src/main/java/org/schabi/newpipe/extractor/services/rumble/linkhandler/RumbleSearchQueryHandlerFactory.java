package org.schabi.newpipe.extractor.services.rumble.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.Filter;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.rumble.RumbleService;
import org.schabi.newpipe.extractor.services.rumble.search.RumbleFilters;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class RumbleSearchQueryHandlerFactory extends SearchQueryHandlerFactory {
    private static final RumbleSearchQueryHandlerFactory INSTANCE =
            new RumbleSearchQueryHandlerFactory();
    private final RumbleFilters filters = new RumbleFilters();

    public static RumbleSearchQueryHandlerFactory getInstance() {
        return INSTANCE;
    }

    private RumbleSearchQueryHandlerFactory() {
    }

    @Override
    public String getUrl(final String query,
                         final List<FilterItem> contentFilter,
                         final List<FilterItem> sortFilter) throws ParsingException {
        return RumbleService.BASE_URL + "/search/video?q="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }

    @Override
    public Filter getAvailableContentFilter() {
        return filters.getContentFilters();
    }

    @Override
    public FilterItem getFilterItem(final int filterId) {
        return filters.getFilterItem(filterId);
    }
}
