package org.schabi.newpipe.extractor.services.bitchute.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.Filter;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.bitchute.search.BitChuteFilters;

import java.util.List;

public final class BitChuteSearchQueryHandlerFactory extends SearchQueryHandlerFactory {
    private static final BitChuteSearchQueryHandlerFactory INSTANCE =
            new BitChuteSearchQueryHandlerFactory();
    private final BitChuteFilters filters = new BitChuteFilters();

    public static BitChuteSearchQueryHandlerFactory getInstance() {
        return INSTANCE;
    }

    private BitChuteSearchQueryHandlerFactory() {
    }

    @Override
    public String getUrl(final String query,
                         final List<FilterItem> contentFilter,
                         final List<FilterItem> sortFilter) throws ParsingException {
        return "https://api.bitchute.com/api/beta/search/videos";
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
