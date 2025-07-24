package br.ueg.tc.ueg_provider.serviceprovider;

import br.ueg.tc.apiai.service.AiService;
import br.ueg.tc.pipa_integrator.ai.AIClient;
import br.ueg.tc.pipa_integrator.annotations.ServiceProviderClass;
import br.ueg.tc.pipa_integrator.annotations.ServiceProviderMethod;
import br.ueg.tc.pipa_integrator.enums.WeekDay;
import br.ueg.tc.pipa_integrator.exceptions.GenericBusinessException;
import br.ueg.tc.pipa_integrator.exceptions.institution.InstitutionComunicationException;
import br.ueg.tc.pipa_integrator.exceptions.intent.IntentNotSupportedException;
import br.ueg.tc.pipa_integrator.exceptions.user.UserNotFoundException;
import br.ueg.tc.pipa_integrator.interfaces.providers.IBaseInstitutionProvider;
import br.ueg.tc.pipa_integrator.interfaces.platform.IUser;
import br.ueg.tc.pipa_integrator.interfaces.providers.info.*;
import br.ueg.tc.pipa_integrator.interfaces.providers.EmailDetails;
import br.ueg.tc.pipa_integrator.interfaces.providers.IPlataformService;
import br.ueg.tc.pipa_integrator.interfaces.providers.parameters.ParameterValue;
import br.ueg.tc.ueg_provider.UEGProvider;
import br.ueg.tc.ueg_provider.ai.AIApi;
import br.ueg.tc.ueg_provider.formatter.Formatter;
import br.ueg.tc.ueg_provider.infos.ComplementaryActivityUEG;
import br.ueg.tc.ueg_provider.infos.UserDataUEG;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static br.ueg.tc.ueg_provider.UEGEndpoint.*;
import static br.ueg.tc.ueg_provider.enums.DocEnum.ACADEMIC_RECORD;

@Service
@ServiceProviderClass(personas = {"Aluno"})
public class StudentService extends InstitutionService {

    @Autowired
    AiService<AIClient> aiService;
    private String acuId;

    public StudentService() {
        super();
    }
    public StudentService(IUser user){
        super(user);
    }

