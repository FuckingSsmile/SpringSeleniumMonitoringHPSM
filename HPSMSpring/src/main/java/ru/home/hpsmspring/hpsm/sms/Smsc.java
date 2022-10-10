package ru.home.hpsmspring.hpsm.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.home.hpsmspring.monitoring.MonitoringEventSmsCall;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Set;

@Component
public class Smsc{

    @Autowired
    private MonitoringEventSmsCall monitoringEventSmsCall;
    @Value("${smsc.user}")
    private String SMSC_LOGIN;     // логин клиента
    @Value("${smsc.password}")
    private String SMSC_PASSWORD;  // пароль
    private boolean SMSC_HTTPS = false;         // использовать HTTPS протокол
    private String SMSC_CHARSET = "utf-8";       // кодировка сообщения: koi8-r, windows-1251 или utf-8 (по умолчанию)
    private boolean SMSC_DEBUG = false;         // флаг отладки
    private boolean SMSC_POST = false;         // Использовать метод POST

    //запустить планировщика для обновлений
    public void runCheckEventForPhone(Set<String> allPhones) {

            if(monitoringEventSmsCall.smsPackages != null && !monitoringEventSmsCall.smsPackages.isEmpty()){
                sendSms(monitoringEventSmsCall.smsPackages.pollFirst(), allPhones);
            }
    }

    private void sendSms(SmsPackage smsPackage, Set<String> allPhones) {
        String phones = allPhones.toString();
        phones = phones.substring(1, phones.length()-1);
        send_sms(phones, smsPackage.getMessage(), 0, "", "", smsPackage.getFormat(), "", smsPackage.getQuery());
    }



    /**
     * Отправка SMS
     *
     * @param phones   - список телефонов через запятую или точку с запятой
     * @param message  - отправляемое сообщение
     * @param translit - переводить или нет в транслит (1,2 или 0)
     * @param time     - необходимое время доставки в виде строки (DDMMYYhhmm, h1-h2, 0ts, +m)
     * @param id       - идентификатор сообщения. Представляет собой 32-битное число в диапазоне от 1 до 2147483647.
     * @param format   - формат сообщения (0 - обычное sms, 1 - flash-sms, 2 - wap-push, 3 - hlr, 4 - bin, 5 - bin-hex, 6 - ping-sms, 7 - mms, 8 - mail, 9 - call, 10 - viber, 11 - soc)
     * @param sender   - имя отправителя (Sender ID). Для отключения Sender ID по умолчанию необходимо в качестве имени передать пустую строку или точку.
     * @param query    - строка дополнительных параметров, добавляемая в URL-запрос ("valid=01:00&maxsms=3&tz=2")
     * @return array (<id>, <количество sms>, <стоимость>, <баланс>) в случае успешной отправки
     * или массив (<id>, -<код ошибки>) в случае ошибки
     */

    private String[] send_sms(String phones, String message, int translit, String time, String id, int format, String sender, String query) {
        String[] formats = {"", "flash=1", "push=1", "hlr=1", "bin=1", "bin=2", "ping=1", "mms=1", "mail=1", "call=1", "viber=1", "soc=1"};
        String[] m = {};

        try {
            m = _smsc_send_cmd("send", "cost=3&phones=" + URLEncoder.encode(phones, SMSC_CHARSET)
                    + "&mes=" + URLEncoder.encode(message, SMSC_CHARSET)
                    + "&translit=" + translit + "&id=" + id + (format > 0 ? "&" + formats[format] : "")
                    + (sender == "" ? "" : "&sender=" + URLEncoder.encode(sender, SMSC_CHARSET))
                    + (time == "" ? "" : "&time=" + URLEncoder.encode(time, SMSC_CHARSET))
                    + (query == "" ? "" : "&" + query));
        } catch (UnsupportedEncodingException e) {

        }

        if (m.length > 1) {
            if (SMSC_DEBUG) {
                if (Integer.parseInt(m[1]) > 0) {
                    System.out.println("Сообщение отправлено успешно. ID: " + m[0] + ", всего SMS: " + m[1] + ", стоимость: " + m[2] + ", баланс: " + m[3]);
                } else {
                    System.out.print("Ошибка №" + Math.abs(Integer.parseInt(m[1])));
                    System.out.println(Integer.parseInt(m[0]) > 0 ? (", ID: " + m[0]) : "");
                }
            }
        } else {
            System.out.println("Не получен ответ от сервера.");
        }

        return m;
    }

    private String[] _smsc_send_cmd(String cmd, String arg) {
        /* String[] m = {}; */
        String ret = ",";

        try {
            String _url = (SMSC_HTTPS ? "https" : "http") + "://smsc.ru/sys/" + cmd + ".php?login=" + URLEncoder.encode(SMSC_LOGIN, SMSC_CHARSET)
                    + "&psw=" + URLEncoder.encode(SMSC_PASSWORD, SMSC_CHARSET)
                    + "&fmt=1&charset=" + SMSC_CHARSET + "&" + arg;

            String url = _url;
            int i = 0;
            do {
                if (i++ > 0) {
                    url = _url;
                    url = url.replace("://smsc.ru/", "://www" + (i) + ".smsc.ru/");
                }
                ret = _smsc_read_url(url);
            }
            while (ret == "" && i < 5);
        } catch (UnsupportedEncodingException e) {

        }

        return ret.split(",");
    }

    /**
     * Чтение URL
     *
     * @param url - ID cообщения
     * @return line - ответ сервера
     */
    private String _smsc_read_url(String url) {

        String line = "", real_url = url;
        String[] param = {};
        boolean is_post = (SMSC_POST || url.length() > 2000);

        if (is_post) {
            param = url.split("\\?", 2);
            real_url = param[0];
        }

        try {
            URL u = new URL(real_url);
            InputStream is;

            if (is_post) {
                URLConnection conn = u.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream(), SMSC_CHARSET);
                os.write(param[1]);
                os.flush();
                os.close();
                System.out.println("post");
                is = conn.getInputStream();
            } else {
                is = u.openStream();
            }

            InputStreamReader reader = new InputStreamReader(is, SMSC_CHARSET);

            int ch;
            while ((ch = reader.read()) != -1) {
                line += (char) ch;
            }

            reader.close();
        } catch (MalformedURLException e) { // Неверно урл, протокол...

        } catch (IOException e) {

        }

        return line;
    }

    private static String _implode(String[] ary, String delim) {
        String out = "";

        for (int i = 0; i < ary.length; i++) {
            if (i != 0)
                out += delim;
            out += ary[i];
        }

        return out;
    }


}