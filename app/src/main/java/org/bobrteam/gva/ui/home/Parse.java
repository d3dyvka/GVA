package org.bobrteam.gva.ui.home;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parse {

    public static class Concert {
        private final String title;
        private final String time;
        private final String location;

        public Concert(String title, String time, String location) {
            this.title = title;
            this.time = time;
            this.location = location;
        }

        public String getTitle() {
            return title;
        }

        public String getTime() {
            return time;
        }

        public String getLocation() {
            return location;
        }

        @Override
        public String toString() {
            return "Концерт: " + title + "\n" +
                    "Время: " + time + "\n" +
                    "Место: " + location + "\n";
        }
    }

    public List<Concert> getConcerts() throws IOException {
        List<Concert> concertList = new ArrayList<>();

        // Подключаемся к странице
        Document doc = Jsoup.connect("https://afisha.yandex.ru/novosibirsk/concert?source=menu&preset=tomorrow").get();

        // Ищем все элементы с атрибутом data-component="EventCard"
        Elements concerts = doc.select("[data-component=EventCard]");

        for (Element concert : concerts) {
            // Находим заголовок концерта
            String title = concert.select("[data-test-id=eventCard.eventInfoTitle]").text();
            // Находим время концерта
            String time = concert.select("[data-test-id=eventCard.eventInfoDetails] li").first().text();  // Первый элемент li содержит дату и время
            // Находим место проведения концерта
            String location = concert.select("[data-test-id=eventCard.eventInfoDetails] li").get(1).text();  // Второй элемент li содержит место

            // Добавляем концерт в список
            concertList.add(new Concert(title, time, location));
        }

        return concertList;
    }
}
