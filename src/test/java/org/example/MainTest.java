package org.example;

import io.netty.channel.ConnectTimeoutException;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainTest {
    private WebDriver driver;

    @BeforeClass
    public void setUp() {
        // Initialize WebDriver with additional options
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Ajdin\\Desktop\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
    }

//1. Testing Search For Correct Keywords
    @Test
    public void testSearchFunctionalityForVariousKeywords() throws InterruptedException {
        driver.get("https://domod.ba");

        String[] searchKeywords = {
                "laptop",
                "telefon",
                "televizor"
        };

        for (String keyword : searchKeywords) {
            WebElement searchBox = driver.findElement(By.id("autocomplete"));
            searchBox.clear();
            searchBox.sendKeys(keyword);
            searchBox.submit();
            Thread.sleep(1000);
            assertTrue( "Search results are not displayed for keyword: " + keyword,driver.findElements(By.cssSelector(".list-product")).size() > 0);
        }
    }


    //2. Testing Navigation To Categories
    @Test
    public void testNavigationToCategories() throws InterruptedException {
        driver.get("https://domod.ba");

        String[] categories = {
                "/shop/bijela-tehnika",
                "/shop/mali-kucanski-aparati",
                "/shop/telefonija"
        };

        for (String categoryPath : categories) {
            WebElement categoryButton = driver.findElement(By.cssSelector(".categories .expand-link[href='https://domod.ba" + categoryPath + "']"));
            categoryButton.click();
            Thread.sleep(1000);
            assertTrue( "Navigation to category '" + categoryPath + "' failed.",driver.getCurrentUrl().contains(categoryPath));
            driver.navigate().back();
        }
    }

    //3. Testing Adding Items To Cart
    @Test
    public void testAddingItemsToCart() throws InterruptedException {
        driver.get("https://domod.ba/shop/proizvod/philips-lumea-bri977-00-glatkoljeto/ST6928");

        Thread.sleep(1000);
        WebElement addToCartButton = driver.findElement(By.cssSelector(".red-btn.add-to-cart"));
        addToCartButton.click();
        Thread.sleep(2000);

        WebElement cartCount = driver.findElement(By.id("number-of-items"));
        String cartCountText = cartCount.getText();
        Thread.sleep(1000);
        assertTrue("Item was not added to cart", !cartCountText.isEmpty() && Integer.parseInt(cartCountText) > 0);

    }

    //4. Test Removing Items From Cart
    @Test
    public void testRemovingItemsFromCart() throws InterruptedException {
        driver.get("https://domod.ba/shop/proizvod/philips-lumea-bri977-00-glatkoljeto/ST6928");

        Thread.sleep(1000);
        WebElement addToCartButton = driver.findElement(By.cssSelector(".red-btn.add-to-cart"));
        addToCartButton.click();
        Thread.sleep(2000);
        //If you don't have cookies , this will cause the program to crash, just remove the cookiePopup
        //code and it will work, we can explain on project defense if needed.
        WebElement cookiePopup = driver.findElement(By.xpath("/html/body/div[4]/a[1]"));
        if (cookiePopup.isDisplayed()) {
            cookiePopup.click();
            System.out.println("Cookie popup clicked.");
        }
        WebElement removeButton = driver.findElement(By.cssSelector(".clear"));
        removeButton.click();
        Thread.sleep(1000);
        WebElement cartCount = driver.findElement(By.id("number-of-items"));
        assertEquals(Integer.parseInt(cartCount.getText()), 0, "Item was not removed from the cart.");
    }

    //5. Test Promotional Banner Functionality
    @Test
    public void testPromotionalBannerFunctionality() throws InterruptedException {
        driver.get("https://domod.ba");

        Thread.sleep(1000);
        WebElement promoBanner = driver.findElement(By.cssSelector(".banner-counter .banner-counter-cta"));
        promoBanner.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue("Promotional banner link is incorrect.", currentUrl.contains("/catalogs"));
    }


    //6. test Responsive Design
    @Test
    public void testResponsiveDesign() throws InterruptedException {
        driver.get("https://domod.ba");
        Thread.sleep(1000);


        List<Dimension> screenSizes = Arrays.asList(
                new Dimension(375, 812), // iPhone size
                new Dimension(768, 1024), // iPad size
                new Dimension(1366, 768) // Desktop size
        );

        for (Dimension size : screenSizes) {
            driver.manage().window().setSize(size);


            if (size.getWidth() <= 768) {
                WebElement mobileMenuButton = driver.findElement(By.cssSelector(".navbar-toggle"));
                Thread.sleep(1000);
                assertTrue( "Mobile menu button is not visible on screen size: " + size,mobileMenuButton.isDisplayed());
            } else {
                WebElement desktopMenu = driver.findElement(By.id("side-navbar"));
                assertTrue( "Desktop menu is not visible on screen size: " + size,desktopMenu.isDisplayed());
            }
        }
    }

    //7. Testing Connection Security
    @Test
    public void testSecureConnection() {
        driver.get("https://domod.ba");

        assertTrue("The website does not use HTTPS.", driver.getCurrentUrl().startsWith("https://"));

        List<WebElement> insecureResources = driver.findElements(By.xpath("//img[starts-with(@src, 'http://')]"));
        assertTrue("There are mixed content issues with non-secure resources.",insecureResources.isEmpty());

    }

//8. Testing Invalid Search Messages
    @Test
    public void testInvalidSearchErrorMessage() throws InterruptedException {
        driver.get("https://domod.ba");

        List<String> invalidQueries = Arrays.asList("invalidSearchQuery123", "%%%###@@@", "1234567890");

        for (String query : invalidQueries) {
            WebElement searchBox = driver.findElement(By.id("autocomplete"));
            searchBox.clear();
            searchBox.sendKeys(query);
            searchBox.submit();

            Thread.sleep(2000);

            WebElement noResults = driver.findElement(By.cssSelector(".error-message"));
            assertTrue("No results message is not displayed for query: " + query, noResults.isDisplayed());
        }
    }

    //9. Test Form Submission
    @Test
    public void testFormSubmission() throws InterruptedException {
        driver.get("https://domod.ba/kontakt");

        Thread.sleep(1000);
        WebElement nameField = driver.findElement(By.id("name"));
        WebElement surnameField = driver.findElement(By.id("surname"));
        WebElement phoneField = driver.findElement(By.id("phone"));
        WebElement emailField = driver.findElement(By.id("email"));
        WebElement messageField = driver.findElement(By.id("msg"));

        WebElement submitButton = driver.findElement(By.cssSelector(".red-btn.pull-right"));
        nameField.sendKeys("Testo");
        surnameField.sendKeys("Uzerović");
        phoneField.sendKeys("062123123");
        emailField.sendKeys("test@example.com");
        messageField.sendKeys("This is a test message.");
        //If you don't have cookies , this will cause the program to crash, just remove the cookiePopup
        //code and it will work, we can explain on project defense if needed.
            WebElement cookiePopup = driver.findElement(By.xpath("/html/body/div[4]/a[1]"));
            if (cookiePopup.isDisplayed()) {
                cookiePopup.click();
                System.out.println("Cookie popup clicked.");
            }
        Thread.sleep(1000);
        submitButton.click();
        Thread.sleep(2000);
        WebElement successMessage = driver.findElement(By.cssSelector(".alert.alert-success"));
        Assert.assertTrue(successMessage.isDisplayed(), "Form submission failed for the first data set.");
        driver.get("https://domod.ba/kontakt");
        nameField = driver.findElement(By.id("name"));
        surnameField = driver.findElement(By.id("surname"));
        phoneField = driver.findElement(By.id("phone"));
        emailField = driver.findElement(By.id("email"));
        messageField = driver.findElement(By.id("msg"));
        submitButton = driver.findElement(By.cssSelector(".red-btn.pull-right"));
        nameField.sendKeys("Drugi");
        surnameField.sendKeys("Test");
        phoneField.sendKeys("061111222");
        emailField.sendKeys("test2@example.com");
        messageField.sendKeys("This is a test message2.");
        submitButton.click();
        successMessage = driver.findElement(By.cssSelector(".alert.alert-success"));
        assertTrue("Form submission failed for the second data set.", successMessage.isDisplayed());
    }



    // 10. Validate Website Page Load Performance
    @Test
    public void testPageLoadPerformance() {
        long startTime = System.currentTimeMillis();
        driver.get("https://domod.ba");
        long endTime = System.currentTimeMillis();
        long loadTime = endTime - startTime;
        assertTrue("Page load time exceeded 3 seconds.", loadTime < 3000);
    }


    //Additional 5 test scenarios

    //1. Testing Social Media Links
    @Test
    public void testSocialMediaLinksForVariousPlatforms() throws InterruptedException {
        driver.get("https://www.domod.ba");
        Thread.sleep(1000);
        List<WebElement> socialLinks = driver.findElements(By.cssSelector(".footer-social a"));
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            assertTrue("Unexpected social media link: " + href,href.contains("facebook.com") || href.contains("instagram.com"));
        }
    }


    // 2. Test Navigation to Category and Sorting by Lowest Price
    @Test
    public void testSortingLowestPrice() throws InterruptedException {
        driver.get("https://domod.ba");
        WebElement categoryButton = driver.findElement(By.cssSelector(".categories .expand-link[href='https://domod.ba/shop/bijela-tehnika']"));
        categoryButton.click();
        Thread.sleep(2000);
        assertTrue("Navigation to Bijela Tehnika failed", driver.getCurrentUrl().contains("/shop/bijela-tehnika"));


        WebElement sortDropdown = driver.findElement(By.id("dropdownMenu1"));
        sortDropdown.click();
        Thread.sleep(2000);
        WebElement sortByLowestPrice = driver.findElement(By.cssSelector("a[data-sort='price'][data-order='asc']"));
        sortByLowestPrice.click();
        Thread.sleep(2000);
        List<WebElement> prices = driver.findElements(By.className("product-price"));
        for (int i = 0; i < prices.size() - 1; i++) {
            double price1 = Double.parseDouble(prices.get(i).getText().replace("$", ""));
            double price2 = Double.parseDouble(prices.get(i + 1).getText().replace("$", ""));
            assertTrue(price1 <= price2);
        }
    }

    //3. Testing Katalog page
    @Test
    public void testKatalog() throws InterruptedException {
        driver.get("https://www.domod.ba/katalozi");

        WebElement smallButton = driver.findElement(By.className("small-btn"));
        smallButton.click();
        Thread.sleep(2000);
        String currentUrl = driver.getCurrentUrl();
        assertEquals("https://domod.ba/assets/files/catalogs/1734693596-sta-ima-novo-u-domodu.pdf", currentUrl);
    }

    //4. Testing adding same product multiple times, then filling form and proceeding checkout
    @Test
    public void testAddingMultipleItemsToCart() throws InterruptedException {
        driver.get("https://domod.ba/shop/proizvod/philips-lumea-bri977-00-glatkoljeto/ST6928");

        WebElement addToCartButton = driver.findElement(By.cssSelector(".red-btn.add-to-cart"));
        addToCartButton.click();
        Thread.sleep(2000);
        WebElement addAnotherOneButton=driver.findElement(By.xpath("//*[@id=\"main-content\"]/ul[3]/li[2]/ul/li[4]/form/div/div[1]"));
        addAnotherOneButton.click();
        //If you don't have cookies , this will cause the program to crash, just remove the cookiePopup
        //code and it will work, we can explain on project defense if needed.
        try {
            WebElement cookiePopup = driver.findElement(By.xpath("/html/body/div[4]/a[1]"));
            if (cookiePopup.isDisplayed()) {
                cookiePopup.click();
                System.out.println("Cookie popup clicked.");
            }
        } catch (NoSuchElementException e) {
            System.out.println("Cookie popup not found.");
        }

        WebElement finishPurchaseButton = driver.findElement(By.xpath("//a[contains(@class, 'checkout-btn') and text()='Završi kupovinu']"));
        finishPurchaseButton.click();
        Thread.sleep(1000);
        WebElement nameField = driver.findElement(By.id("buyer_name"));
        nameField.sendKeys("John Doe");
        WebElement emailField = driver.findElement(By.id("buyer_email"));
        emailField.sendKeys("johndoe@example.com");
        WebElement phoneField = driver.findElement(By.id("buyer_phone"));
        phoneField.sendKeys("123456789");
        WebElement addressField = driver.findElement(By.id("buyer_address"));
        addressField.sendKeys("123 Main Street");
        WebElement cityField = driver.findElement(By.id("buyer_city"));
        cityField.sendKeys("Sarajevo");
        WebElement postalCodeField = driver.findElement(By.id("buyer_postal_code"));
        postalCodeField.sendKeys("71000");
        WebElement countryField = driver.findElement(By.id("buyer_country"));
        countryField.sendKeys("Bosnia and Herzegovina");
        WebElement submitButton = driver.findElement(By.xpath("//*[@id=\"main-content\"]/div/div[1]/form/button"));
        submitButton.click();
        Thread.sleep(2000);
        WebElement confirmationMessage = driver.findElement(By.xpath("//*[@id=\"main-content\"]/h1"));
        assertTrue(confirmationMessage.isDisplayed());


    }

    //5. Testing Location page fullscreen and finding a domod shop on map
    @Test
    public void testLocationFullscreen() throws InterruptedException {
        driver.get("https://www.domod.ba");
        WebElement locationSection=driver.findElement(By.xpath("//*[@id=\"navbar\"]/ul/li[5]/a"));
        locationSection.click();
        Thread.sleep(4000);
        WebElement fullScreenButton=driver.findElement(By.xpath("//*[@id=\"locations\"]/div/div[3]/div[7]/button"));
        fullScreenButton.click();
        Thread.sleep(2000);
        WebElement domodTuzlaButton=driver.findElement(By.xpath("//*[@id=\"locations\"]/div/div[3]/div[1]/div[2]/div/div[3]/div[16]/img"));
        domodTuzlaButton.click();
        Thread.sleep(2000);
        WebElement domodTuzlaText=driver.findElement(By.xpath("//*[@id=\"locations\"]/div/div[3]/div[1]/div[2]/div/div[4]/div/div/div/div[1]/div[2]/div/div"));
        assertTrue("Text is not Domod Tuzla,",domodTuzlaText.getText().equals("Domod Tuzla"));
    }




    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
