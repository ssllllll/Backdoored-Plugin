package me.ego.ezbd.lib.fo.model;

import me.ego.ezbd.lib.fo.Common;

public abstract class ChunkedTask {
    private int waitPeriodTicks = 20;
    private final int processAmount;
    private int currentIndex = 0;

    public final void startChain() {
        Common.runLater(() -> {
            long now = System.currentTimeMillis();
            boolean finished = false;
            int processed = 0;

            for(int i = this.currentIndex; i < this.currentIndex + this.processAmount; ++i) {
                if (!this.canContinue(i)) {
                    finished = true;
                    break;
                }

                this.onProcess(i);
                ++processed;
            }

            if (processed > 0 || !finished) {
                Common.log(new String[]{this.getProcessMessage(now, processed)});
            }

            if (!finished) {
                this.currentIndex += this.processAmount;
                Common.runLaterAsync(this.waitPeriodTicks, this::startChain);
            } else {
                this.onFinish();
            }

        });
    }

    protected abstract void onProcess(int var1);

    protected abstract boolean canContinue(int var1);

    protected String getProcessMessage(long initialTime, int processed) {
        return "Processed " + String.format("%,d", processed) + " " + this.getLabel() + ". Took " + (System.currentTimeMillis() - initialTime) + " ms";
    }

    protected void onFinish() {
    }

    protected String getLabel() {
        return "blocks";
    }

    public ChunkedTask(int processAmount) {
        this.processAmount = processAmount;
    }

    public void setWaitPeriodTicks(int waitPeriodTicks) {
        this.waitPeriodTicks = waitPeriodTicks;
    }

    public int getCurrentIndex() {
        return this.currentIndex;
    }
}