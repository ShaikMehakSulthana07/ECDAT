package com.ecdat.backend.input.security;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * SSRF (Server-Side Request Forgery) protection for repository URLs.
 * Validates DNS resolution results and blocks internal/private addresses.
 */
public class SsrfProtection {

    // Private IPv4 ranges
    private static final Set<String> PRIVATE_IPV4_RANGES = Set.of(
        "10.0.0.0/8",           // 10.0.0.0 - 10.255.255.255
        "172.16.0.0/12",        // 172.16.0.0 - 172.31.255.255
        "192.168.0.0/16",       // 192.168.0.0 - 192.168.255.255
        "127.0.0.0/8",          // Loopback
        "0.0.0.0/8",            // Current network
        "169.254.0.0/16",       // Link-local
        "100.64.0.0/10",        // Carrier-grade NAT
        "192.0.0.0/24",         // IETF Protocol Assignments
        "192.0.2.0/24",         // TEST-NET-1
        "198.18.0.0/15",        // Network interconnect device benchmark testing
        "198.51.100.0/24",      // TEST-NET-2
        "203.0.113.0/24",       // TEST-NET-3
        "224.0.0.0/4",          // Multicast
        "240.0.0.0/4"           // Reserved
    );

    // Cloud metadata endpoints
    private static final Set<String> BLOCKED_HOSTNAMES = Set.of(
        "metadata",
        "169.254.169.254",
        "localhost",
        "localhost.localdomain"
    );

    /**
     * Validates that a repository URI does not resolve to blocked addresses.
     *
     * @param uri the repository URI to validate
     * @throws RepositoryAnalysisException if the host is blocked
     */
    public void validateDestination(URI uri) throws RepositoryAnalysisException {
        String host = uri.getHost();
        if (host == null) {
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.REPOSITORY_HOST_BLOCKED,
                "Repository URL must include a valid hostname."
            );
        }

        // Check hostname blocklist
        String lowerHost = host.toLowerCase();
        for (String blocked : BLOCKED_HOSTNAMES) {
            if (lowerHost.equals(blocked) || lowerHost.startsWith(blocked + ".")) {
                throw new RepositoryAnalysisException(
                    RepositoryErrorCode.REPOSITORY_HOST_BLOCKED,
                    "Repository host is not permitted."
                );
            }
        }

        // Resolve DNS and validate IP addresses
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                if (isBlockedAddress(address)) {
                    throw new RepositoryAnalysisException(
                        RepositoryErrorCode.REPOSITORY_HOST_BLOCKED,
                        "Repository host is not permitted."
                    );
                }
            }
        } catch (UnknownHostException e) {
            throw new RepositoryAnalysisException(
                RepositoryErrorCode.REPOSITORY_HOST_BLOCKED,
                "Repository host could not be resolved."
            );
        }
    }

    /**
     * Checks if an IP address is in a blocked range.
     *
     * @param address the IP address to check
     * @return true if blocked, false otherwise
     */
    private boolean isBlockedAddress(InetAddress address) {
        if (address.isLoopbackAddress()) {
            return true;
        }

        if (address.isLinkLocalAddress()) {
            return true;
        }

        if (address.isSiteLocalAddress()) {
            return true;
        }

        if (address.isAnyLocalAddress()) {
            return true;
        }

        if (address.isMulticastAddress()) {
            return true;
        }

        // Additional IPv4 private range checks
        if (address instanceof Inet4Address ipv4) {
            byte[] bytes = ipv4.getAddress();
            int first = bytes[0] & 0xFF;
            int second = bytes[1] & 0xFF;

            // 10.0.0.0/8
            if (first == 10) return true;

            // 172.16.0.0/12
            if (first == 172 && second >= 16 && second <= 31) return true;

            // 192.168.0.0/16
            if (first == 192 && second == 168) return true;

            // 127.0.0.0/8 (loopback - already checked but double-check)
            if (first == 127) return true;

            // 0.0.0.0/8
            if (first == 0) return true;

            // 169.254.0.0/16 (link-local - already checked but double-check)
            if (first == 169 && second == 254) return true;

            // Cloud metadata endpoint
            if (first == 169 && second == 254 && (bytes[2] & 0xFF) == 169 && (bytes[3] & 0xFF) == 254) {
                return true;
            }
        }

        // IPv6 private ranges
        if (address instanceof Inet6Address ipv6) {
            // fc00::/7 (unique local)
            // fe80::/10 (link-local)
            // ::1 (loopback)
            // :: (unspecified)
            byte[] bytes = ipv6.getAddress();
            
            // fc00::/7 - Unique Local
            if ((bytes[0] & 0xFE) == 0xFC) return true;
            
            // fe80::/10 - Link-Local
            if ((bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xC0) == 0x80) return true;
            
            // Loopback ::1
            boolean isLoopback = true;
            for (int i = 0; i < 15; i++) {
                if (bytes[i] != 0) {
                    isLoopback = false;
                    break;
                }
            }
            if (isLoopback && bytes[15] == 1) return true;
            
            // Unspecified ::
            boolean isUnspecified = true;
            for (byte b : bytes) {
                if (b != 0) {
                    isUnspecified = false;
                    break;
                }
            }
            if (isUnspecified) return true;
        }

        return false;
    }

    /**
     * Validates a redirect target URI.
     * This is called if Git follows redirects during clone.
     *
     * @param redirectUri the redirect target URI
     * @throws RepositoryAnalysisException if the redirect is to a blocked host
     */
    public void validateRedirect(URI redirectUri) throws RepositoryAnalysisException {
        validateDestination(redirectUri);
    }
}
