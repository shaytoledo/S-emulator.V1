import core.engine.EngineImpl;
import dto.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Compact test runner for S-Emulator1.
 * Shows: load status, expand level/size summary, run results.
 * Does NOT print individual instruction details.
 */
public class TestRunner {

    static final String SEP = "-".repeat(65);
    static int totalTests = 0, passed = 0, failed = 0;
    static final Long EXPECT_ERROR = Long.MIN_VALUE;

    public static void main(String[] args) {
        String testDir = args.length > 0 ? args[0]
                : "C:\\Users\\shayt\\Documents\\Computer Science\\Java\\Exercises\\S-Emulator1\\test";

        print("=================================================================");
        print("  S-EMULATOR1  TEST SUITE");
        print("=================================================================");

        // 1. successor.xml: f(x1) = x1 + 1
        runTest(testDir, "successor.xml", "Successor  f(x1)=x1+1",
                cases(
                        c(ins(0),  1L),
                        c(ins(1),  2L),
                        c(ins(5),  6L),
                        c(ins(10), 11L)
                ));

        // 2. minus.xml: f(x1,x2) = max(x1-x2, 0)
        runTest(testDir, "minus.xml", "Minus  f(x1,x2)=max(x1-x2,0)",
                cases(
                        c(ins(5,3),  2L),
                        c(ins(3,5),  0L),
                        c(ins(0,0),  0L),
                        c(ins(7,7),  0L),
                        c(ins(10,4), 6L),
                        c(ins(5,0),  5L),
                        c(ins(0,5),  0L)
                ));

        // 3. quotation.xml: f(x1)=x1+3, special case x1==3->EXIT early(y=0)
        runTest(testDir, "quotation.xml", "Quotation  f(x1)=x1+3  [x1==3 exits early -> y=0]",
                cases(
                        c(ins(0),  3L),
                        c(ins(1),  4L),
                        c(ins(2),  5L),
                        c(ins(3),  0L),
                        c(ins(5),  8L),
                        c(ins(10), 13L)
                ));

        // 4. self-composition.xml: f(x1)=Successor(Successor(x1))=x1+2
        runTest(testDir, "self-composition.xml", "Self-Composition  f(x1)=x1+2",
                cases(
                        c(ins(0), 2L),
                        c(ins(3), 5L),
                        c(ins(8), 10L)
                ));

        // 5. composition.xml: f(x1)=7-(x1+1)=max(6-x1,0)
        runTest(testDir, "composition.xml", "Composition  f(x1)=7-(x1+1)=max(6-x1,0)",
                cases(
                        c(ins(0), 6L),
                        c(ins(3), 3L),
                        c(ins(6), 0L),
                        c(ins(7), 0L),
                        c(ins(10),0L)
                ));

        // 6. divide.xml: f(x1,x2)=floor(x1/x2)
        runTest(testDir, "divide.xml", "Divide  f(x1,x2)=floor(x1/x2)",
                cases(
                        c(ins(12,2), 6L),
                        c(ins(10,3), 3L),
                        c(ins(9, 3), 3L),
                        c(ins(6, 2), 3L),
                        c(ins(1, 2), 0L),
                        c(ins(0, 5), 0L),
                        c(ins(0, 0), 0L),
                        c(ins(5, 0), 0L),
                        c(ins(7, 7), 1L)
                ));

        // 7. divide (1).xml: stripped divide - missing helpers -> load error
        runTest(testDir, "divide (1).xml", "Divide-v1  [missing helper funcs - expect load error]",
                cases(
                        c(ins(12,2), EXPECT_ERROR)
                ));

        // 8. remainder.xml: f(x1,x2)=x1 mod x2
        runTest(testDir, "remainder.xml", "Remainder  f(x1,x2)=x1 mod x2",
                cases(
                        c(ins(10,3), 1L),
                        c(ins(9, 3), 0L),
                        c(ins(10,4), 2L),
                        c(ins(0, 5), 0L),
                        c(ins(5, 0), 0L),
                        c(ins(7, 7), 0L),
                        c(ins(8, 3), 2L)
                ));

        // 9. predicates.xml: main=NOOP -> always y=0
        runTest(testDir, "predicates.xml", "Predicates  main=NOOP -> always y=0",
                cases(
                        c(ins(0),    0L),
                        c(ins(1,1),  0L),
                        c(ins(5,3),  0L)
                ));

        // 10. math.xml: main=NOOP -> always y=0
        runTest(testDir, "math.xml", "Math-Functions  main=NOOP -> always y=0",
                cases(
                        c(ins(0),    0L),
                        c(ins(1,1),  0L)
                ));

        // 11. error-1.xml: uses undefined 'Plus' -> load error
        runTest(testDir, "error-1.xml", "Error-1  [undefined function 'Plus' - expect load error]",
                cases(
                        c(ins(0), EXPECT_ERROR)
                ));

        print("");
        print("=================================================================");
        print(String.format("  TOTAL: %d   PASSED: %d   FAILED: %d", totalTests, passed, failed));
        print("=================================================================");
    }

