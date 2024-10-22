package org.bobrteam.gva.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NotificationsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public NotificationsViewModel() {
        mText = new MutableLiveData<>();
        fetchConcerts();
    }

    public LiveData<String> getText() {
        return mText;
    }

    private void fetchConcerts() {
        Future<?> future = executorService.submit(() -> {
            try {
                // Подключаемся к странице
                Document doc = Jsoup.connect("https://afisha.yandex.ru/novosibirsk/concert?source=menu&preset=tomorrow").get();

                // Ищем все элементы с атрибутом data-component="EventCard"
                Elements concerts = doc.select("[data-component=EventCard]");

                StringBuilder concertDetails = new StringBuilder();
                for (Element concert : concerts) {
                    // Находим заголовок концерта
                    String title = concert.select("[data-test-id=eventCard.eventInfoTitle]").text();
                    // Находим время концерта
                    String time = concert.select("[data-test-id=eventCard.eventInfoDetails] li").first().text();  // Первый элемент li содержит дату и время
                    // Находим место проведения концерта
                    String location = concert.select("[data-test-id=eventCard.eventInfoDetails] li").get(1).text();  // Второй элемент li содержит место

                    concertDetails.append("Концерт: ").append(title).append("\n")
                            .append("Время: ").append(time).append("\n")
                            .append("Место: ").append(location).append("\n\n");
                }

                if (concertDetails.length() > 0) {
                    mText.postValue(concertDetails.toString());
                } else {
                    mText.postValue("Концерты не найдены.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                mText.postValue("Ошибка при загрузке концертов.");
            }
        });
    }
}
