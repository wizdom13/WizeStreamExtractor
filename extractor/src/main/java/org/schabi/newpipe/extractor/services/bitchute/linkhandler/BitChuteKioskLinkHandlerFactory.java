package org.schabi.newpipe.extractor.services.bitchute.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.bitchute.BitChuteService;

import java.util.List;

public final class BitChuteKioskLinkHandlerFactory extends ListLinkHandlerFactory {
    public static final String TRENDING = "Trending";
    public static final String POPULAR = "Popular";
    public static final String RECENT = "Recent";
    private static final BitChuteKioskLinkHandlerFactory INSTANCE =
            new BitChuteKioskLinkHandlerFactory();

    public static BitChuteKioskLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    private BitChuteKioskLinkHandlerFactory() {
    }

    @Override
    public String getId(final String url) throws ParsingException {
        final int marker = url.indexOf("#");
        if (marker < 0 || marker == url.length() - 1) {
            throw new ParsingException("Invalid BitChute kiosk URL");
        }
        return url.substring(marker + 1);
    }

    @Override
    public String getUrl(final String id,
                         final List<FilterItem> contentFilters,
                         final List<FilterItem> sortFilter) {
        return BitChuteService.BASE_URL + "/#" + id;
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        try {
            return getId(url) != null;
        } catch (final ParsingException e) {
            return false;
        }
    }
}
