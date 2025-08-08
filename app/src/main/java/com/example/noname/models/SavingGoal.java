// SavingGoal.java
package com.example.noname.models;

import android.os.Parcel;
import android.os.Parcelable;

public class SavingGoal implements Parcelable {
    private String name;
    private long targetAmount;
    private long currentAmount;
    private String targetDate;

    public SavingGoal(String name, long targetAmount, long currentAmount, String targetDate) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
    }

    protected SavingGoal(Parcel in) {
        name = in.readString();
        targetAmount = in.readLong();
        currentAmount = in.readLong();
        targetDate = in.readString();
    }

    public static final Creator<SavingGoal> CREATOR = new Creator<SavingGoal>() {
        @Override
        public SavingGoal createFromParcel(Parcel in) {
            return new SavingGoal(in);
        }

        @Override
        public SavingGoal[] newArray(int size) {
            return new SavingGoal[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(targetAmount);
        dest.writeLong(currentAmount);
        dest.writeString(targetDate);
    }

    public String getName() { return name; }
    public long getTargetAmount() { return targetAmount; }
    public long getCurrentAmount() { return currentAmount; }
    public String getTargetDate() { return targetDate; }
}