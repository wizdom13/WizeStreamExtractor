package org.schabi.newpipe.extractor.services.bitchute.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bitchute.BitChuteService;
import org.schabi.newpipe.extractor.utils.Parser;

public final class BitChuteStreamLinkHandlerFactory extends LinkHandlerFactory {
    private static final String PATTERN =
            "(?i)(?:www\\.|old\\.)?bitchute\\.com/(?:video|embed|torrent/[^/?#]+)/"
                    + "([^/?#&]+)";
    private static final BitChuteStreamLinkHandlerFactory INSTANCE =
            new BitChuteStreamLinkHandlerFactory();

    public static BitChuteStreamLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    private BitChuteStreamLinkHandlerFactory() {
    }

    @Override
    public String getId(final String url) throws ParsingException {
        return Parser.matchGroup1(PATTERN, url);
    }

    @Override
    public String getUrl(final String id) {
        return BitChuteService.BASE_URL + "/video/" + id + "/";
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
