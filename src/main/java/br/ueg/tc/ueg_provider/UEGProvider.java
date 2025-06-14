package br.ueg.tc.ueg_provider;

import br.ueg.tc.pipa_integrator.converter.IConverterInstitution;
import br.ueg.tc.pipa_integrator.exceptions.institution.InstitutionComunicationException;
import br.ueg.tc.pipa_integrator.exceptions.intent.IntentNotSupportedException;
import br.ueg.tc.pipa_integrator.exceptions.user.UserNotAuthenticatedException;
import br.ueg.tc.pipa_integrator.exceptions.user.UserNotFoundException;
import br.ueg.tc.pipa_integrator.interfaces.providers.IBaseInstitutionProvider;
import br.ueg.tc.pipa_integrator.interfaces.providers.KeyValue;
import br.ueg.tc.pipa_integrator.interfaces.platform.IUser;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.IUserData;
import br.ueg.tc.ueg_provider.converter.ConverterUEG;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import lombok.Getter;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
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

    public UEGProvider(IUser user) {
        setUserAccessData(user.getKeyValueList());
        this.localContext = HttpClientContext.create();
        this.httpClient =
                HttpClients.custom()
                        .setDefaultCookieStore(httpCookieStore).build();
        this.converterUEG = new ConverterUEG();
    }

    private void setUserAccessData(List<KeyValue> keyValueList) {
        this.httpCookieStore = new BasicCookieStore();
        for (KeyValue accessData : keyValueList){
            BasicClientCookie basicClientCookie = new BasicClientCookie(accessData.getKey(), accessData.getValue());
            basicClientCookie.setDomain("www.app.ueg.br");
            basicClientCookie.setPath("/");
            this.httpCookieStore.addCookie(basicClientCookie);
        }
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
        return hmtlResponse.contains("Selecione um dos sistemas");
    }

    private List<KeyValue> cookiesToKeyValue() {

        List<KeyValue> userAccessData = new ArrayList<>();
        for (Cookie cookie : this.getHttpCookieStore().getCookies()) {
            KeyValue cookies = new KeyValue();
            cookies.setKey(cookie.getName());
            cookies.setValue(cookie.getValue());
            userAccessData.add(cookies);
        }
        return userAccessData;
    }

    @Override
    public List<KeyValue> refreshUserAccessData(List<KeyValue> accessData) {
        enterPortal("Aluno");
        return cookiesToKeyValue();
    }

    private void enterPortal(String persona) {
        System.out.println("ENTER PORTAL PERSONA: " + persona);
        HttpGet httpGet;
        if (persona.equals("Professor")) {
            httpGet = new HttpGet(ENTRA_PORTAL_PROFESSOR);
        } else {
            httpGet = new HttpGet(ENTRA_PORTAL_ESTUDANTE);
        }
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet, localContext);
            if (responseOK(httpResponse)) {
                getPersonId();
            }
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }

    public void getPersonId() {
        acuId = getUserData().getPersonId();
    }



    @Override
    public IUserData getUserData() throws IntentNotSupportedException, InstitutionComunicationException {
        HttpGet httpGet = new HttpGet(PERFIL);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet, localContext);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                return converterUEG.getUserDataFromJson(JsonParser.
                        parseString(EntityUtils.toString(entity)));
            }
            throw new UserNotFoundException();
        } catch (Throwable error) {
            throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }
    }

    private boolean responseOK(CloseableHttpResponse httpResponse) {
        return httpResponse.getCode() == 200;
    }

    @Override
    public List<String> getPersonas() {
        return List.of(
                "Aluno"
        );
    }

    @Override
    public String getInstitutionName() {
        return "Universidade estadual de goiás";
    }

    @Override
    public String getSalutationPhrase() {
        return "Bem vindo! Faça login com os dados do ADMS";
    }

    @Override
    public String getPasswordFieldName() {
        return "Senha";
    }

    @Override
    public String getUsernameFieldName() {
        return "CPF";
    }

    public String generateNewAcademicRecordHTML() {
        return "";
    }

    public String generateNewAttendanceDeclarationHTML() {
        return "";
    }
}
