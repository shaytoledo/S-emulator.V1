package application.model;

import core.program.Function;
import core.program.Program;
import dto.InstructionView;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ProgramEntry {
    public final Program program;
    public final String xmlContent;        // raw XML for building fresh engine instances
    public final String uploaderName;
    public final boolean isMainProgram;
    public final String parentProgramName; // for helper functions: the main program they were uploaded with; null for main programs
    public final String userString;        // human-readable representation (from Function.getUserString()); null for main programs
    public final String minArchitecture;   // minimum architecture required to run this program (I/II/III/IV)
    private final AtomicInteger timesRun = new AtomicInteger(0);
    private final AtomicLong totalCreditsUsed = new AtomicLong(0);

    public ProgramEntry(Program program, String xmlContent, String uploaderName,
                        boolean isMainProgram, String parentProgramName) {
        this.program = program;
        this.xmlContent = xmlContent;
        this.uploaderName = uploaderName;
        this.isMainProgram = isMainProgram;
        this.parentProgramName = parentProgramName;
        this.userString = (program instanceof Function f) ? f.getUserString() : null;
        List<String> instrNames = program.getInstructions().stream()
                .map(i -> i.getName())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.minArchitecture = Architecture.computeMinFor(instrNames).name();
    }

    public String getName() { return program.getName(); }
    public int getTimesRun() { return timesRun.get(); }
    public double getAvgCreditsUsed() {
        int runs = timesRun.get();
        return runs == 0 ? 0.0 : (double) totalCreditsUsed.get() / runs;
    }

    public int getInstructionCount() {
        try {
            List<InstructionView> views = program.instructionViewsAfterExtendRunShow(0);
            return views == null ? 0 : views.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public int getMaxLevel() {
        try {
            return program.calculateMaxDegree();
        } catch (Exception e) {
            return 0;
        }
    }

    public void recordRun(long creditsUsed) {
        timesRun.incrementAndGet();
        totalCreditsUsed.addAndGet(creditsUsed);
    }
}
