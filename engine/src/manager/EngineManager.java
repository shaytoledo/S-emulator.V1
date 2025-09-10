package manager;

import controler.MainControler;
import core.engine.Engine;
import core.engine.EngineImpl;
import dto.LoadReport;

import java.nio.file.Path;

public class EngineManager implements basicMethods{

     static final Engine engine = new EngineImpl();
     static MainControler controler;

    private EngineManager() {
        controler.setEngineManager(this);
    }

    @Override
    public void load(Path file) {
        LoadReport report = engine.loadProgram(file);
        report.ok(); // error string
    }

    @Override
    public void show() {

    }

    @Override
    public void expend(int level) {

    }

    @Override
    public void run() {

    }

    @Override
    public void hisory() {

    }

    @Override
    public void exit() {

    }
}
