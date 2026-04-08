package application.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class UserInfo {
    public final String name;
    private final AtomicLong credits = new AtomicLong(1000); // start with 1000 credits
    private final AtomicLong creditsUsed = new AtomicLong(0);
    private final AtomicInteger programsUploaded = new AtomicInteger(0);
    private final AtomicInteger functionsContributed = new AtomicInteger(0);
    private final AtomicInteger runCount = new AtomicInteger(0);
    private final List<RunHistoryEntry> history = new ArrayList<>();
    private final AtomicInteger runNumberCounter = new AtomicInteger(0);

    public UserInfo(String name) {
        this.name = name;
    }

    public long getCredits() { return credits.get(); }
    public long getCreditsUsed() { return creditsUsed.get(); }
    public int getProgramsUploaded() { return programsUploaded.get(); }
    public int getFunctionsContributed() { return functionsContributed.get(); }
    public int getRunCount() { return runCount.get(); }

    public synchronized List<RunHistoryEntry> getHistory() {
        return new ArrayList<>(history);
    }

    /** Atomic compare-and-deduct. Returns new balance, or -1 if insufficient. */
    public long tryDeductCredits(long amount) {
        while (true) {
            long current = credits.get();
            if (current < amount) return -1;
            if (credits.compareAndSet(current, current - amount)) {
                creditsUsed.addAndGet(amount);
                return current - amount;
            }
        }
    }

    public void addCredits(long amount) {
        credits.addAndGet(amount);
    }

    public void incrementProgramsUploaded() { programsUploaded.incrementAndGet(); }
    public void addFunctionsContributed(int n) { functionsContributed.addAndGet(n); }
    public void incrementRunCount() { runCount.incrementAndGet(); }

    public synchronized RunHistoryEntry addHistoryEntry(boolean isMainProgram, String programName,
                                                        String architecture, int level,
                                                        long yResult, long cyclesUsed) {
        int num = runNumberCounter.incrementAndGet();
        RunHistoryEntry entry = new RunHistoryEntry(num, isMainProgram, programName,
                architecture, level, yResult, cyclesUsed);
        history.add(entry);
        return entry;
    }
}
