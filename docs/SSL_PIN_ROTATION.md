# SSL public-key pin rotation

The Android SDK enforces SPKI SHA-256 pins for the explicitly inventoried Kommunicate API and chat hosts in release builds. Android's normal CA, certificate-validity, and hostname checks run before the additional pin check. Unlisted hosts, external attachments, customer overrides, Google, and AWS URLs are not assigned Kommunicate pins and continue to use platform TLS validation.

Release-only Network Security Configuration files repeat the same exact-host inventory so Android-managed HTTP stacks, including Glide and WebView requests that honor the application configuration, receive the same pins. Debug builds retain the unpinned system-CA-only configuration. Consuming applications that replace `android:networkSecurityConfig` must merge these domain pin sets into their own release configuration.

## Pin roles

- Each entry in `pinsByHost` in `SSLPinningConfig.kt` contains the currently deployed leaf public-key pin and that host's issuer backup pin.
- Every backup pin was verified against the issuer/intermediate certificate in the host's served chain on 2026-08-26. It allows the server to deploy a renewed or re-keyed leaf certificate without an app release only while the new chain continues to include that pinned issuer key.
- These are issuer fallback pins, not dormant Kommunicate-controlled leaf keys. Changing CA or intermediate requires the replacement leaf or issuer pin to be shipped in an SDK release before the server chain changes.
- A pin is a public identifier, not a private key or secret. Never add certificate private keys to this repository.

## Pin expiration policy

The release pin sets intentionally do not declare an expiration date. Android disables pinning after that date, which avoids stranding an old app but also makes pinning fail open. This SDK applies the same pin inventory through both Network Security Configuration and programmatic verification, so an XML-only expiration would provide inconsistent behavior and would not prevent programmatic connections from failing.

The selected policy is to fail closed and preserve the VAPT control. Availability is handled through the verified issuer fallback pins and the staged rotation procedure below. Before changing an issuer/intermediate, Infrastructure must provide the new chain and a compatible SDK must be released and adopted. If the organization later chooses a fail-open expiration policy, the same reviewed deadline must be implemented in both enforcement paths and covered by release tests.

## Verified host inventory

Verification was performed from the TLS chains served on 2026-08-26. Expiry timestamps are UTC.

| Host | Role | Certificate subject | Certificate expiry | SPKI SHA-256 |
| --- | --- | --- | --- | --- |
| `api.kommunicate.io` | Primary | `CN=api.kommunicate.io` | 2026-10-18 13:41:48 | `TVBTCkZ55/FSdN5KDEWeF6aQMEsf4tmuHbQy92W4OuY=` |
| `api.kommunicate.io` | Backup | `CN=WR3, O=Google Trust Services, C=US` | 2029-02-20 14:00:00 | `OdSlmQD9NWJh4EbcOHBxkhygPwNSwA9Q91eounfbcoE=` |
| `api-test.kommunicate.io` | Primary | `CN=api-test.kommunicate.io` | 2026-10-18 15:14:06 | `gY8gSDmi6lg/wV1MbM3FzNMYrcX6EKseYfJg7QnA02A=` |
| `api-test.kommunicate.io` | Backup | `CN=WR3, O=Google Trust Services, C=US` | 2029-02-20 14:00:00 | `OdSlmQD9NWJh4EbcOHBxkhygPwNSwA9Q91eounfbcoE=` |
| `api-eu.kommunicate.io` | Primary | `CN=api-eu.kommunicate.io` | 2026-10-21 15:01:26 | `cqN64LYMqSbYAlnj5gqix01wTHc4f+IFGOe68iV7LHY=` |
| `api-eu.kommunicate.io` | Backup | `CN=YR1, O=Let's Encrypt, C=US` | 2028-09-02 23:59:59 | `LoMHBotttiDko50Gi13uXW71eIy7LAttI+rYT8wXF4w=` |
| `api-in.kommunicate.io` | Primary | `CN=api-in.kommunicate.io` | 2026-10-20 13:45:53 | `1zJGg1EdjGEFdxqpA0bNZoXNomD9oXfxci/SmbWp7e0=` |
| `api-in.kommunicate.io` | Backup | `CN=YE1, O=Let's Encrypt, C=US` | 2028-09-02 23:59:59 | `brzvtCELCIZUo4sD/qPX0ccRtPsd3DY6RfmxpOU9oB4=` |
| `chat.kommunicate.io` | Primary | `CN=chat.kommunicate.io` | 2026-10-10 01:19:47 | `MpB2iHEUO7nmKSdp/YuWdkjSP9hp7Ax7P/9c9jeGQO4=` |
| `chat.kommunicate.io` | Backup | `CN=WR3, O=Google Trust Services, C=US` | 2029-02-20 14:00:00 | `OdSlmQD9NWJh4EbcOHBxkhygPwNSwA9Q91eounfbcoE=` |
| `chat-test.kommunicate.io` | Primary | `CN=chat-test.kommunicate.io` | 2026-10-07 08:18:46 | `eWPDImElk5nVbsn19Uz9VXDUVfPispeX5ZlINItM/7c=` |
| `chat-test.kommunicate.io` | Backup | `CN=WR3, O=Google Trust Services, C=US` | 2029-02-20 14:00:00 | `OdSlmQD9NWJh4EbcOHBxkhygPwNSwA9Q91eounfbcoE=` |
| `chat-eu.kommunicate.io` | Primary | `CN=chat-eu.kommunicate.io` | 2026-10-10 15:29:14 | `w/h1ivN8L5msp4gPmxqFr2qqqb+dvxG+XGd8XDatx+s=` |
| `chat-eu.kommunicate.io` | Backup | `CN=WR3, O=Google Trust Services, C=US` | 2029-02-20 14:00:00 | `OdSlmQD9NWJh4EbcOHBxkhygPwNSwA9Q91eounfbcoE=` |

