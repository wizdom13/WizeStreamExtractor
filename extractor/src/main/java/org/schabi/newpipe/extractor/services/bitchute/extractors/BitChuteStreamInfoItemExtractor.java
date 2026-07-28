package org.schabi.newpipe.extractor.services.bitchute.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.bitchute.BitChuteParsingHelper;
import org.schabi.newpipe.extractor.services.bitchute.BitChuteService;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nullable;

public final class BitChuteStreamInfoItemExtractor implements StreamInfoItemExtractor {
    private final JsonObject item;

    public BitChuteStreamInfoItemExtractor(final JsonObject item) {
        this.item = item;
    }

    @Override
    public String getName() {
        return item.getString("video_name", item.getString("title", ""));
    }

    @Override
    public String getUrl() {
        return BitChuteService.BASE_URL + "/video/"
                + item.getString("video_id", item.getString("id", "")) + "/";
    }

    @Override
    public String getThumbnailUrl() {
        return item.getString("thumbnail_url", "");
    }

    @Override
    public StreamType getStreamType() {
        return "live".equals(item.getString("state_id"))
                ? StreamType.LIVE_STREAM : StreamType.VIDEO_STREAM;
    }

    @Override
    public long getDuration() {
        return BitChuteParsingHelper.parseDuration(item.get("duration"));
    }

    @Override
    public long getViewCount() {
        return item.getLong("view_count", -1);
    }

    @Override
    public String getUploaderName() {
        final JsonObject channel = item.getObject("channel");
        return channel == null ? item.getString("channel_name", "")
                : channel.getString("channel_name", "");
    }

    @Override
    public String getUploaderUrl() {
        final JsonObject channel = item.getObject("channel");
        final String url = channel == null ? item.getString("channel_url", "")
                : channel.getString("channel_url", "");
        if (url.startsWith("http")) {
            return url;
        }
        return BitChuteService.BASE_URL + (url.startsWith("/") ? "" : "/") + url;
    }

    @Nullable
    @Override
    public String getUploaderAvatarUrl() {
        final JsonObject channel = item.getObject("channel");
        return channel == null ? null : channel.getString("thumbnail_url");
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return item.getString("date_published");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return BitChuteParsingHelper.parseDate(getTextualUploadDate());
    }

    @Nullable
    @Override
    public String getShortDescription() {
        return item.getString("description");
    }
}
