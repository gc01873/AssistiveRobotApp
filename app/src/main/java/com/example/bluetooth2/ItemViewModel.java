package com.example.bluetooth2;

import android.content.ClipData;
import android.widget.ListView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


//This class will be used to allow the data in the fragment to communicate with the host activity

public class ItemViewModel extends ViewModel {
    //private final MutableLiveData<ClipData.Item> selectedItem = new MutableLiveData<ClipData.Item>();
    private final MutableLiveData<ListView> selectedItem = new MutableLiveData<ListView>();
    public void selectItem(ListView item) {
        selectedItem.setValue(item);
    }
    public LiveData<ListView> getSelectedItem() {
        return selectedItem;
    }

}

/*public class ItemViewModel extends ViewModel {
    private final MutableLiveData<Item> selectedItem = new MutableLiveData<Item>();
    public void selectItem(Item item) {
        selectedItem.setValue(item);
    }
    public LiveData<Item> getSelectedItem() {
        return selectedItem;
    }
}*/
