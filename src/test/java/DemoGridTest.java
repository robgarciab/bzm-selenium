import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runner.Description;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import static org.junit.Assert.*;

public class DemoGridTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoGridTest.class);

    private final static String API_KEY = System.getenv("API_KEY");
    private final static String API_SECRET = System.getenv("API_SECRET");
    private final static String BASE = "a.blazemeter.com";


    private final static String curl = String.format("https://%s/api/v4/grid/wd/hub", BASE);

    private static RemoteWebDriver driver;

    @Rule
    public final TestName bzmTestCaseReporter = new TestName() {

        @Override
        protected void starting(Description description) {
            Map<String, String> map = new HashMap<>();
            map.put("testCaseName", description.getMethodName());
            map.put("testSuiteName", description.getClassName());
            driver.executeAsyncScript("/* FLOW_MARKER test-case-start */", map);
        }

        @Override
        protected void succeeded(Description description) {
            if (driver != null) {
                Map<String, String> map = new HashMap<>();
                map.put("status", "success");
                map.put("message", "");
                driver.executeAsyncScript("/* FLOW_MARKER test-case-stop */", map);
            }
        }

        @Override
        protected void failed(Throwable e, Description description) {
            Map<String, String> map = new HashMap<>();
            if (e instanceof AssertionError) {
                map.put("status", "failed");
            } else {
                map.put("status", "broken");
            }
            map.put("message", e.getMessage());
            driver.executeAsyncScript("/* FLOW_MARKER test-case-stop */", map);
        }
    };

    @BeforeClass
    public static void setUp() throws MalformedURLException {
        URL url = new URL(curl);

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("blazemeter.apiKey", API_KEY);
        capabilities.setCapability("blazemeter.apiSecret", API_SECRET);

        capabilities.setCapability("blazemeter.reportName", "Add Products to Car");
        capabilities.setCapability("blazemeter.sessionName", "Chrome browser test");

        capabilities.setCapability("blazemeter.projectId", "1437484");
        capabilities.setCapability("blazemeter.testId", "13269058");
//        capabilities.setCapability("blazemeter.buildId", "randomString");
//        capabilities.setCapability("blazemeter.locationId", "harbor-");

        capabilities.setCapability("browserName", "chrome");
//        capabilities.setCapability("browserVersion", "69");

        driver = new RemoteWebDriver(url, capabilities);

        String reportURL = String.format("https://%s/api/v4/grid/sessions/%s/redirect/to/report", BASE, driver.getSessionId());
        System.out.println("Report url: " + reportURL);
        openInBrowser(reportURL);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    public static void openInBrowser(String string) {
        if (java.awt.Desktop.isDesktopSupported()) {
            try {
                java.awt.Desktop.getDesktop().browse(new URI(string));
            } catch (Exception ex) {
                LOGGER.warn("Failed to open in browser", ex);
            }
        }
    }

    @AfterClass
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void shouldAddProduct1ToCart() throws InterruptedException {
        String productURL = getProductURL(1);
        driver.get(productURL);
        driver.findElement(By.xpath("//*[text() = \"Add to cart\"]")).click();
        Thread.sleep(2000);
        driver.switchTo().alert().accept();
        Thread.sleep(2000);
        driver.findElement(By.xpath("//*[text() = \"Cart\"]")).click();
        Thread.sleep(2000);
        driver.findElement(By.xpath("//*[text() = \"Place Order\"]")).click();
        Thread.sleep(2000);
        assertEquals("Place order", driver.findElement(By.xpath("//*[text() = \"Place order\"]")).getText());
    }

    @Test
    public void shouldAddProduct2ToCart() throws InterruptedException {
        String productURL = getProductURL(2);
        driver.get(productURL);
        driver.findElement(By.xpath("//*[text() = \"Add to cart\"]")).click();
        Thread.sleep(2000);
        driver.switchTo().alert().accept();
        Thread.sleep(2000);
        driver.findElement(By.xpath("//*[text() = \"Cart\"]")).click();
        Thread.sleep(2000);
        driver.findElement(By.xpath("//*[text() = \"Place Order\"]")).click();
        Thread.sleep(2000);
        assertEquals("Place order", driver.findElement(By.xpath("//*[text() = \"Place order\"]")).getText());
    }

    public static String getProductURL(int productId) {
        String url = "http://mockservicedynamicurl312311.mock.blazemeter.com/products?id=" + productId;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            // Set the appropriate headers if any
            // request.setHeader("Accept", "application/json");

            try (CloseableHttpResponse response = client.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(responseBody);

                // Assuming $.product_url is at the root level of the JSON structure
                JsonNode productUrlNode = rootNode.path("product_url");

                if (!productUrlNode.isMissingNode()) {
                    return productUrlNode.asText();
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
