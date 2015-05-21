package de.tudarmstadt.informatik.bp.bonfirechat.network;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mw on 21.05.15.
 *
 */
public class ConnectionManager {
    private static ConnectionManager instance;
    public static ConnectionManager get() {
        if (instance  == null) instance = new ConnectionManager();
        return instance;
    }

    public List<IProtocol> connections = new ArrayList<IProtocol>();

    private ConnectionManager() {}

    public IProtocol getConnection(Class typ) {
        for(IProtocol p : connections) {
            if (typ.isInstance(p)) return p;
        }
        return null;
    }

    public IProtocol getOrCreateConnection(Class typ) {
        IProtocol p = getConnection(typ);
        if (p == null) {
            try {
                p = (IProtocol) typ.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
            connections.add(p);
        }
        return p;
    }

}
