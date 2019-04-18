package com.github.fcannizzaro.materialstepper.interfaces;

/**
 * @author Francesco Cannizzaro (fcannizzaro).
 */
public interface Stepable {

    void onSkip();

    void onNext();

    void onError();

    void onUpdate();

}
