package org.schabi.newpipe.extractor.services.rumble.extractors;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.rumble.RumbleParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nullable;

public final class RumbleStreamInfoItemExtractor implements StreamInfoItemExtractor {
    private final Element card;
    private final Element link;

    public RumbleStreamInfoItemExtractor(final Element card, final Element link) {
        this.card = card;
        this.link = link;
    }

    @Override
    public String getName() {
        final Element title = card.selectFirst(
                ".videostream__title, .video-item--title, [data-js=video-title]");
        if (title != null && !title.text().isEmpty()) {
            return title.text();
        }
        final String titleAttribute = link.attr("title");
        return titleAttribute.isEmpty() ? link.text() : titleAttribute;
    }

    @Override
    public String getUrl() {
        return link.absUrl("href");
    }

    @Override
    public String getThumbnailUrl() {
        final Element image = card.selectFirst("img");
        if (image == null) {
            return "";
        }
        final String source = firstNonEmpty(image.absUrl("src"),
                image.absUrl("data-src"), image.absUrl("data-original"));
        return source == null ? "" : source;
    }

    @Override
    public StreamType getStreamType() {
        return card.selectFirst(".live, [data-live], .videostream__status--live") == null
                ? StreamType.VIDEO_STREAM : StreamType.LIVE_STREAM;
    }

    @Override
    public long getDuration() {
        final Element duration = card.selectFirst(
                ".videostream__duration, .video-item--duration, [data-js=duration]");
        return duration == null ? -1 : RumbleParsingHelper.parseDuration(duration.text());
    }

    @Override
    public long getViewCount() {
        final Element views = card.selectFirst(
                ".videostream__views, .video-item--views, [data-js=views]");
        return views == null ? -1 : RumbleParsingHelper.parseCount(views.text(), -1);
    }

    @Override
    public String getUploaderName() {
        final Element uploader = uploader();
        return uploader == null ? "" : uploader.text();
    }

    @Override
    public String getUploaderUrl() {
        final Element uploader = uploader();
        return uploader == null ? "" : uploader.absUrl("href");
    }

    @Nullable
    @Override
    public String getUploaderAvatarUrl() {
        return null;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        final Element time = card.selectFirst("time[datetime]");
        return time == null ? null : time.attr("datetime");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return RumbleParsingHelper.parseDate(getTextualUploadDate());
    }

    @Nullable
    @Override
    public String getShortDescription() {
        final Element description = card.selectFirst(
                ".videostream__description, .video-item--description");
        return description == null ? null : description.text();
    }

    private Element uploader() {
        return card.selectFirst("a[href^=/c/], a[href^=/user/]");
    }

    private static String firstNonEmpty(final String... values) {
        for (final String value : values) {
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }
}
