package kz.kaznu.telegram.client.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class AuthorizationCodeService {

    @Value("${authorization.code.file.absolute.path}")
    private String fileName;

    public void receiveAuthorizationCode(String code) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(code);
        writer.close();
    }

}
