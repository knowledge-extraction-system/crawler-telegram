package kz.kaznu.telegram.client.exceptions;

/**
 * Created by yerzhan on 10/21/19.
 */
public class TelegramClientException extends Exception {

  private int code;
  private String message;

  public TelegramClientException(String message) {
    this.message = message;
  }

  public TelegramClientException(int code, String message) {
    this.code = code;
    this.message = message;
  }
}
