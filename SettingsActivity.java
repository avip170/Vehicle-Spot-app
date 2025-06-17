public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
    super.onAuthenticationSucceeded(result);
    Toast.makeText(SettingsActivity.this, "Authentication succeeded!", Toast.LENGTH_SHORT).show();
    Button biometricButton = findViewById(R.id.buttonBiometric);
    biometricButton.setEnabled(false); // Disable after success
    // Proceed with secure action
}