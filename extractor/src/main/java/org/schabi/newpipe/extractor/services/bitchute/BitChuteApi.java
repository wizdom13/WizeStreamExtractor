package org.schabi.newpipe.extractor.services.bitchute;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BitChuteApi {
    public static final String API_BASE = "https://api.bitchute.com/api/";
    private static final long TOKEN_LIFETIME_MILLIS = 45L * 60L * 1000L;
    private static String serviceToken;
    private static long tokenExpiresAt;

    private BitChuteApi() {
    }

    public static JsonObject post(final Downloader downloader,
                                  final String endpoint,
                                  final JsonObject payload,
                                  final boolean needsToken)
            throws IOException, ExtractionException {
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", Collections.singletonList("application/json"));
        headers.put("Content-Type", Collections.singletonList("application/json"));
        headers.put("Origin", Collections.singletonList(BitChuteService.BASE_URL));
        headers.put("Referer", Collections.singletonList(BitChuteService.BASE_URL + "/"));
        if (needsToken) {
            headers.put("x-service-info",
                    Collections.singletonList(getServiceToken(downloader)));
        }

        final byte[] body = JsonWriter.string(payload).getBytes(UTF_8);
        final Response response = downloader.post(API_BASE + endpoint, headers, body);
        if (response.responseCode() < 200 || response.responseCode() >= 300) {
            throw new ExtractionException("BitChute API " + endpoint + " returned HTTP "
                    + response.responseCode());
        }
        try {
            return JsonParser.object().from(response.responseBody());
        } catch (final JsonParserException e) {
            throw new ExtractionException("Could not parse BitChute API response from "
                    + endpoint, e);
        }
    }

    private static synchronized String getServiceToken(final Downloader downloader)
            throws IOException, ExtractionException {
        final long now = System.currentTimeMillis();
        if (serviceToken != null && now < tokenExpiresAt) {
            return serviceToken;
        }

        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("Accept", Collections.singletonList("application/json"));
        headers.put("Content-Type", Collections.singletonList("application/json"));
        headers.put("Origin", Collections.singletonList(BitChuteService.BASE_URL));
        headers.put("Referer", Collections.singletonList(BitChuteService.BASE_URL + "/"));
        final Response response = downloader.post(API_BASE + "timer/", headers,
                "{}".getBytes(UTF_8));
        if (response.responseCode() < 200 || response.responseCode() >= 300) {
            throw new ExtractionException("Could not obtain BitChute service token: HTTP "
                    + response.responseCode());
        }

        final String body = response.responseBody().trim();
        String token = body;
        if (body.startsWith("\"") && body.endsWith("\"") && body.length() > 2) {
            token = body.substring(1, body.length() - 1);
        } else if (body.startsWith("{")) {
            try {
                final JsonObject object = JsonParser.object().from(body);
                token = object.getString("token");
                if (token == null) {
                    token = object.getString("serviceInfo");
                }
                if (token == null) {
                    token = object.getString("xServiceInfo");
                }
                if (token == null) {
                    token = object.getString("x-service-info");
                }
                if (token == null) {
                    token = object.getString("value");
                }
            } catch (final JsonParserException e) {
                throw new ExtractionException("Could not parse BitChute service token", e);
            }
        }

        if (token == null || !token.matches("[A-Za-z0-9_-]{28}")) {
            throw new ExtractionException("BitChute returned an invalid service token");
        }
        serviceToken = token;
        tokenExpiresAt = now + TOKEN_LIFETIME_MILLIS;
        return serviceToken;
    }

    public static JsonObject object(final Object... values) {
        final JsonObject object = new JsonObject();
        for (int i = 0; i + 1 < values.length; i += 2) {
            object.put((String) values[i], values[i + 1]);
        }
        return object;
    }
}
