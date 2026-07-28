package org.schabi.newpipe.extractor.services.bitchute.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.bitchute.BitChuteApi;

import java.io.IOException;

import javax.annotation.Nonnull;

public final class BitChuteSearchExtractor extends SearchExtractor {
    private static final int PAGE_SIZE = 25;

    public BitChuteSearchExtractor(final StreamingService service,
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
        return fetch(0);
    }

    @Nonnull
    @Override
    protected InfoItemsPage<InfoItem> getPageInternal(final Page page)
            throws IOException, ExtractionException {
        return fetch(Integer.parseInt(page.getId()));
    }

    private InfoItemsPage<InfoItem> fetch(final int offset)
            throws IOException, ExtractionException {
        final JsonObject response = BitChuteApi.post(getDownloader(), "beta/search/videos",
                BitChuteApi.object(
                        "offset", offset,
                        "limit", PAGE_SIZE,
                        "query", getSearchString(),
                        "sensitivity_id", "normal",
                        "sort", "new"),
                true);
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());
        final JsonArray videos = response.getArray("videos");
        if (videos != null) {
            for (final Object entry : videos) {
                if (entry instanceof JsonObject) {
                    collector.commit(new BitChuteStreamInfoItemExtractor((JsonObject) entry));
                }
            }
        }
        final Page next = videos == null || videos.isEmpty() ? null
                : new Page(getUrl(), Integer.toString(offset + PAGE_SIZE));
        return new InfoItemsPage<>(collector, next);
    }
}
