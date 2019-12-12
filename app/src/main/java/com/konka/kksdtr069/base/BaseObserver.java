package com.konka.kksdtr069.base;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class BaseObserver implements LifecycleObserver {

    private CompositeDisposable mDisposable = new CompositeDisposable();

    protected void addObserver(@NonNull Disposable disposable) {
        mDisposable.add(disposable);
    }

    protected void removeObserver(@NonNull Disposable disposable) {
        mDisposable.remove(disposable);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected void release() {
        mDisposable.clear();
    }

}
