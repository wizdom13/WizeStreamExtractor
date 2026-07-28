package org.schabi.newpipe.extractor.services.rumble.extractors;

import static org.schabi.newpipe.extractor.stream.Stream.ID_UNKNOWN;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.rumble.RumbleParsingHelper;
import org.schabi.newpipe.extractor.services.rumble.RumbleService;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class RumbleStreamExtractor extends StreamExtractor {
    private static final Pattern PLAY_ID_PATTERN = Pattern.compile(
            "Rumble\\(\\s*[\"']play[\"']\\s*,\\s*\\{[^}]*"
                    + "[\"']?video[\"']?\\s*:\\s*[\"']([0-9a-z]+)[\"']",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern EMBED_ID_PATTERN = Pattern.compile(
            "rumble\\.com/embed/(?:[0-9a-z]+\\.)?([0-9a-z]+)",
            Pattern.CASE_INSENSITIVE);

    private JsonObject video;
    private Document watchDocument;

    public RumbleStreamExtractor(final StreamingService service,
                                 final LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String embedId;
        if (getUrl().contains("/embed/")) {
            embedId = getUrl().substring(getUrl().lastIndexOf('/') + 1);
        } else {
            final String html = downloader.get(getUrl()).responseBody();
            watchDocument = Jsoup.parse(html, getUrl());
            embedId = extractEmbedId(html);
        }

        final String apiUrl = RumbleService.BASE_URL
                + "/embedJS/u3/?request=video&ver=2&v=" + embedId;
        try {
            video = JsonParser.object().from(downloader.get(apiUrl).responseBody());
        } catch (final JsonParserException e) {
            throw new ExtractionException("Could not parse Rumble player response", e);
        }
        if (video == null || video.getString("title", "").isEmpty()) {
            throw new ExtractionException("Rumble did not return playable video metadata");
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return video.getString("title", "");
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() {
        final String image = video.getString("i", "");
        if (!image.isEmpty()) {
            return image;
        }
        final JsonObject thumbnails = video.getObject("t");
        if (thumbnails != null) {
            for (final Object value : thumbnails.values()) {
                if (value instanceof JsonObject) {
                    final String url = ((JsonObject) value).getString("i", "");
                    if (!url.isEmpty()) {
                        return url;
                    }
                }
            }
        }
        return "";
    }

    @Nonnull
    @Override
    public Description getDescription() {
        if (watchDocument == null) {
            return Description.EMPTY_DESCRIPTION;
        }
        final Element description = watchDocument.selectFirst(".media-description");
        return description == null ? Description.EMPTY_DESCRIPTION
                : new Description(description.html(), Description.HTML);
    }

    @Override
    public int getAgeLimit() {
        return watchDocument != null
                && watchDocument.selectFirst(".age-restricted, [data-age-restricted]") != null
                ? 18 : NO_AGE_LIMIT;
    }

    @Override
    public long getLength() {
        return getStreamType() == StreamType.LIVE_STREAM
                ? 0 : video.getLong("duration", 0);
    }

    @Override
    public long getViewCount() {
        if (watchDocument == null) {
            return -1;
        }
        final Element count = watchDocument.selectFirst(
                "[itemprop=userInteractionCount], [data-js=view-count]");
        final String value = count == null ? "" : firstNonEmpty(
                count.attr("content"), count.text());
        return RumbleParsingHelper.parseCount(value, -1);
    }

    @Override
    public long getLikeCount() {
        return pageCount("[data-js=rumbles_up_votes]");
    }

    @Override
    public long getDislikeCount() {
        return pageCount("[data-js=rumbles_down_votes]");
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return video.getString("pubDate");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return RumbleParsingHelper.parseDate(getTextualUploadDate());
    }

    @Nonnull
    @Override
    public String getUploaderUrl() {
        final JsonObject author = video.getObject("author");
        return author == null ? "" : author.getString("url", "");
    }

    @Nonnull
    @Override
    public String getUploaderName() {
        final JsonObject author = video.getObject("author");
        return author == null ? "" : author.getString("name", "");
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() {
        return "";
    }

    @Override
    public long getUploaderSubscriberCount() {
        return UNKNOWN_SUBSCRIBER_COUNT;
    }

    @Override
    public List<AudioStream> getAudioStreams() {
        final List<AudioStream> streams = new ArrayList<>();
        final JsonObject formats = video.getObject("ua");
        if (formats == null) {
            return streams;
        }
        addAudioValues(streams, formats.get("audio"));
        return streams;
    }

    @Override
    public List<VideoStream> getVideoStreams() {
        final List<VideoStream> streams = new ArrayList<>();
        final JsonObject formats = video.getObject("ua");
        if (formats == null) {
            return streams;
        }
        for (final Map.Entry<String, Object> entry : formats.entrySet()) {
            if ("tar".equals(entry.getKey()) || "timeline".equals(entry.getKey())
                    || "audio".equals(entry.getKey())) {
                continue;
            }
            addVideoValues(streams, entry.getKey(), entry.getValue());
        }
        return streams;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitlesDefault() {
        final JsonObject captions = video.getObject("cc");
        if (captions == null) {
            return Collections.emptyList();
        }
        final List<SubtitlesStream> subtitles = new ArrayList<>();
        for (final Map.Entry<String, Object> entry : captions.entrySet()) {
            if (!(entry.getValue() instanceof JsonObject)) {
                continue;
            }
            final String path = ((JsonObject) entry.getValue()).getString("path", "");
            if (!path.isEmpty()) {
                subtitles.add(new SubtitlesStream.Builder()
                        .setContent(path, true)
                        .setMediaFormat(MediaFormat.VTT)
                        .setLanguageCode(entry.getKey())
                        .setAutoGenerated(false)
                        .build());
            }
        }
        return subtitles;
    }

    @Override
    public StreamType getStreamType() {
        return video != null && video.getInt("live", 0) > 0
                ? StreamType.LIVE_STREAM : StreamType.VIDEO_STREAM;
    }

    private static String extractEmbedId(final String html) throws ExtractionException {
        Matcher matcher = PLAY_ID_PATTERN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        matcher = EMBED_ID_PATTERN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new ExtractionException("Could not find the Rumble player ID");
    }

    private void addVideoValues(final List<VideoStream> streams,
                                final String formatType,
                                final Object value) {
        if (value instanceof JsonArray) {
            for (final Object item : (JsonArray) value) {
                addVideoValues(streams, formatType, item);
            }
        } else if (value instanceof JsonObject) {
            final JsonObject object = (JsonObject) value;
            final String url = object.getString("url", "");
            if (!url.isEmpty()) {
                final JsonObject meta = object.getObject("meta");
                final int height = meta == null ? 0 : meta.getInt("h", 0);
                final VideoStream.Builder builder = new VideoStream.Builder()
                        .setId(ID_UNKNOWN)
                        .setContent(url, true)
                        .setIsVideoOnly(false)
                        .setMediaFormat("hls".equals(formatType) ? null : MediaFormat.MPEG_4)
                        .setResolution(height > 0 ? height + "p"
                                : VideoStream.RESOLUTION_UNKNOWN);
                if (meta != null) {
                    builder.setWidth(meta.getInt("w", 0)).setHeight(height);
                }
                if ("hls".equals(formatType)) {
                    builder.setDeliveryMethod(DeliveryMethod.HLS).setManifestUrl(url);
                }
                streams.add(builder.build());
                return;
            }
            for (final Object child : object.values()) {
                addVideoValues(streams, formatType, child);
            }
        }
    }

    private void addAudioValues(final List<AudioStream> streams, final Object value) {
        if (value instanceof JsonArray) {
            for (final Object item : (JsonArray) value) {
                addAudioValues(streams, item);
            }
        } else if (value instanceof JsonObject) {
            final JsonObject object = (JsonObject) value;
            final String url = object.getString("url", "");
            if (!url.isEmpty()) {
                streams.add(new AudioStream.Builder()
                        .setId(ID_UNKNOWN)
                        .setContent(url, true)
                        .setMediaFormat(MediaFormat.M4A)
                        .build());
                return;
            }
            for (final Object child : object.values()) {
                addAudioValues(streams, child);
            }
        }
    }

    private long pageCount(final String selector) {
        if (watchDocument == null) {
            return -1;
        }
        final Element element = watchDocument.selectFirst(selector);
        return element == null ? -1
                : RumbleParsingHelper.parseCount(element.text(), -1);
    }

    private static String firstNonEmpty(final String first, final String second) {
        return first == null || first.isEmpty() ? second : first;
    }
}