### Retired Canada endpoint

`api-ca.kommunicate.io` is officially retired from this SDK. On 2026-08-26 it returned authoritative NXDOMAIN responses from both Google Public DNS and Cloudflare DNS, and the public SDK server configuration supports only the default and EU environments. The legacy API, dashboard, and Help Center CA entries were removed from `km_urls.xml`. `api-ca.kommunicate.io` remains in the programmatic `retiredHosts` denylist so an application carrying an old manual configuration fails closed instead of making an unpinned connection.

Do not re-enable the Canada endpoint until Infrastructure provides a supported resolving hostname, its served certificate chain is verified, and both primary and backup pins are added to this inventory, `pinsByHost`, and every release Network Security Configuration.

## Planned rotation

1. At least one SDK release before changing the server key or issuer, obtain the new certificate chain from Infrastructure.
2. Calculate the SHA-256 hash of the DER-encoded SubjectPublicKeyInfo for the new leaf and issuer keys.
3. Add the new values to the relevant host's `PinSet` in `pinsByHost` and to every release Network Security Configuration, retaining the currently deployed values.
4. Run the pinning unit tests and the release MITM test described below, then publish the SDK.
5. Wait until the agreed minimum supported SDK adoption threshold is reached.
6. Deploy the new server certificate and verify login, conversations, uploads, downloads, and notification media with both the previous and latest supported SDK releases.
7. Remove obsolete pins only in a later SDK release, while retaining a verified fallback for the next approved certificate chain.

Do not deploy a certificate chain for which no SPKI in a supported app's pin set is present. If an emergency rotation cannot use a pre-shipped pin, restore the previous certificate while a compatible SDK release is distributed; do not add a remote or release-mode pinning bypass.

## Generate and verify a pin

For each hostname, save every certificate in the served chain separately and calculate:

```shell
openssl x509 -in certificate.pem -pubkey -noout \
  | openssl pkey -pubin -outform DER \
  | openssl dgst -sha256 -binary \
  | openssl base64 -A
```

Record the hostname, certificate subject, expiry, pin role, verification date, and Infrastructure owner in the rotation ticket. Review pins before certificate expiry and whenever the CDN, load balancer, CA, or regional endpoint changes.

## Release verification

1. Build the release variant and inspect its merged manifest. It must use `network_security_config`, disallow cleartext traffic, trust only system CAs, and contain no debug override or user CA.
2. On a non-rooted test device, install a normal user CA from an HTTPS interception proxy.
3. Confirm login and Kommunicate API requests fail with a generic connection error while interception is active.
4. Remove the proxy and confirm login, conversations, message send/receive, attachment upload/download, thumbnails, and notification media succeed.
5. Confirm customer-controlled and presigned storage URLs still work through normal platform TLS validation.

Rooted-device/runtime-instrumentation tools can bypass client-side checks. This control increases resistance and does not claim to prevent every bypass on a compromised device.
