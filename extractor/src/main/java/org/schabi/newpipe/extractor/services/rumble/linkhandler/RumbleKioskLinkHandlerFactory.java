package org.schabi.newpipe.extractor.services.rumble.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.rumble.RumbleService;

import java.util.List;

public final class RumbleKioskLinkHandlerFactory extends ListLinkHandlerFactory {
    public static final String LATEST = "Latest";
    public static final String POPULAR = "Popular";
    public static final String LIVE = "Live";
    private static final RumbleKioskLinkHandlerFactory INSTANCE =
            new RumbleKioskLinkHandlerFactory();

    public static RumbleKioskLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    private RumbleKioskLinkHandlerFactory() {
    }

    @Override
    public String getId(final String url) throws ParsingException {
        if (url.contains("/browse/live")) {
            return LIVE;
        }
        if (url.contains("sort=views")) {
            return POPULAR;
        }
        if (url.contains("/videos")) {
            return LATEST;
        }
        throw new ParsingException("Invalid Rumble kiosk URL");
    }

    @Override
    public String getUrl(final String id,
                         final List<FilterItem> contentFilter,
                         final List<FilterItem> sortFilter) throws ParsingException {
        switch (id) {
            case LIVE:
                return RumbleService.BASE_URL + "/browse/live";
            case POPULAR:
                return RumbleService.BASE_URL + "/videos?sort=views";
            case LATEST:
                return RumbleService.BASE_URL + "/videos";
            default:
                throw new ParsingException("Unknown Rumble kiosk: " + id);
        }
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        try {
            getId(url);
            return true;
        } catch (final ParsingException e) {
            return false;
        }
    }
}
