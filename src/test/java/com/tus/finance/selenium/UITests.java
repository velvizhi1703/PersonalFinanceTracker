package com.tus.finance.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
// or for multiple common matchers:
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UITests {
	private WebDriver driver;
	private WebDriverWait wait;
	private final String BASE_URL = "http://localhost:9091";

	@BeforeAll
	void setup() {
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--remote-allow-origins=*");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		driver = new ChromeDriver(options);
		wait = new WebDriverWait(driver, Duration.ofSeconds(15));
	}

	@BeforeEach
	void beforeEach() {
		driver.get(BASE_URL + "/index.html");
		wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));
	}

	// Helper methods
	private void login(String email, String password) {
		try {
			// Navigate directly to login page
			driver.get(BASE_URL + "/index.html#login");

			// Wait for login form to be present
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginForm")));

			WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginEmail")));
			WebElement passwordField = driver.findElement(By.id("loginPassword"));
			WebElement loginButton = driver.findElement(By.id("loginButton"));

			emailField.clear();
			emailField.sendKeys(email);
			passwordField.clear();
			passwordField.sendKeys(password);

			// Use JavaScript click as fallback
			try {
				loginButton.click();
			} catch (Exception e) {
				((JavascriptExecutor)driver).executeScript("arguments[0].click();", loginButton);
			}

			// Wait for either success or error
			wait.until(ExpectedConditions.or(
					ExpectedConditions.urlContains("dashboard"),
					ExpectedConditions.presenceOfElementLocated(By.id("loginAlert"))
					));

			// Check for error
			WebElement alert = driver.findElement(By.id("loginAlert"));
			if (alert.isDisplayed() && alert.getText().toLowerCase().contains("error")) {
				throw new RuntimeException("Login failed: " + alert.getText());
			}

		} catch (Exception e) {
			takeScreenshot("login_error");
			throw new RuntimeException("Login failed: " + e.getMessage(), e);
		}
	}

	private void logout() {
		driver.get(BASE_URL + "/logout");
		wait.until(ExpectedConditions.urlContains("login"));
	}

	private void takeScreenshot(String testName) {
		try {
			if (((RemoteWebDriver) driver).getSessionId() != null) {
				File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				FileUtils.copyFile(screenshot, new File("target/screenshots/" + testName + ".png"));
			}
		} catch (Exception e) {
			System.err.println("Failed to take screenshot: " + e.getMessage());
		}
	}
	// Regular user tests
	@Test
	@Order(1)
	void testRegularUserLogin() {
		try {
			// Navigate to login page directly
			driver.get(BASE_URL + "/index.html#login");

			// Wait for page to fully load
			wait.until(webDriver -> ((JavascriptExecutor) webDriver)
					.executeScript("return document.readyState").equals("complete"));

			// Debug: Print current state
			System.out.println("Current URL: " + driver.getCurrentUrl());
			System.out.println("Page title: " + driver.getTitle());
			takeScreenshot("before_login_attempt");

			// Find elements using correct selectors
			WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.id("loginEmail")));
			WebElement passwordField = driver.findElement(By.id("loginPassword"));
			// Change this in your test code
			WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginButton")));

			// Enter credentials
			emailField.clear();
			emailField.sendKeys("keerthi@example.com");
			passwordField.clear();
			passwordField.sendKeys("keerthi123");

			// Click using JavaScript to ensure it works
			((JavascriptExecutor)driver).executeScript("arguments[0].click();", loginButton);

			// Handle the success case first
			try {
				// Wait for either dashboard to load or success message
				wait.until(ExpectedConditions.or(
						ExpectedConditions.urlContains("dashboard"),
						ExpectedConditions.presenceOfElementLocated(By.cssSelector(".dashboard-content")),
						ExpectedConditions.textToBePresentInElementLocated(
								By.id("loginAlert"), "Login Successful! Redirecting...")
						));

				// If we get here, login was successful
				System.out.println("Login successful, checking for dashboard...");

				// Wait for navigation to complete
				wait.until(ExpectedConditions.or(
						ExpectedConditions.urlContains("dashboard"),
						ExpectedConditions.presenceOfElementLocated(By.cssSelector(".dashboard-content"))
						));

				// Verify successful login
				assertTrue(driver.getCurrentUrl().contains("dashboard") || 
						driver.findElements(By.cssSelector(".dashboard-content")).size() > 0,
						"Should be redirected after login");

			} catch (TimeoutException e) {
				// Check if there was an actual error message
				WebElement alert = driver.findElement(By.id("loginAlert"));
				if (alert.isDisplayed() && !alert.getText().contains("Login Successful")) {
					fail("Login failed with error: " + alert.getText());
				} else {
					fail("Login neither succeeded nor showed an error message");
				}
			}

			takeScreenshot("after_login_attempt");

		} catch (Exception e) {
			takeScreenshot("login_failure");
			throw new RuntimeException("Login test failed", e);
		} 
	}

	@Test
	@Order(2)
	void testTransactionHistory() {
		login("keerthi@example.com", "keerthi123");

		// Wait for dashboard to load
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.cssSelector(".dashboard-content")));

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
		WebElement transactionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("transactionsHistoryLink")));
		transactionsLink.click();

		// Wait for transaction table to load
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.id("transactionTableBody")));

		// Verify transactions are loaded
		wait.until(d -> {
			List<WebElement> rows = d.findElements(By.cssSelector("#transactionTableBody tr"));
			return !rows.isEmpty();
		});
	}

	@Test
	@Order(3)
	void testNewTransaction() {
		try {
			// 1. Navigate to transaction page
			driver.get(BASE_URL + "/index.html#new-transaction");

			// 2. Wait for form to load completely
			wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("transactionForm")));
			takeScreenshot("new_transaction_page_loaded");

			// Debug: Print page title and form status
			System.out.println("Page title: " + driver.getTitle());
			System.out.println("Form displayed: " + driver.findElement(By.id("transactionForm")).isDisplayed());

			// 3. Fill the form with test data
			// Amount field
			WebElement amountField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
			amountField.clear();
			amountField.sendKeys("50.00");

			// Transaction Type dropdown
			WebElement typeDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("type")));
			new Select(typeDropdown).selectByVisibleText("Debit");

			// Category dropdown
			WebElement categoryDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("category")));
			new Select(categoryDropdown).selectByVisibleText("Food");

			// Date field - using current date in correct format
			WebElement dateField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("date")));
			String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			((JavascriptExecutor)driver).executeScript(
					"arguments[0].value = arguments[1];", 
					dateField, 
					currentDate
					);

			// Debug: Print all form values before submission
			System.out.println("Form values before submission:");
			System.out.println("Amount: " + amountField.getAttribute("value"));
			System.out.println("Type: " + new Select(typeDropdown).getFirstSelectedOption().getText());
			System.out.println("Category: " + new Select(categoryDropdown).getFirstSelectedOption().getText());
			System.out.println("Date: " + dateField.getAttribute("value"));
			takeScreenshot("form_filled");

			// 4. Submit the form
			WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
					By.cssSelector("#transactionForm button[type='submit']")));

			// Verify button is enabled and visible
			assertTrue(saveButton.isEnabled(), "Save button should be enabled");
			assertTrue(saveButton.isDisplayed(), "Save button should be visible");

			// Scroll to button and click using JavaScript
			((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", saveButton);
			((JavascriptExecutor)driver).executeScript("arguments[0].click();", saveButton);

			// 5. Verify successful submission
			try {
				// Wait for either success message or redirect
				wait.until(ExpectedConditions.or(
						// Option 1: Success message appears
						ExpectedConditions.visibilityOfElementLocated(
								By.cssSelector("#alertMessage.alert-success:not(.d-none)")),

						// Option 2: URL changes
						ExpectedConditions.urlContains("dashboard"),
						ExpectedConditions.urlContains("transactions"),

						// Option 3: Form gets reset
						ExpectedConditions.textToBe(By.id("amount"), ""),
						ExpectedConditions.textToBe(By.id("date"), "")
						));

				// Additional verification if success message appears
				List<WebElement> successMessages = driver.findElements(
						By.cssSelector("#alertMessage.alert-success:not(.d-none)"));
				if (!successMessages.isEmpty()) {
					assertTrue(successMessages.get(0).isDisplayed(), 
							"Success message should be visible");
					System.out.println("Success message: " + successMessages.get(0).getText());
				} else {
					System.out.println("Form submitted successfully (no message but URL or form changed)");
				}

			} catch (TimeoutException e) {
				// Check for error messages
				List<WebElement> errorMessages = driver.findElements(
						By.cssSelector("#alertMessage.alert-danger:not(.d-none)"));

				if (!errorMessages.isEmpty()) {
					String errorText = errorMessages.get(0).getText();
					System.out.println("Error message: " + errorText);
					fail("Submission failed with error: " + errorText);
				} else {
					// Check console logs for JavaScript errors
					LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
					if (!logs.getAll().isEmpty()) {
						System.out.println("Browser console errors:");
						logs.forEach(log -> System.out.println(log.getMessage()));
					}

					takeScreenshot("submission_failed_no_error_message");
					fail("Transaction submission failed without any visible error message");
				}
			}

			takeScreenshot("after_submission");

		} catch (Exception e) {
			takeScreenshot("test_new_transaction_error");
			System.err.println("Test failed with exception: " + e.getMessage());
			throw e;
		}
	}

	@Test
	@Order(4)
	void testDeleteTransaction() throws Exception {
		try {
			// 1. Login
			login("keerthi@example.com", "keerthi123");

			// 2. Navigate to transactions
			WebElement transactionTab = wait.until(ExpectedConditions.elementToBeClickable(
					By.id("transactionsHistoryLink")));
			((JavascriptExecutor)driver).executeScript("arguments[0].click();", transactionTab);

			// 3. Wait for table to load
			wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
					By.cssSelector("#transactionTableBody tr"), 0));

			// 4. Get initial row count
			List<WebElement> initialRows = driver.findElements(
					By.cssSelector("#transactionTableBody tr"));
			int initialRowCount = initialRows.size();

			if (initialRowCount == 0) {
				fail("No transactions available to delete");
			}

			// 5. Click delete button on first row
			WebElement firstDeleteButton = wait.until(ExpectedConditions.elementToBeClickable(
					By.cssSelector("#transactionTableBody tr:first-child .btn-danger")));
			((JavascriptExecutor)driver).executeScript("arguments[0].click();", firstDeleteButton);

			// 6. Handle confirmation dialog
			try {
				WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(3));
				Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());

				// Verify this is the confirmation dialog
				String alertText = alert.getText();
				assertThat(alertText, containsString("Are you sure you want to delete this transaction?"));

				// Accept the confirmation
				alert.accept();

				// Now wait for success message (either alert or on-page notification)
				try {
					// Option 1: If success shows as another alert
					Alert successAlert = new WebDriverWait(driver, Duration.ofSeconds(5))
							.until(ExpectedConditions.alertIsPresent());
					assertThat(successAlert.getText(), containsString("Transaction deleted successfully"));
					successAlert.accept();
				} catch (TimeoutException e) {
					// Option 2: If success shows as an on-page element
					WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
							By.cssSelector(".alert-success")));
					assertThat(successMessage.getText(), containsString("Transaction deleted successfully"));
				}

			} catch (TimeoutException e) {
				// Handle modal confirmation case
				WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
						By.cssSelector(".modal-footer .btn-primary")));
				confirmButton.click();

				// Wait for success notification
				WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
						By.cssSelector(".alert-success, .toast-success")));
				assertThat(successMessage.getText(), containsString("Transaction deleted successfully"));
			}

			// 7. Verify row count decreased
			wait.until(ExpectedConditions.numberOfElementsToBe(
					By.cssSelector("#transactionTableBody tr"), initialRowCount - 1));

		} catch (Exception e) {
			// Screenshot and error handling remains the same
			throw e;
		}
	}

	@Test
	@Order(5)
	void testAdminLogin() {
		// Clear session and navigate to login page
		driver.manage().deleteAllCookies();
		driver.get(BASE_URL + "/index.html#login");

		// Wait for login form to be present (with extended timeout)
		WebElement loginForm = wait.until(ExpectedConditions.presenceOfElementLocated(
				By.cssSelector("form#loginForm")));

		// Fill credentials with explicit waits
		WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(
				By.cssSelector("input#loginEmail, input[name='email']")));
		emailField.clear();
		emailField.sendKeys("vel@example.com");

		WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(
				By.cssSelector("input#loginPassword, input[name='password']")));
		passwordField.clear();
		passwordField.sendKeys("vel123");

		// Find login button using more flexible selector
		WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
				By.xpath("//button[contains(text(),'Login') or contains(@class,'login-button')]")));

		// Scroll to button and click using JavaScript
		((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", loginButton);
		((JavascriptExecutor)driver).executeScript("arguments[0].click();", loginButton);

		// Verify admin dashboard loaded
		try {
			wait.until(ExpectedConditions.or(
					ExpectedConditions.urlContains("admin"),
					ExpectedConditions.presenceOfElementLocated(
							By.cssSelector(".admin-dashboard, [href='#users']"))
					));

			// Additional verification
			String currentUrl = driver.getCurrentUrl();
			Assertions.assertTrue(currentUrl.contains("admin") || currentUrl.contains("dashboard"));

		} catch (TimeoutException e) {
			takeScreenshot("admin_login_failure");
			fail("Admin dashboard not loaded after login. Current URL: " + driver.getCurrentUrl());
		}
	}


    @AfterEach
	void tearDown() {
		try {
			// Clear session storage
			((JavascriptExecutor)driver).executeScript("window.sessionStorage.clear();");
			// Clear local storage
			((JavascriptExecutor)driver).executeScript("window.localStorage.clear();");
			// Delete all cookies
			driver.manage().deleteAllCookies();
			// Navigate to blank page
			driver.get("about:blank");
		} catch (Exception e) {
			System.err.println("Cleanup failed: " + e.getMessage());
		}
	}

	@AfterAll
	void teardown() {
		if (driver != null) {
			driver.quit();
		}
	}
}