    private boolean responseOK(CloseableHttpResponse httpResponse) {
        return httpResponse.getCode() == 200;
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

    @ServiceProviderMethod(activationPhrases = {"Qual minha média geral",
            "média geral", "qual minha nota geral", "média", "qual a nota geral?"})
    public String getGeneralGrade() {
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_ACADEMICOS);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;

                String mediaGeral = extractFromJson(entityString, "media_geral");

                return mediaGeral != null ?
                        "Sua média geral é: " + mediaGeral :
                        "Não foi possível encontrar sua média geral no sistema.";
            } else {
                throw new InstitutionComunicationException("Não foi possível se comunicar com o servidor da UEG. Tente novamente mais tarde.");
            }

        } catch (Exception error) {
            throw new InstitutionComunicationException("Ocorreu um problema na obtenção da nota geral. Tente novamente mais tarde.");
        }
    }

    @ServiceProviderMethod(activationPhrases = {"Qual minha nota em matemática",
            "média geral em programação", "qual minha nota em portugues"})
    public List<IDisciplineGrade> getGradeByDiscipline(String discipline) {
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_DISCIPLINAS + acuId);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                Formatter formatter = new Formatter();
                if (entityString == null || entityString.isEmpty()) return null;
                discipline = getDisciplineNameResponse(discipline, entityString);
                return formatter.disciplineGradeByDisciplineName(discipline,
                        converterUEG.getGradesWithDetailedGradeFromJson((JsonArray) JsonParser.parseString(entityString)));

            } else {
                throw new InstitutionComunicationException("Não foi possível se comunicar com o servidor da UEG. Tente novamente mais tarde.");
            }

        } catch (Exception error) {
            throw new InstitutionComunicationException("Ocorreu um problema na obtenção da nota geral. Tente novamente mais tarde.");
        }
    }

    @ServiceProviderMethod(activationPhrases = {"Quais minhas notas do primeiro semestre",
            "notas do periodo 7", "3° periodo notas"})
    public List<IDisciplineGrade> getGradesBySemester(String semester) {
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_DISCIPLINAS + acuId);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                Formatter formatter = new Formatter();
                if (entityString == null || entityString.isEmpty()) return null;
                return formatter.disciplineGradeBySemester(semester,
                        converterUEG.getGradesWithDetailedGradeFromJson((JsonArray) JsonParser.parseString(entityString)));

            } else {
                throw new InstitutionComunicationException("Não foi possível se comunicar com o servidor da UEG. Tente novamente mais tarde.");
            }

        } catch (Exception error) {
            throw new InstitutionComunicationException("Ocorreu um problema na obtenção da nota geral. Tente novamente mais tarde.");
        }
    }



    @ServiceProviderMethod(activationPhrases = {"Quais minhas aulas?", "Aulas da semana", "Quais minhas aulas da semana", "Horário de aula"})
    public String getAllSchedule() throws IntentNotSupportedException {
        HttpGet httpGet = new HttpGet(HORARIO_AULA);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                Formatter formatter = new Formatter();
                return formatter.formatSchedule(converterUEG.getDisciplinesWithScheduleFromJson
                        ((JsonArray) JsonParser.parseString(entityString)
                        ));
            } else
                throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG, " +
                        "tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionComunicationException("Ocorreu um problema na obtenção do horario," +
                    " tente novamente mais tarde");
        }
    }

    @ServiceProviderMethod(activationPhrases = {"Quais minhas aulas de segunda",
            "Aula de terça", "Aulas de Sábado", "Quais minhas aulas hoje", "Aulas de amanhã"})
    public String getScheduleByDay(String day){
        HttpGet httpGet = new HttpGet(HORARIO_AULA);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();

            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                Formatter formatter = new Formatter();
                day = getWeekByValue(day);
                if(Objects.equals(day, "NENHUMA"))
                {
                    return "Você não tem aulas nesse dia";
                }
                return formatter.formatSchedule(formatter.disciplinesWithScheduleByDay(WeekDay.getByShortName(day),
                        converterUEG.getDisciplinesWithScheduleFromJson
                                ((JsonArray) JsonParser.parseString(entityString)))
                );
            } else
                throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                        " tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionComunicationException("Ocorreu um problema na obtenção do horario," +
                    " tente novamente mais tarde");
        }
    }


    @ServiceProviderMethod(activationPhrases = {"Quais minha aulas em matemática",
            "Aula de português", "Quando é a aula de Português",
            "Quando é minha aula de infra",
            "Quando é minha aula de INFRAESTRUTURA PARA SISTEMAS DE INFORMAÇÃO"})
    public String getScheduleByDisciplineName(String disciplineToGetSchedule){
        HttpGet httpGet = new HttpGet(HORARIO_AULA);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                disciplineToGetSchedule = getDisciplineNameResponse(disciplineToGetSchedule, entityString);
                if(Objects.equals(disciplineToGetSchedule, "NENHUMA"))
                {
                    return "Você não tem aulas dessa matéria";
                }
                Formatter formatter = new Formatter();
                return formatter.formatSchedule(formatter.scheduleByDisciplineName(disciplineToGetSchedule, converterUEG.getDisciplinesWithScheduleFromJson
                        ((JsonArray) JsonParser.parseString(entityString)))
                );
            } else
                throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                        " tente novamente mais tarde");

        } catch (Exception error) {
            throw new InstitutionComunicationException("Ocorreu um problema na obtenção do horário, " +
                    "tente novamente mais tarde");
        }
    }

    @ServiceProviderMethod(activationPhrases = {
            "Ajuda",
            "O que você pode fazer",
            "O que posso fazer",
            "Funcionalidades"})
    public String getAllFunctionalities(){
        return "Consultar:\n * Aulas *\n* Notas *\n* Faltas *";
    }

    @ServiceProviderMethod(activationPhrases = {"Quais minhas faltas em português", "Quais minhas faltas em matematica", "Faltas em biologia II"})
    public String getAbsencesByDiscipline(String discipline){
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_DISCIPLINAS + acuId);
        try{
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                discipline = getDisciplineNameResponse(discipline, entityString);
                if(Objects.equals(discipline, "NENHUMA"))
                {
                    return "Você não tem notas nessa matéria";
                }
                Formatter formatter = new Formatter();
                return formatter.formatAbsence(formatter.absencesByDisciplineName(discipline,
                        converterUEG.getDisciplinesWithAbsencesFromJson(
                        ((JsonArray) JsonParser.parseString(entityString)))
                ));
            } else
                throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                        " tente novamente mais tarde");

        }catch (Exception error) {
            throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }

    }

    @ServiceProviderMethod(activationPhrases = {"Quais materias ja fiz", "materias concluidas", "Disciplinas completas"})
    public String getCompletedCourses() {
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_DISCIPLINAS + acuId);
        try{
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                Formatter formatter = new Formatter();
                return formatter.formatDiscipline(formatter.disciplineByStatus("aprovado",
                        converterUEG.getDisciplinesFromJson(
                                ((JsonArray) JsonParser.parseString(entityString)))
                ));
            } else
                throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                        " tente novamente mais tarde");

        }catch (Exception error) {
            throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }

    }

    

    @ServiceProviderMethod(activationPhrases = {"Atividades complementares", "Como estão minhas atividades complementares", "detalhes das atividades complementares", "todas as atividades complementares"})
    public String getAllDetailedComplementaryActivities() {
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_ATV_COMPLEMENTARES + acuId + "&page=1&rows_limit=1000&sort_by=&descending=f");
        try{
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                Formatter formatter = new Formatter();
                return formatter.formatComplementaryActivities(
                        converterUEG.getComplementaryActivitiesFromJson(
                                (JsonParser.parseString(entityString)))
                );
            } else
                throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                        " tente novamente mais tarde");

        }catch (Exception error) {
            throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }

    }
    @ServiceProviderMethod(activationPhrases = {"Resumo da atividades complementares", "Resuma como estão minhas atividades complementares", "status das atividades complementares", "resumo atv complementares"})
    public String getDigestComplementaryActivities() {
        getPersonId();
        HttpGet httpGet = new HttpGet(DADOS_ATV_COMPLEMENTARES_HORAS + acuId );
        try{
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (responseOK(httpResponse)) {
                String entityString = EntityUtils.toString(entity);
                if (entityString == null || entityString.isEmpty()) return null;
                ComplementaryActivityUEG complementaryActivity = converterUEG.getComplementaryHoursActivitiesFromJson(
                        (JsonParser.parseString(entityString)));
                Formatter formatter = new Formatter();
                return formatter.formatComplementaryActivities(complementaryActivity);
            } else
                throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                        " tente novamente mais tarde");

        }catch (Exception error) {
            throw new InstitutionComunicationException("Não foi possivel se comunicar com o servidor da UEG," +
                    " tente novamente mais tarde");
        }

    }


    //TODO: [FUNCIONALIDADE PENDENTE] Implementar 'getCompletedCourses' para listar as disciplinas já cursadas.
    // A funcionalidade atual envia o histórico por e-mail, mas uma consulta direta na interface seria útil.
    // Pode-se extrair essa informação do endpoint 'DADOS_DISCIPLINAS' e formatar a resposta.
    // Adicionar @ServiceProviderMethod com frases como "quais matérias eu já fiz?", "disciplinas cursadas".

    //TODO: [FUNCIONALIDADE PENDENTE] Implementar 'getComplementaryActivities' para consultar o status das atividades complementares.
    // Necessitará de um novo endpoint da API da UEG.
    // Adicionar @ServiceProviderMethod com frases como "minhas atividades complementares", "quantas horas complementares eu tenho?".

    //TODO: [FUNCIONALIDADE PENDENTE] Implementar 'getExtensionActivities' para consultar as atividades de extensão.
    // Necessitará de um novo endpoint da API da UEG.
    // Adicionar @ServiceProviderMethod com frases como "minhas atividades de extensão", "ver projetos de extensão que participei".


    //Métodos internos

    private String getDisciplineNameResponse(String discipline, String entityString) {
      discipline = aiService.sendPrompt(AIApi.startDisciplineNameQuestion + entityString + AIApi.endDisciplineNameQuestion + discipline);
        return discipline;
    }

    private String getWeekByValue(String weekDay) {
        return aiService.sendPrompt(AIApi.startWeekNameQuestion + LocalDateTime.now() + AIApi.endWeekNameQuestion + weekDay);
    }

    private UserDataUEG getStudentData(Set<ParameterValue> parameterValues) {
        return parameterValues.stream()
                .findFirst()
                .map(parameterValue -> (UserDataUEG) parameterValue.getValue())
                .orElseThrow(() -> new GenericBusinessException
                        ("Não foram encontrados os dados do estudante para envio do histórico acadêmico"));
    }

    private void validateStudentEmail(UserDataUEG studentData) {
        if (Objects.isNull(studentData) ||
                Objects.isNull(studentData.getEmail()) || studentData.getEmail().isEmpty()) {
            throw new GenericBusinessException("Não foi encontrado o email do estudante para envio da histórico");
        }
    }

    private String generateAcademicRecordPDF(IBaseInstitutionProvider institution, IPlataformService plataformService) {
        String attendanceDeclarationHTML = ((UEGProvider) institution).generateNewAcademicRecordHTML();
        return plataformService.HTMLToPDF(attendanceDeclarationHTML, ACADEMIC_RECORD.getFolderPath(),
                ACADEMIC_RECORD.getFilePrefix());

    }

    private EmailDetails buildEmailDetails(UserDataUEG studentData, String pdfPath) {
        return new EmailDetails(studentData.getFirstName(), studentData.getEmail(),
                "HISTÓRICO ACADÊMICO UEG",
                "Olá, segue em anexo seu Histórico Acadêmico da UEG",
                "Histórico_Acadêmico", pdfPath);
    }

    private String sendEmail(IPlataformService plataformService, EmailDetails emailDetails) {
        if (plataformService.sendEmailWithFileAttachment(emailDetails)) {
            return "Seu Histórico Acadêmico foi enviado para o seu e-mail acadêmico.";
        }
        return "Houve um erro ao enviar seu Histórico Acadêmico, tente novamente mais tarde";
    }

}
