# Android ChatGPT Demo

This repository serves as the foundation for a series of articles that aim to demonstrate that hiding secrets in a mobile app binary, through code obfuscation and/or string obfuscation/encryption, just gives to the mobile app developer a false sense of security, a [Maginot Line](https://approov.io/blog/is-code-obfuscation-worth-it).

To avoid a Maginot Line in their security defences, mobile app developers are recommended to retrieve secrets securely from a backend, just-in-time of making the API request, rather than using hard-coded secrets. These runtime secrets are only provided when the remote mobile app attestation succeeds in attesting the integrity of both the device and the app. To learn more about this approach, check out the series of articles listed on this repository or visit the [Approov website](https://approov.io).
