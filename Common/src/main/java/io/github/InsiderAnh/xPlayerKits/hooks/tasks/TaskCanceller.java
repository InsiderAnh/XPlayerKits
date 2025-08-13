package io.github.InsiderAnh.xPlayerKits.hooks.tasks;

public class TaskCanceller {

    private final Runnable canceller;

    public TaskCanceller(Runnable canceller) {
        this.canceller = canceller;
    }

    public void cancel() {
        canceller.run();
    }

}