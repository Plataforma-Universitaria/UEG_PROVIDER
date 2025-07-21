package br.ueg.tc.ueg_provider.serviceprovider;

import br.ueg.tc.pipa_integrator.converter.IConverterInstitution;
import br.ueg.tc.pipa_integrator.interfaces.providers.KeyValue;
import br.ueg.tc.pipa_integrator.interfaces.platform.IUser;
import br.ueg.tc.pipa_integrator.interfaces.providers.service.IServiceProvider;
import br.ueg.tc.ueg_provider.UEGProvider;
import br.ueg.tc.ueg_provider.converter.ConverterUEG;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.client5.http.protocol.HttpClientContext;

import java.util.List;

public abstract class InstitutionService implements IServiceProvider {

    protected CookieStore httpCookieStore;
    protected final HttpClientContext localContext;
    protected final CloseableHttpClient httpClient;
    protected final ConverterUEG converterUEG;
    protected final UEGProvider uegProvider;

    public InstitutionService() {
        this.httpCookieStore = new BasicCookieStore();
        this.localContext = HttpClientContext.create();
        this.httpClient =
                HttpClients.custom().setDefaultCookieStore(httpCookieStore).build();
        this.converterUEG = new ConverterUEG();
        this.uegProvider = new UEGProvider();
    }

    public InstitutionService(IUser user) {
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
        for (KeyValue accessData : keyValueList){
            BasicClientCookie basicClientCookie = new BasicClientCookie(accessData.getKey(), accessData.getValue());
            basicClientCookie.setDomain("www.app.ueg.br");
            basicClientCookie.setPath("/");
            this.httpCookieStore.addCookie(basicClientCookie);
        }
    }

    /**
     * Extrai dinamicamente um campo do JSON retornado pela UEG.
     *
     * @param json           JSON de entrada (string)
     * @param field  Campo que será extraído
     * @return valor do campo como String (ou null se não existir)
     */
    public String extractFromJson(String json, String field) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode campo = rootNode.get(field);
            return campo != null ? campo.asText() : null;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao extrair campo do JSON: " + field, e);
        }
    }

}
