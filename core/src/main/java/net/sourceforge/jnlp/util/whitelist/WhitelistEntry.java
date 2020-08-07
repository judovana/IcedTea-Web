package net.sourceforge.jnlp.util.whitelist;

import net.adoptopenjdk.icedteaweb.Assert;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;

/**
 * ...
 */
public class WhitelistEntry {
    private static final String WILDCARD = "*";
    private static final String HOST_PART_REGEX = "\\.";

    private final String rawWhitelistEntry;
    private final URL effectiveWhitelistEntry;
    private final String errorMessage;

    static WhitelistEntry validWhitelistEntry(final String wlEntry, URL effectiveWhitelistEntry) {
        Assert.requireNonNull(wlEntry, "wlEntry");
        Assert.requireNonNull(effectiveWhitelistEntry, "effectiveWhitelistEntry");
        return new WhitelistEntry(wlEntry, effectiveWhitelistEntry, null);
    }

    static WhitelistEntry invalidWhitelistEntry(final String wlEntry, final String errorMessage) {
        Assert.requireNonNull(wlEntry, "wlEntry");
        Assert.requireNonNull(errorMessage, "errorMessage");
        return new WhitelistEntry(wlEntry, null, errorMessage);
    }

    private WhitelistEntry(final String rawWhitelistEntry, final URL effectiveWhitelistEntry, final String errorMessage) {
        this.rawWhitelistEntry = rawWhitelistEntry;
        this.effectiveWhitelistEntry = effectiveWhitelistEntry;
        this.errorMessage = errorMessage;
    }

    public String getRawWhitelistEntry() {
        return rawWhitelistEntry;
    }

    public String getEffectiveWhitelistEntry() {
        return Optional.ofNullable(effectiveWhitelistEntry).map(Objects::toString).orElse(null);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isValid() {
        return errorMessage == null;
    }

    public boolean matches(URL url) {
        if (!isValid() || url == null) {
            // ignore invalid url or whitelist entries
            return false;
        } else {
            return isUrlProtocolMatching(effectiveWhitelistEntry, url) && isUrlHostMatching(effectiveWhitelistEntry, url) && isUrlPortMatching(effectiveWhitelistEntry, url);
        }
    }
    private static boolean isUrlProtocolMatching(URL url1, URL url2) {
        return Objects.equals(url1.getProtocol(), url2.getProtocol());
    }

    private static boolean isUrlHostMatching(URL wlUrl, URL url) {
        // proto://*:port
        if (Objects.equals(wlUrl.getHost(), WILDCARD)) {
            return true;
        }

        final String[] wlUrlHostParts = wlUrl.getHost().split(HOST_PART_REGEX);
        final String[] urlHostParts = url.getHost().split(HOST_PART_REGEX);

        if (wlUrlHostParts.length != urlHostParts.length) {
            return false;
        }

        boolean result = true;
        for (int i = 0; i < wlUrlHostParts.length; i++) {
            // hostparts are equal if whitelist url has * or they are same
            result = result && (Objects.equals(wlUrlHostParts[i], WILDCARD) || Objects.equals(wlUrlHostParts[i], urlHostParts[i]));
        }
        return result;
    }

    private static boolean isUrlPortMatching(URL wlUrl, URL url) {
        if (wlUrl.getPort() != -1) {
            // url does not have port then force default port as we do the same for whitelist url
            if (url.getPort() == -1) {
                return wlUrl.getPort() == url.getDefaultPort();
            } else {
                return wlUrl.getPort() == url.getPort();
            }
        }
        return true;
    }
}
