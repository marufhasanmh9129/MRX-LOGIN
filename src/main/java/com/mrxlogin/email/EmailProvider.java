package com.mrxlogin.email;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Direct provider integration. API keys are read from environment variables, never bundled in the JAR. */
public final class EmailProvider {
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    public void sendResend(String apiKey, String from, String to, String subject, String html) throws Exception {
        String body = "{\"from\":\""+esc(from)+"\",\"to\":[\""+esc(to)+"\"],\"subject\":\""+esc(subject)+"\",\"html\":\""+esc(html)+"\"}";
        send("https://api.resend.com/emails", apiKey, body, false);
    }
    public void sendBrevo(String apiKey, String senderName, String senderEmail, String to, String subject, String html) throws Exception {
        String body = "{\"sender\":{\"name\":\""+esc(senderName)+"\",\"email\":\""+esc(senderEmail)+"\"},\"to\":[{\"email\":\""+esc(to)+"\"}],\"subject\":\""+esc(subject)+"\",\"htmlContent\":\""+esc(html)+"\"}";
        send("https://api.brevo.com/v3/smtp/email", apiKey, body, true);
    }
    private void send(String url,String key,String body,boolean brevo) throws Exception {
        if(key==null||key.isBlank()) throw new IllegalStateException("Email provider API key is not configured in the server environment.");
        HttpRequest.Builder b=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20)).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        b.header(brevo?"api-key":"Authorization",brevo?key:"Bearer "+key);
        HttpResponse<String> r=client.send(b.build(),HttpResponse.BodyHandlers.ofString());
        if(r.statusCode()<200||r.statusCode()>=300) throw new IllegalStateException("Email provider rejected the request (HTTP "+r.statusCode()+"). API response body intentionally omitted to avoid leaking sensitive data.");
    }
    private static String esc(String s){return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r");}
}
