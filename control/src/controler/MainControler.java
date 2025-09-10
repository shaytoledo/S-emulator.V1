package controler;


import application.AppController;
import manager.EngineManager;

public class MainControler implements ControlInter {

    private EngineManager manager;
    private AppController gui;

   public void setProgramSceneController(AppController gui) {
       this.gui = gui;
   }
    public void setEngineManager(EngineManager manager) {
        this.manager = manager;
    }
    @Override
    public void load() {
        //manager.load();
    }

}
