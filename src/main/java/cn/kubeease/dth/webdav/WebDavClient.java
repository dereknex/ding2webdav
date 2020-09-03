package cn.kubeease.dth.webdav;

import com.google.common.flogger.FluentLogger;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;


public class WebDavClient {

    private final String host;
    private final String path;
    private final HttpClient httpClient;
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();


    public WebDavClient(String host, String path, String user, String password) {
        this.host = host;
        this.path = path;
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(user, password)
        );
        this.httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
    }

    public int uploadCard(VCard card) throws IOException {
        HttpPut put = new HttpPut(this.host + this.path+ card.getExtendedProperty("userID").getValue().strip());
        StringEntity body= new StringEntity(Ezvcard.write(card).version(VCardVersion.V4_0).go(),"UTF-8");
        put.setEntity(body);
        HttpResponse res = this.httpClient.execute(put);
        int code = res.getStatusLine().getStatusCode();
        if (code>=400){
            logger.atSevere().log("upload vcard failed: %d %s", code, res.toString());
        }
        return res.getStatusLine().getStatusCode();
    }

    public boolean removeCardWithName(String id) {
        HttpDelete delete = new HttpDelete(this.host + this.path+ id);
        try {
            HttpResponse res = this.httpClient.execute(delete);
            if (res.getStatusLine().getStatusCode() == 204){
                return true;
            }
        } catch (IOException e) {
            logger.atSevere().withCause(e);
            return false;
        }
        return false;
    }
}