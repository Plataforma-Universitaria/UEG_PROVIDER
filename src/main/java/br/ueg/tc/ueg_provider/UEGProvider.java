package br.ueg.tc.ueg_provider;

import br.ueg.tc.pipa_integrator.converter.IConverterInstitution;
import br.ueg.tc.pipa_integrator.exceptions.institution.InstitutionComunicationException;
import br.ueg.tc.pipa_integrator.exceptions.intent.IntentNotSupportedException;
import br.ueg.tc.pipa_integrator.exceptions.user.UserNotAuthenticatedException;
import br.ueg.tc.pipa_integrator.institutions.IBaseInstitutionProvider;
import br.ueg.tc.pipa_integrator.institutions.KeyValue;
import br.ueg.tc.pipa_integrator.institutions.info.IUserData;
import br.ueg.tc.ueg_provider.converter.ConverterUEG;
import com.google.gson.JsonArray;
import lombok.Getter;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

@Getter
public class UEGProvider implements IBaseInstitutionProvider, UEGEndpoint {

    private CookieStore httpCookieStore;
    private final HttpClientContext localContext;
    private final CloseableHttpClient httpClient;
    private String acuId;
    private JsonArray jsonClassSchedule;
    private JsonArray jsonDisciplineGrade;
    public final IConverterInstitution converterUEG;

    public UEGProvider() {
        this.httpCookieStore = new BasicCookieStore();
        this.localContext = HttpClientContext.create();
        this.httpClient =
                HttpClients.custom().setDefaultCookieStore(httpCookieStore).build();
        this.converterUEG = new ConverterUEG();
    }

    @Override
    public List<KeyValue> authenticateUser(String username, String password) throws UserNotAuthenticatedException, InstitutionComunicationException {
        HttpPost httpPost = new HttpPost(VALIDA_LOGIN);
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("cpf", username));
        nvps.add(new BasicNameValuePair("senha", password));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost, localContext);
            HttpEntity entity = httpResponse.getEntity();
            String hmtlResponse = EntityUtils.toString(entity);
            if(responseLoginOK(hmtlResponse)) return cookiesToKeyValue();
            return null;
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }

    private boolean responseLoginOK(String hmtlResponse) {
        return hmtlResponse.contains("Portal");
    }

    private List<KeyValue> cookiesToKeyValue() {

        List<KeyValue> studentAccessData = new ArrayList<>();
        for (Cookie cookie : this.getHttpCookieStore().getCookies()){
            KeyValue cookies = new KeyValue();
            cookies.setKey(cookie.getName());
            cookies.setValue(cookie.getValue());
            studentAccessData.add(cookies);
        }
        return studentAccessData;
    }

    @Override
    public void setUserAccessData(List<KeyValue> accessData) {

    }

    @Override
    public List<KeyValue> refreshUserAccessData(List<KeyValue> accessData) {
        return List.of();
    }


    @Override
    public IUserData getUserData() throws IntentNotSupportedException, InstitutionComunicationException {
        return null;
    }

    @Override
    public List<String> getAllServiceProvider() {
        return List.of();
    }

    public String generateNewAcademicRecordHTML() {
        return "";
    }

    public String generateNewAttendanceDeclarationHTML() {
        return "";
    }
}
