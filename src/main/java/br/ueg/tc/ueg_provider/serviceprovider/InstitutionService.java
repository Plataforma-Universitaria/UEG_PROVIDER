package br.ueg.tc.ueg_provider.serviceprovider;

import br.ueg.tc.pipa_integrator.exceptions.institution.InstitutionCommunicationException;
import br.ueg.tc.pipa_integrator.exceptions.intent.IntentNotSupportedException;
import br.ueg.tc.pipa_integrator.exceptions.user.UserNotFoundException;
import br.ueg.tc.pipa_integrator.interfaces.platform.IUser;
import br.ueg.tc.pipa_integrator.interfaces.providers.KeyValue;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.IUserData;
import br.ueg.tc.pipa_integrator.interfaces.providers.service.IServiceProvider;
import br.ueg.tc.ueg_provider.UEGProvider;
import br.ueg.tc.ueg_provider.converter.ConverterUEG;
import br.ueg.tc.ueg_provider.dto.Token;
import br.ueg.tc.ueg_provider.services.EmailServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.util.List;

import static br.ueg.tc.ueg_provider.UEGEndpoint.*;

public abstract class InstitutionService implements IServiceProvider {

    protected CookieStore httpCookieStore;
    protected final HttpClientContext localContext;
    protected final CloseableHttpClient httpClient;
    protected final ConverterUEG converterUEG;
    protected final UEGProvider uegProvider;
    protected final IUser user;

    public InstitutionService() {
        this.httpCookieStore = new BasicCookieStore();
        this.localContext = HttpClientContext.create();
        this.httpClient =
                HttpClients.custom().setDefaultCookieStore(httpCookieStore).build();
        this.converterUEG = new ConverterUEG();
        this.uegProvider = new UEGProvider();
        this.user = null;

    }

    public InstitutionService(IUser user) {
        this.user = user;
        setUserAccessData(user.getKeyValueList());
        this.localContext = HttpClientContext.create();
        this.httpClient =
                HttpClients.custom()
                        .setDefaultCookieStore(httpCookieStore).build();
        this.converterUEG = new ConverterUEG();
        this.uegProvider = new UEGProvider();
    }

    void setUserAccessData(List<KeyValue> keyValueList) {
        this.httpCookieStore = new BasicCookieStore();
        for (KeyValue accessData : keyValueList) {
            BasicClientCookie basicClientCookie = new BasicClientCookie(accessData.getKey(), accessData.getValue());
            basicClientCookie.setDomain("www.sistema.beta.ueg.br");
            basicClientCookie.setPath("/");
            this.httpCookieStore.addCookie(basicClientCookie);
        }
    }

    protected boolean responseOK(CloseableHttpResponse httpResponse) {
        return httpResponse.getCode() == 200;
    }

    public Token getJwt() {
        HttpGet httpGet;
        if(getUserPersonas().contains("Professor")){
            httpGet = new HttpGet(GET_JWT_TOKEN_PROFESSOR);
        }
        else
        {
            httpGet = new HttpGet(GET_JWT_TOKEN);
        }
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet, localContext);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                return converterUEG.getTokenFromJson(JsonParser.
                        parseString(EntityUtils.toString(entity)));
            }
            throw new UserNotFoundException();
        } catch (Throwable error) {
            throw new InstitutionCommunicationException("Não foi possível se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }
    }

    public IUserData getUserData() throws IntentNotSupportedException, InstitutionCommunicationException {
        HttpGet httpGet;
        if(getUserPersonas().contains("Professor")){
            httpGet = new HttpGet(PERFIL_PROFESSOR);
        }
        else
        {
            httpGet = new HttpGet(PERFIL);
        }
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet, localContext);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                if(getUserPersonas().contains("Professor")){
                    JsonElement jsonElement = JsonParser.parseString(EntityUtils.toString(entity));
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    return converterUEG.getUserDataTeacherFromJson(jsonArray.get(0));
                }
                else
                {
                    return converterUEG.getUserDataFromJson(JsonParser.
                            parseString(EntityUtils.toString(entity)));
                }
            }
            throw new UserNotFoundException();
        } catch (Throwable error) {
            throw new InstitutionCommunicationException("Não foi possível se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }
    }

    List<String> getUserPersonas(){
        return this.user.getPersonas();
    }

    /**
     * Extrai dinamicamente um campo do JSON retornado pela UEG.
     *
     * @param json  JSON de entrada (string)
     * @param field Campo que será extraído
     * @return valor do campo como String (ou null se não existir)
     */
    public String extractFromJson(String json, String field) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode campo = rootNode.get(field);
            return campo != null ? campo.toString() : null;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao extrair campo do JSON: " + field, e);
        }
    }

    private JsonObject fetchAndMergeJson(JsonObject baseJson, String endpointUrl, String keyName) throws Throwable {
        HttpGet httpGet = new HttpGet(endpointUrl);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet, localContext)) {
            if (responseOK(httpResponse)) {
                String jsonString = EntityUtils.toString(httpResponse.getEntity());
                JsonElement jsonElement = JsonParser.parseString(jsonString);

                if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
                    baseJson.add(keyName, jsonElement);
                }
            }
        }
        return baseJson;
    }

}
