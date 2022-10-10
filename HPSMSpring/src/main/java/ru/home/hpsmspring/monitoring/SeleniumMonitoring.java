package ru.home.hpsmspring.monitoring;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.home.hpsmspring.hpsm.HPSM;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class SeleniumMonitoring {

    private ChromeOptions options;
    private ChromeDriver driver;

    @Value("${selenium.url}")
    private String HPSM_URL;
    @Value("${selenium.login}")
    private String HPSM_LOGIN;
    @Value("${selenium.password}")
    private String HPSM_PASSWORD;

    public void startSelenium() {

        WebDriverManager.chromedriver().setup();

        options = new ChromeOptions();

        options.setHeadless(true);

        options.addArguments("--no-sandbox");
//        options.addArguments("--disable-gpu-sandbox");
//        options.addArguments("--disable-dev-shm-usage");
//        options.add_argument("--headless") # Runs Chrome in headless mode.
//                options.add_argument('--no-sandbox') # # Bypass OS security model
        options.addArguments("start-maximized");
//        options.add_argument('disable-infobars')

        options.setImplicitWaitTimeout(Duration.ofSeconds(120));
        options.setPageLoadTimeout(Duration.ofSeconds(120));
        options.setScriptTimeout(Duration.ofSeconds(120));

        driver = new ChromeDriver(options);

        Dimension dim = new Dimension(1280, 720);
        driver.manage().window().setSize(dim);


        //заходим на нужный урл
        driver.get(HPSM_URL);

        authorizationHpsm();

        //подсчитываем количество фреймов(видимая обланость внутри браузера)
//        List<WebElement> f2 = driver.findElements(By.xpath("//iframe"));
//        System.out.println("Total number " + f2.size());

        timeUnitSleep(5);
    }

    public void get(){
        driver.get(HPSM_URL);
    }

    public synchronized boolean workWithHpsm(String id) {

        if (Thread.currentThread().getName().equals("@ptnl_hpsm_notification_bot Telegram Executor")) {

            return takeTask(id);

        }

        return false;

    }

    public synchronized boolean workWithHpsm() {

        return checkNewTask();

    }

    private boolean checkNewTask() {

        try {

            if (!checkAuthorization()) {
                driver.get(HPSM_URL);
                authorizationHpsm();
            }

            driver.switchTo().defaultContent();

            timeUnitSleep(2);

            driver.findElement(By.xpath("//button[@aria-posinset=\"1\"][contains(text(),'Обновить')]")).click();
//            timeUnitSleep(4);
//            driver.findElement(By.xpath("//button[@aria-posinset=\"1\"][contains(text(),'Обновить')]")).click();

            //переключаемся на область, где видны заявки
            driver.switchTo().frame(0);

            timeUnitSleep(10);

            //собираем в лист - номера инцидентов, рабочую группу и статус
            List<WebElement> listIdTasks = driver.findElements(By.xpath("//a[contains(text(),'IM') or contains(text(),'RF')]"));
            List<WebElement> listRG = driver.findElements(By.xpath("//div[contains(text(),'РГ')]"));
            List<WebElement> listStatus = driver.findElements(By.xpath("//div[contains(text(),'Отложен') " +
                    "or contains(text(),'Ожидание')  " +
                    "or contains(text(),'В работе')  " +
                    "or contains(text(),'Назначен')]"));

            String id = "";
            String rg = "";
            String status = "";

            for (int i = 0; i < listIdTasks.size(); i++) {
                id = listIdTasks.get(i).getText();
                rg = listRG.get(i).getText();
                status = listStatus.get(i).getText();
                new HPSM(id, rg, status, LocalDateTime.now());
            }

            driver.switchTo().defaultContent();
            return true;

        } catch (StaleElementReferenceException | TimeoutException | ElementClickInterceptedException e){

            System.out.println("Исключение - StaleElementReferenceException");

            e.printStackTrace();

            driver.get(HPSM_URL);
            return false;

        } catch (org.openqa.selenium.WebDriverException e){
            System.out.println("Исключение - WebDriverException");

            e.printStackTrace();

            finishSelenium();

            startSelenium();

            return false;
        }
    }

    //алгоритм для взятий задач в ХРСМ
    private boolean takeTask(String id) {

        if (!checkAuthorization()) {
            driver.get(HPSM_URL);
            authorizationHpsm();
        }

        //переключаемся на дефолтный фрейм и жмем кнопку обновить
        driver.switchTo().defaultContent();
        driver.findElement(By.xpath("//button[@aria-posinset=\"1\"][contains(text(),'Обновить')]")).click();

        //переключаемся на фрейм, где видны заявки
        driver.switchTo().frame(0);

        timeUnitSleep(3);

        //собираем в лист - номера тасков
        List<WebElement> listIdTasks = driver.findElements(By.xpath("//a[contains(text(),'IM') or contains(text(),'RF')]"));

        timeUnitSleep(3);

        //ищем нужный таск и нажимаем на него
        for (int i = 0; i < listIdTasks.size(); i++) {
            if (listIdTasks.get(i).getText().equals(id)) {
                System.out.println("OK нашли нужную заявку, нужно брать в работу");
                listIdTasks.get(i).click();
            }
        }

        timeUnitSleep(5);

        //переключаемся на дефолтный фрейм, где есть кнопка и пробуем взять в работу
        driver.switchTo().defaultContent();

        //Проверяем что есть кнопка - в работу и нажимаем её
//        if(driver.findElement(By.xpath("//button[@aria-label='В работу']")).isDisplayed()){

        try {

            WebElement buttonOk = driver.findElement(By.xpath("//button[@aria-label='В работу']"));
            buttonOk.click();

            return true;

        } catch (org.openqa.selenium.NoSuchElementException | org.openqa.selenium.StaleElementReferenceException | org.openqa.selenium.TimeoutException e) {

            e.printStackTrace();
            System.out.println("NoSuchElementException | org.openqa.selenium.StaleElementReferenceException | org.openqa.selenium.TimeoutException");

            return false;
        } finally {

            timeUnitSleep(10);

            //закрываем открытую вкладку заявки
            WebElement closeTab = driver.findElement(By.xpath("(//li[2]//a[@class='x-tab-strip-close'])"));

            timeUnitSleep(5);

            //переключаемся на дефолтный фрейм, где есть кнопка и пробуем взять в работу
            driver.switchTo().defaultContent();

            closeTab.click();
        }
    }

    private void authorizationHpsm() {


        //кликаем на поле логина и вводим данные
        WebElement login = driver.findElement(By.xpath("//input[@id='LoginUsername']"));
        login.sendKeys(HPSM_LOGIN);

        //кликаем на поле пароля и вводим данные
        WebElement password = driver.findElement(By.xpath("//input[@id='LoginPassword']"));
        password.sendKeys(HPSM_PASSWORD);

        //нажимаем ОК и проходим авторизацию
        WebElement buttonOk = driver.findElement(By.xpath("//input[@id='loginBtn']"));
        buttonOk.click();
    }

    private boolean checkAuthorization() {
        try {
//            return driver.findElement(By.xpath("//strong[contains(text(),'По вопросам технической поддержки обращаться:')]")).isDisplayed();
            return driver.findElement(By.xpath("//span[contains(text(),'Список дел')]")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void timeUnitSleep(int second) {

        try {
            TimeUnit.SECONDS.sleep(second);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void LogOutHpsm() {
        driver.switchTo().defaultContent();

        WebElement aboutUser = driver.findElement(By.xpath("//button[text()='Информация о пользователе']"));


        aboutUser.click();

        WebElement logOut = driver.findElement(By.xpath("//button[text()='Выход']"));

        logOut.click();

        Alert test_alert = driver.switchTo().alert(); // Получить всплывающее окно
        System.out.print(test_alert.getText()); // Получаем текстовое содержимое в поле
        // окно подтверждения: test_alert.dismiss ();
        test_alert.accept();

        timeUnitSleep(5);

        finishSelenium();
    }

    public void finishSelenium() {

        driver.quit();
    }


    //TODO дописать блок
    //Тестируется. Воспроизведение ошибки, когда селениум закрыт
    void checkSelenium() {

        boolean a = true;

        while (a) {

            System.out.println(driver.toString());
            System.out.println("1");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(driver.toString());
            System.out.println("2");
            a = false;

        }


        driver.get("https://google.com");
        System.out.println(driver.getPageSource());

//finally WebDriverException
//отправить уведомление или сообщение в консоль
    }
}

