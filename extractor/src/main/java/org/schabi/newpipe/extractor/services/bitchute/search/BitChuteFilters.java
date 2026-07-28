package org.schabi.newpipe.extractor.services.bitchute.search;

import org.schabi.newpipe.extractor.search.filter.Filter;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.search.filter.SearchFiltersBase;

public final class BitChuteFilters extends SearchFiltersBase {
    public static final String VIDEOS = "videos";

    public BitChuteFilters() {
        init();
        build();
    }

    @Override
    protected void init() {
        final int videos = builder.addFilterItem(
                new FilterItem(Filter.ITEM_IDENTIFIER_UNKNOWN, VIDEOS));
        defaultContentFilterId = videos;
        addContentFilter(builder.createSortGroup(null, true,
                new FilterItem[]{builder.getFilterForId(videos)}));
    }
}
