import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'hardware_encryption_platform_interface.dart';

/// An implementation of [HardwareEncryptionPlatform] that uses method channels.
class MethodChannelHardwareEncryption extends HardwareEncryptionPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('hardware_encryption');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<bool?> generateKey(String alias) async {
    final success =
        await methodChannel.invokeMethod<bool>('generateKey', {"alias": alias});
    return success;
  }

  @override
  Future<String?> encrypt(String alias, String data) async {
    final encryptedData = await methodChannel
        .invokeMethod<String>('encrypt', {"alias": alias, "data": data});
    return encryptedData;
  }

  @override
  Future<String?> decrypt(String alias, String data) async {
    final decryptedData = await methodChannel
        .invokeMethod<String>('decrypt', {"alias": alias, "data": data});
    return decryptedData;
  }
}
