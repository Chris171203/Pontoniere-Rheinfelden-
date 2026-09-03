# PFVR Android test signing

The committed keystore is an intentionally public test-only signing key for `ch.pfvr.app.test`.
It exists so test APKs from 0.9.5 onward can be installed as updates when `versionCode` increases.

Never use this key for `ch.pfvr.app`, Google Play, or any production/release build.

Certificate SHA-256: `0daff6f63ee0a399d6cbcf64c69dc3cf8bc9fd5f0e5b27174be3c23321be4119`
