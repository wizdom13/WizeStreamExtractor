package org.schabi.newpipe.extractor.services.rumble.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.rumble.RumbleService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

public final class RumbleChannelLinkHandlerFactory extends ListLinkHandlerFactory {
    private static final RumbleChannelLinkHandlerFactory INSTANCE =
            new RumbleChannelLinkHandlerFactory();

    public static RumbleChannelLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    private RumbleChannelLinkHandlerFactory() {
    }

    @Override
    public String getId(final String url) throws ParsingException {
        try {
            final URI uri = new URI(url);
            final String host = uri.getHost() == null
                    ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            final String path = uri.getPath();
            if ((!host.equals("rumble.com") && !host.equals("www.rumble.com"))
                    || (!path.startsWith("/c/") && !path.startsWith("/user/"))) {
                throw new ParsingException("Unsupported Rumble channel URL");
            }
            final String[] pieces = path.substring(1).split("/");
            if (pieces.length < 2 || pieces[1].isEmpty()) {
                throw new ParsingException("Rumble channel ID is missing");
            }
            return pieces[0] + "/" + pieces[1];
        } catch (final URISyntaxException e) {
            throw new ParsingException("Invalid Rumble channel URL", e);
        }
    }

    @Override
    public String getUrl(final String id,
                         final List<FilterItem> contentFilter,
                         final List<FilterItem> sortFilter) {
        return RumbleService.BASE_URL + "/" + id;
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
