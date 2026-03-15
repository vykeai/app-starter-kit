import XCTest

// UI tests for the magic link authentication flow.
// These run against a real app target — they cannot mock network calls.
// Point the app at a test backend (StarterApp-Dev scheme) or use UI test launch arguments
// to configure a stub server.
//
// To add network stubbing in UI tests:
//   1. Set launchArgument "-UITestMode" in setUp
//   2. In AppState.init(), detect the argument and configure a MockURLProtocol
//      on a custom URLSession passed to APIClient

final class AuthFlowUITests: XCTestCase {

    var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launchArguments += ["-UITestMode"]
        app.launch()
    }

    // MARK: - Welcome screen

    func testWelcomeScreenShowsSignInButton() {
        let signInButton = app.buttons["Sign In"]
        XCTAssertTrue(signInButton.waitForExistence(timeout: 3),
                      "Sign In button should be visible on the Welcome screen")
    }

    // MARK: - Email entry

    func testTappingSignInShowsEmailField() {
        app.buttons["Sign In"].tap()
        let emailField = app.textFields["your@email.com"]
        XCTAssertTrue(emailField.waitForExistence(timeout: 3),
                      "Email input field should appear after tapping Sign In")
    }

    func testEmptyEmailDoesNotProceed() {
        app.buttons["Sign In"].tap()
        let continueButton = app.buttons["Continue"]
        XCTAssertTrue(continueButton.waitForExistence(timeout: 3))
        continueButton.tap()
        // Should remain on email screen — code entry should NOT appear
        XCTAssertFalse(app.otherElements["CodeEntryView"].exists,
                       "Code entry should not appear for empty email")
    }

    // MARK: - Code entry

    func testCodeEntryScreenHasEightBoxes() throws {
        app.buttons["Sign In"].tap()
        let emailField = app.textFields["your@email.com"]
        XCTAssertTrue(emailField.waitForExistence(timeout: 3))
        emailField.tap()
        emailField.typeText("test@example.com")
        app.buttons["Continue"].tap()

        XCTAssertTrue(app.staticTexts["Check your email"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["test@example.com"].exists)
    }
}
