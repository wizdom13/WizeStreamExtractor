package org.schabi.newpipe.extractor.services.rumble.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;

import javax.annotation.Nonnull;

public final class RumbleKioskExtractor extends KioskExtractor<StreamInfoItem> {
    private final String kioskId;

    public RumbleKioskExtractor(final StreamingService service,
                                final ListLinkHandler linkHandler,
                                final String kioskId) {
        super(service, linkHandler, kioskId);
        this.kioskId = kioskId;
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) {
    }

    @Nonnull
    @Override
    public String getName() {
        return kioskId;
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage()
            throws IOException, ExtractionException {
        return fetch(new Page(getUrl()));
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        return fetch(page);
    }

    private InfoItemsPage<StreamInfoItem> fetch(final Page page)
            throws IOException, ExtractionException {
        final Document document = Jsoup.parse(
                getDownloader().get(page.getUrl()).responseBody(), getUrl());
        final StreamInfoItemsCollector collector =
                new StreamInfoItemsCollector(getServiceId());
        RumbleHtmlHelper.collectStreams(document, collector);
        return new InfoItemsPage<>(collector,
                RumbleHtmlHelper.nextPage(document, page.getUrl()));
    }
}
