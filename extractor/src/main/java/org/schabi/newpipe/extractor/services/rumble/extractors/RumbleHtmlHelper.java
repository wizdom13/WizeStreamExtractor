package org.schabi.newpipe.extractor.services.rumble.extractors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

final class RumbleHtmlHelper {
    private RumbleHtmlHelper() {
    }

    static void collectStreams(final Document document,
                               final StreamInfoItemsCollector collector) {
        final Set<String> urls = new HashSet<>();
        final Elements links = document.select(
                "a.videostream__link[href], a.video-item--a[href], "
                        + "a[href^=/v][href$=.html]");
        for (final Element link : links) {
            final String url = link.absUrl("href");
            if (url.isEmpty() || !urls.add(url)) {
                continue;
            }
            collector.commit(new RumbleStreamInfoItemExtractor(findCard(link), link));
        }
    }

    static void collectStreams(final Document document,
                               final MultiInfoItemsCollector collector) {
        final Set<String> urls = new HashSet<>();
        final Elements links = document.select(
                "a.videostream__link[href], a.video-item--a[href], "
                        + "a[href^=/v][href$=.html]");
        for (final Element link : links) {
            final String url = link.absUrl("href");
            if (url.isEmpty() || !urls.add(url)) {
                continue;
            }
            collector.commit(new RumbleStreamInfoItemExtractor(findCard(link), link));
        }
    }

    static Page nextPage(final Document document, final String currentUrl) {
        final Element next = document.selectFirst(
                "a[rel=next][href], .pagination a[aria-label*=Next][href], "
                        + ".pagination a[title*=Next][href]");
        if (next != null && !next.absUrl("href").isEmpty()) {
            return new Page(next.absUrl("href"));
        }
        return null;
    }

    static String appendPage(final String url, final int pageNumber) {
        try {
            final URI uri = new URI(url);
            final String query = uri.getQuery();
            final String nextQuery = (query == null || query.isEmpty() ? "" : query + "&")
                    + "page=" + pageNumber;
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(),
                    nextQuery, null).toString();
        } catch (final URISyntaxException e) {
            return url + (url.contains("?") ? "&" : "?") + "page=" + pageNumber;
        }
    }

    private static Element findCard(final Element link) {
        Element element = link;
        while (element.parent() != null) {
            if (element.hasClass("videostream") || element.hasClass("video-item")
                    || element.hasClass("video-listing-entry")) {
                return element;
            }
            element = element.parent();
        }
        return link;
    }
}
