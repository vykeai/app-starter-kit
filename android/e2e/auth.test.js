describe('Auth flow', () => {
  beforeAll(async () => {
    await device.launchApp({ newInstance: true });
  });

  afterEach(async () => {
    await device.reloadReactNative?.() ?? null;
  });

  it('should show welcome screen on launch', async () => {
    await expect(element(by.text('Get Started'))).toBeVisible();
    await expect(element(by.text('AppStarterKit'))).toBeVisible();
  });

  it('should navigate to email input on tap', async () => {
    await element(by.text('Get Started')).tap();
    await expect(element(by.text('Enter your email'))).toBeVisible();
  });

  it('should show error for empty email', async () => {
    // Clear any existing text and submit empty
    await element(by.id('emailTextField')).clearText();
    await element(by.text('Continue')).tap();
    await expect(element(by.text('Please enter your email'))).toBeVisible();
  });
});
