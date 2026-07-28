package org.schabi.newpipe.extractor.services.youtube.extractors.kiosk;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeShortsInfoItemExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeTrendingExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Exposes the Shorts shelves returned with YouTube's public trending browse response.
 *
 * <p>The public {@code /shorts} page is a watch sequence rather than a stable list endpoint.
 * The trending browse response provides a locale-aware Shorts collection which can be exposed as
 * a normal WizeStream kiosk without requiring an account or cookies.</p>
 */
public final class YoutubeShortsExtractor extends YoutubeTrendingExtractor {
    public YoutubeShortsExtractor(final StreamingService service,
                                  final ListLinkHandler linkHandler,
                                  final String kioskId) {
        super(service, linkHandler, kioskId);
    }

    @Nonnull
    @Override
    public String getName() {
        return "YouTube Shorts";
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws ParsingException {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        final Set<String> collectedUrls = new HashSet<>();
        collectShorts(getTrendingTabRenderer().getObject("content"), collector, collectedUrls);

        if (collector.getItems().isEmpty()) {
            throw new ParsingException("Could not get YouTube Shorts");
        }

        if (ServiceList.YouTube.getFilterTypes().contains("recommendations")) {
            collector.applyBlocking(ServiceList.YouTube.getFilterConfig());
        }
        return new InfoItemsPage<>(collector, null);
    }

    private void collectShorts(final Object value,
                               final StreamInfoItemsCollector collector,
                               final Set<String> collectedUrls) {
        if (value instanceof JsonObject) {
            final JsonObject object = (JsonObject) value;
            if (object.has("shortsLockupViewModel")) {
                commitShortsLockup(object.getObject("shortsLockupViewModel"),
                        collector, collectedUrls);
                return;
            }
            if (object.has("reelItemRenderer")) {
                commitVideoRenderer(object.getObject("reelItemRenderer"),
                        collector, collectedUrls, true);
                return;
            }
            if (object.has("videoRenderer")) {
                commitVideoRenderer(object.getObject("videoRenderer"),
                        collector, collectedUrls, false);
                return;
            }

            for (final Map.Entry<String, Object> entry : object.entrySet()) {
                collectShorts(entry.getValue(), collector, collectedUrls);
            }
        } else if (value instanceof JsonArray) {
            for (final Object item : (JsonArray) value) {
                collectShorts(item, collector, collectedUrls);
            }
        }
    }

    private void commitShortsLockup(final JsonObject shortsLockup,
                                    final StreamInfoItemsCollector collector,
                                    final Set<String> collectedUrls) {
        final YoutubeShortsInfoItemExtractor extractor =
                new YoutubeShortsInfoItemExtractor(shortsLockup);
        try {
            if (collectedUrls.add(extractor.getUrl())) {
                collector.commit(extractor);
            }
        } catch (final ParsingException e) {
            collector.commit(extractor);
        }
    }

    private void commitVideoRenderer(final JsonObject videoRenderer,
                                     final StreamInfoItemsCollector collector,
                                     final Set<String> collectedUrls,
                                     final boolean knownShort) {
        final YoutubeStreamInfoItemExtractor extractor =
                new YoutubeStreamInfoItemExtractor(videoRenderer, getTimeAgoParser());
        try {
            if ((knownShort || extractor.isShortFormContent())
                    && collectedUrls.add(extractor.getUrl())) {
                collector.commit(extractor);
            }
        } catch (final ParsingException e) {
            collector.commit(extractor);
        }
    }
}
