package modularwotclaninfo;

/**
 *
 * @author Yoyo117 (johnp)
 */
public class ProvincesAPIException extends Exception implements ProgrammException {

    private final String result;
    private final GUI gui;

    public ProvincesAPIException(String result, GUI gui) {
        super();
        this.result = result;
        this.gui = gui;
    }

    @Override
    public void publish() {
        this.gui.errorPanel("Error while getting provinces: "+result, " ProvincesAPIError");
    }
}
