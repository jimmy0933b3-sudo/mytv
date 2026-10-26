# mytv: signed Android CI and latest Release

On each push to `master` or `main`, GitHub Actions runs unit tests, builds Debug, signs Release with the **existing Android signing key**, verifies the APK, publishes a new Release, then removes older Releases and their tags. Pull requests run tests/Debug only.

## Required GitHub Actions Secrets

Configure these in **Settings > Secrets and variables > Actions > Repository secrets**:

| Secret | Value |
| --- | --- |
| `KEYSTORE_BASE64` | Base64-encoded existing Android JKS/PKCS12 keystore file |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Private-key alias inside the keystore |
| `KEY_PASSWORD` | Password for that private-key entry |

The workflow aborts on missing/invalid secrets. It **never creates a replacement signing key**, never uploads the keystore, and never falls back to Debug signing. Keep the original keystore safely backed up. Using a different key means the installed app cannot be updated in place.

## Release details

- APK: `mytv-latest.apk` in the newest GitHub Release.
- Tags: `v<versionName>-build.<run_number>.<run_attempt>`.
- CI sets `versionCode = 10000 + GITHUB_RUN_NUMBER`, allowing each published build to update the preceding build. Local Gradle builds continue using the source `versionCode`.
- Signed APK is verified with Android `apksigner`; the package ID must be `com.taiwanfreetv.app`.
- The SHA-256 signing certificate fingerprint is recorded in Release notes and compared against the preceding Release before publication.
- Old Releases and their auto-generated tags are deleted **only after** the new APK upload has been verified.
- To build manually, use **Actions > Android CI and Signed Release > Run workflow** on `master` or `main`.
- A successful run produces a single published Release; a failed run preserves the previously released APK.

Do not commit keystore files, signing passwords, or base64 keystore content to Git.
