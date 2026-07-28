package org.schabi.newpipe.extractor.services.bitchute.extractors;

import static org.schabi.newpipe.extractor.stream.Stream.ID_UNKNOWN;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.bitchute.BitChuteApi;
import org.schabi.newpipe.extractor.services.bitchute.BitChuteParsingHelper;
import org.schabi.newpipe.extractor.services.bitchute.BitChuteService;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class BitChuteStreamExtractor extends StreamExtractor {
    private JsonObject video;
    private JsonObject channel;
    private String mediaUrl;

    public BitChuteStreamExtractor(final StreamingService service,
                                   final LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final JsonObject payload = BitChuteApi.object("video_id", getId());
        video = BitChuteApi.post(downloader, "beta/video", payload, false);
        mediaUrl = BitChuteApi.post(downloader, "beta/video/media", payload, false)
                .getString("media_url");
        final JsonObject videoChannel = video.getObject("channel");
        if (videoChannel != null) {
            final String channelId = videoChannel.getString("channel_id");
            if (channelId != null && !channelId.isEmpty()) {
                channel = BitChuteApi.post(downloader, "beta/channel",
                        BitChuteApi.object("channel_id", channelId), false);
            }
        }
        if (mediaUrl == null || mediaUrl.isEmpty()) {
            throw new ExtractionException("BitChute did not return a playable media URL");
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return video.getString("video_name", "");
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() {
        return video.getString("thumbnail_url", "");
    }

    @Nonnull
    @Override
    public Description getDescription() {
        return new Description(video.getString("description", ""), Description.PLAIN_TEXT);
    }

    @Override
    public int getAgeLimit() {
        final String sensitivity = video.getString("sensitivity_id", "normal");
        return "normal".equals(sensitivity) ? NO_AGE_LIMIT : 18;
    }

    @Override
    public long getLength() {
        return BitChuteParsingHelper.parseDuration(video.get("duration"));
    }

    @Override
    public long getViewCount() {
        return video.getLong("view_count", -1);
    }

    @Override
    public long getLikeCount() {
        return video.getLong("like_count", -1);
    }

    @Override
    public long getDislikeCount() {
        return video.getLong("dislike_count", -1);
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return video.getString("date_published");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return BitChuteParsingHelper.parseDate(getTextualUploadDate());
    }

    @Nonnull
    @Override
    public String getUploaderUrl() {
        final String url = channel == null ? "" : channel.getString("channel_url", "");
        if (url.startsWith("http")) {
            return url;
        }
        return BitChuteService.BASE_URL + (url.startsWith("/") ? "" : "/") + url;
    }

    @Nonnull
    @Override
    public String getUploaderName() {
        if (channel != null) {
            return channel.getString("channel_name", "");
        }
        final JsonObject videoChannel = video.getObject("channel");
        return videoChannel == null ? "" : videoChannel.getString("channel_name", "");
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() {
        return channel == null ? "" : channel.getString("thumbnail_url", "");
    }

    @Override
    public long getUploaderSubscriberCount() {
        return channel == null ? UNKNOWN_SUBSCRIBER_COUNT
                : BitChuteParsingHelper.parseCount(
                        channel.get("subscriber_count"), UNKNOWN_SUBSCRIBER_COUNT);
    }

    @Override
    public List<AudioStream> getAudioStreams() {
        return Collections.emptyList();
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
        if (mediaUrl == null || mediaUrl.isEmpty()) {
            throw new ExtractionException("BitChute media URL is empty");
        }
        final VideoStream.Builder builder = new VideoStream.Builder()
                .setId(ID_UNKNOWN)
                .setContent(mediaUrl, true)
                .setIsVideoOnly(false)
                .setMediaFormat(mediaUrl.contains(".m3u8") ? null : MediaFormat.MPEG_4)
                .setResolution(VideoStream.RESOLUTION_UNKNOWN);
        if (mediaUrl.contains(".m3u8")) {
            builder.setDeliveryMethod(DeliveryMethod.HLS).setManifestUrl(mediaUrl);
        }
        return Collections.singletonList(builder.build());
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() {
        return "live".equals(video.getString("state_id"))
                ? StreamType.LIVE_STREAM : StreamType.VIDEO_STREAM;
    }
}
