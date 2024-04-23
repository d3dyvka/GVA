package org.bobrteam.gva.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public DashboardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Набери номер один: Иван\nНабери номер два: Дмитрий\nНабери номер три: РЖД-Медицина Клиническая больница\nНабери номер четыре: Новосибирская областная больница\nНабери номер пять: Городская клиническая больница №11\nНабери номер шесть: Городская клиническая поликлиника №13\nНабери номер для записи");
    }

    public LiveData<String> getText() {
        return mText;
    }
}