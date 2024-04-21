package org.bobrteam.gva.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public DashboardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Набери номер один: токсичный мегазорд1\nНабери номер два: ибанутый сербский битмекер\nНабери номер три: nest\nНабери номер четыре: альтушка для скуфов\nНабери номер пять: звезда чулыма\n");
    }

    public LiveData<String> getText() {
        return mText;
    }
}