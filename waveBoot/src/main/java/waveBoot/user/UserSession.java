package waveBoot.user;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import waveBoot.service.CsvMLService;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserSession{

    /* This class is used to store a user's http session info, to keep track of ML trained data */

    private boolean csvIsTrained;
    private CsvMLService cml;

    public UserSession(){
        this.csvIsTrained = false;
        cml = null;
    }

    public void setCsvIsTrained(boolean csvIsTrained){
        this.csvIsTrained = csvIsTrained;
    }

    public boolean getCsvIsTrained(){
        return this.csvIsTrained;
    }

    public void setCml(CsvMLService cml){
        this.cml = cml;
    }

    public CsvMLService getCml(){
        return this.cml;
    }
}
