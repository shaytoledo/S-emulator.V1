package manager;

import java.nio.file.Path;

public interface basicMethods {

    void load(Path file);
    void show();
    void expend(int level);
    void run();
    void hisory();
    void exit();
}
