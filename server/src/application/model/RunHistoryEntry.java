package application.model;

public class RunHistoryEntry {
    public int runNumber;
    public boolean isMainProgram;
    public String programName;
    public String architecture;
    public int level;
    public long yResult;
    public long cyclesUsed;

    public RunHistoryEntry(int runNumber, boolean isMainProgram, String programName,
                           String architecture, int level, long yResult, long cyclesUsed) {
        this.runNumber = runNumber;
        this.isMainProgram = isMainProgram;
        this.programName = programName;
        this.architecture = architecture;
        this.level = level;
        this.yResult = yResult;
        this.cyclesUsed = cyclesUsed;
    }
}
