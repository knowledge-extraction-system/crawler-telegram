package kz.kaznu.telegram.client.tdlib;

public class AuthorizationRequestHandler implements Client.ResultHandler {
    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.Error.CONSTRUCTOR:
                System.err.println("Receive an error:\n" + object);
//                onAuthorizationStateUpdated(null); // repeat last action
                break;
            case TdApi.Ok.CONSTRUCTOR:
                // result is already received through UpdateAuthorizationState, nothing to do
                break;
            default:
                System.err.println("Receive wrong response from TDLib:\n" + object);
        }
    }
}
