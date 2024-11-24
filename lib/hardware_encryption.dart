import 'dart:convert';
import 'dart:io';

import 'package:secure_enclave/secure_enclave.dart';

import 'hardware_encryption_platform_interface.dart';

class HardwareEncryption {
  static Future<String?> getPlatformVersion() {
    return HardwareEncryptionPlatform.instance.getPlatformVersion();
  }

  static Future<bool?> generateKey(String alias) async {
    if (Platform.isIOS) {
      final secureEnclavePlugin = SecureEnclave();
      await secureEnclavePlugin.generateKeyPair(
        accessControl: AccessControlModel(
          options: [
            // AccessControlOption.applicationPassword,
            AccessControlOption.privateKeyUsage,
          ],
          tag: alias,
        ),
      );
      return true;
    } else {
      return HardwareEncryptionPlatform.instance.generateKey(alias);
    }
  }

  static Future<String?> encrypt(String alias, String data) async {
    if (Platform.isIOS) {
      final secureEnclavePlugin = SecureEnclave();
      final res = await secureEnclavePlugin.encrypt(
        message: data,
        tag: alias,
      );

      if (res.error != null) {
        // (res.error!.desc.toString());
      } else if (res.value != null) {
        return base64Encode(res.value!);
      }
      return null;
    }
    return HardwareEncryptionPlatform.instance.encrypt(alias, data);
  }

  static Future<String?> decrypt(String alias, String data) async {
    if (Platform.isIOS) {
      final secureEnclavePlugin = SecureEnclave();
      final bytes = base64Decode(data);
      final res = await secureEnclavePlugin.decrypt(
        message: bytes,
        tag: alias,
      );

      if (res.error != null) {
        print(res.error!.desc.toString());
        return null;
      } else {
        return res.value;
      }
    } else {
      return await HardwareEncryptionPlatform.instance.decrypt(alias, data);
    }
  }
}
