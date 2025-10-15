package com.email.email_writer;

import org.springframework.stereotype.Service;

@Service
public class EmailGeneratorService {
    public String generateEmailReply(EmailRequest emailRequest){
        String prompt = buildpromt(emailRequest);
    }
    private String buildpromt(EmailRequest emailRequest){
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email from the following form:\n");
        if(emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()){
            prompt.append("Tone").append(emailRequest.getTone()).append("\n");
        }
        prompt.append("\noriginal email\n").append(emailRequest.getEmailcontent());
        return prompt.toString();
    }

}
