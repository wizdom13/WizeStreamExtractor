package org.schabi.newpipe.extractor.services.bitchute.linkhandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bitchute.BitChuteService;
import org.schabi.newpipe.extractor.utils.Parser;

public final class BitChuteChannelLinkHandlerFactory extends ListLinkHandlerFactory {
    private static final String PATTERN =
            "(?i)(?:www\\.|old\\.)?bitchute\\.com/channel/([^/?#&]+)";
    private static final BitChuteChannelLinkHandlerFactory INSTANCE =
            new BitChuteChannelLinkHandlerFactory();

    public static BitChuteChannelLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    private BitChuteChannelLinkHandlerFactory() {
    }

    @Override
    public String getId(final String url) throws ParsingException {
        return Parser.matchGroup1(PATTERN, url);
    }

    @Override
    public String getUrl(final String id,
                         final java.util.List<org.schabi.newpipe.extractor.search.filter.FilterItem>
                                 contentFilters,
                         final java.util.List<org.schabi.newpipe.extractor.search.filter.FilterItem>
                                 sortFilter) {
        return BitChuteService.BASE_URL + "/channel/" + id + "/";
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