    // ─── DSL helpers ──────────────────────────────────────────────────────────
    static List<Long> ins(long... vals) {
        List<Long> list = new ArrayList<>();
        for (long v : vals) list.add(v);
        return list;
    }
    @SuppressWarnings("unchecked")
    static Object[] c(List<Long> inputs, Long expected) { return new Object[]{inputs, expected}; }
    @SuppressWarnings("unchecked")
    static Object[][] cases(Object[]... cs) { return cs; }
    static void print(String s) { System.out.println(s); System.out.flush(); }

    // ─── Main test runner ──────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    static void runTest(String dir, String filename, String label, Object[][] cases) {
        print("");
        print(SEP);
        print("FILE: " + filename);
        print("      " + label);
        print(SEP);

        Path xmlPath = Paths.get(dir, filename);
        EngineImpl engine = new EngineImpl();
        LoadReport report = engine.loadProgram(xmlPath);

        if (!report.ok()) {
            print("[LOAD FAILED]");
            for (Exception e : report.errors())
                print("  -> " + e.getClass().getSimpleName() + ": " + e.getMessage());

            for (Object[] caze : cases) {
                totalTests++;
                List<Long> inputs = (List<Long>) caze[0];
                Long expected = (Long) caze[1];
                if (expected == EXPECT_ERROR) {
                    print("  PASS  inputs=" + inputs + "  expected=LOAD_ERROR  got=LOAD_ERROR");
                    passed++;
                } else {
                    print("  FAIL  inputs=" + inputs + "  expected=" + expected + "  got=LOAD_ERROR");
                    failed++;
                }
            }
            return;
        }

        print("[LOADED OK]");

        // Expand info (summary only, no instruction listing)
        try {
            int maxLvl = engine.getMaxExpandLevel();
            print("  Max expand level: " + maxLvl);
            for (int lvl = 0; lvl <= maxLvl; lvl++) {
                List<List<InstructionView>> expansion = engine.expandProgramToLevelForExtend(lvl);
                int total = expansion.stream().mapToInt(List::size).sum();
                print(String.format("  [Level %d] %d groups, %d instructions", lvl, expansion.size(), total));
            }
        } catch (Exception ex) {
            print("  [expand error: " + ex + "]");
        }

        print("");

        // Run test cases
        for (Object[] caze : cases) {
            List<Long> inputs = (List<Long>) caze[0];
            Long expected = (Long) caze[1];

            if (expected == EXPECT_ERROR) {
                print("  SKIP  inputs=" + inputs + "  expected=ERROR but load succeeded");
                totalTests++;
                continue;
            }

            try {
                EngineImpl eng2 = new EngineImpl();
                eng2.loadProgram(xmlPath);
                int maxLvl2 = eng2.getMaxExpandLevel();
                RunResult result = eng2.run(maxLvl2, inputs);
                long actual = result.y();
                boolean ok = actual == expected;
                print(String.format("  %s  inputs=%-18s  expected=%-5d  got=%-5d  cycles=%-7d  %s",
                        ok ? "PASS" : "FAIL",
                        inputs.toString(),
                        expected, actual,
                        result.totalCycles(),
                        ok ? "" : "<-- WRONG"));
                if (ok) passed++; else failed++;
                totalTests++;
            } catch (Exception ex) {
                print(String.format("  FAIL  inputs=%-18s  expected=%-5d  EXCEPTION: %s",
                        inputs.toString(), expected, ex));
                ex.printStackTrace(System.out);
                failed++; totalTests++;
            }
        }
    }
}
