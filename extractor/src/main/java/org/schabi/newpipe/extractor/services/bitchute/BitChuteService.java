package org.schabi.newpipe.extractor.services.bitchute;

import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.VIDEO;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.bitchute.extractors.BitChuteChannelExtractor;
import org.schabi.newpipe.extractor.services.bitchute.extractors.BitChuteKioskExtractor;
import org.schabi.newpipe.extractor.services.bitchute.extractors.BitChuteSearchExtractor;
import org.schabi.newpipe.extractor.services.bitchute.extractors.BitChuteStreamExtractor;
import org.schabi.newpipe.extractor.services.bitchute.linkhandler.BitChuteChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bitchute.linkhandler.BitChuteKioskLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.bitchute.linkhandler.BitChuteSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.services.bitchute.linkhandler.BitChuteStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.util.Collections;

public final class BitChuteService extends StreamingService {
    public static final String BASE_URL = "https://www.bitchute.com";

    public BitChuteService(final int id) {
        super(id, "BitChute", Collections.singletonList(VIDEO));
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    public LinkHandlerFactory getStreamLHFactory() {
        return BitChuteStreamLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelLHFactory() {
        return BitChuteChannelLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelTabLHFactory() {
        return null;
    }

    @Override
    public ListLinkHandlerFactory getPlaylistLHFactory() {
        return null;
    }

    @Override
    public SearchQueryHandlerFactory getSearchQHFactory() {
        return BitChuteSearchQueryHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getCommentsLHFactory() {
        return null;
    }

    @Override
    public SearchExtractor getSearchExtractor(final SearchQueryHandler queryHandler) {
        return new BitChuteSearchExtractor(this, queryHandler);
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor() {
        return null;
    }

    @Override
    public SubscriptionExtractor getSubscriptionExtractor() {
        return null;
    }

    @Override
    public KioskList getKioskList() throws ExtractionException {
        final KioskList list = new KioskList(this);
        final BitChuteKioskLinkHandlerFactory handler =
                BitChuteKioskLinkHandlerFactory.getInstance();
        final KioskList.KioskExtractorFactory factory = (service, url, id) ->
                new BitChuteKioskExtractor(service, handler.fromId(id), id);
        try {
            list.addKioskEntry(factory, handler, BitChuteKioskLinkHandlerFactory.TRENDING);
            list.addKioskEntry(factory, handler, BitChuteKioskLinkHandlerFactory.POPULAR);
            list.addKioskEntry(factory, handler, BitChuteKioskLinkHandlerFactory.RECENT);
            list.setDefaultKiosk(BitChuteKioskLinkHandlerFactory.TRENDING);
        } catch (final Exception e) {
            throw new ExtractionException(e);
        }
        return list;
    }

    @Override
    public ChannelExtractor getChannelExtractor(final ListLinkHandler linkHandler) {
        return new BitChuteChannelExtractor(this, linkHandler);
    }

    @Override
    public ChannelTabExtractor getChannelTabExtractor(final ListLinkHandler linkHandler) {
        return null;
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(final ListLinkHandler linkHandler) {
        return null;
    }

    @Override
    public StreamExtractor getStreamExtractor(final LinkHandler linkHandler) {
        return new BitChuteStreamExtractor(this, linkHandler);
    }

    @Override
    public CommentsExtractor getCommentsExtractor(final ListLinkHandler linkHandler) {
        return null;
    }
}
