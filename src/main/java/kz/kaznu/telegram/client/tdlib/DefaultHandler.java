package kz.kaznu.telegram.client.tdlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultHandler implements Client.ResultHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Override
    public void onResult(TdApi.Object object) {
        LOGGER.info(object.toString());
    }
}
