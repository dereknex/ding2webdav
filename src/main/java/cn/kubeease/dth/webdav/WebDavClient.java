package cn.kubeease.dth.webdav;

import com.google.common.flogger.FluentLogger;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class WebDavClient {

    private final String host;
    private final String path;
    private final HttpClient httpClient;
    private final List<NameValuePair> credentials;
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();


    public WebDavClient(String host, String path, String user, String password) {
        this.host = host;
        this.path = path;
        this.httpClient = HttpClients.createDefault();
        credentials = new ArrayList<>();
        credentials.add(new BasicNameValuePair("username", user));
        credentials.add(new BasicNameValuePair("password", password));
//        HttpPost httpPost = new HttpPost("http://targethost/login");
//        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
//        nvps.add(new BasicNameValuePair("username", "vip"));
//        nvps.add(new BasicNameValuePair("password", "secret"));
//        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
//        Credentials credentials = new UsernamePasswordCredentials(user, password);
//        this.httpClient.getState().setCredentials(AuthScope.ANY, credentials);
    }

    public int uploadCard(VCard card) throws IOException {
        HttpPut put = new HttpPut(this.host + this.path+ card.getExtendedProperty("userID").getValue().strip());
        put.setEntity(new UrlEncodedFormEntity(this.credentials));
        HttpResponse res = this.httpClient.execute(put);
        int code = res.getStatusLine().getStatusCode();
        if (code>=400){
            logger.atSevere().log("upload vcard failed: %d %s", code, res.toString());
        }
        return res.getStatusLine().getStatusCode();
//        RequestEntity request = new StringRequestEntity(Ezvcard.write(card).version(VCardVersion.V4_0).go(), "text/plain", "UTF-8");
//        method.setRequestEntity(request);
//        httpClient.executeMethod(method);
        //return method.getStatusCode();
    }

}