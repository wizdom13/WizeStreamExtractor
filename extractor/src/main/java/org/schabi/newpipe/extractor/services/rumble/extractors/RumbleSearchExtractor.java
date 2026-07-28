package org.schabi.newpipe.extractor.services.rumble.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.SearchExtractor;

import java.io.IOException;

import javax.annotation.Nonnull;

public final class RumbleSearchExtractor extends SearchExtractor {
    public RumbleSearchExtractor(final StreamingService service,
                                 final SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) {
    }

    @Nonnull
    @Override
    protected InfoItemsPage<InfoItem> getInitialPageInternal()
            throws IOException, ExtractionException {
        return fetch(new Page(getUrl()));
    }

    @Nonnull
    @Override
    protected InfoItemsPage<InfoItem> getPageInternal(final Page page)
            throws IOException, ExtractionException {
        return fetch(page);
    }

    private InfoItemsPage<InfoItem> fetch(final Page page)
            throws IOException, ExtractionException {
        final Document document = Jsoup.parse(
                getDownloader().get(page.getUrl()).responseBody(), getUrl());
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());
        RumbleHtmlHelper.collectStreams(document, collector);
        return new InfoItemsPage<>(collector,
                RumbleHtmlHelper.nextPage(document, page.getUrl()));
    }
}
