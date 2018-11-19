package test;

import org.apache.camel.main.Main;

/**
 * A Camel Application
 */
public class MainApp {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Main main = new Main();
        // main.addRouteBuilder(new MyRouteBuilder());
        main.addRouteBuilder(new MyDBRouteBuilder());
        main.run(args);
    }

}

