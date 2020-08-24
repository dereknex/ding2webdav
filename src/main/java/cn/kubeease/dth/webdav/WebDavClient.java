package cn.kubeease.dth.webdav;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class WebDavClient {

    private final String host;
    private final String path;
    private HttpClient httpClient;


    public WebDavClient(String host, String path, String user, String password) {
        this.host = host;
        this.path = path;
        this.httpClient = new HttpClient();
        Credentials credentials = new UsernamePasswordCredentials(user, password);
        this.httpClient.getState().setCredentials(AuthScope.ANY, credentials);
    }

    public void uploadCard(VCard card) throws IOException {
        PutMethod method = new PutMethod(this.host + this.path+ card.getExtendedProperty("userID").getValue().strip());
        RequestEntity request = new StringRequestEntity(Ezvcard.write(card).version(VCardVersion.V4_0).go(), "text/plain", "UTF-8");
        method.setRequestEntity(request);
        httpClient.executeMethod(method);
        //return method.getStatusCode();
    }

}