package com.screenlocker.secure.socket.policy.model;

public class PolicyModel {

    private String stepName;
    private String status;
    private int img;
    private int progress;

    public PolicyModel(String stepName, String status, int img, int progress) {
        this.stepName = stepName;
        this.status = status;
        this.img = img;
        this.progress = progress;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
