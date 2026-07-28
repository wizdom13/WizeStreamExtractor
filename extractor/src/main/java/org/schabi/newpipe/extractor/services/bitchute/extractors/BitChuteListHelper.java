package org.schabi.newpipe.extractor.services.bitchute.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

final class BitChuteListHelper {
    private BitChuteListHelper() {
    }

    static InfoItemsPage<StreamInfoItem> collect(final int serviceId,
                                                 final JsonObject response,
                                                 final int nextOffset) {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(serviceId);
        final JsonArray videos = response.getArray("videos");
        if (videos != null) {
            for (final Object entry : videos) {
                if (entry instanceof JsonObject) {
                    collector.commit(new BitChuteStreamInfoItemExtractor((JsonObject) entry));
                }
            }
        }
        final Page next = videos == null || videos.isEmpty()
                ? null : new Page("https://api.bitchute.com/api/beta/videos",
                Integer.toString(nextOffset));
        return new InfoItemsPage<>(collector, next);
    }
}
