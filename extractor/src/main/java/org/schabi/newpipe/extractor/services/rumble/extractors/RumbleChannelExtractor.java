package org.schabi.newpipe.extractor.services.rumble.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.rumble.RumbleParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public final class RumbleChannelExtractor extends ChannelExtractor {
    private Document document;

    public RumbleChannelExtractor(final StreamingService service,
                                  final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        document = Jsoup.parse(downloader.get(getUrl()).responseBody(), getUrl());
    }

    @Nonnull
    @Override
    public String getName() {
        final Element heading = document.selectFirst(
                "h1.channel-header--title, h1");
        if (heading != null && !heading.text().isEmpty()) {
            return heading.text();
        }
        return meta("og:title").replace(" | Rumble", "");
    }

    @Override
    public String getAvatarUrl() {
        final Element image = document.selectFirst(
                ".channel-header img, img.channel-header--img");
        return image == null ? meta("og:image") : image.absUrl("src");
    }

    @Override
    public String getBannerUrl() {
        final Element banner = document.selectFirst(
                ".channel-header--background, .channel-banner img");
        if (banner == null) {
            return "";
        }
        final String source = banner.absUrl("src");
        return source.isEmpty() ? banner.absUrl("data-src") : source;
    }

    @Override
    public String getFeedUrl() {
        return null;
    }

    @Override
    public long getSubscriberCount() {
        final Element count = document.selectFirst(
                "[data-js=followers-count], .channel-header--followers");
        return count == null ? -1 : RumbleParsingHelper.parseCount(count.text(), -1);
    }

    @Override
    public String getDescription() {
        return meta("og:description");
    }

    @Override
    public String getParentChannelName() {
        return null;
    }

    @Override
    public String getParentChannelUrl() {
        return null;
    }

    @Override
    public String getParentChannelAvatarUrl() {
        return null;
    }

    @Override
    public boolean isVerified() {
        return document.selectFirst(
                ".verified, [data-js=verified], .channel-header--verified") != null;
    }

    @Nonnull
    @Override
    public List<ListLinkHandler> getTabs() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage()
            throws IOException, ExtractionException {
        return collect(document, getUrl());
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        final Document pageDocument = Jsoup.parse(
                getDownloader().get(page.getUrl()).responseBody(), getUrl());
        return collect(pageDocument, page.getUrl());
    }

    private InfoItemsPage<StreamInfoItem> collect(final Document pageDocument,
                                                  final String pageUrl) {
        final StreamInfoItemsCollector collector =
                new StreamInfoItemsCollector(getServiceId());
        RumbleHtmlHelper.collectStreams(pageDocument, collector);
        return new InfoItemsPage<>(collector,
                RumbleHtmlHelper.nextPage(pageDocument, pageUrl));
    }

    private String meta(final String property) {
        final Element element = document.selectFirst("meta[property=" + property + "]");
        return element == null ? "" : element.attr("content");
    }
}
