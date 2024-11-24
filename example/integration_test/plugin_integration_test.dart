// This is a basic Flutter integration test.
//
// Since integration tests run in a full Flutter application, they can interact
// with the host side of a plugin implementation, unlike Dart unit tests.
//
// For more information about Flutter integration tests, please see
// https://flutter.dev/to/integration-testing

import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';

import 'package:hardware_encryption/hardware_encryption.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('getPlatformVersion test', (WidgetTester tester) async {
    final HardwareEncryption plugin = HardwareEncryption();
    final String? version = await plugin.getPlatformVersion();
    // The version string depends on the host platform running the test, so
    // just assert that some non-empty string is returned.
    print('Version: $version');
    expect(version?.isNotEmpty, true);
  });

  testWidgets('encrypt/decrypt test', (WidgetTester tester) async {
    final String data = 'Hello, World!';
    final String alias = 'test';
    final bool? success = await HardwareEncryption.generateKey(alias);
    expect(success, true);
    final String? encryptedData = await HardwareEncryption.encrypt(alias, data);
    print("Encrypted data: $encryptedData");
    expect(encryptedData?.isNotEmpty, true);
    final String? decryptedData =
        await HardwareEncryption.decrypt(alias, encryptedData!);
    print("Decrypted data: $decryptedData");
    expect(decryptedData, data);
  });
}
