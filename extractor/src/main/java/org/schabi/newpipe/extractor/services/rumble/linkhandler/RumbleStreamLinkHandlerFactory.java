package org.schabi.newpipe.extractor.services.rumble.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.services.rumble.RumbleService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public final class RumbleStreamLinkHandlerFactory extends LinkHandlerFactory {
    private static final String EMBED_PREFIX = "embed:";
    private static final RumbleStreamLinkHandlerFactory INSTANCE =
            new RumbleStreamLinkHandlerFactory();

    public static RumbleStreamLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    private RumbleStreamLinkHandlerFactory() {
    }

    @Override
    public String getId(final String url) throws ParsingException {
        try {
            final URI uri = new URI(url);
            final String host = uri.getHost() == null
                    ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            if (!host.equals("rumble.com") && !host.equals("www.rumble.com")) {
                throw new ParsingException("Not a Rumble URL");
            }
            final String path = uri.getPath();
            if (path.startsWith("/embed/")) {
                final String value = path.substring("/embed/".length()).split("/")[0];
                final String[] parts = value.split("\\.");
                return EMBED_PREFIX + parts[parts.length - 1];
            }
            final String page = path.startsWith("/") ? path.substring(1) : path;
            if (page.toLowerCase(Locale.ROOT).startsWith("v")
                    && page.toLowerCase(Locale.ROOT).endsWith(".html")) {
                return page;
            }
            throw new ParsingException("Unsupported Rumble video URL");
        } catch (final URISyntaxException e) {
            throw new ParsingException("Invalid Rumble URL", e);
        }
    }

    @Override
    public String getUrl(final String id) {
        if (id.startsWith(EMBED_PREFIX)) {
            return RumbleService.BASE_URL + "/embed/" + id.substring(EMBED_PREFIX.length());
        }
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
