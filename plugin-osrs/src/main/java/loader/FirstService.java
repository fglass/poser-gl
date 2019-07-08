package loader;

import api.TestService;

public class FirstService implements TestService {

    public String getAccountId() {
        return "works!";
    }
}
