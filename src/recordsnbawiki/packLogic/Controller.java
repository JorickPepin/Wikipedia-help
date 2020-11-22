package recordsnbawiki.packLogic;

import recordsnbawiki.packLogic.json.JsonManagement;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import recordsnbawiki.packVue.Observer;
import recordsnbawiki.utils.ESPNException;
import recordsnbawiki.utils.RealGMException;

/**
 *
 * @author Jorick
 */
public class Controller implements Observable {

    /**
     * Observer list
     */
    private ArrayList<Observer> observateurs;

    /**
     * Model
     */
    private DataManagement dataManagement;

    public Controller(DataManagement dataManagement) {
        this.observateurs = new ArrayList<>();
        this.dataManagement = dataManagement;
    }

    /**
     * Generates content from the player's RealGM and ESPN identifier
     *
     * @param RealGM_id - the player's RealGM identifier
     * @param ESPN_id - the player's ESPN identifier
     * @param header - true if the header is needed, false otherwise
     * @throws RealGMException
     * @throws ESPNException
     */
    public void generateContent(int RealGM_id, int ESPN_id, boolean header) throws ESPNException, RealGMException {

        try {
            JsonManagement Json = new JsonManagement();
            dataManagement.setJson(Json);
            
            String RealGM_content = dataManagement.getRealGMContent(RealGM_id);
            String ESPN_content = dataManagement.getESPNContent(ESPN_id);
            String final_content;

            if (header) { // true means that the header must be added 
                String contenuEnTete = dataManagement.getHeader();
                final_content = contenuEnTete + RealGM_content + ESPN_content;
            } else {
                final_content = RealGM_content + ESPN_content;
            }

            if (!namesAreCompatible()) { // display warning message if the two names are not identical
                notifyObservateurs("names incompatibility");
            }
            
            dataManagement.setFinalContent(final_content);

        } catch (RealGMException e) {
            if (null == e.getMessage()) {
                notifyObservateurs("errorRealGM");
            } else switch (e.getMessage()) {
                case "ID issue":
                    notifyObservateurs("errorNoPlayerRealGM");
                    break;
                case "never played in NBA":
                    notifyObservateurs("errorNeverPlayedInNBARealGM");
                    break;
            }
        } catch (ESPNException e) {
            if ("ID issue".equals(e.getMessage())) {
                notifyObservateurs("errorNoPlayerESPN");
            } else {
                notifyObservateurs("errorESPN");
            }
        } catch (FileNotFoundException e) {
            if (null == e.getMessage()) {
                notifyObservateurs("fileIssue");
            } else switch (e.getMessage()) {
                case "teams.json":
                    notifyObservateurs("teams.jsonIssue");
                    break;
                case "stats.json":
                    notifyObservateurs("stats.jsonIssue");
                    break;
                case "header_playoffs.txt":
                    notifyObservateurs("header_playoffs.txtIssue");
                    break;
                case "header_noplayoffs.txt":
                    notifyObservateurs("header_noplayoffs.txtIssue");
                    break;
            }
        } catch (IOException e) {
            notifyObservateurs("fileIssue");
        }
    }

    /**
     * Tests if the two recovered names are identical
     * @return true if the two names are identical, false otherwise
     */
    private boolean namesAreCompatible() {
     
        String RealGM_name = dataManagement.recuperationNomJoueurRealGM();
        String ESPN_name = dataManagement.recuperationNomJoueurESPN();
        
        String RealGM_name_formatted = "";
        String ESPN_name_formatted = "";
        
        for (char c : RealGM_name.toCharArray()) {
            if (Character.isLetter(c) || c == ' ') { // keep only letters and spaces to test without punctuation
                RealGM_name_formatted += c;
            }
        }
        
        for (char c : ESPN_name.toCharArray()) {
            if (Character.isLetter(c) || c == ' ') {
                ESPN_name_formatted += c;
            }
        }
          
        return RealGM_name_formatted.equalsIgnoreCase(ESPN_name_formatted);
    }
    
    @Override
    public void notifyObservateurs(String code) {
        observateurs.forEach((obs) -> {
            obs.update(code);
        });
    }

    @Override
    public void addObservateur(Observer obs) {
        observateurs.add(obs);
    }
}
