package org.schabi.newpipe.extractor.services.bitchute.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.bitchute.BitChuteApi;
import org.schabi.newpipe.extractor.services.bitchute.BitChuteParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;

import javax.annotation.Nonnull;

public final class BitChuteChannelExtractor extends ChannelExtractor {
    private static final int PAGE_SIZE = 25;
    private JsonObject channel;
    private JsonObject initialVideos;

    public BitChuteChannelExtractor(final StreamingService service,
                                    final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String slug = getId();
        final JsonObject search = BitChuteApi.post(downloader, "beta/search/channels",
                BitChuteApi.object(
                        "offset", 0,
                        "limit", 25,
                        "query", slug,
                        "sensitivity_id", "normal"),
                true);
        final JsonArray channels = search.getArray("channels");
        if (channels == null || channels.isEmpty()) {
            throw new ExtractionException("BitChute channel was not found: " + slug);
        }
        channel = channels.getObject(0);
        for (final Object item : channels) {
            if (!(item instanceof JsonObject)) {
                continue;
            }
            final JsonObject candidate = (JsonObject) item;
            if (slug.equalsIgnoreCase(candidate.getString("url_slug"))
                    || candidate.getString("channel_url", "").endsWith("/" + slug + "/")) {
                channel = candidate;
                break;
            }
        }
        final String channelId = channel.getString("channel_id",
                channel.getString("id", ""));
        if (channelId.isEmpty()) {
            throw new ExtractionException("BitChute channel has no channel ID");
        }
        channel = BitChuteApi.post(downloader, "beta/channel",
                BitChuteApi.object("channel_id", channelId), false);
        initialVideos = fetchVideos(0);
    }

    private JsonObject fetchVideos(final int offset) throws IOException, ExtractionException {
        return BitChuteApi.post(getDownloader(), "beta/channel/videos",
                BitChuteApi.object(
                        "channel_id", channel.getString("channel_id"),
                        "offset", offset,
                        "limit", PAGE_SIZE,
                        "order_by", "latest"),
                true);
    }

    @Nonnull
    @Override
    public String getName() {
        return channel.getString("channel_name", "");
    }

    @Override
    public String getAvatarUrl() {
        return channel.getString("thumbnail_url", "");
    }

    @Override
    public String getBannerUrl() {
        return channel.getString("banner_url", "");
    }

    @Override
    public String getFeedUrl() {
        return getLinkHandler().getUrl();
    }

    @Override
    public long getSubscriberCount() {
        return BitChuteParsingHelper.parseCount(
                channel.get("subscriber_count"), UNKNOWN_SUBSCRIBER_COUNT);
    }

    @Override
    public String getDescription() {
        return channel.getString("description", "");
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() {
        return BitChuteListHelper.collect(getServiceId(), initialVideos, PAGE_SIZE);
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        final int offset = Integer.parseInt(page.getId());
        return BitChuteListHelper.collect(getServiceId(), fetchVideos(offset),
                offset + PAGE_SIZE);
    }

}
