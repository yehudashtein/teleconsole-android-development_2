package com.telebroad.teleconsole.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class LiveDataList<T> extends MutableLiveData<List<T>> implements List<T>{
    private List<T> data = new ArrayList<>();

    public LiveDataList(){
        super();
        Utils.updateLiveData(this, data);
    }
    public LiveDataList(List<T> value){
        super(value);
        data = value;
    }

    // Special method for replacing all items. This is needed because we only want to notify once
    public boolean replaceAll(@NonNull Collection<? extends T> c) {
        data.clear();
        boolean b = data.addAll(c);
        notifyObservers();
        return b;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean contains(@Nullable Object o) {
        return data.contains(o);
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return data.toArray();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray(@NonNull T1[] a) {
        return data.toArray(a);
    }

    @Override
    public boolean add(T t) {
        boolean result = data.add(t);
        notifyObservers();
        return result;
    }
    private void notifyObservers(){
        Utils.updateLiveData(this, data);
    }
    @Override
    public boolean remove(@Nullable Object o) {
        boolean remove = data.remove(o);
        notifyObservers();
        return remove;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return data.containsAll(c);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        boolean b = data.addAll(c);
        notifyObservers();
        return b;
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends T> c) {
        boolean b = addAll(index, c);
        notifyObservers();
        return b;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        boolean b = data.removeAll(c);
        notifyObservers();
        return b;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return data.retainAll(c);
    }

    @Override
    public void clear() {
        data.clear();
        notifyObservers();
    }

    @Override
    public T get(int index) {
        return data.get(index);
    }

    @Override
    public T set(int index, T element) {
        return data.set(index,element);
    }

    @Override
    public void add(int index, T element) {
        data.add(index, element);
        notifyObservers();
    }

    @Override
    public T remove(int index) {
        T remove = data.remove(index);
        notifyObservers();
        return remove;

    }

    @Override
    public int indexOf(@Nullable Object o) {
        return data.indexOf(o);
    }

    @Override
    public int lastIndexOf(@Nullable Object o) {
        return data.lastIndexOf(o);
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator() {
        return data.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return data.listIterator(index);
    }

    @NonNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return data.subList(fromIndex, toIndex);
    }

    @NonNull
    @Override
    public String toString() {
        return data.toString();
    }
}
