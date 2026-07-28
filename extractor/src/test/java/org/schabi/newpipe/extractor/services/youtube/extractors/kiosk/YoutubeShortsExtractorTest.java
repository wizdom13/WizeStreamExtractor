package org.schabi.newpipe.extractor.services.youtube.extractors.kiosk;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeShortsLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YoutubeShortsExtractorTest {
    @Test
    void collectsNestedShortsShelfItemsAndRemovesDuplicates() throws Exception {
        final JsonObject response = JsonParser.object().from("""
                {
                  "contents": [{
                    "reelShelfRenderer": {
                      "items": [{
                        "shortsLockupViewModel": {
                          "overlayMetadata": {
                            "primaryText": {"content": "A short"},
                            "secondaryText": {"content": "123 views"}
                          },
                          "onTap": {
                            "innertubeCommand": {
                              "commandMetadata": {
                                "webCommandMetadata": {
                                  "url": "/shorts/abcdefghijk"
                                }
                              }
                            }
                          },
                          "thumbnail": {
                            "sources": [{
                              "url": "https://i.ytimg.com/vi/abcdefghijk/hqdefault.jpg"
                            }]
                          }
                        }
                      }, {
                        "shortsLockupViewModel": {
                          "overlayMetadata": {
                            "primaryText": {"content": "A duplicate"},
                            "secondaryText": {"content": "123 views"}
                          },
                          "onTap": {
                            "innertubeCommand": {
                              "commandMetadata": {
                                "webCommandMetadata": {
                                  "url": "/shorts/abcdefghijk"
                                }
                              }
                            }
                          },
                          "thumbnail": {
                            "sources": [{
                              "url": "https://i.ytimg.com/vi/abcdefghijk/hqdefault.jpg"
                            }]
                          }
                        }
                      }]
                    }
                  }]
                }
                """);
        final YoutubeShortsExtractor extractor = new YoutubeShortsExtractor(
                ServiceList.YouTube,
                YoutubeShortsLinkHandlerFactory.INSTANCE.fromId(
                        YoutubeShortsLinkHandlerFactory.KIOSK_ID),
                YoutubeShortsLinkHandlerFactory.KIOSK_ID);
        final StreamInfoItemsCollector collector =
                new StreamInfoItemsCollector(ServiceList.YouTube.getServiceId());

        extractor.collectShorts(response, collector, new HashSet<>());

        assertEquals(1, collector.getItems().size());
        assertEquals("https://youtube.com/shorts/abcdefghijk",
                collector.getItems().get(0).getUrl());
        assertTrue(collector.getItems().get(0).isShortFormContent());
    }
}
