package kz.kaznu.telegram.client.tdlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddProxyRequestHandler implements Client.ResultHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.Error.CONSTRUCTOR:
                System.err.println("Receive an error:\n" + object);
//                onAuthorizationStateUpdated(null); // repeat last action
                break;
            case TdApi.Proxy.CONSTRUCTOR:
                LOGGER.info("Proxy added successfully! ( ^__^ )");
                break;
            default:
                System.err.println("Receive wrong response from TDLib:\n" + object);
        }
    }
}
