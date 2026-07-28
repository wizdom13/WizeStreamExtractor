package org.schabi.newpipe.extractor.services.rumble;

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
import org.schabi.newpipe.extractor.services.rumble.extractors.RumbleChannelExtractor;
import org.schabi.newpipe.extractor.services.rumble.extractors.RumbleKioskExtractor;
import org.schabi.newpipe.extractor.services.rumble.extractors.RumbleSearchExtractor;
import org.schabi.newpipe.extractor.services.rumble.extractors.RumbleStreamExtractor;
import org.schabi.newpipe.extractor.services.rumble.linkhandler.RumbleChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.rumble.linkhandler.RumbleKioskLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.rumble.linkhandler.RumbleSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.services.rumble.linkhandler.RumbleStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.util.Collections;

public final class RumbleService extends StreamingService {
    public static final String BASE_URL = "https://rumble.com";

    public RumbleService(final int id) {
        super(id, "Rumble", Collections.singletonList(VIDEO));
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    public LinkHandlerFactory getStreamLHFactory() {
        return RumbleStreamLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelLHFactory() {
        return RumbleChannelLinkHandlerFactory.getInstance();
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
        return RumbleSearchQueryHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getCommentsLHFactory() {
        return null;
    }

    @Override
    public SearchExtractor getSearchExtractor(final SearchQueryHandler queryHandler) {
        return new RumbleSearchExtractor(this, queryHandler);
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
        final RumbleKioskLinkHandlerFactory handler =
                RumbleKioskLinkHandlerFactory.getInstance();
        final KioskList.KioskExtractorFactory factory = (service, url, id) ->
                new RumbleKioskExtractor(service, handler.fromId(id), id);
        try {
            list.addKioskEntry(factory, handler, RumbleKioskLinkHandlerFactory.LATEST);
            list.addKioskEntry(factory, handler, RumbleKioskLinkHandlerFactory.POPULAR);
            list.addKioskEntry(factory, handler, RumbleKioskLinkHandlerFactory.LIVE);
            list.setDefaultKiosk(RumbleKioskLinkHandlerFactory.LATEST);
        } catch (final Exception e) {
            throw new ExtractionException(e);
        }
        return list;
    }

    @Override
    public ChannelExtractor getChannelExtractor(final ListLinkHandler linkHandler) {
        return new RumbleChannelExtractor(this, linkHandler);
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
        return new RumbleStreamExtractor(this, linkHandler);
    }

    @Override
    public CommentsExtractor getCommentsExtractor(final ListLinkHandler linkHandler) {
        return null;
    }
}
