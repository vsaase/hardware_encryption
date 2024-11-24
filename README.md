# hardware_encryption

Providing hardware based symmetric encryption for iOS and Android.

This ensures that the encryption key is never kept in memory and can not be exposed.

- iOS: uses Secure Enclave
- Android: uses AES-GCM with KeyStore.

## Getting Started

```
final String alias = "<name of key>";
await HardwareEncryption.generateKey(alias);

final String secret = "top_secret";
final String? encrypted = HardwareEncryption.encrypt(alias, secret);
final String? decrypted = HardwareEncryption.decrypt(alias, encrypted);

assert(decrypted == secret);
```

Calling generateKey a second time with the same alias will not overwrite the existing key.
