package br.ueg.tc.ueg_provider.serviceprovider;

import br.ueg.tc.pipa_integrator.annotations.ActivationPhrases;
import br.ueg.tc.pipa_integrator.converter.IConverterInstitution;
import br.ueg.tc.pipa_integrator.exceptions.BusinessException;
import br.ueg.tc.pipa_integrator.exceptions.institution.InstitutionComunicationException;
import br.ueg.tc.pipa_integrator.exceptions.intent.IntentNotSupportedException;
import br.ueg.tc.pipa_integrator.exceptions.user.UserNotFoundException;
import br.ueg.tc.pipa_integrator.institutions.KeyValue;
import br.ueg.tc.pipa_integrator.institutions.definations.IUser;
import br.ueg.tc.pipa_integrator.institutions.info.IDisciplineSchedule;
import br.ueg.tc.pipa_integrator.institutions.info.IUserData;
import br.ueg.tc.pipa_integrator.serviceprovider.service.IServiceProvider;
import br.ueg.tc.ueg_provider.UEGProvider;
import br.ueg.tc.ueg_provider.converter.ConverterUEG;
import br.ueg.tc.ueg_provider.formatter.FormatterScheduleByDisciplineName;
import com.google.gson.JsonArray;
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
import org.springframework.stereotype.Service;

import java.util.List;

import static br.ueg.tc.ueg_provider.UEGEndpoint.HORARIO_AULA;
import static br.ueg.tc.ueg_provider.UEGEndpoint.PERFIL;

@Service
public class StudentScheduleService implements IServiceProvider {

    private CookieStore httpCookieStore;
    private final HttpClientContext localContext;
    private final CloseableHttpClient httpClient;
    private String acuId;
    private final IConverterInstitution converterUEG;
    private final UEGProvider uegProvider;

    public StudentScheduleService() {
        this.httpCookieStore = new BasicCookieStore();
        this.localContext = HttpClientContext.create();
        this.httpClient =
                HttpClients.custom().setDefaultCookieStore(httpCookieStore).build();
        this.converterUEG = new ConverterUEG();
        this.uegProvider = new UEGProvider();
    }

    public StudentScheduleService(IUser user) {
        setUserAccessData(user.getKeyValueList());
        this.localContext = HttpClientContext.create();
        this.httpClient =
                HttpClients.custom()
                        .setDefaultCookieStore(httpCookieStore).build();
        this.converterUEG = new ConverterUEG();
        this.uegProvider = new UEGProvider();
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
    public String doService(String activationPhrase, IUserData userData) throws BusinessException {
        return "Deu certo";
    }

    public void getPersonId() {
        acuId = getUserData().getPersonId();
    }

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

    @Override
    public List<String> getValidPersonas() {
        return List.of("Aluno");
    }

    @Override
    public Boolean isValidPersona(String persona) {
        return getValidPersonas().contains(persona);
    }

    @Override
    public Boolean manipulatesData() {
        return false;
    }

    private boolean responseOK(CloseableHttpResponse httpResponse) {
        return httpResponse.getCode() == 200;
    }

    @ActivationPhrases(value = {"Quais minhas aulas de segunda",
            "Aula de terça", "Aaulas de Sábado"})
    public List<IDisciplineSchedule> getWeekSchedule() throws IntentNotSupportedException {
        HttpGet httpGet = new HttpGet(HORARIO_AULA);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                return converterUEG.getDisciplinesWithScheduleFromJson
                        ((JsonArray) JsonParser.parseString(entityString)
                        );
            } else
                throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG, " +
                        "tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionComunicationException("Ocorreu um problema na obtenção do horario," +
                    " tente novamente mais tarde");
        }
    }

    @ActivationPhrases(value = {"Quais minha aulas em matemática",
            "Aula de português", "Quando é a aula de Português", "Quando é minha aula de INFRAESTRUTURA DE REDES"})
    public List<IDisciplineSchedule> getScheduleByDisciplineName(String disciplineToGetSchedule){
        HttpGet httpGet = new HttpGet(HORARIO_AULA);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                disciplineToGetSchedule = getScheduleByDisciplineNameResponse(disciplineToGetSchedule, entityString);
                FormatterScheduleByDisciplineName formatter = new FormatterScheduleByDisciplineName();
                return formatter.scheduleByDisciplineName(disciplineToGetSchedule, converterUEG.getDisciplinesWithScheduleFromJson
                        ((JsonArray) JsonParser.parseString(entityString))
                );
            } else
                throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                        " tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionComunicationException("Ocorreu um problema na obtenção do horário, " +
                    "tente novamente mais tarde");
        }
    }

    private String getScheduleByDisciplineNameResponse(String disciplineToGetSchedule, String entityString) {
//        disciplineToGetSchedule = aiService.sendPrompt(AIApi.startDisciplineNameQuestion + entityString + AIApi.endDisciplineNameQuestion + disciplineToGetSchedule);
        return disciplineToGetSchedule;
    }
}
